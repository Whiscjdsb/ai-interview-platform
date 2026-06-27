package com.example.aiinterview.module.ai.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiScoreContext {

    private Integer deepseekScore;

    private String userAnswer;

    private String questionType;

    private boolean fallback;

    @Builder.Default
    private List<String> referencePoints = List.of();
}
