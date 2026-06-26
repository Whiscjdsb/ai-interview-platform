package com.example.aiinterview.module.answer.controller;

import com.example.aiinterview.common.PageResult;
import com.example.aiinterview.common.Result;
import com.example.aiinterview.module.answer.dto.WrongQuestionQueryRequest;
import com.example.aiinterview.module.answer.service.WrongQuestionService;
import com.example.aiinterview.module.answer.vo.WrongQuestionItemVO;
import com.example.aiinterview.security.UserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/wrong-questions")
@RequiredArgsConstructor
public class WrongQuestionController {

    private final WrongQuestionService wrongQuestionService;

    @GetMapping
    public Result<PageResult<WrongQuestionItemVO>> pageWrongQuestions(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @ModelAttribute WrongQuestionQueryRequest request) {
        return Result.success(wrongQuestionService.pageWrongQuestions(principal.getId(), request));
    }

    @DeleteMapping("/{questionId}")
    public Result<Void> removeWrongQuestion(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable @Positive(message = "must be positive") Long questionId) {
        wrongQuestionService.removeWrongQuestion(principal.getId(), questionId);
        return Result.success(null);
    }
}
