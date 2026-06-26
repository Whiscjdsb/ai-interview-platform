package com.example.aiinterview.module.ai.controller;

import com.example.aiinterview.common.Result;
import com.example.aiinterview.module.ai.service.AiInterviewShareService;
import com.example.aiinterview.module.ai.vo.AiInterviewShareLinkVO;
import com.example.aiinterview.module.ai.vo.AiInterviewSubmitResponseVO;
import com.example.aiinterview.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class AiInterviewShareController {

    private final AiInterviewShareService aiInterviewShareService;

    @PostMapping("/api/ai/interview/{id}/share")
    public Result<AiInterviewShareLinkVO> generateShareLink(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable @Positive(message = "must be positive") Long id,
            HttpServletRequest request) {
        return Result.success(aiInterviewShareService.generateShareLink(
                principal.getId(),
                id,
                request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()));
    }

    @GetMapping("/api/share/{token}")
    public Result<AiInterviewSubmitResponseVO> getSharedInterview(
            @PathVariable @NotBlank(message = "token is required") String token) {
        return Result.success(aiInterviewShareService.getSharedInterview(token));
    }
}
