package com.example.aiinterview.module.ai.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiInterviewSubmitRequest {

    @NotEmpty(message = "cannot be empty")
    private List<@Valid AiInterviewAnswerSubmitRequest> answers = new ArrayList<>();
}
