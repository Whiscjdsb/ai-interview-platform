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
import com.example.aiinterview.module.answer.dto.PageQueryRequest;
import com.example.aiinterview.module.answer.entity.UserFavorite;
import com.example.aiinterview.module.answer.mapper.UserFavoriteMapper;
import com.example.aiinterview.module.answer.service.FavoriteService;
import com.example.aiinterview.module.answer.vo.FavoriteItemVO;
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
public class FavoriteServiceImpl implements FavoriteService {

    private static final int PUBLISHED_STATUS = 1;

    private final UserFavoriteMapper userFavoriteMapper;
    private final QuestionMapper questionMapper;
    private final QuestionTagRelationMapper questionTagRelationMapper;
    private final StatisticsCacheService statisticsCacheService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FavoriteItemVO addFavorite(Long userId, Long questionId) {
        Question question = getAvailableQuestionOrThrow(questionId);
        UserFavorite favorite = findFavorite(userId, questionId);
        if (favorite != null && Integer.valueOf(0).equals(favorite.getDeleted())) {
            throw new BusinessException(ErrorCode.CONFLICT, "Question is already in favorites");
        }

        LocalDateTime now = LocalDateTime.now();
        if (favorite == null) {
            favorite = new UserFavorite();
            favorite.setUserId(userId);
            favorite.setQuestionId(questionId);
            favorite.setDeleted(0);
            userFavoriteMapper.insert(favorite);
        } else {
            favorite.setDeleted(0);
            favorite.setCreateTime(now);
            favorite.setUpdateTime(now);
            userFavoriteMapper.updateById(favorite);
        }
        statisticsCacheService.evictOverview(userId);
        Map<Long, List<TagVO>> tagsByQuestionId = AnswerViewSupport.loadTags(questionTagRelationMapper, List.of(questionId));
        return toVo(favorite, question, tagsByQuestionId.getOrDefault(questionId, List.of()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFavorite(Long userId, Long questionId) {
        UserFavorite favorite = findActiveFavorite(userId, questionId);
        if (favorite == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Favorite record does not exist");
        }
        favorite.setDeleted(1);
        favorite.setUpdateTime(LocalDateTime.now());
        userFavoriteMapper.updateById(favorite);
        statisticsCacheService.evictOverview(userId);
    }

    @Override
    public PageResult<FavoriteItemVO> pageFavorites(Long userId, PageQueryRequest request) {
        Page<UserFavorite> page = userFavoriteMapper.selectPage(
                new Page<>(request.getPage(), request.getSize()),
                new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getUserId, userId)
                        .eq(UserFavorite::getDeleted, 0)
                        .orderByDesc(UserFavorite::getCreateTime));
        List<Long> questionIds = page.getRecords().stream()
                .map(UserFavorite::getQuestionId)
                .toList();
        Map<Long, Question> questionMap = loadQuestionMap(questionIds);
        Map<Long, List<TagVO>> tagsByQuestionId = AnswerViewSupport.loadTags(questionTagRelationMapper, questionIds);
        List<FavoriteItemVO> records = page.getRecords().stream()
                .map(favorite -> toVo(
                        favorite,
                        questionMap.get(favorite.getQuestionId()),
                        tagsByQuestionId.getOrDefault(favorite.getQuestionId(), List.of())))
                .filter(item -> item.getTitle() != null)
                .toList();
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getPages(), records);
    }

    private Question getAvailableQuestionOrThrow(Long questionId) {
        Question question = questionMapper.selectById(questionId);
        if (question == null || !Integer.valueOf(PUBLISHED_STATUS).equals(question.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Question does not exist");
        }
        return question;
    }

    private UserFavorite findFavorite(Long userId, Long questionId) {
        return userFavoriteMapper.selectOne(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getQuestionId, questionId)
                .last("LIMIT 1"));
    }

    private UserFavorite findActiveFavorite(Long userId, Long questionId) {
        return userFavoriteMapper.selectOne(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getQuestionId, questionId)
                .eq(UserFavorite::getDeleted, 0)
                .last("LIMIT 1"));
    }

    private Map<Long, Question> loadQuestionMap(List<Long> questionIds) {
        if (questionIds.isEmpty()) {
            return Map.of();
        }
        return questionMapper.selectBatchIds(questionIds).stream()
                .collect(Collectors.toMap(Question::getId, Function.identity(), (left, right) -> left));
    }

    private FavoriteItemVO toVo(UserFavorite favorite, Question question, List<TagVO> tags) {
        return new FavoriteItemVO(
                favorite.getId(),
                favorite.getQuestionId(),
                question == null ? null : question.getTitle(),
                question == null ? null : question.getContent(),
                question == null ? null : question.getQuestionType(),
                question == null ? null : question.getDifficulty(),
                tags,
                favorite.getCreateTime());
    }
}
