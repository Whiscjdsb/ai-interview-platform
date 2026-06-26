package com.example.aiinterview.module.ai.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.aiinterview.module.ai.enums.AiRecordType;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiHistoryDetailVO {

    private final Long id;

    private final AiRecordType recordType;

    private final Long questionId;

    private final String questionTitle;

    private final BigDecimal score;

    private final String modelName;

    private final LocalDateTime createTime;

    private final JsonNode result;
}
