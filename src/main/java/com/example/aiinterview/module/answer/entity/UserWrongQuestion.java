package com.example.aiinterview.module.answer.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.aiinterview.module.answer.enums.WrongQuestionStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("user_wrong_question")
public class UserWrongQuestion {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long questionId;

    private Integer wrongCount;

    private LocalDateTime lastWrongTime;

    private WrongQuestionStatus status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;
}
