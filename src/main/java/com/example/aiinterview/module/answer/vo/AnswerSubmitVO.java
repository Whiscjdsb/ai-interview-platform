package com.example.aiinterview.module.answer.vo;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AnswerSubmitVO {

    private final Long answerRecordId;
    private final Boolean isCorrect;
    private final BigDecimal score;
    private final String correctAnswer;
    private final String analysis;
    private final String message;
}
