package com.example.aiinterview.module.admin.vo;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatisticsOverviewVO {

    private long totalUserCount;
    private long todayNewUserCount;
    private long totalQuestionCount;
    private long todayAnswerCount;
    private long totalAnswerCount;
    private BigDecimal averageAccuracyRate;
    private long activeUserCount;
    private long totalAiReviewCount;
    private long todayAiReviewCount;
}
