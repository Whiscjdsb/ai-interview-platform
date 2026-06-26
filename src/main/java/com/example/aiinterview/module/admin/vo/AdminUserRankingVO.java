package com.example.aiinterview.module.admin.vo;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminUserRankingVO {

    private final Long userId;
    private final String username;
    private final String nickname;
    private final long answerCount;
    private final long correctCount;
    private final BigDecimal accuracyRate;
    private final long studyDuration;
}
