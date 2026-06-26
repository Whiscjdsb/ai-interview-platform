package com.example.aiinterview.module.statistics.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WrongAnalysisVO {

    private final Long tagId;
    private final String tagName;
    private final long wrongQuestionCount;
    private final long totalWrongCount;
}
