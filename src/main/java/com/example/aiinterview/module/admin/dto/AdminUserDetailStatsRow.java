package com.example.aiinterview.module.admin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserDetailStatsRow {

    private Long answerCount;
    private Long correctCount;
    private Long favoriteCount;
    private Long activeWrongQuestionCount;
    private Long aiReviewCount;
}
