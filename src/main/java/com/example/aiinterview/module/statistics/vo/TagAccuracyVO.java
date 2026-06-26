package com.example.aiinterview.module.statistics.vo;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TagAccuracyVO {

    private final Long tagId;
    private final String tagName;
    private final long answerCount;
    private final long correctCount;
    private final BigDecimal accuracyRate;
}
