package com.example.aiinterview.module.admin.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.aiinterview.module.statistics.vo.LearningTrendVO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminUserDetailVO {

    private final Long id;
    private final String username;
    private final String nickname;
    private final String email;
    private final Integer status;
    private final List<String> roles;
    private final LocalDateTime createTime;
    private final LocalDateTime lastLoginTime;
    private final long answerCount;
    private final long correctCount;
    private final BigDecimal accuracyRate;
    private final long favoriteCount;
    private final long activeWrongQuestionCount;
    private final long aiReviewCount;
    private final List<LearningTrendVO> recentTrend;
}
