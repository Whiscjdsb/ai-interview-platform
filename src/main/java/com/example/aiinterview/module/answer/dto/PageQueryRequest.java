package com.example.aiinterview.module.answer.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageQueryRequest {

    @Min(value = 1, message = "must be at least 1")
    private Long page = 1L;

    @Min(value = 1, message = "must be at least 1")
    @Max(value = 100, message = "must be at most 100")
    private Long size = 10L;
}
