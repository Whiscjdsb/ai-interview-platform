package com.example.aiinterview.module.answer.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerSubmitRequest {

    @NotNull(message = "cannot be null")
    @Positive(message = "must be positive")
    private Long questionId;

    @NotBlank(message = "cannot be blank")
    private String userAnswer;

    @NotNull(message = "cannot be null")
    @Min(value = 0, message = "must be greater than or equal to 0")
    private Integer answerDuration;
}
