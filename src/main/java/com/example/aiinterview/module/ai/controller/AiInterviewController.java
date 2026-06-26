package com.example.aiinterview.module.ai.controller;

import com.example.aiinterview.common.Result;
import com.example.aiinterview.module.ai.dto.AiInterviewSubmitRequest;
import com.example.aiinterview.module.ai.service.AiInterviewService;
import com.example.aiinterview.module.ai.vo.AiInterviewDetailVO;
import com.example.aiinterview.module.ai.vo.AiInterviewSubmitResponseVO;
import com.example.aiinterview.security.UserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/ai/interviews")
@RequiredArgsConstructor
public class AiInterviewController {

    private final AiInterviewService aiInterviewService;

    @GetMapping("/{id}")
    public Result<AiInterviewDetailVO> getInterview(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable @Positive(message = "must be positive") Long id) {
        return Result.success(aiInterviewService.getInterview(principal.getId(), id));
    }

    @PostMapping("/{id}/submit")
    public Result<AiInterviewSubmitResponseVO> submitInterview(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable @Positive(message = "must be positive") Long id,
            @Valid @RequestBody AiInterviewSubmitRequest request) {
        return Result.success(aiInterviewService.submitInterview(principal.getId(), id, request));
    }
}
