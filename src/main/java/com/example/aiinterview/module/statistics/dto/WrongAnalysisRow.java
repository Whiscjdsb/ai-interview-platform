package com.example.aiinterview.module.statistics.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WrongAnalysisRow {

    private Long tagId;

    private String tagName;

    private Long wrongQuestionCount;

    private Long totalWrongCount;
}
