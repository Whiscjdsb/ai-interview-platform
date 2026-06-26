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
public class AiInterviewQuestionResultVO {

    private Long questionId;

    private Integer questionNo;

    private String question;

    private String userAnswer;

    private String answer;

    private Integer score;

    private String comment;

    private String review;

    private List<String> advantages;

    private List<String> improvements;

    private String referenceAnswer;

    private String suggestedAnswer;
}
