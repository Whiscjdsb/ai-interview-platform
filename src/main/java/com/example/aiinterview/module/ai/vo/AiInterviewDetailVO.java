package com.example.aiinterview.module.ai.vo;

import java.time.LocalDateTime;
import java.util.List;

import com.example.aiinterview.module.question.enums.QuestionDifficulty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiInterviewDetailVO {

    private final Long id;

    private final String position;

    private final QuestionDifficulty difficulty;

    private final Integer questionCount;

    private final List<String> focusAreas;

    private final String status;

    private final List<AiInterviewQuestionVO> questions;

    private final String modelName;

    private final LocalDateTime createTime;
}
