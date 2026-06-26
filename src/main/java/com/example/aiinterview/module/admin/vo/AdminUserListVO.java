package com.example.aiinterview.module.admin.vo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminUserListVO {

    private final Long id;
    private final String username;
    private final String nickname;
    private final String email;
    private final Integer status;
    private final List<String> roles;
    private final long answerCount;
    private final LocalDateTime lastAnswerTime;
    private final LocalDateTime createTime;
}
