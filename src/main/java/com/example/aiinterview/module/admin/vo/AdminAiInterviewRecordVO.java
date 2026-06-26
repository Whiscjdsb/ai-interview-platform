package com.example.aiinterview.module.admin.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminAiInterviewRecordVO {

    private final Long id;
    private final Long userId;
    private final String username;
    private final String nickname;
    private final String position;
    private final BigDecimal score;
    private final String modelName;
    private final LocalDateTime createTime;
}
