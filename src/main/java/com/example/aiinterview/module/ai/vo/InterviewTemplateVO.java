package com.example.aiinterview.module.ai.vo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.example.aiinterview.module.ai.enums.EnterpriseInterviewerType;
import com.example.aiinterview.module.question.enums.QuestionDifficulty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InterviewTemplateVO {

    private final Long id;

    private final String positionModel;

    private final EnterpriseInterviewerType companyType;

    private final QuestionDifficulty difficulty;

    private final Integer questionCount;

    private final List<String> focusAreas;

    private final Map<String, Integer> scoringWeights;

    private final LocalDateTime createTime;
}
