package com.example.aiinterview.module.admin.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminPopularQuestionRow {

    private Long questionId;
    private String title;
    private String questionType;
    private String difficulty;
    private Long answerCount;
    private BigDecimal accuracyRate;
}
