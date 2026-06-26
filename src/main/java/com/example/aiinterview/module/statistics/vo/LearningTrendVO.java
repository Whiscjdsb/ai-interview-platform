package com.example.aiinterview.module.statistics.vo;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LearningTrendVO {

    private final LocalDate date;
    private final long answerCount;
    private final long correctCount;
    private final BigDecimal accuracyRate;
    private final long studyDuration;
}
