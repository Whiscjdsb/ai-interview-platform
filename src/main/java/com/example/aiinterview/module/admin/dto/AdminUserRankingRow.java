package com.example.aiinterview.module.admin.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserRankingRow {

    private Long userId;
    private String username;
    private String nickname;
    private Long answerCount;
    private Long correctCount;
    private BigDecimal accuracyRate;
    private Long studyDuration;
}
