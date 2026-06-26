package com.example.aiinterview.module.ai.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.aiinterview.module.ai.enums.AiRecordType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiHistoryItemVO {

    private final Long id;

    private final AiRecordType recordType;

    private final Long questionId;

    private final String questionTitle;

    private final BigDecimal score;

    private final String modelName;

    private final LocalDateTime createTime;
}
