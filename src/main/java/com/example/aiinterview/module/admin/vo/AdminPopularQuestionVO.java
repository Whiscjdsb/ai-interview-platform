package com.example.aiinterview.module.admin.vo;

import java.math.BigDecimal;
import java.util.List;

import com.example.aiinterview.module.question.enums.QuestionDifficulty;
import com.example.aiinterview.module.question.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminPopularQuestionVO {

    private Long questionId;
    private String title;
    private QuestionType questionType;
    private QuestionDifficulty difficulty;
    private long answerCount;
    private BigDecimal accuracyRate;
    private List<AdminTagBriefVO> tags;
}
