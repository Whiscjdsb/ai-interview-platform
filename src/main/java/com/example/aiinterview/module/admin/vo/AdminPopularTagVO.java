package com.example.aiinterview.module.admin.vo;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminPopularTagVO {

    private Long tagId;
    private String tagName;
    private long answerCount;
    private long answerUserCount;
    private BigDecimal averageAccuracyRate;
}
