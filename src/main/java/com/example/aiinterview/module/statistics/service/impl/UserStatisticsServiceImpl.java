package com.example.aiinterview.module.statistics.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aiinterview.module.answer.entity.UserFavorite;
import com.example.aiinterview.module.answer.entity.UserWrongQuestion;
import com.example.aiinterview.module.answer.enums.WrongQuestionStatus;
import com.example.aiinterview.module.answer.mapper.UserFavoriteMapper;
import com.example.aiinterview.module.answer.mapper.UserWrongQuestionMapper;
import com.example.aiinterview.module.statistics.dto.StatisticsTrendRequest;
import com.example.aiinterview.module.statistics.dto.TagAccuracyRow;
import com.example.aiinterview.module.statistics.dto.WrongAnalysisRow;
import com.example.aiinterview.module.statistics.entity.DailyLearningRecord;
import com.example.aiinterview.module.statistics.mapper.DailyLearningRecordMapper;
import com.example.aiinterview.module.statistics.mapper.StatisticsMapper;
import com.example.aiinterview.module.statistics.service.StatisticsCacheService;
import com.example.aiinterview.module.statistics.service.UserStatisticsService;
import com.example.aiinterview.module.statistics.vo.LearningTrendVO;
import com.example.aiinterview.module.statistics.vo.TagAccuracyVO;
import com.example.aiinterview.module.statistics.vo.UserStatisticsOverviewVO;
import com.example.aiinterview.module.statistics.vo.WrongAnalysisVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserStatisticsServiceImpl implements UserStatisticsService {

    private final DailyLearningRecordMapper dailyLearningRecordMapper;
    private final UserFavoriteMapper userFavoriteMapper;
    private final UserWrongQuestionMapper userWrongQuestionMapper;
    private final StatisticsMapper statisticsMapper;
    private final StatisticsCacheService statisticsCacheService;

    @Override
    public UserStatisticsOverviewVO getOverview(Long userId) {
        UserStatisticsOverviewVO cached = statisticsCacheService.getOverview(userId);
        if (cached != null) {
            return cached;
        }

        List<DailyLearningRecord> dailyRecords = dailyLearningRecordMapper.selectList(new LambdaQueryWrapper<DailyLearningRecord>()
                .eq(DailyLearningRecord::getUserId, userId)
                .eq(DailyLearningRecord::getDeleted, 0));
        long totalAnswerCount = dailyRecords.stream().mapToLong(record -> nullToZero(record.getAnswerCount())).sum();
        long correctAnswerCount = dailyRecords.stream().mapToLong(record -> nullToZero(record.getCorrectCount())).sum();
        long totalStudyDuration = dailyRecords.stream().mapToLong(record -> nullToZero(record.getStudyDuration())).sum();

        long favoriteCount = userFavoriteMapper.selectCount(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getDeleted, 0));
        long wrongQuestionCount = userWrongQuestionMapper.selectCount(new LambdaQueryWrapper<UserWrongQuestion>()
                .eq(UserWrongQuestion::getUserId, userId)
                .eq(UserWrongQuestion::getDeleted, 0));
        long activeWrongQuestionCount = userWrongQuestionMapper.selectCount(new LambdaQueryWrapper<UserWrongQuestion>()
                .eq(UserWrongQuestion::getUserId, userId)
                .eq(UserWrongQuestion::getDeleted, 0)
                .eq(UserWrongQuestion::getStatus, WrongQuestionStatus.ACTIVE));
        long resolvedWrongQuestionCount = userWrongQuestionMapper.selectCount(new LambdaQueryWrapper<UserWrongQuestion>()
                .eq(UserWrongQuestion::getUserId, userId)
                .eq(UserWrongQuestion::getDeleted, 0)
                .eq(UserWrongQuestion::getStatus, WrongQuestionStatus.RESOLVED));

        LocalDate today = LocalDate.now();
        DailyLearningRecord todayRecord = dailyLearningRecordMapper.selectOne(new LambdaQueryWrapper<DailyLearningRecord>()
                .eq(DailyLearningRecord::getUserId, userId)
                .eq(DailyLearningRecord::getRecordDate, today)
                .eq(DailyLearningRecord::getDeleted, 0)
                .last("LIMIT 1"));

        UserStatisticsOverviewVO overview = new UserStatisticsOverviewVO(
                totalAnswerCount,
                correctAnswerCount,
                accuracyRate(correctAnswerCount, totalAnswerCount),
                totalStudyDuration,
                favoriteCount,
                wrongQuestionCount,
                activeWrongQuestionCount,
                resolvedWrongQuestionCount,
                continuousLearningDays(userId, today),
                todayRecord == null ? 0 : nullToZero(todayRecord.getAnswerCount()),
                todayRecord == null ? 0 : nullToZero(todayRecord.getStudyDuration()));
        statisticsCacheService.setOverview(userId, overview);
        return overview;
    }

    @Override
    public List<LearningTrendVO> getTrend(Long userId, StatisticsTrendRequest request) {
        int days = request.getDays() == null ? 7 : request.getDays();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1L);
        List<DailyLearningRecord> records = dailyLearningRecordMapper.selectList(new LambdaQueryWrapper<DailyLearningRecord>()
                .eq(DailyLearningRecord::getUserId, userId)
                .ge(DailyLearningRecord::getRecordDate, startDate)
                .le(DailyLearningRecord::getRecordDate, endDate)
                .eq(DailyLearningRecord::getDeleted, 0));
        Map<LocalDate, DailyLearningRecord> recordMap = records.stream()
                .collect(Collectors.toMap(DailyLearningRecord::getRecordDate, Function.identity(), (left, right) -> left));

        List<LearningTrendVO> trend = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            DailyLearningRecord record = recordMap.get(date);
            long answerCount = record == null ? 0 : nullToZero(record.getAnswerCount());
            long correctCount = record == null ? 0 : nullToZero(record.getCorrectCount());
            long studyDuration = record == null ? 0 : nullToZero(record.getStudyDuration());
            trend.add(new LearningTrendVO(date, answerCount, correctCount, accuracyRate(correctCount, answerCount), studyDuration));
        }
        return trend;
    }

    @Override
    public List<TagAccuracyVO> getTagAccuracy(Long userId) {
        return statisticsMapper.selectTopTagAccuracy(userId).stream()
                .map(row -> new TagAccuracyVO(
                        row.getTagId(),
                        row.getTagName(),
                        nullToZero(row.getAnswerCount()),
                        nullToZero(row.getCorrectCount()),
                        accuracyRate(nullToZero(row.getCorrectCount()), nullToZero(row.getAnswerCount()))))
                .toList();
    }

    @Override
    public List<WrongAnalysisVO> getWrongAnalysis(Long userId) {
        return statisticsMapper.selectWrongAnalysis(userId).stream()
                .map(row -> new WrongAnalysisVO(
                        row.getTagId(),
                        row.getTagName(),
                        nullToZero(row.getWrongQuestionCount()),
                        nullToZero(row.getTotalWrongCount())))
                .toList();
    }

    private int continuousLearningDays(Long userId, LocalDate today) {
        List<LocalDate> dates = statisticsMapper.selectLearningDatesBeforeToday(userId, today);
        int days = 0;
        LocalDate cursor = today;
        for (LocalDate date : dates) {
            if (date.equals(cursor)) {
                days++;
                cursor = cursor.minusDays(1);
                continue;
            }
            if (date.isBefore(cursor)) {
                break;
            }
        }
        return days;
    }

    private BigDecimal accuracyRate(long correctCount, long answerCount) {
        if (answerCount <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(correctCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(answerCount), 2, RoundingMode.HALF_UP);
    }

    private long nullToZero(Number value) {
        return value == null ? 0 : value.longValue();
    }
}
