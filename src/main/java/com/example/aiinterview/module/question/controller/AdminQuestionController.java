package com.example.aiinterview.module.question.controller;

import com.example.aiinterview.common.PageResult;
import com.example.aiinterview.common.Result;
import com.example.aiinterview.module.question.dto.QuestionQueryRequest;
import com.example.aiinterview.module.question.dto.QuestionSaveRequest;
import com.example.aiinterview.module.question.service.QuestionService;
import com.example.aiinterview.module.question.vo.QuestionVO;
import com.example.aiinterview.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/questions")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminQuestionController {

    private final QuestionService questionService;

    @GetMapping
    public Result<PageResult<QuestionVO>> pageQuestions(@Valid @ModelAttribute QuestionQueryRequest request) {
        return Result.success(questionService.pageQuestions(request));
    }

    @GetMapping("/{id}")
    public Result<QuestionVO> getQuestion(@PathVariable Long id) {
        return Result.success(questionService.getAdminDetail(id));
    }

    @PostMapping
    public Result<QuestionVO> createQuestion(
            @Valid @RequestBody QuestionSaveRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return Result.success(questionService.createQuestion(request, principal.getId()));
    }

    @PutMapping("/{id}")
    public Result<QuestionVO> updateQuestion(@PathVariable Long id, @Valid @RequestBody QuestionSaveRequest request) {
        return Result.success(questionService.updateQuestion(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return Result.success(null);
    }
}
