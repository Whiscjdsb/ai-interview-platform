package com.example.aiinterview.module.admin.vo;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminTrendVO {

    private LocalDate date;
    private long newUserCount;
    private long answerCount;
    private long activeUserCount;
    private long aiReviewCount;
}
