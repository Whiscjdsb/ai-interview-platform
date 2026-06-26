package com.example.aiinterview.module.admin.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminAiInterviewDetailVO {

    private final Long id;
    private final Long userId;
    private final String username;
    private final String nickname;
    private final String position;
    private final BigDecimal score;
    private final String modelName;
    private final LocalDateTime createTime;
    private final JsonNode result;
}
