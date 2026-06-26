package com.example.aiinterview.module.user.vo;

import java.util.List;

import lombok.Getter;

@Getter
public class LoginVO extends UserInfoVO {

    private final String token;
    private final Long expiresIn;

    public LoginVO(String token, Long expiresIn, Long id, String username, String nickname, List<String> roles) {
        super(id, username, nickname, roles);
        this.token = token;
        this.expiresIn = expiresIn;
    }
}
