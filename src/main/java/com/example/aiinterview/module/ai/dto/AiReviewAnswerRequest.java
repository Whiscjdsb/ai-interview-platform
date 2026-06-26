package com.example.aiinterview.module.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiReviewAnswerRequest {

    @NotNull(message = "cannot be null")
    @Positive(message = "must be positive")
    private Long questionId;

    @NotBlank(message = "cannot be blank")
    @Size(min = 20, max = 5000, message = "length must be between 20 and 5000")
    private String answer;
}
