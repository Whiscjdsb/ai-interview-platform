package com.example.aiinterview.module.statistics.vo;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsOverviewVO {

    private long totalAnswerCount;
    private long correctAnswerCount;
    private BigDecimal accuracyRate;
    private long totalStudyDuration;
    private long favoriteCount;
    private long wrongQuestionCount;
    private long activeWrongQuestionCount;
    private long resolvedWrongQuestionCount;
    private int continuousLearningDays;
    private long todayAnswerCount;
    private long todayStudyDuration;
}
