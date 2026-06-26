package com.example.aiinterview.module.statistics.controller;

import java.util.List;

import com.example.aiinterview.common.Result;
import com.example.aiinterview.module.statistics.dto.StatisticsTrendRequest;
import com.example.aiinterview.module.statistics.service.UserStatisticsService;
import com.example.aiinterview.module.statistics.vo.LearningTrendVO;
import com.example.aiinterview.module.statistics.vo.TagAccuracyVO;
import com.example.aiinterview.module.statistics.vo.UserStatisticsOverviewVO;
import com.example.aiinterview.module.statistics.vo.WrongAnalysisVO;
import com.example.aiinterview.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/statistics/user")
@RequiredArgsConstructor
public class UserStatisticsController {

    private final UserStatisticsService userStatisticsService;

    @GetMapping("/overview")
    public Result<UserStatisticsOverviewVO> getOverview(@AuthenticationPrincipal UserPrincipal principal) {
        return Result.success(userStatisticsService.getOverview(principal.getId()));
    }

    @GetMapping("/trend")
    public Result<List<LearningTrendVO>> getTrend(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @ModelAttribute StatisticsTrendRequest request) {
        return Result.success(userStatisticsService.getTrend(principal.getId(), request));
    }

    @GetMapping("/tag-accuracy")
    public Result<List<TagAccuracyVO>> getTagAccuracy(@AuthenticationPrincipal UserPrincipal principal) {
        return Result.success(userStatisticsService.getTagAccuracy(principal.getId()));
    }

    @GetMapping("/wrong-analysis")
    public Result<List<WrongAnalysisVO>> getWrongAnalysis(@AuthenticationPrincipal UserPrincipal principal) {
        return Result.success(userStatisticsService.getWrongAnalysis(principal.getId()));
    }
}
