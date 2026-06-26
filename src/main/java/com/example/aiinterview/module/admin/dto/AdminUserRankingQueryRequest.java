package com.example.aiinterview.module.admin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserRankingQueryRequest {

    @Min(value = 1, message = "must be at least 1")
    @Max(value = 30, message = "must be at most 30")
    private Integer days = 7;

    @Min(value = 1, message = "must be at least 1")
    @Max(value = 50, message = "must be at most 50")
    private Integer limit = 10;

    @Pattern(regexp = "answerCount|studyDuration|accuracyRate", message = "must be answerCount, studyDuration or accuracyRate")
    private String sortBy = "answerCount";
}
