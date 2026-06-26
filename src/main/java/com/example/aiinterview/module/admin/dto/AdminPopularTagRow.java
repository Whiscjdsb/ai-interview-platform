package com.example.aiinterview.module.admin.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminPopularTagRow {

    private Long tagId;
    private String tagName;
    private Long answerCount;
    private Long answerUserCount;
    private BigDecimal averageAccuracyRate;
}
