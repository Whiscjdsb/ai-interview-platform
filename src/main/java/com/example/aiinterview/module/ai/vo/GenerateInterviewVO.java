package com.example.aiinterview.module.ai.vo;

import java.util.List;

import com.example.aiinterview.module.question.enums.QuestionDifficulty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GenerateInterviewVO {

    private Long id;

    private String position;

    private QuestionDifficulty difficulty;

    private Integer questionCount;

    private List<String> focusAreas;

    private String status;

    private List<MockInterviewQuestionVO> questions;

    private String modelName;

    public GenerateInterviewVO(
            String position,
            QuestionDifficulty difficulty,
            List<MockInterviewQuestionVO> questions,
            String modelName) {
        this.position = position;
        this.difficulty = difficulty;
        this.questionCount = questions == null ? 0 : questions.size();
        this.focusAreas = List.of();
        this.status = "IN_PROGRESS";
        this.questions = questions;
        this.modelName = modelName;
    }
}
