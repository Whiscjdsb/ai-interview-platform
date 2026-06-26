package com.example.aiinterview.module.statistics.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TagAccuracyRow {

    private Long tagId;

    private String tagName;

    private Long answerCount;

    private Long correctCount;
}
