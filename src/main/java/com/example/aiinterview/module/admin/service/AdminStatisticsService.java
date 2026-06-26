package com.example.aiinterview.module.admin.service;

import java.util.List;

import com.example.aiinterview.module.admin.dto.AdminLimitQueryRequest;
import com.example.aiinterview.module.admin.dto.AdminTrendQueryRequest;
import com.example.aiinterview.module.admin.dto.AdminUserRankingQueryRequest;
import com.example.aiinterview.module.admin.vo.AdminPopularQuestionVO;
import com.example.aiinterview.module.admin.vo.AdminPopularTagVO;
import com.example.aiinterview.module.admin.vo.AdminStatisticsOverviewVO;
import com.example.aiinterview.module.admin.vo.AdminTrendVO;
import com.example.aiinterview.module.admin.vo.AdminUserRankingVO;

public interface AdminStatisticsService {

    AdminStatisticsOverviewVO getOverview();

    List<AdminTrendVO> getTrend(AdminTrendQueryRequest request);

    List<AdminPopularQuestionVO> getPopularQuestions(AdminLimitQueryRequest request);

    List<AdminPopularTagVO> getPopularTags(AdminLimitQueryRequest request);

    List<AdminUserRankingVO> getUserRanking(AdminUserRankingQueryRequest request);
}
