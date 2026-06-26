package com.example.aiinterview.module.ai.vo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EnterpriseFitAnalysisVO {

    private final Integer fitScore;

    private final List<String> gapAreas;

    private final String recommendation;
}
