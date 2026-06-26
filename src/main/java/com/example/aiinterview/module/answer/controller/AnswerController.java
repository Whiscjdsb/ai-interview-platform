package com.example.aiinterview.module.answer.controller;

import com.example.aiinterview.common.PageResult;
import com.example.aiinterview.common.Result;
import com.example.aiinterview.module.answer.dto.AnswerHistoryQueryRequest;
import com.example.aiinterview.module.answer.dto.AnswerSubmitRequest;
import com.example.aiinterview.module.answer.service.AnswerService;
import com.example.aiinterview.module.answer.vo.AnswerHistoryDetailVO;
import com.example.aiinterview.module.answer.vo.AnswerHistoryItemVO;
import com.example.aiinterview.module.answer.vo.AnswerSubmitVO;
import com.example.aiinterview.security.UserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/answers")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;

    @PostMapping("/submit")
    public Result<AnswerSubmitVO> submitAnswer(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AnswerSubmitRequest request) {
        return Result.success(answerService.submitAnswer(principal.getId(), request));
    }

    @GetMapping("/history")
    public Result<PageResult<AnswerHistoryItemVO>> pageHistory(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @ModelAttribute AnswerHistoryQueryRequest request) {
        return Result.success(answerService.pageHistory(principal.getId(), request));
    }

    @GetMapping("/history/{id}")
    public Result<AnswerHistoryDetailVO> getHistoryDetail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable @Positive(message = "must be positive") Long id) {
        return Result.success(answerService.getHistoryDetail(principal.getId(), id));
    }
}
