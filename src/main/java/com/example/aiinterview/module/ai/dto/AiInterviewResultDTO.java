package com.example.aiinterview.module.ai.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiInterviewResultDTO {

    private Integer score;

    private List<String> strengths;

    private List<String> weaknesses;

    private List<String> suggestions;

    private String referenceAnswer;
}
