package com.example.aiinterview.module.question.controller;

import com.example.aiinterview.common.PageResult;
import com.example.aiinterview.common.Result;
import com.example.aiinterview.module.question.dto.QuestionQueryRequest;
import com.example.aiinterview.module.question.service.QuestionService;
import com.example.aiinterview.module.question.vo.QuestionVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping
    public Result<PageResult<QuestionVO>> pageQuestions(@Valid @ModelAttribute QuestionQueryRequest request) {
        return Result.success(questionService.pageQuestions(request));
    }

    @GetMapping("/{id}")
    public Result<QuestionVO> getQuestion(@PathVariable Long id) {
        return Result.success(questionService.getPublicDetail(id));
    }
}
