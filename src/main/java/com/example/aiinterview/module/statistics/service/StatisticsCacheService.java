package com.example.aiinterview.module.statistics.service;

import com.example.aiinterview.module.statistics.vo.UserStatisticsOverviewVO;

public interface StatisticsCacheService {

    UserStatisticsOverviewVO getOverview(Long userId);

    void setOverview(Long userId, UserStatisticsOverviewVO overview);

    void evictOverview(Long userId);
}
