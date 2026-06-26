package com.example.aiinterview.module.ai.vo;

import java.util.List;

import com.example.aiinterview.module.question.enums.QuestionDifficulty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MockInterviewQuestionVO {

    private Long id;

    private Integer questionNo;

    private String question;

    private String category;

    private QuestionDifficulty difficulty;

    private List<String> referencePoints;

    public MockInterviewQuestionVO(
            Integer questionNo,
            String question,
            String category,
            QuestionDifficulty difficulty,
            List<String> referencePoints) {
        this.id = questionNo == null ? null : questionNo.longValue();
        this.questionNo = questionNo;
        this.question = question;
        this.category = category;
        this.difficulty = difficulty;
        this.referencePoints = referencePoints;
    }
}
