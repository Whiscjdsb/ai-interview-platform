package com.example.aiinterview.module.answer.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.aiinterview.common.BusinessException;
import com.example.aiinterview.common.ErrorCode;
import com.example.aiinterview.common.PageResult;
import com.example.aiinterview.module.admin.service.AdminStatisticsCacheService;
import com.example.aiinterview.module.answer.dto.AnswerHistoryQueryRequest;
import com.example.aiinterview.module.answer.dto.AnswerSubmitRequest;
import com.example.aiinterview.module.answer.entity.UserAnswerRecord;
import com.example.aiinterview.module.answer.entity.UserWrongQuestion;
import com.example.aiinterview.module.answer.enums.WrongQuestionStatus;
import com.example.aiinterview.module.answer.mapper.UserAnswerRecordMapper;
import com.example.aiinterview.module.answer.mapper.UserWrongQuestionMapper;
import com.example.aiinterview.module.answer.service.AnswerService;
import com.example.aiinterview.module.answer.vo.AnswerHistoryDetailVO;
import com.example.aiinterview.module.answer.vo.AnswerHistoryItemVO;
import com.example.aiinterview.module.answer.vo.AnswerSubmitVO;
import com.example.aiinterview.module.question.entity.Question;
import com.example.aiinterview.module.question.enums.QuestionType;
import com.example.aiinterview.module.question.mapper.QuestionMapper;
import com.example.aiinterview.module.question.mapper.QuestionTagRelationMapper;
import com.example.aiinterview.module.question.vo.TagVO;
import com.example.aiinterview.module.statistics.service.DailyLearningRecordService;
import com.example.aiinterview.module.statistics.service.StatisticsCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AnswerServiceImpl implements AnswerService {

    private static final int PUBLISHED_STATUS = 1;
    private static final BigDecimal FULL_SCORE = new BigDecimal("100.00");
    private static final BigDecimal ZERO_SCORE = new BigDecimal("0.00");
    private static final String WAITING_REVIEW_MESSAGE = "\u7b49\u5f85 AI \u70b9\u8bc4\u6216\u4eba\u5de5\u8bc4\u4f30";

    private final UserAnswerRecordMapper userAnswerRecordMapper;
    private final UserWrongQuestionMapper userWrongQuestionMapper;
    private final QuestionMapper questionMapper;
    private final QuestionTagRelationMapper questionTagRelationMapper;
    private final DailyLearningRecordService dailyLearningRecordService;
    private final StatisticsCacheService statisticsCacheService;
    private final AdminStatisticsCacheService adminStatisticsCacheService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnswerSubmitVO submitAnswer(Long userId, AnswerSubmitRequest request) {
        Question question = getAvailableQuestionOrThrow(request.getQuestionId());
        Boolean isCorrect = judge(question, request.getUserAnswer());
        BigDecimal score = isCorrect == null ? null : (Boolean.TRUE.equals(isCorrect) ? FULL_SCORE : ZERO_SCORE);

        UserAnswerRecord record = new UserAnswerRecord();
        record.setUserId(userId);
        record.setQuestionId(question.getId());
        record.setUserAnswer(request.getUserAnswer());
        record.setAnswerContent(request.getUserAnswer());
        record.setIsCorrect(isCorrect);
        record.setScore(score);
        record.setAnswerDuration(request.getAnswerDuration());
        record.setAnswerTime(LocalDateTime.now());
        record.setReviewSummary(isCorrect == null ? WAITING_REVIEW_MESSAGE : null);
        userAnswerRecordMapper.insert(record);

        if (isObjective(question.getQuestionType())) {
            if (Boolean.TRUE.equals(isCorrect)) {
                resolveWrongQuestion(userId, question.getId());
            } else {
                recordWrongQuestion(userId, question.getId());
            }
        }
        dailyLearningRecordService.increaseDailyLearning(userId, LocalDate.now(), isCorrect, request.getAnswerDuration());
        statisticsCacheService.evictOverview(userId);
        adminStatisticsCacheService.evictAll();

        return new AnswerSubmitVO(
                record.getId(),
                isCorrect,
                score,
                question.getCorrectAnswer(),
                question.getAnalysis(),
                isCorrect == null ? WAITING_REVIEW_MESSAGE : "\u5224\u9898\u5b8c\u6210");
    }

    @Override
    public PageResult<AnswerHistoryItemVO> pageHistory(Long userId, AnswerHistoryQueryRequest request) {
        List<Long> filteredQuestionIds = findQuestionIdsByType(request.getQuestionType());
        if (request.getQuestionType() != null && filteredQuestionIds.isEmpty()) {
            return new PageResult<>(request.getPage(), request.getSize(), 0, 0, List.of());
        }

        LambdaQueryWrapper<UserAnswerRecord> wrapper = new LambdaQueryWrapper<UserAnswerRecord>()
                .eq(UserAnswerRecord::getUserId, userId)
                .eq(request.getIsCorrect() != null, UserAnswerRecord::getIsCorrect, request.getIsCorrect())
                .ge(request.getStartTime() != null, UserAnswerRecord::getCreateTime, request.getStartTime())
                .le(request.getEndTime() != null, UserAnswerRecord::getCreateTime, request.getEndTime())
                .in(request.getQuestionType() != null, UserAnswerRecord::getQuestionId, filteredQuestionIds)
                .orderByDesc(UserAnswerRecord::getCreateTime);

        Page<UserAnswerRecord> page = userAnswerRecordMapper.selectPage(new Page<>(request.getPage(), request.getSize()), wrapper);
        Map<Long, Question> questionMap = loadQuestionMap(page.getRecords().stream()
                .map(UserAnswerRecord::getQuestionId)
                .toList());
        List<AnswerHistoryItemVO> records = page.getRecords().stream()
                .map(record -> toHistoryItem(record, questionMap.get(record.getQuestionId())))
                .toList();
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getPages(), records);
    }

    @Override
    public AnswerHistoryDetailVO getHistoryDetail(Long userId, Long id) {
        UserAnswerRecord record = userAnswerRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Answer record does not exist");
        }
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Access is denied");
        }
        Question question = getAvailableQuestionOrThrow(record.getQuestionId());
        Map<Long, List<TagVO>> tagsByQuestionId = AnswerViewSupport.loadTags(questionTagRelationMapper, List.of(question.getId()));
        return new AnswerHistoryDetailVO(
                record.getId(),
                question.getId(),
                question.getTitle(),
                question.getContent(),
                question.getQuestionType(),
                question.getDifficulty(),
                tagsByQuestionId.getOrDefault(question.getId(), List.of()),
                record.getUserAnswer(),
                question.getCorrectAnswer(),
                question.getAnalysis(),
                record.getIsCorrect(),
                record.getScore(),
                record.getAnswerDuration(),
                answerTime(record));
    }

    private Question getAvailableQuestionOrThrow(Long questionId) {
        Question question = questionMapper.selectById(questionId);
        if (question == null || !Integer.valueOf(PUBLISHED_STATUS).equals(question.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Question does not exist");
        }
        return question;
    }

    private Boolean judge(Question question, String userAnswer) {
        if (!isObjective(question.getQuestionType())) {
            return null;
        }
        if (!StringUtils.hasText(question.getCorrectAnswer())) {
            return false;
        }
        if (QuestionType.MULTIPLE_CHOICE.equals(question.getQuestionType())) {
            return normalizeMultiChoice(question.getCorrectAnswer()).equals(normalizeMultiChoice(userAnswer));
        }
        return normalizeSimple(question.getCorrectAnswer()).equals(normalizeSimple(userAnswer));
    }

    private boolean isObjective(QuestionType questionType) {
        return QuestionType.SINGLE_CHOICE.equals(questionType)
                || QuestionType.MULTIPLE_CHOICE.equals(questionType)
                || QuestionType.JUDGE.equals(questionType);
    }

    private String normalizeSimple(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private Set<String> normalizeMultiChoice(String value) {
        if (!StringUtils.hasText(value)) {
            return Set.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(String::toUpperCase)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private void recordWrongQuestion(Long userId, Long questionId) {
        UserWrongQuestion wrongQuestion = findWrongQuestion(userId, questionId);
        LocalDateTime now = LocalDateTime.now();
        if (wrongQuestion == null) {
            wrongQuestion = new UserWrongQuestion();
            wrongQuestion.setUserId(userId);
            wrongQuestion.setQuestionId(questionId);
            wrongQuestion.setWrongCount(1);
            wrongQuestion.setLastWrongTime(now);
            wrongQuestion.setStatus(WrongQuestionStatus.ACTIVE);
            wrongQuestion.setDeleted(0);
            userWrongQuestionMapper.insert(wrongQuestion);
            return;
        }
        wrongQuestion.setWrongCount((wrongQuestion.getWrongCount() == null ? 0 : wrongQuestion.getWrongCount()) + 1);
        wrongQuestion.setLastWrongTime(now);
        wrongQuestion.setStatus(WrongQuestionStatus.ACTIVE);
        wrongQuestion.setDeleted(0);
        userWrongQuestionMapper.updateById(wrongQuestion);
    }

    private void resolveWrongQuestion(Long userId, Long questionId) {
        UserWrongQuestion wrongQuestion = findWrongQuestion(userId, questionId);
        if (wrongQuestion == null || !Integer.valueOf(0).equals(wrongQuestion.getDeleted())) {
            return;
        }
        wrongQuestion.setStatus(WrongQuestionStatus.RESOLVED);
        userWrongQuestionMapper.updateById(wrongQuestion);
    }

    private UserWrongQuestion findWrongQuestion(Long userId, Long questionId) {
        return userWrongQuestionMapper.selectOne(new LambdaQueryWrapper<UserWrongQuestion>()
                .eq(UserWrongQuestion::getUserId, userId)
                .eq(UserWrongQuestion::getQuestionId, questionId)
                .last("LIMIT 1"));
    }

    private List<Long> findQuestionIdsByType(QuestionType questionType) {
        if (questionType == null) {
            return List.of();
        }
        return questionMapper.selectList(new LambdaQueryWrapper<Question>()
                        .eq(Question::getQuestionType, questionType)
                        .eq(Question::getStatus, PUBLISHED_STATUS))
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

    private AnswerHistoryItemVO toHistoryItem(UserAnswerRecord record, Question question) {
        return new AnswerHistoryItemVO(
                record.getId(),
                record.getQuestionId(),
                question == null ? null : question.getTitle(),
                question == null ? null : question.getQuestionType(),
                question == null ? null : question.getDifficulty(),
                record.getUserAnswer(),
                record.getIsCorrect(),
                record.getScore(),
                record.getAnswerDuration(),
                answerTime(record));
    }

    private LocalDateTime answerTime(UserAnswerRecord record) {
        return record.getAnswerTime() != null ? record.getAnswerTime() : record.getCreateTime();
    }
}
