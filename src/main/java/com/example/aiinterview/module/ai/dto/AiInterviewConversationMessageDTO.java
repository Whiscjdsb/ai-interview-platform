package com.example.aiinterview.module.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiInterviewConversationMessageDTO {

    @NotBlank(message = "role is required")
    @Size(max = 32, message = "role length must be less than 32")
    private String role;

    @NotBlank(message = "content is required")
    @Size(max = 5000, message = "content length must be less than 5000")
    private String content;
}
