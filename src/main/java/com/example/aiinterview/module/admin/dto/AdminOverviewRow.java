package com.example.aiinterview.module.admin.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminOverviewRow {

    private Long totalUserCount;
    private Long todayNewUserCount;
    private Long totalQuestionCount;
    private Long todayAnswerCount;
    private Long totalAnswerCount;
    private BigDecimal averageAccuracyRate;
    private Long activeUserCount;
    private Long totalAiReviewCount;
    private Long todayAiReviewCount;
}
