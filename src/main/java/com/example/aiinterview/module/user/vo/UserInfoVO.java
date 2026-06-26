package com.example.aiinterview.module.user.vo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfoVO {

    private final Long id;
    private final String username;
    private final String nickname;
    private final List<String> roles;
}
