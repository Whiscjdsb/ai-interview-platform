package com.example.aiinterview.module.answer.vo;

import java.time.LocalDateTime;
import java.util.List;

import com.example.aiinterview.module.answer.enums.WrongQuestionStatus;
import com.example.aiinterview.module.question.enums.QuestionDifficulty;
import com.example.aiinterview.module.question.enums.QuestionType;
import com.example.aiinterview.module.question.vo.TagVO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WrongQuestionItemVO {

    private final Long id;
    private final Long questionId;
    private final String title;
    private final String content;
    private final QuestionType questionType;
    private final QuestionDifficulty difficulty;
    private final List<TagVO> tags;
    private final Integer wrongCount;
    private final LocalDateTime lastWrongTime;
    private final WrongQuestionStatus status;
}
