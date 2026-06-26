package com.example.aiinterview.module.answer.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.aiinterview.module.question.enums.QuestionDifficulty;
import com.example.aiinterview.module.question.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AnswerHistoryItemVO {

    private final Long id;
    private final Long questionId;
    private final String title;
    private final QuestionType questionType;
    private final QuestionDifficulty difficulty;
    private final String userAnswer;
    private final Boolean isCorrect;
    private final BigDecimal score;
    private final Integer answerDuration;
    private final LocalDateTime answerTime;
}
