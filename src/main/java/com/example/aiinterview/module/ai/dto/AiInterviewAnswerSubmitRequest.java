package com.example.aiinterview.module.ai.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiInterviewAnswerSubmitRequest {

    private Long questionId;

    private Integer questionNo;

    @Size(max = 5000, message = "length must be at most 5000")
    private String answer;
}
