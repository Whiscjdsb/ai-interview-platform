package com.example.aiinterview.module.ai.dto;

import java.util.ArrayList;
import java.util.List;

import com.example.aiinterview.module.ai.enums.EnterpriseInterviewerType;
import com.example.aiinterview.module.question.enums.QuestionDifficulty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenerateInterviewRequest {

    @NotBlank(message = "cannot be blank")
    @Size(max = 128, message = "length must be at most 128")
    private String position;

    @NotNull(message = "cannot be null")
    private QuestionDifficulty difficulty;

    @Size(max = 10, message = "size must be at most 10")
    private List<@Size(max = 64, message = "length must be at most 64") String> focusTags = new ArrayList<>();

    @Min(value = 1, message = "must be at least 1")
    @Max(value = 10, message = "must be at most 10")
    @NotNull(message = "cannot be null")
    private Integer questionCount = 5;

    private EnterpriseInterviewerType interviewerType = EnterpriseInterviewerType.STARTUP;

    @Size(max = 128, message = "length must be at most 128")
    private String positionModel;

    private Boolean pressureMode = false;

    private Long templateId;
}
