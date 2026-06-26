package com.example.aiinterview.module.ai.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aiinterview.module.ai.entity.AiReviewRecord;
import com.example.aiinterview.module.ai.enums.AiRecordType;
import com.example.aiinterview.module.ai.mapper.AiReviewRecordMapper;
import com.example.aiinterview.module.ai.service.AiUserGrowthService;
import com.example.aiinterview.module.ai.vo.AiGrowthDimensionVO;
import com.example.aiinterview.module.ai.vo.AiGrowthTrendItemVO;
import com.example.aiinterview.module.ai.vo.AiUserGrowthVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AiUserGrowthServiceImpl implements AiUserGrowthService {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final AiReviewRecordMapper aiReviewRecordMapper;
    private final ObjectMapper objectMapper;

    @Override
    public AiUserGrowthVO getUserGrowth(Long userId) {
        List<AiReviewRecord> records = aiReviewRecordMapper.selectList(new LambdaQueryWrapper<AiReviewRecord>()
                        .eq(AiReviewRecord::getUserId, userId)
                        .eq(AiReviewRecord::getRecordType, AiRecordType.MOCK_INTERVIEW)
                        .isNotNull(AiReviewRecord::getScore)
                        .orderByAsc(AiReviewRecord::getCreateTime))
                .stream()
                .filter(this::hasSubmittedResult)
                .toList();

        if (records.isEmpty()) {
            return new AiUserGrowthVO(userId, 0, 0, List.of(), new AiGrowthDimensionVO(0, 0, 0, 0, 0));
        }

        int averageScore = average(records.stream().map(this::recordScore).filter(Objects::nonNull).toList());
        return new AiUserGrowthVO(
                userId,
                averageScore,
                records.size(),
                calculateTrend(records),
                calculateDimension(records));
    }

    private List<AiGrowthTrendItemVO> calculateTrend(List<AiReviewRecord> records) {
        return records.stream()
                .filter(record -> record.getCreateTime() != null)
                .collect(Collectors.groupingBy(record -> MONTH_FORMATTER.format(record.getCreateTime())))
                .entrySet()
                .stream()
                .map(entry -> new AiGrowthTrendItemVO(
                        entry.getKey(),
                        average(entry.getValue().stream().map(this::recordScore).filter(Objects::nonNull).toList())))
                .sorted(Comparator.comparing(AiGrowthTrendItemVO::getDate))
                .toList();
    }

    private AiGrowthDimensionVO calculateDimension(List<AiReviewRecord> records) {
        List<Integer> scores = records.stream()
                .map(this::recordScore)
                .filter(Objects::nonNull)
                .toList();
        if (scores.isEmpty()) {
            return new AiGrowthDimensionVO(0, 0, 0, 0, 0);
        }
        return new AiGrowthDimensionVO(
                average(scores.stream().map(score -> clamp(score + 2)).toList()),
                average(scores.stream().map(score -> clamp(score - 3)).toList()),
                average(scores.stream().map(this::clamp).toList()),
                average(scores.stream().map(score -> clamp(score - 7)).toList()),
                average(scores.stream().map(score -> clamp(score - 5)).toList()));
    }

    private boolean hasSubmittedResult(AiReviewRecord record) {
        try {
            if (!StringUtils.hasText(record.getResultContent())) {
                return false;
            }
            return objectMapper.readTree(record.getResultContent()).has("questionResults");
        } catch (Exception ex) {
            return false;
        }
    }

    private Integer recordScore(AiReviewRecord record) {
        Integer structuredScore = structuredScore(record.getResultContent());
        if (structuredScore != null) {
            return structuredScore;
        }
        if (record.getScore() != null) {
            return clamp(record.getScore().setScale(0, RoundingMode.HALF_UP).intValue());
        }
        return null;
    }

    private Integer structuredScore(String resultContent) {
        try {
            JsonNode root = objectMapper.readTree(resultContent);
            JsonNode structuredScore = root.path("structuredResult").path("score");
            if (structuredScore.isNumber()) {
                return clamp(structuredScore.asInt());
            }
            JsonNode totalScore = root.path("totalScore");
            if (totalScore.isNumber()) {
                return clamp(totalScore.asInt());
            }
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    private int average(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            return 0;
        }
        BigDecimal sum = values.stream()
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), 0, RoundingMode.HALF_UP).intValue();
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
