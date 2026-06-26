package com.example.aiinterview.module.admin.controller;

import java.util.List;

import com.example.aiinterview.common.Result;
import com.example.aiinterview.module.admin.dto.AdminLimitQueryRequest;
import com.example.aiinterview.module.admin.dto.AdminTrendQueryRequest;
import com.example.aiinterview.module.admin.dto.AdminUserRankingQueryRequest;
import com.example.aiinterview.module.admin.service.AdminStatisticsService;
import com.example.aiinterview.module.admin.vo.AdminPopularQuestionVO;
import com.example.aiinterview.module.admin.vo.AdminPopularTagVO;
import com.example.aiinterview.module.admin.vo.AdminStatisticsOverviewVO;
import com.example.aiinterview.module.admin.vo.AdminTrendVO;
import com.example.aiinterview.module.admin.vo.AdminUserRankingVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin/statistics")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminStatisticsController {

    private final AdminStatisticsService adminStatisticsService;

    @GetMapping("/overview")
    public Result<AdminStatisticsOverviewVO> getOverview() {
        return Result.success(adminStatisticsService.getOverview());
    }

    @GetMapping("/trend")
    public Result<List<AdminTrendVO>> getTrend(@Valid @ModelAttribute AdminTrendQueryRequest request) {
        return Result.success(adminStatisticsService.getTrend(request));
    }

    @GetMapping("/popular-questions")
    public Result<List<AdminPopularQuestionVO>> getPopularQuestions(@Valid @ModelAttribute AdminLimitQueryRequest request) {
        return Result.success(adminStatisticsService.getPopularQuestions(request));
    }

    @GetMapping("/popular-tags")
    public Result<List<AdminPopularTagVO>> getPopularTags(@Valid @ModelAttribute AdminLimitQueryRequest request) {
        return Result.success(adminStatisticsService.getPopularTags(request));
    }

    @GetMapping("/user-ranking")
    public Result<List<AdminUserRankingVO>> getUserRanking(@Valid @ModelAttribute AdminUserRankingQueryRequest request) {
        return Result.success(adminStatisticsService.getUserRanking(request));
    }
}
