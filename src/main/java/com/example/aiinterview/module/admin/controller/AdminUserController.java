package com.example.aiinterview.module.admin.controller;

import com.example.aiinterview.common.PageResult;
import com.example.aiinterview.common.Result;
import com.example.aiinterview.module.admin.dto.AdminUserQueryRequest;
import com.example.aiinterview.module.admin.dto.AdminUserRolesRequest;
import com.example.aiinterview.module.admin.dto.AdminUserStatusRequest;
import com.example.aiinterview.module.admin.service.AdminUserService;
import com.example.aiinterview.module.admin.vo.AdminUserDetailVO;
import com.example.aiinterview.module.admin.vo.AdminUserListVO;
import com.example.aiinterview.security.UserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public Result<PageResult<AdminUserListVO>> pageUsers(@Valid @ModelAttribute AdminUserQueryRequest request) {
        return Result.success(adminUserService.pageUsers(request));
    }

    @GetMapping("/{id}")
    public Result<AdminUserDetailVO> getUserDetail(@PathVariable @Positive(message = "must be positive") Long id) {
        return Result.success(adminUserService.getUserDetail(id));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable @Positive(message = "must be positive") Long id,
            @Valid @RequestBody AdminUserStatusRequest request) {
        adminUserService.updateStatus(principal.getId(), id, request);
        return Result.success(null);
    }

    @PutMapping("/{id}/roles")
    public Result<Void> updateRoles(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable @Positive(message = "must be positive") Long id,
            @Valid @RequestBody AdminUserRolesRequest request) {
        adminUserService.updateRoles(principal.getId(), id, request);
        return Result.success(null);
    }
}
