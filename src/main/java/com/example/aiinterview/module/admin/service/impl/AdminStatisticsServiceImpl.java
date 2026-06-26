package com.example.aiinterview.module.admin.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.example.aiinterview.module.admin.dto.AdminDailyMetricRow;
import com.example.aiinterview.module.admin.dto.AdminLimitQueryRequest;
import com.example.aiinterview.module.admin.dto.AdminOverviewRow;
import com.example.aiinterview.module.admin.dto.AdminPopularQuestionRow;
import com.example.aiinterview.module.admin.dto.AdminPopularTagRow;
import com.example.aiinterview.module.admin.dto.AdminTrendQueryRequest;
import com.example.aiinterview.module.admin.dto.AdminUserRankingQueryRequest;
import com.example.aiinterview.module.admin.dto.AdminUserRankingRow;
import com.example.aiinterview.module.admin.mapper.AdminStatisticsMapper;
import com.example.aiinterview.module.admin.service.AdminStatisticsCacheService;
import com.example.aiinterview.module.admin.service.AdminStatisticsService;
import com.example.aiinterview.module.admin.vo.AdminPopularQuestionVO;
import com.example.aiinterview.module.admin.vo.AdminPopularTagVO;
import com.example.aiinterview.module.admin.vo.AdminStatisticsOverviewVO;
import com.example.aiinterview.module.admin.vo.AdminTagBriefVO;
import com.example.aiinterview.module.admin.vo.AdminTrendVO;
import com.example.aiinterview.module.admin.vo.AdminUserRankingVO;
import com.example.aiinterview.module.question.dto.QuestionTagPair;
import com.example.aiinterview.module.question.enums.QuestionDifficulty;
import com.example.aiinterview.module.question.enums.QuestionType;
import com.example.aiinterview.module.question.mapper.QuestionTagRelationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
public class AdminStatisticsServiceImpl implements AdminStatisticsService {

    private final AdminStatisticsMapper adminStatisticsMapper;
    private final QuestionTagRelationMapper questionTagRelationMapper;
    private final AdminStatisticsCacheService adminStatisticsCacheService;

    @Override
    public AdminStatisticsOverviewVO getOverview() {
        AdminStatisticsOverviewVO cached = adminStatisticsCacheService.getOverview();
        if (cached != null) {
            return cached;
        }

        LocalDate today = LocalDate.now();
        AdminOverviewRow row = adminStatisticsMapper.selectOverview(
                today.atStartOfDay(),
                today.plusDays(1).atStartOfDay(),
                today.minusDays(6).atStartOfDay());
        AdminStatisticsOverviewVO overview = new AdminStatisticsOverviewVO(
                nullToZero(row.getTotalUserCount()),
                nullToZero(row.getTodayNewUserCount()),
                nullToZero(row.getTotalQuestionCount()),
                nullToZero(row.getTodayAnswerCount()),
                nullToZero(row.getTotalAnswerCount()),
                scale(row.getAverageAccuracyRate()),
                nullToZero(row.getActiveUserCount()),
                nullToZero(row.getTotalAiReviewCount()),
                nullToZero(row.getTodayAiReviewCount()));
        adminStatisticsCacheService.setOverview(overview);
        return overview;
    }

    @Override
    public List<AdminTrendVO> getTrend(AdminTrendQueryRequest request) {
        int days = request.getDays() == null ? 7 : request.getDays();
        List<AdminTrendVO> cached = adminStatisticsCacheService.getTrend(days);
        if (cached != null) {
            return cached;
        }

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1L);
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endExclusive = endDate.plusDays(1).atStartOfDay();
        Map<LocalDate, AdminDailyMetricRow> newUsers = rowsByDate(adminStatisticsMapper.selectNewUsersByDate(startTime, endExclusive));
        Map<LocalDate, AdminDailyMetricRow> answers = rowsByDate(adminStatisticsMapper.selectAnswersByDate(startTime, endExclusive));
        Map<LocalDate, AdminDailyMetricRow> aiReviews = rowsByDate(adminStatisticsMapper.selectAiReviewsByDate(startTime, endExclusive));

        List<AdminTrendVO> trend = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            AdminDailyMetricRow answerRow = answers.get(date);
            trend.add(new AdminTrendVO(
                    date,
                    nullToZero(newUsers.get(date) == null ? null : newUsers.get(date).getCount()),
                    nullToZero(answerRow == null ? null : answerRow.getCount()),
                    nullToZero(answerRow == null ? null : answerRow.getActiveUserCount()),
                    nullToZero(aiReviews.get(date) == null ? null : aiReviews.get(date).getCount())));
        }
        adminStatisticsCacheService.setTrend(days, trend);
        return trend;
    }

    @Override
    public List<AdminPopularQuestionVO> getPopularQuestions(AdminLimitQueryRequest request) {
        int limit = request.getLimit() == null ? 10 : request.getLimit();
        List<AdminPopularQuestionVO> cached = adminStatisticsCacheService.getPopularQuestions(limit);
        if (cached != null) {
            return cached;
        }

        List<AdminPopularQuestionRow> rows = adminStatisticsMapper.selectPopularQuestions(limit);
        Map<Long, List<AdminTagBriefVO>> tagsByQuestionId = tagsByQuestionId(rows.stream()
                .map(AdminPopularQuestionRow::getQuestionId)
                .toList());
        List<AdminPopularQuestionVO> result = rows.stream()
                .map(row -> new AdminPopularQuestionVO(
                        row.getQuestionId(),
                        row.getTitle(),
                        QuestionType.valueOf(row.getQuestionType()),
                        QuestionDifficulty.valueOf(row.getDifficulty()),
                        nullToZero(row.getAnswerCount()),
                        scale(row.getAccuracyRate()),
                        tagsByQuestionId.getOrDefault(row.getQuestionId(), List.of())))
                .toList();
        adminStatisticsCacheService.setPopularQuestions(limit, result);
        return result;
    }

    @Override
    public List<AdminPopularTagVO> getPopularTags(AdminLimitQueryRequest request) {
        int limit = request.getLimit() == null ? 10 : request.getLimit();
        List<AdminPopularTagVO> cached = adminStatisticsCacheService.getPopularTags(limit);
        if (cached != null) {
            return cached;
        }

        List<AdminPopularTagVO> result = adminStatisticsMapper.selectPopularTags(limit).stream()
                .map(this::toPopularTag)
                .toList();
        adminStatisticsCacheService.setPopularTags(limit, result);
        return result;
    }

    @Override
    public List<AdminUserRankingVO> getUserRanking(AdminUserRankingQueryRequest request) {
        int days = request.getDays() == null ? 7 : request.getDays();
        int limit = request.getLimit() == null ? 10 : request.getLimit();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1L);
        return adminStatisticsMapper.selectUserRanking(startDate, endDate, request.getSortBy(), limit).stream()
                .map(this::toRanking)
                .toList();
    }

    private Map<LocalDate, AdminDailyMetricRow> rowsByDate(List<AdminDailyMetricRow> rows) {
        return rows.stream()
                .collect(Collectors.toMap(AdminDailyMetricRow::getMetricDate, Function.identity(), (left, right) -> left));
    }

    private Map<Long, List<AdminTagBriefVO>> tagsByQuestionId(List<Long> questionIds) {
        if (CollectionUtils.isEmpty(questionIds)) {
            return Map.of();
        }
        List<QuestionTagPair> pairs = questionTagRelationMapper.selectTagPairsByQuestionIds(questionIds);
        return pairs.stream().collect(Collectors.groupingBy(
                QuestionTagPair::getQuestionId,
                Collectors.mapping(
                        pair -> new AdminTagBriefVO(pair.getTagId(), pair.getTagName(), pair.getDescription()),
                        Collectors.toList())));
    }

    private AdminPopularTagVO toPopularTag(AdminPopularTagRow row) {
        return new AdminPopularTagVO(
                row.getTagId(),
                row.getTagName(),
                nullToZero(row.getAnswerCount()),
                nullToZero(row.getAnswerUserCount()),
                scale(row.getAverageAccuracyRate()));
    }

    private AdminUserRankingVO toRanking(AdminUserRankingRow row) {
        return new AdminUserRankingVO(
                row.getUserId(),
                row.getUsername(),
                row.getNickname(),
                nullToZero(row.getAnswerCount()),
                nullToZero(row.getCorrectCount()),
                scale(row.getAccuracyRate()),
                nullToZero(row.getStudyDuration()));
    }

    private BigDecimal scale(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    private long nullToZero(Number value) {
        return value == null ? 0 : value.longValue();
    }
}
