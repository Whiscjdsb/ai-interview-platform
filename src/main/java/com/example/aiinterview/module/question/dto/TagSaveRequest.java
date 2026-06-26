package com.example.aiinterview.module.question.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TagSaveRequest {

    @NotBlank(message = "cannot be blank")
    @Size(max = 64, message = "length must be at most 64")
    private String tagName;

    @Size(max = 255, message = "length must be at most 255")
    private String description;
}
