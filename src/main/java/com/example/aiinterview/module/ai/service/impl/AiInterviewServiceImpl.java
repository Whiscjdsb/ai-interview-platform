package com.example.aiinterview.module.ai.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aiinterview.common.BusinessException;
import com.example.aiinterview.common.ErrorCode;
import com.example.aiinterview.module.admin.service.AdminStatisticsCacheService;
import com.example.aiinterview.module.ai.dto.AiInterviewAnswerSubmitRequest;
import com.example.aiinterview.module.ai.dto.AiInterviewSubmitRequest;
import com.example.aiinterview.module.ai.dto.GenerateInterviewRequest;
import com.example.aiinterview.module.ai.entity.AiReviewRecord;
import com.example.aiinterview.module.ai.enums.AiRecordType;
import com.example.aiinterview.module.ai.mapper.AiReviewRecordMapper;
import com.example.aiinterview.module.ai.vo.AiInterviewDetailVO;
import com.example.aiinterview.module.ai.vo.AiInterviewQuestionResultVO;
import com.example.aiinterview.module.ai.vo.AiInterviewQuestionVO;
import com.example.aiinterview.module.ai.vo.AiInterviewSubmitResponseVO;
import com.example.aiinterview.module.ai.vo.GenerateInterviewVO;
import com.example.aiinterview.module.ai.vo.MockInterviewQuestionVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AiInterviewServiceImpl implements com.example.aiinterview.module.ai.service.AiInterviewService {

    private final AiReviewRecordMapper aiReviewRecordMapper;
    private final ObjectMapper objectMapper;
    private final AdminStatisticsCacheService adminStatisticsCacheService;

    @Override
    public AiInterviewDetailVO getInterview(Long userId, Long id) {
        AiReviewRecord record = getInterviewRecordOrThrow(userId, id);
        GenerateInterviewRequest input = readValue(record.getInputContent(), GenerateInterviewRequest.class);
        InterviewSnapshot snapshot = readInterviewSnapshot(record);
        return new AiInterviewDetailVO(
                record.getId(),
                snapshot.position(),
                snapshot.difficulty(),
                snapshot.questions().size(),
                input.getFocusTags(),
                snapshot.submitted() ? "SUBMITTED" : "IN_PROGRESS",
                snapshot.questions().stream()
                        .map(question -> new AiInterviewQuestionVO(
                                question.id(),
                                question.questionNo(),
                                question.question(),
                                question.category(),
                                question.difficulty(),
                                question.referencePoints(),
                                null))
                        .toList(),
                snapshot.modelName(),
                record.getCreateTime());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiInterviewSubmitResponseVO submitInterview(Long userId, Long id, AiInterviewSubmitRequest request) {
        AiReviewRecord record = getInterviewRecordOrThrow(userId, id);
        InterviewSnapshot snapshot = readInterviewSnapshot(record);
        if (snapshot.questions().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Interview questions are empty");
        }

        Map<Integer, AiInterviewAnswerSubmitRequest> answersByNo = request.getAnswers().stream()
                .filter(answer -> answer.getQuestionNo() != null)
                .collect(Collectors.toMap(
                        AiInterviewAnswerSubmitRequest::getQuestionNo,
                        Function.identity(),
                        (left, right) -> right));
        Map<Long, AiInterviewAnswerSubmitRequest> answersById = request.getAnswers().stream()
                .filter(answer -> answer.getQuestionId() != null)
                .collect(Collectors.toMap(
                        AiInterviewAnswerSubmitRequest::getQuestionId,
                        Function.identity(),
                        (left, right) -> right));

        List<AiInterviewQuestionResultVO> questionResults = snapshot.questions().stream()
                .sorted(Comparator.comparing(question -> question.questionNo() == null ? 0 : question.questionNo()))
                .map(question -> scoreQuestion(question, findAnswer(question, answersByNo, answersById)))
                .toList();
        int totalScore = Math.round((float) questionResults.stream()
                .mapToInt(AiInterviewQuestionResultVO::getScore)
                .average()
                .orElse(0D));
        List<String> disadvantages = buildDisadvantages(questionResults);
        AiInterviewSubmitResponseVO result = new AiInterviewSubmitResponseVO(
                record.getId(),
                snapshot.position(),
                snapshot.difficulty(),
                totalScore,
                level(totalScore),
                summary(totalScore),
                buildAdvantages(questionResults),
                disadvantages,
                disadvantages,
                buildSuggestions(snapshot, questionResults),
                questionResults,
                snapshot.modelName(),
                LocalDateTime.now());

        try {
            String resultJson = objectMapper.writeValueAsString(result);
            record.setResultContent(resultJson);
            record.setReviewResult(resultJson);
            record.setScore(BigDecimal.valueOf(totalScore));
            aiReviewRecordMapper.updateById(record);
            adminStatisticsCacheService.evictAll();
            return result;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to save interview result");
        }
    }

    private AiReviewRecord getInterviewRecordOrThrow(Long userId, Long id) {
        AiReviewRecord record = aiReviewRecordMapper.selectOne(new LambdaQueryWrapper<AiReviewRecord>()
                .eq(AiReviewRecord::getId, id)
                .eq(AiReviewRecord::getUserId, userId)
                .eq(AiReviewRecord::getRecordType, AiRecordType.MOCK_INTERVIEW)
                .last("LIMIT 1"));
        if (record == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "AI interview does not exist");
        }
        return record;
    }

    private InterviewSnapshot readInterviewSnapshot(AiReviewRecord record) {
        try {
            if (StringUtils.hasText(record.getResultContent())
                    && objectMapper.readTree(record.getResultContent()).has("questionResults")) {
                AiInterviewSubmitResponseVO submitted = objectMapper.readValue(
                        record.getResultContent(),
                        AiInterviewSubmitResponseVO.class);
                return InterviewSnapshot.fromSubmitted(submitted);
            }
            GenerateInterviewVO generated = readValue(record.getResultContent(), GenerateInterviewVO.class);
            return InterviewSnapshot.fromGenerated(record, generated);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to parse interview data");
        }
    }

    private AiInterviewQuestionResultVO scoreQuestion(
            InterviewQuestionSnapshot question,
            AiInterviewAnswerSubmitRequest answerRequest) {
        String answer = answerRequest == null ? "" : normalize(answerRequest.getAnswer());
        int score = calculateScore(question, answer);
        List<String> advantages = new ArrayList<>();
        if (StringUtils.hasText(answer)) {
            advantages.add("能够给出完整作答");
        }
        long hitCount = referenceHitCount(question.referencePoints(), answer);
        if (hitCount > 0) {
            advantages.add("回答覆盖了 " + hitCount + " 个参考要点");
        }
        if (answer.length() >= 120) {
            advantages.add("回答展开较充分");
        }
        if (advantages.isEmpty()) {
            advantages.add("已完成题目阅读，建议补充作答内容");
        }

        List<String> improvements = missingReferencePoints(question.referencePoints(), answer).stream()
                .limit(2)
                .map(point -> "补充说明：" + point)
                .collect(Collectors.toCollection(ArrayList::new));
        if (answer.length() < 80) {
            improvements.add("回答篇幅偏短，可增加原理、场景和案例");
        }
        if (improvements.isEmpty()) {
            improvements.add("继续补充项目实践中的取舍和边界条件");
        }

        String referenceAnswer = referenceAnswer(question);
        String comment = comment(score, question.category());
        return new AiInterviewQuestionResultVO(
                question.id(),
                question.questionNo(),
                question.question(),
                answer,
                answer,
                score,
                comment,
                comment,
                advantages,
                improvements,
                referenceAnswer,
                referenceAnswer);
    }

    private int calculateScore(InterviewQuestionSnapshot question, String answer) {
        if (!StringUtils.hasText(answer)) {
            return 35;
        }
        int lengthScore = Math.min(30, answer.length() / 8);
        int referenceScore = (int) Math.min(30, referenceHitCount(question.referencePoints(), answer) * 10);
        int structureScore = containsAny(answer, List.of("首先", "其次", "最后", "例如", "场景", "原因", "总结")) ? 10 : 0;
        return Math.max(0, Math.min(100, 40 + lengthScore + referenceScore + structureScore));
    }

    private long referenceHitCount(List<String> referencePoints, String answer) {
        String normalized = answer.toLowerCase(Locale.ROOT);
        return referencePoints.stream()
                .filter(StringUtils::hasText)
                .filter(point -> normalized.contains(point.toLowerCase(Locale.ROOT)))
                .count();
    }

    private List<String> missingReferencePoints(List<String> referencePoints, String answer) {
        String normalized = answer.toLowerCase(Locale.ROOT);
        return referencePoints.stream()
                .filter(StringUtils::hasText)
                .filter(point -> !normalized.contains(point.toLowerCase(Locale.ROOT)))
                .toList();
    }

    private boolean containsAny(String answer, List<String> keywords) {
        return keywords.stream().anyMatch(answer::contains);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private AiInterviewAnswerSubmitRequest findAnswer(
            InterviewQuestionSnapshot question,
            Map<Integer, AiInterviewAnswerSubmitRequest> answersByNo,
            Map<Long, AiInterviewAnswerSubmitRequest> answersById) {
        AiInterviewAnswerSubmitRequest answer = answersById.get(question.id());
        if (answer != null) {
            return answer;
        }
        return answersByNo.get(question.questionNo());
    }

    private String referenceAnswer(InterviewQuestionSnapshot question) {
        if (question.referencePoints().isEmpty()) {
            return "建议从核心概念、应用场景、底层原理和常见问题四个角度组织回答。";
        }
        return "建议覆盖：" + String.join("、", question.referencePoints()) + "，并结合项目经验说明落地方式。";
    }

    private String comment(int score, String category) {
        if (score >= 85) {
            return "回答较完整，能够覆盖 " + category + " 的核心要点。";
        }
        if (score >= 70) {
            return "回答方向正确，但 " + category + " 的细节和案例还可以继续补充。";
        }
        return "回答偏简略，建议重新梳理 " + category + " 的概念、原理和典型场景。";
    }

    private String level(int score) {
        if (score >= 90) {
            return "优秀";
        }
        if (score >= 80) {
            return "良好";
        }
        if (score >= 60) {
            return "可提升";
        }
        return "需要加强";
    }

    private String summary(int score) {
        if (score >= 85) {
            return "整体回答较完整，知识覆盖和表达结构表现较好。";
        }
        if (score >= 70) {
            return "整体回答方向正确，但部分底层原理和项目案例仍可加强。";
        }
        return "整体回答较薄弱，建议先补齐核心概念，再练习结构化表达。";
    }

    private List<String> buildAdvantages(List<AiInterviewQuestionResultVO> questionResults) {
        int answered = (int) questionResults.stream().filter(item -> StringUtils.hasText(item.getUserAnswer())).count();
        return List.of(
                "完成了 " + answered + " 道题的作答",
                "能够围绕题目给出面试表达",
                "已形成可复盘的逐题记录");
    }

    private List<String> buildDisadvantages(List<AiInterviewQuestionResultVO> questionResults) {
        List<String> result = new ArrayList<>();
        if (questionResults.stream().anyMatch(item -> !StringUtils.hasText(item.getUserAnswer()))) {
            result.add("存在未作答题目，需要补全回答");
        }
        if (questionResults.stream().anyMatch(item -> item.getScore() < 70)) {
            result.add("部分题目得分偏低，核心要点覆盖不足");
        }
        result.add("建议增加项目案例和边界条件说明");
        return result;
    }

    private List<String> buildSuggestions(InterviewSnapshot snapshot, List<AiInterviewQuestionResultVO> questionResults) {
        List<String> lowScoreCategories = questionResults.stream()
                .filter(item -> item.getScore() < 75)
                .map(AiInterviewQuestionResultVO::getQuestion)
                .limit(2)
                .toList();
        List<String> suggestions = new ArrayList<>();
        suggestions.add("按“结论、原理、场景、案例”四段式重新整理回答");
        suggestions.add("围绕 " + snapshot.position() + " 准备 3 个可复用项目案例");
        if (!lowScoreCategories.isEmpty()) {
            suggestions.add("优先复盘低分题：" + String.join("；", lowScoreCategories));
        }
        return suggestions;
    }

    private <T> T readValue(String content, Class<T> clazz) {
        try {
            if (!StringUtils.hasText(content)) {
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Interview data is empty");
            }
            return objectMapper.readValue(content, clazz);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to parse interview data");
        }
    }

    private record InterviewSnapshot(
            String position,
            com.example.aiinterview.module.question.enums.QuestionDifficulty difficulty,
            List<InterviewQuestionSnapshot> questions,
            String modelName,
            boolean submitted) {

        static InterviewSnapshot fromGenerated(AiReviewRecord record, GenerateInterviewVO generated) {
            List<MockInterviewQuestionVO> generatedQuestions = generated.getQuestions() == null
                    ? List.of()
                    : generated.getQuestions();
            return new InterviewSnapshot(
                    generated.getPosition(),
                    generated.getDifficulty(),
                    generatedQuestions.stream()
                            .map(question -> new InterviewQuestionSnapshot(
                                    question.getId() == null && question.getQuestionNo() == null
                                            ? null
                                            : question.getId() == null ? question.getQuestionNo().longValue() : question.getId(),
                                    question.getQuestionNo(),
                                    question.getQuestion(),
                                    question.getCategory(),
                                    question.getDifficulty(),
                                    question.getReferencePoints() == null ? List.of() : question.getReferencePoints()))
                            .toList(),
                    generated.getModelName() == null ? record.getModelName() : generated.getModelName(),
                    false);
        }

        static InterviewSnapshot fromSubmitted(AiInterviewSubmitResponseVO submitted) {
            List<AiInterviewQuestionResultVO> submittedResults = submitted.getQuestionResults() == null
                    ? List.of()
                    : submitted.getQuestionResults();
            return new InterviewSnapshot(
                    submitted.getPosition(),
                    submitted.getDifficulty(),
                    submittedResults.stream()
                            .map(result -> new InterviewQuestionSnapshot(
                                    result.getQuestionId(),
                                    result.getQuestionNo(),
                                    result.getQuestion(),
                                    "Interview",
                                    submitted.getDifficulty(),
                                    StringUtils.hasText(result.getReferenceAnswer())
                                            ? List.of(result.getReferenceAnswer())
                                            : List.of()))
                            .toList(),
                    submitted.getModelName(),
                    true);
        }
    }

    private record InterviewQuestionSnapshot(
            Long id,
            Integer questionNo,
            String question,
            String category,
            com.example.aiinterview.module.question.enums.QuestionDifficulty difficulty,
            List<String> referencePoints) {
    }
}
