package com.example.aiinterview.module.statistics.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("daily_learning_record")
public class DailyLearningRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private LocalDate recordDate;

    private Integer answerCount;

    private Integer correctCount;

    private Integer studyDuration;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;
}
