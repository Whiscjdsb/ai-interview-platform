package com.example.aiinterview.module.admin.controller;

import com.example.aiinterview.common.PageResult;
import com.example.aiinterview.common.Result;
import com.example.aiinterview.module.admin.dto.AdminRecordQueryRequest;
import com.example.aiinterview.module.admin.service.AdminService;
import com.example.aiinterview.module.admin.vo.AdminAiInterviewDetailVO;
import com.example.aiinterview.module.admin.vo.AdminAiInterviewRecordVO;
import com.example.aiinterview.module.admin.vo.AdminAnswerRecordVO;
import com.example.aiinterview.module.admin.vo.AdminDashboardVO;
import com.example.aiinterview.module.admin.vo.AdminFavoriteRecordVO;
import com.example.aiinterview.module.admin.vo.AdminWrongQuestionRecordVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public Result<AdminDashboardVO> getDashboard() {
        return Result.success(adminService.getDashboard());
    }

    @GetMapping("/favorites")
    public Result<PageResult<AdminFavoriteRecordVO>> pageFavorites(
            @Valid @ModelAttribute AdminRecordQueryRequest request) {
        return Result.success(adminService.pageFavorites(request));
    }

    @GetMapping("/wrong-questions")
    public Result<PageResult<AdminWrongQuestionRecordVO>> pageWrongQuestions(
            @Valid @ModelAttribute AdminRecordQueryRequest request) {
        return Result.success(adminService.pageWrongQuestions(request));
    }

    @GetMapping("/answers")
    public Result<PageResult<AdminAnswerRecordVO>> pageAnswers(
            @Valid @ModelAttribute AdminRecordQueryRequest request) {
        return Result.success(adminService.pageAnswers(request));
    }

    @GetMapping("/ai-interviews")
    public Result<PageResult<AdminAiInterviewRecordVO>> pageAiInterviews(
            @Valid @ModelAttribute AdminRecordQueryRequest request) {
        return Result.success(adminService.pageAiInterviews(request));
    }

    @GetMapping("/ai-interviews/{id}")
    public Result<AdminAiInterviewDetailVO> getAiInterviewDetail(@PathVariable Long id) {
        return Result.success(adminService.getAiInterviewDetail(id));
    }
}
