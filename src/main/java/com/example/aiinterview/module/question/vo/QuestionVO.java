package com.example.aiinterview.module.question.vo;

import java.time.LocalDateTime;
import java.util.List;

import com.example.aiinterview.module.question.enums.QuestionDifficulty;
import com.example.aiinterview.module.question.enums.QuestionType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionVO {

    private final Long id;
    private final String title;
    private final String content;
    private final QuestionType questionType;
    private final QuestionDifficulty difficulty;
    private final String correctAnswer;
    private final String analysis;
    private final Long creatorId;
    private final LocalDateTime createTime;
    private final LocalDateTime updateTime;
    private final List<TagVO> tags;
}
