package com.example.aiinterview.module.admin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminLimitQueryRequest {

    @Min(value = 1, message = "must be at least 1")
    @Max(value = 20, message = "must be at most 20")
    private Integer limit = 10;
}
