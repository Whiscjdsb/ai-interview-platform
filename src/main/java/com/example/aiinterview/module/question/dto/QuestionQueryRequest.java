package com.example.aiinterview.module.question.dto;

import com.example.aiinterview.module.question.enums.QuestionDifficulty;
import com.example.aiinterview.module.question.enums.QuestionType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionQueryRequest {

    @Min(value = 1, message = "must be at least 1")
    private Long page = 1L;

    @Min(value = 1, message = "must be at least 1")
    @Max(value = 100, message = "must be at most 100")
    private Long size = 10L;

    private String keyword;

    private QuestionType questionType;

    private QuestionDifficulty difficulty;

    private Long tagId;
}
