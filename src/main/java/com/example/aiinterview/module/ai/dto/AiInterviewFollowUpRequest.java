package com.example.aiinterview.module.ai.dto;

import java.util.ArrayList;
import java.util.List;

import com.example.aiinterview.module.ai.enums.EnterpriseInterviewerType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiInterviewFollowUpRequest {

    @NotBlank(message = "question is required")
    @Size(max = 2000, message = "question length must be less than 2000")
    private String question;

    @NotBlank(message = "answer is required")
    @Size(max = 5000, message = "answer length must be less than 5000")
    private String answer;

    @Valid
    @Size(max = 30, message = "history length must be less than 30")
    private List<AiInterviewConversationMessageDTO> history = new ArrayList<>();

    private EnterpriseInterviewerType interviewerType = EnterpriseInterviewerType.STARTUP;
}
