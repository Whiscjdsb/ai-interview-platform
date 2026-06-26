package com.example.aiinterview.module.question.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionTagPair {

    private Long questionId;

    private Long tagId;

    private String tagName;

    private String description;
}
