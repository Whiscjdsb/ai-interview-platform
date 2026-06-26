package com.example.aiinterview.module.ai.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EnterpriseScoreVO {

    private final Integer technicalScore;

    private final Integer architectureScore;

    private final Integer communicationScore;

    private final Integer complexityScore;
}
