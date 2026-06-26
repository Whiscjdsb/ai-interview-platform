package com.example.aiinterview.module.ai.vo;

import java.time.LocalDateTime;
import java.util.List;

import com.example.aiinterview.module.question.enums.QuestionDifficulty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiInterviewSubmitResponseVO {

    private Long id;

    private String position;

    private QuestionDifficulty difficulty;

    private Integer totalScore;

    private String level;

    private String summary;

    private List<String> advantages;

    private List<String> disadvantages;

    private List<String> improvements;

    private List<String> suggestions;

    private List<AiInterviewQuestionResultVO> questionResults;

    private String modelName;

    private LocalDateTime createTime;
}
