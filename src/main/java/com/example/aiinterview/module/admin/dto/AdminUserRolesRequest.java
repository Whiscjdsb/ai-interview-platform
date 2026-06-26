package com.example.aiinterview.module.admin.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserRolesRequest {

    @NotEmpty(message = "cannot be empty")
    private List<@Pattern(regexp = "USER|ADMIN", message = "must be USER or ADMIN") String> roleCodes;
}
