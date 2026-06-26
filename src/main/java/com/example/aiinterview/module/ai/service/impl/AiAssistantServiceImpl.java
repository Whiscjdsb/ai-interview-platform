package com.example.aiinterview.module.ai.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.aiinterview.common.BusinessException;
import com.example.aiinterview.common.ErrorCode;
import com.example.aiinterview.common.PageResult;
import com.example.aiinterview.module.admin.service.AdminStatisticsCacheService;
import com.example.aiinterview.module.ai.dto.AiHistoryQueryRequest;
import com.example.aiinterview.module.ai.dto.AiReviewAnswerRequest;
import com.example.aiinterview.module.ai.dto.GenerateInterviewRequest;
import com.example.aiinterview.module.ai.entity.AiReviewRecord;
import com.example.aiinterview.module.ai.enums.AiRecordType;
import com.example.aiinterview.module.ai.enums.EnterpriseInterviewerType;
import com.example.aiinterview.module.ai.mapper.AiReviewRecordMapper;
import com.example.aiinterview.module.ai.service.AiAssistantService;
import com.example.aiinterview.module.ai.service.AiService;
import com.example.aiinterview.module.ai.service.AiServiceFactory;
import com.example.aiinterview.module.ai.vo.AiAnswerReviewVO;
import com.example.aiinterview.module.ai.vo.AiHistoryDetailVO;
import com.example.aiinterview.module.ai.vo.AiHistoryItemVO;
import com.example.aiinterview.module.ai.vo.GenerateInterviewVO;
import com.example.aiinterview.module.ai.vo.WeaknessSummaryVO;
import com.example.aiinterview.module.question.dto.QuestionTagPair;
import com.example.aiinterview.module.question.entity.Question;
import com.example.aiinterview.module.question.entity.QuestionTag;
import com.example.aiinterview.module.question.entity.QuestionTagRelation;
import com.example.aiinterview.module.question.enums.QuestionType;
import com.example.aiinterview.module.question.mapper.QuestionMapper;
import com.example.aiinterview.module.question.mapper.QuestionTagMapper;
import com.example.aiinterview.module.question.mapper.QuestionTagRelationMapper;
import com.example.aiinterview.module.question.vo.TagVO;
import com.example.aiinterview.module.statistics.service.UserStatisticsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AiAssistantServiceImpl implements AiAssistantService {

    private static final int PUBLISHED_STATUS = 1;

    private final AiReviewRecordMapper aiReviewRecordMapper;
    private final QuestionMapper questionMapper;
    private final QuestionTagMapper questionTagMapper;
    private final QuestionTagRelationMapper questionTagRelationMapper;
    private final AiServiceFactory aiServiceFactory;
    private final UserStatisticsService userStatisticsService;
    private final ObjectMapper objectMapper;
    private final AdminStatisticsCacheService adminStatisticsCacheService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiAnswerReviewVO reviewAnswer(Long userId, AiReviewAnswerRequest request) {
        Question question = getQuestionOrThrow(request.getQuestionId());
        if (!QuestionType.SHORT_ANSWER.equals(question.getQuestionType()) && !QuestionType.CODING.equals(question.getQuestionType())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Only SHORT_ANSWER or CODING questions can be reviewed");
        }

        List<TagVO> tags = loadTags(List.of(question.getId())).getOrDefault(question.getId(), List.of());
        AiService aiService = aiServiceFactory.current();
        AiAnswerReviewVO result = aiService.reviewAnswer(question, tags, request.getAnswer());
        result.setScore(result.getScore() == null ? 0 : Math.max(0, Math.min(100, result.getScore())));
        result.setModelName(aiService.modelName());

        saveRecord(userId, question.getId(), AiRecordType.ANSWER_REVIEW, request, result, BigDecimal.valueOf(result.getScore()), result.getModelName());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GenerateInterviewVO generateInterview(Long userId, GenerateInterviewRequest request) {
        AiService aiService = aiServiceFactory.current();
        GenerateInterviewVO result = aiService.generateInterview(request, loadSeedQuestions(request));
        result.setModelName(aiService.modelName());
        result.setQuestionCount(result.getQuestions() == null ? 0 : result.getQuestions().size());
        result.setFocusAreas(request.getFocusTags());
        result.setStatus("IN_PROGRESS");
        result.setInterviewerType(EnterpriseInterviewerType.defaultIfNull(request.getInterviewerType()));
        result.setPositionModel(request.getPositionModel());
        result.setPressureMode(Boolean.TRUE.equals(request.getPressureMode()));
        AiReviewRecord record = saveRecord(userId, null, AiRecordType.MOCK_INTERVIEW, request, result, null, result.getModelName());
        result.setId(record.getId());
        updateRecordResult(record, result, null);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WeaknessSummaryVO summarizeWeakness(Long userId) {
        AiService aiService = aiServiceFactory.current();
        WeaknessSummaryVO result = aiService.summarizeWeakness(
                userStatisticsService.getTagAccuracy(userId),
                userStatisticsService.getWrongAnalysis(userId));
        result.setModelName(aiService.modelName());
        saveRecord(userId, null, AiRecordType.WEAKNESS_SUMMARY, Map.of("source", "user-statistics"), result, null, result.getModelName());
        return result;
    }

    @Override
    public PageResult<AiHistoryItemVO> pageHistory(Long userId, AiHistoryQueryRequest request) {
        Page<AiReviewRecord> page = aiReviewRecordMapper.selectPage(
                new Page<>(request.getPage(), request.getSize()),
                new LambdaQueryWrapper<AiReviewRecord>()
                        .eq(AiReviewRecord::getUserId, userId)
                        .eq(request.getRecordType() != null, AiReviewRecord::getRecordType, request.getRecordType())
                        .orderByDesc(AiReviewRecord::getCreateTime));
        Map<Long, Question> questionMap = loadQuestionMap(page.getRecords().stream()
                .map(AiReviewRecord::getQuestionId)
                .filter(id -> id != null)
                .toList());
        List<AiHistoryItemVO> records = page.getRecords().stream()
                .map(record -> toHistoryItem(record, questionMap.get(record.getQuestionId())))
                .toList();
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getPages(), records);
    }

    @Override
    public AiHistoryDetailVO getHistoryDetail(Long userId, Long id) {
        AiReviewRecord record = getHistoryOrThrow(id);
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Access is denied");
        }
        Question question = record.getQuestionId() == null ? null : questionMapper.selectById(record.getQuestionId());
        return new AiHistoryDetailVO(
                record.getId(),
                record.getRecordType(),
                record.getQuestionId(),
                question == null ? null : question.getTitle(),
                record.getScore(),
                record.getModelName(),
                record.getCreateTime(),
                parseResult(record.getResultContent()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteHistory(Long userId, Long id) {
        AiReviewRecord record = getHistoryOrThrow(id);
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Access is denied");
        }
        aiReviewRecordMapper.deleteById(id);
    }

    private Question getQuestionOrThrow(Long questionId) {
        Question question = questionMapper.selectById(questionId);
        if (question == null || !Integer.valueOf(PUBLISHED_STATUS).equals(question.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Question does not exist");
        }
        return question;
    }

    private AiReviewRecord getHistoryOrThrow(Long id) {
        AiReviewRecord record = aiReviewRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "AI history record does not exist");
        }
        return record;
    }

    private List<Question> loadSeedQuestions(GenerateInterviewRequest request) {
        List<Long> questionIds = questionIdsByFocusTags(request.getFocusTags());
        if (!CollectionUtils.isEmpty(request.getFocusTags()) && questionIds.isEmpty()) {
            return List.of();
        }
        return questionMapper.selectList(new LambdaQueryWrapper<Question>()
                .eq(Question::getStatus, PUBLISHED_STATUS)
                .eq(Question::getDifficulty, request.getDifficulty())
                .in(!questionIds.isEmpty(), Question::getId, questionIds)
                .orderByDesc(Question::getCreateTime)
                .last("LIMIT " + request.getQuestionCount()));
    }

    private List<Long> questionIdsByFocusTags(List<String> focusTags) {
        if (CollectionUtils.isEmpty(focusTags)) {
            return List.of();
        }
        Set<String> names = focusTags.stream()
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        if (names.isEmpty()) {
            return List.of();
        }
        List<QuestionTag> tags = questionTagMapper.selectList(new LambdaQueryWrapper<QuestionTag>()
                .in(QuestionTag::getTagName, names));
        if (tags.isEmpty()) {
            return List.of();
        }
        List<Long> tagIds = tags.stream().map(QuestionTag::getId).toList();
        return questionTagRelationMapper.selectList(new LambdaQueryWrapper<QuestionTagRelation>()
                        .in(QuestionTagRelation::getTagId, tagIds))
                .stream()
                .map(QuestionTagRelation::getQuestionId)
                .distinct()
                .toList();
    }

    private Map<Long, List<TagVO>> loadTags(List<Long> questionIds) {
        if (CollectionUtils.isEmpty(questionIds)) {
            return Map.of();
        }
        List<QuestionTagPair> pairs = questionTagRelationMapper.selectTagPairsByQuestionIds(questionIds);
        return pairs.stream().collect(Collectors.groupingBy(
                QuestionTagPair::getQuestionId,
                Collectors.mapping(pair -> new TagVO(pair.getTagId(), pair.getTagName(), pair.getDescription()), Collectors.toList())));
    }

    private Map<Long, Question> loadQuestionMap(List<Long> questionIds) {
        if (CollectionUtils.isEmpty(questionIds)) {
            return Map.of();
        }
        return questionMapper.selectBatchIds(questionIds).stream()
                .collect(Collectors.toMap(Question::getId, Function.identity(), (left, right) -> left));
    }

    private AiReviewRecord saveRecord(Long userId, Long questionId, AiRecordType recordType, Object input, Object result, BigDecimal score, String modelName) {
        try {
            String inputJson = objectMapper.writeValueAsString(input);
            String resultJson = objectMapper.writeValueAsString(result);
            AiReviewRecord record = new AiReviewRecord();
            record.setUserId(userId);
            record.setQuestionId(questionId);
            record.setRecordType(recordType);
            record.setInputContent(inputJson);
            record.setPrompt(inputJson);
            record.setResultContent(resultJson);
            record.setReviewResult(resultJson);
            record.setScore(score);
            record.setModelName(modelName);
            if (input instanceof GenerateInterviewRequest interviewRequest) {
                record.setInterviewerType(EnterpriseInterviewerType.defaultIfNull(interviewRequest.getInterviewerType()));
            }
            aiReviewRecordMapper.insert(record);
            adminStatisticsCacheService.evictAll();
            return record;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to save AI history record");
        }
    }

    private void updateRecordResult(AiReviewRecord record, Object result, BigDecimal score) {
        try {
            String resultJson = objectMapper.writeValueAsString(result);
            record.setResultContent(resultJson);
            record.setReviewResult(resultJson);
            record.setScore(score);
            aiReviewRecordMapper.updateById(record);
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to update AI history record");
        }
    }

    private JsonNode parseResult(String resultContent) {
        try {
            if (!StringUtils.hasText(resultContent)) {
                return objectMapper.createObjectNode();
            }
            return objectMapper.readTree(resultContent);
        } catch (Exception ex) {
            return objectMapper.createObjectNode();
        }
    }

    private AiHistoryItemVO toHistoryItem(AiReviewRecord record, Question question) {
        return new AiHistoryItemVO(
                record.getId(),
                record.getRecordType(),
                record.getQuestionId(),
                question == null ? null : question.getTitle(),
                record.getScore(),
                record.getModelName(),
                record.getCreateTime());
    }
}
