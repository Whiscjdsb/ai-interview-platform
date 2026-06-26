package com.example.aiinterview.module.ai.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
public class InterviewTemplateCreateRequest {

    @NotBlank(message = "position model is required")
    @Size(max = 128, message = "position model length must be at most 128")
    private String positionModel;

    @NotNull(message = "company type is required")
    private EnterpriseInterviewerType companyType;

    @NotNull(message = "difficulty is required")
    private QuestionDifficulty difficulty;

    @Min(value = 1, message = "question count must be at least 1")
    @Max(value = 10, message = "question count must be at most 10")
    @NotNull(message = "question count is required")
    private Integer questionCount = 5;

    @Size(max = 10, message = "focus areas size must be at most 10")
    private List<@Size(max = 64, message = "focus area length must be at most 64") String> focusAreas = new ArrayList<>();

    private Map<@Size(max = 64, message = "weight key length must be at most 64") String, @Min(0) @Max(100) Integer> scoringWeights = Map.of(
            "java", 20,
            "spring", 20,
            "systemDesign", 25,
            "project", 20,
            "communication", 15);
}
