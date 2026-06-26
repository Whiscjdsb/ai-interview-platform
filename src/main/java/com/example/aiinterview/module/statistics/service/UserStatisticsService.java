package com.example.aiinterview.module.statistics.service;

import java.util.List;

import com.example.aiinterview.module.statistics.dto.StatisticsTrendRequest;
import com.example.aiinterview.module.statistics.vo.LearningTrendVO;
import com.example.aiinterview.module.statistics.vo.TagAccuracyVO;
import com.example.aiinterview.module.statistics.vo.UserStatisticsOverviewVO;
import com.example.aiinterview.module.statistics.vo.WrongAnalysisVO;

public interface UserStatisticsService {

    UserStatisticsOverviewVO getOverview(Long userId);

    List<LearningTrendVO> getTrend(Long userId, StatisticsTrendRequest request);

    List<TagAccuracyVO> getTagAccuracy(Long userId);

    List<WrongAnalysisVO> getWrongAnalysis(Long userId);
}
