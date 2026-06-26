package com.example.aiinterview.module.ai.controller;

import com.example.aiinterview.common.PageResult;
import com.example.aiinterview.common.Result;
import com.example.aiinterview.module.ai.dto.AiHistoryQueryRequest;
import com.example.aiinterview.module.ai.dto.AiInterviewFollowUpRequest;
import com.example.aiinterview.module.ai.dto.AiReviewAnswerRequest;
import com.example.aiinterview.module.ai.dto.GenerateInterviewRequest;
import com.example.aiinterview.module.ai.service.AiAssistantService;
import com.example.aiinterview.module.ai.service.AiInterviewService;
import com.example.aiinterview.module.ai.service.AiUserGrowthService;
import com.example.aiinterview.module.ai.vo.AiAnswerReviewVO;
import com.example.aiinterview.module.ai.vo.AiInterviewFollowUpVO;
import com.example.aiinterview.module.ai.vo.AiUserGrowthVO;
import com.example.aiinterview.module.ai.vo.AiHistoryDetailVO;
import com.example.aiinterview.module.ai.vo.AiHistoryItemVO;
import com.example.aiinterview.module.ai.vo.GenerateInterviewVO;
import com.example.aiinterview.module.ai.vo.WeaknessSummaryVO;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;
    private final AiInterviewService aiInterviewService;
    private final AiUserGrowthService aiUserGrowthService;

    @PostMapping("/review-answer")
    public Result<AiAnswerReviewVO> reviewAnswer(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AiReviewAnswerRequest request) {
        return Result.success(aiAssistantService.reviewAnswer(principal.getId(), request));
    }

    @PostMapping("/generate-interview")
    public Result<GenerateInterviewVO> generateInterview(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody GenerateInterviewRequest request) {
        return Result.success(aiAssistantService.generateInterview(principal.getId(), request));
    }

    @PostMapping("/weakness-summary")
    public Result<WeaknessSummaryVO> summarizeWeakness(@AuthenticationPrincipal UserPrincipal principal) {
        return Result.success(aiAssistantService.summarizeWeakness(principal.getId()));
    }

    @PostMapping("/interview/next-question")
    public Result<AiInterviewFollowUpVO> nextQuestion(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AiInterviewFollowUpRequest request) {
        return Result.success(aiInterviewService.nextQuestion(request));
    }

    @GetMapping("/user/growth")
    public Result<AiUserGrowthVO> getUserGrowth(@AuthenticationPrincipal UserPrincipal principal) {
        return Result.success(aiUserGrowthService.getUserGrowth(principal.getId()));
    }

    @GetMapping("/history")
    public Result<PageResult<AiHistoryItemVO>> pageHistory(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @ModelAttribute AiHistoryQueryRequest request) {
        return Result.success(aiAssistantService.pageHistory(principal.getId(), request));
    }

    @GetMapping("/history/{id}")
    public Result<AiHistoryDetailVO> getHistoryDetail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable @Positive(message = "must be positive") Long id) {
        return Result.success(aiAssistantService.getHistoryDetail(principal.getId(), id));
    }

    @DeleteMapping("/history/{id}")
    public Result<Void> deleteHistory(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable @Positive(message = "must be positive") Long id) {
        aiAssistantService.deleteHistory(principal.getId(), id);
        return Result.success(null);
    }
}
