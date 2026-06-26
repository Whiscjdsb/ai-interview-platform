package com.example.aiinterview.module.admin.service;

import java.util.List;

import com.example.aiinterview.module.admin.vo.AdminPopularQuestionVO;
import com.example.aiinterview.module.admin.vo.AdminPopularTagVO;
import com.example.aiinterview.module.admin.vo.AdminStatisticsOverviewVO;
import com.example.aiinterview.module.admin.vo.AdminTrendVO;

public interface AdminStatisticsCacheService {

    AdminStatisticsOverviewVO getOverview();

    void setOverview(AdminStatisticsOverviewVO overview);

    List<AdminTrendVO> getTrend(int days);

    void setTrend(int days, List<AdminTrendVO> trend);

    List<AdminPopularQuestionVO> getPopularQuestions(int limit);

    void setPopularQuestions(int limit, List<AdminPopularQuestionVO> questions);

    List<AdminPopularTagVO> getPopularTags(int limit);

    void setPopularTags(int limit, List<AdminPopularTagVO> tags);

    void evictAll();
}
