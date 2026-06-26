package com.example.aiinterview.module.ai.vo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiUserGrowthVO {

    private Long userId;

    private Integer averageScore;

    private Integer interviewCount;

    private List<AiGrowthTrendItemVO> trend;

    private AiGrowthDimensionVO dimension;
}
