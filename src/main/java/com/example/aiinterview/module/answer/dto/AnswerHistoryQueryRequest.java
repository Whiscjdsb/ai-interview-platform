package com.example.aiinterview.module.answer.dto;

import java.time.LocalDateTime;

import com.example.aiinterview.module.question.enums.QuestionType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
public class AnswerHistoryQueryRequest {

    @Min(value = 1, message = "must be at least 1")
    private Long page = 1L;

    @Min(value = 1, message = "must be at least 1")
    @Max(value = 100, message = "must be at most 100")
    private Long size = 10L;

    private QuestionType questionType;

    private Boolean isCorrect;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endTime;
}
