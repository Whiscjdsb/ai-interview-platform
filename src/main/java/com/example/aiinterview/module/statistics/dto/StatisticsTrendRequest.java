package com.example.aiinterview.module.statistics.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatisticsTrendRequest {

    @Min(value = 1, message = "must be at least 1")
    @Max(value = 30, message = "must be at most 30")
    private Integer days = 7;
}
