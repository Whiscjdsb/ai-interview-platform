package com.example.aiinterview.module.ai.vo;

import java.util.List;

import com.example.aiinterview.module.ai.dto.AiInterviewResultDTO;
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

    private String rawResponse;

    private AiInterviewResultDTO structuredResult;

    public AiAnswerReviewVO(
            Integer score,
            String summary,
            List<String> advantages,
            List<String> improvements,
            String suggestedAnswer,
            String modelName) {
        this.score = score;
        this.summary = summary;
        this.advantages = advantages;
        this.improvements = improvements;
        this.suggestedAnswer = suggestedAnswer;
        this.modelName = modelName;
    }
}
