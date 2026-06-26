package com.example.aiinterview.module.ai.vo;

import java.util.List;

import com.example.aiinterview.module.question.enums.QuestionDifficulty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiInterviewQuestionVO {

    private final Long id;

    private final Integer questionNo;

    private final String content;

    private final String category;

    private final QuestionDifficulty difficulty;

    private final List<String> referencePoints;

    private final String referenceAnswer;
}
