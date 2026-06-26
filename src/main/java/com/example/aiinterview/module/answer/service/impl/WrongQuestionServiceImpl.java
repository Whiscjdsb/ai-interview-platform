package com.example.aiinterview.module.answer.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.aiinterview.common.BusinessException;
import com.example.aiinterview.common.ErrorCode;
import com.example.aiinterview.common.PageResult;
import com.example.aiinterview.module.answer.dto.WrongQuestionQueryRequest;
import com.example.aiinterview.module.answer.entity.UserWrongQuestion;
import com.example.aiinterview.module.answer.mapper.UserWrongQuestionMapper;
import com.example.aiinterview.module.answer.service.WrongQuestionService;
import com.example.aiinterview.module.answer.vo.WrongQuestionItemVO;
import com.example.aiinterview.module.question.entity.Question;
import com.example.aiinterview.module.question.mapper.QuestionMapper;
import com.example.aiinterview.module.question.mapper.QuestionTagRelationMapper;
import com.example.aiinterview.module.question.vo.TagVO;
import com.example.aiinterview.module.statistics.service.StatisticsCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WrongQuestionServiceImpl implements WrongQuestionService {

    private static final int PUBLISHED_STATUS = 1;

    private final UserWrongQuestionMapper userWrongQuestionMapper;
    private final QuestionMapper questionMapper;
    private final QuestionTagRelationMapper questionTagRelationMapper;
    private final StatisticsCacheService statisticsCacheService;

    @Override
    public PageResult<WrongQuestionItemVO> pageWrongQuestions(Long userId, WrongQuestionQueryRequest request) {
        List<Long> filteredQuestionIds = findQuestionIdsByFilters(request);
        boolean hasQuestionFilter = request.getQuestionType() != null || request.getDifficulty() != null;
        if (hasQuestionFilter && filteredQuestionIds.isEmpty()) {
            return new PageResult<>(request.getPage(), request.getSize(), 0, 0, List.of());
        }

        Page<UserWrongQuestion> page = userWrongQuestionMapper.selectPage(
                new Page<>(request.getPage(), request.getSize()),
                new LambdaQueryWrapper<UserWrongQuestion>()
                        .eq(UserWrongQuestion::getUserId, userId)
                        .eq(UserWrongQuestion::getDeleted, 0)
                        .eq(request.getStatus() != null, UserWrongQuestion::getStatus, request.getStatus())
                        .in(hasQuestionFilter, UserWrongQuestion::getQuestionId, filteredQuestionIds)
                        .orderByDesc(UserWrongQuestion::getLastWrongTime));

        List<Long> questionIds = page.getRecords().stream()
                .map(UserWrongQuestion::getQuestionId)
                .toList();
        Map<Long, Question> questionMap = loadQuestionMap(questionIds);
        Map<Long, List<TagVO>> tagsByQuestionId = AnswerViewSupport.loadTags(questionTagRelationMapper, questionIds);
        List<WrongQuestionItemVO> records = page.getRecords().stream()
                .map(wrongQuestion -> toVo(
                        wrongQuestion,
                        questionMap.get(wrongQuestion.getQuestionId()),
                        tagsByQuestionId.getOrDefault(wrongQuestion.getQuestionId(), List.of())))
                .filter(item -> item.getTitle() != null)
                .toList();
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getPages(), records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeWrongQuestion(Long userId, Long questionId) {
        UserWrongQuestion wrongQuestion = userWrongQuestionMapper.selectOne(new LambdaQueryWrapper<UserWrongQuestion>()
                .eq(UserWrongQuestion::getUserId, userId)
                .eq(UserWrongQuestion::getQuestionId, questionId)
                .eq(UserWrongQuestion::getDeleted, 0)
                .last("LIMIT 1"));
        if (wrongQuestion == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Wrong question record does not exist");
        }
        wrongQuestion.setDeleted(1);
        wrongQuestion.setUpdateTime(LocalDateTime.now());
        userWrongQuestionMapper.updateById(wrongQuestion);
        statisticsCacheService.evictOverview(userId);
    }

    private List<Long> findQuestionIdsByFilters(WrongQuestionQueryRequest request) {
        if (request.getQuestionType() == null && request.getDifficulty() == null) {
            return List.of();
        }
        return questionMapper.selectList(new LambdaQueryWrapper<Question>()
                        .eq(Question::getStatus, PUBLISHED_STATUS)
                        .eq(request.getQuestionType() != null, Question::getQuestionType, request.getQuestionType())
                        .eq(request.getDifficulty() != null, Question::getDifficulty, request.getDifficulty()))
                .stream()
                .map(Question::getId)
                .toList();
    }

    private Map<Long, Question> loadQuestionMap(List<Long> questionIds) {
        if (questionIds.isEmpty()) {
            return Map.of();
        }
        return questionMapper.selectBatchIds(questionIds).stream()
                .collect(Collectors.toMap(Question::getId, Function.identity(), (left, right) -> left));
    }

    private WrongQuestionItemVO toVo(UserWrongQuestion wrongQuestion, Question question, List<TagVO> tags) {
        return new WrongQuestionItemVO(
                wrongQuestion.getId(),
                wrongQuestion.getQuestionId(),
                question == null ? null : question.getTitle(),
                question == null ? null : question.getContent(),
                question == null ? null : question.getQuestionType(),
                question == null ? null : question.getDifficulty(),
                tags,
                wrongQuestion.getWrongCount(),
                wrongQuestion.getLastWrongTime(),
                wrongQuestion.getStatus());
    }
}
