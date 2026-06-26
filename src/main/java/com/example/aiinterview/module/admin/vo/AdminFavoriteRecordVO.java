package com.example.aiinterview.module.admin.vo;

import java.time.LocalDateTime;

import com.example.aiinterview.module.question.enums.QuestionDifficulty;
import com.example.aiinterview.module.question.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminFavoriteRecordVO {

    private final Long id;
    private final Long userId;
    private final String username;
    private final String nickname;
    private final Long questionId;
    private final String questionTitle;
    private final QuestionType questionType;
    private final QuestionDifficulty difficulty;
    private final LocalDateTime createTime;
}
