package com.example.aiinterview.module.admin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserStatusRequest {

    @NotNull(message = "cannot be null")
    @Min(value = 0, message = "must be at least 0")
    @Max(value = 1, message = "must be at most 1")
    private Integer status;
}
