package com.example.aiinterview.module.user.controller;

import com.example.aiinterview.common.Result;
import com.example.aiinterview.module.user.dto.LoginRequest;
import com.example.aiinterview.module.user.dto.RegisterRequest;
import com.example.aiinterview.module.user.service.AuthService;
import com.example.aiinterview.module.user.vo.LoginVO;
import com.example.aiinterview.module.user.vo.UserInfoVO;
import com.example.aiinterview.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<UserInfoVO> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(authService.register(request));
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @GetMapping("/current")
    public Result<UserInfoVO> current(@AuthenticationPrincipal UserPrincipal principal) {
        return Result.success(authService.current(principal.getId()));
    }
}
