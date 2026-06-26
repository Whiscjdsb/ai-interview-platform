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
public class AiAnswerReviewVO {

    private Integer score;

    private String summary;

    private List<String> advantages;

    private List<String> improvements;

    private String suggestedAnswer;

    private String modelName;
}
