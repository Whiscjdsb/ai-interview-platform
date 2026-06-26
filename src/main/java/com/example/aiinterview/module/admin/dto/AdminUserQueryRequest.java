package com.example.aiinterview.module.admin.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
public class AdminUserQueryRequest {

    @Min(value = 1, message = "must be at least 1")
    private Long page = 1L;

    @Min(value = 1, message = "must be at least 1")
    @Max(value = 100, message = "must be at most 100")
    private Long size = 10L;

    private String keyword;

    @Min(value = 0, message = "must be at least 0")
    @Max(value = 1, message = "must be at most 1")
    private Integer status;

    private String roleCode;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endTime;
}
