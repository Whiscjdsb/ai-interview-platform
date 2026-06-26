package com.example.aiinterview.module.question.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.aiinterview.common.BusinessException;
import com.example.aiinterview.common.ErrorCode;
import com.example.aiinterview.common.PageResult;
import com.example.aiinterview.module.admin.service.AdminStatisticsCacheService;
import com.example.aiinterview.module.question.dto.QuestionQueryRequest;
import com.example.aiinterview.module.question.dto.QuestionSaveRequest;
import com.example.aiinterview.module.question.dto.QuestionTagPair;
import com.example.aiinterview.module.question.entity.Question;
import com.example.aiinterview.module.question.entity.QuestionTag;
import com.example.aiinterview.module.question.entity.QuestionTagRelation;
import com.example.aiinterview.module.question.mapper.QuestionMapper;
import com.example.aiinterview.module.question.mapper.QuestionTagMapper;
import com.example.aiinterview.module.question.mapper.QuestionTagRelationMapper;
import com.example.aiinterview.module.question.service.QuestionService;
import com.example.aiinterview.module.question.vo.QuestionVO;
import com.example.aiinterview.module.question.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private static final int PUBLISHED_STATUS = 1;

    private final QuestionMapper questionMapper;
    private final QuestionTagMapper questionTagMapper;
    private final QuestionTagRelationMapper questionTagRelationMapper;
    private final AdminStatisticsCacheService adminStatisticsCacheService;

    @Override
    public PageResult<QuestionVO> pageQuestions(QuestionQueryRequest request) {
        List<Long> questionIdsByTag = findQuestionIdsByTag(request.getTagId());
        if (request.getTagId() != null && questionIdsByTag.isEmpty()) {
            return new PageResult<>(request.getPage(), request.getSize(), 0, 0, List.of());
        }

        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<Question>()
                .eq(Question::getStatus, PUBLISHED_STATUS)
                .eq(Question::getDeleted, 0)
                .eq(request.getQuestionType() != null, Question::getQuestionType, request.getQuestionType())
                .eq(request.getDifficulty() != null, Question::getDifficulty, request.getDifficulty())
                .in(!questionIdsByTag.isEmpty(), Question::getId, questionIdsByTag)
                .and(StringUtils.hasText(request.getKeyword()), query -> query
                        .like(Question::getTitle, request.getKeyword())
                        .or()
                        .like(Question::getContent, request.getKeyword()))
                .orderByDesc(Question::getCreateTime);

        Page<Question> page = questionMapper.selectPage(new Page<>(request.getPage(), request.getSize()), wrapper);
        Map<Long, List<TagVO>> tagsByQuestionId = loadTags(page.getRecords().stream()
                .map(Question::getId)
                .toList());
        List<QuestionVO> records = page.getRecords().stream()
                .map(question -> toVo(question, tagsByQuestionId.getOrDefault(question.getId(), List.of()), false))
                .toList();
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getPages(), records);
    }

    @Override
    public QuestionVO getPublicDetail(Long id) {
        return toVo(getQuestionOrThrow(id), loadTags(List.of(id)).getOrDefault(id, List.of()), false);
    }

    @Override
    public QuestionVO getAdminDetail(Long id) {
        return toVo(getQuestionOrThrow(id), loadTags(List.of(id)).getOrDefault(id, List.of()), true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuestionVO createQuestion(QuestionSaveRequest request, Long creatorId) {
        validateTags(request.getTagIds());

        Question question = new Question();
        fillQuestion(question, request);
        question.setCreatorId(creatorId);
        question.setStatus(PUBLISHED_STATUS);
        questionMapper.insert(question);

        replaceRelations(question.getId(), request.getTagIds());
        adminStatisticsCacheService.evictAll();
        return getAdminDetail(question.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuestionVO updateQuestion(Long id, QuestionSaveRequest request) {
        Question question = getQuestionOrThrow(id);
        validateTags(request.getTagIds());
        fillQuestion(question, request);
        questionMapper.updateById(question);

        replaceRelations(id, request.getTagIds());
        adminStatisticsCacheService.evictAll();
        return getAdminDetail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteQuestion(Long id) {
        getQuestionOrThrow(id);
        questionMapper.deleteById(id);
        questionTagRelationMapper.deleteByQuestionId(id);
        adminStatisticsCacheService.evictAll();
    }

    private void fillQuestion(Question question, QuestionSaveRequest request) {
        question.setTitle(request.getTitle());
        question.setContent(request.getContent());
        question.setQuestionType(request.getQuestionType());
        question.setDifficulty(request.getDifficulty());
        question.setCorrectAnswer(request.getCorrectAnswer());
        question.setAnalysis(request.getAnalysis());
    }

    private Question getQuestionOrThrow(Long id) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Question does not exist");
        }
        return question;
    }

    private List<Long> findQuestionIdsByTag(Long tagId) {
        if (tagId == null) {
            return List.of();
        }
        QuestionTag tag = questionTagMapper.selectById(tagId);
        if (tag == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Tag does not exist");
        }
        return questionTagRelationMapper.selectList(new LambdaQueryWrapper<QuestionTagRelation>()
                        .eq(QuestionTagRelation::getTagId, tagId)
                        .eq(QuestionTagRelation::getDeleted, 0))
                .stream()
                .map(QuestionTagRelation::getQuestionId)
                .toList();
    }

    private void validateTags(List<Long> tagIds) {
        if (CollectionUtils.isEmpty(tagIds)) {
            return;
        }
        Set<Long> uniqueTagIds = tagIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (uniqueTagIds.size() != tagIds.size()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Tag ids contain duplicate or null values");
        }

        List<QuestionTag> tags = questionTagMapper.selectList(new LambdaQueryWrapper<QuestionTag>()
                .in(QuestionTag::getId, uniqueTagIds)
                .eq(QuestionTag::getDeleted, 0));
        if (tags.size() != uniqueTagIds.size()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Some tags do not exist");
        }
    }

    private void replaceRelations(Long questionId, List<Long> tagIds) {
        questionTagRelationMapper.deleteByQuestionId(questionId);
        if (CollectionUtils.isEmpty(tagIds)) {
            return;
        }
        tagIds.forEach(tagId -> {
            QuestionTagRelation relation = new QuestionTagRelation();
            relation.setQuestionId(questionId);
            relation.setTagId(tagId);
            questionTagRelationMapper.insert(relation);
        });
    }

    private Map<Long, List<TagVO>> loadTags(List<Long> questionIds) {
        if (CollectionUtils.isEmpty(questionIds)) {
            return Collections.emptyMap();
        }
        List<QuestionTagPair> pairs = questionTagRelationMapper.selectTagPairsByQuestionIds(questionIds);
        return pairs.stream().collect(Collectors.groupingBy(
                QuestionTagPair::getQuestionId,
                Collectors.mapping(pair -> new TagVO(pair.getTagId(), pair.getTagName(), pair.getDescription()), Collectors.toList())));
    }

    private QuestionVO toVo(Question question, List<TagVO> tags, boolean includeAnswer) {
        return new QuestionVO(
                question.getId(),
                question.getTitle(),
                question.getContent(),
                question.getQuestionType(),
                question.getDifficulty(),
                includeAnswer ? question.getCorrectAnswer() : null,
                includeAnswer ? question.getAnalysis() : null,
                includeAnswer ? question.getCreatorId() : null,
                question.getCreateTime(),
                question.getUpdateTime(),
                tags);
    }
}
