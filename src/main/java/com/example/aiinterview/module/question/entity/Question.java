package com.example.aiinterview.module.question.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.aiinterview.module.question.enums.QuestionDifficulty;
import com.example.aiinterview.module.question.enums.QuestionType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("question")
public class Question {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    private String correctAnswer;

    private String analysis;

    private QuestionDifficulty difficulty;

    private QuestionType questionType;

    private Long creatorId;

    private String source;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
