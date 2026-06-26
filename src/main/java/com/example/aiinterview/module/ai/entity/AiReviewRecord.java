package com.example.aiinterview.module.ai.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.aiinterview.module.ai.enums.AiRecordType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("ai_review_record")
public class AiReviewRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long questionId;

    private Long answerRecordId;

    private AiRecordType recordType;

    private String inputContent;

    private String resultContent;

    private BigDecimal score;

    private String modelName;

    private String prompt;

    private String reviewResult;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
