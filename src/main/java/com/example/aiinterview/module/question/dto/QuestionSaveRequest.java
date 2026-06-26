package com.example.aiinterview.module.question.dto;

import java.util.ArrayList;
import java.util.List;

import com.example.aiinterview.module.question.enums.QuestionDifficulty;
import com.example.aiinterview.module.question.enums.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionSaveRequest {

    @NotBlank(message = "cannot be blank")
    @Size(max = 255, message = "length must be at most 255")
    private String title;

    @NotBlank(message = "cannot be blank")
    private String content;

    @NotNull(message = "cannot be null")
    private QuestionType questionType;

    @NotNull(message = "cannot be null")
    private QuestionDifficulty difficulty;

    @NotBlank(message = "cannot be blank")
    private String correctAnswer;

    private String analysis;

    private List<Long> tagIds = new ArrayList<>();
}
