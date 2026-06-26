package com.example.aiinterview.module.admin.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserAnswerStatsRow {

    private Long userId;
    private Long answerCount;
    private LocalDateTime lastAnswerTime;
}
