package com.example.aiinterview.module.user.service;

import com.example.aiinterview.module.user.dto.LoginRequest;
import com.example.aiinterview.module.user.dto.RegisterRequest;
import com.example.aiinterview.module.user.vo.LoginVO;
import com.example.aiinterview.module.user.vo.UserInfoVO;

public interface AuthService {

    UserInfoVO register(RegisterRequest request);

    LoginVO login(LoginRequest request);

    UserInfoVO current(Long userId);
}
