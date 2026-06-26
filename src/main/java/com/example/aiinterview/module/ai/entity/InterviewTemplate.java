package com.example.aiinterview.module.ai.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.aiinterview.module.ai.enums.EnterpriseInterviewerType;
import com.example.aiinterview.module.question.enums.QuestionDifficulty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("interview_template")
public class InterviewTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String positionModel;

    private EnterpriseInterviewerType companyType;

    private QuestionDifficulty difficulty;

    private Integer questionCount;

    private String focusAreas;

    private String scoringWeights;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
