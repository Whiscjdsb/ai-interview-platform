package com.example.aiinterview.module.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminDashboardVO {

    private final long totalUserCount;

    private final long totalQuestionCount;

    private final long totalFavoriteCount;

    private final long totalWrongQuestionCount;

    private final long totalAnswerCount;

    private final long totalAiInterviewCount;
}
