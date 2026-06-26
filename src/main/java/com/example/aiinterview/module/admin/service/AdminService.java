package com.example.aiinterview.module.admin.service;

import com.example.aiinterview.common.PageResult;
import com.example.aiinterview.module.admin.dto.AdminRecordQueryRequest;
import com.example.aiinterview.module.admin.vo.AdminAiInterviewDetailVO;
import com.example.aiinterview.module.admin.vo.AdminAiInterviewRecordVO;
import com.example.aiinterview.module.admin.vo.AdminAnswerRecordVO;
import com.example.aiinterview.module.admin.vo.AdminDashboardVO;
import com.example.aiinterview.module.admin.vo.AdminFavoriteRecordVO;
import com.example.aiinterview.module.admin.vo.AdminWrongQuestionRecordVO;

public interface AdminService {

    AdminDashboardVO getDashboard();

    PageResult<AdminFavoriteRecordVO> pageFavorites(AdminRecordQueryRequest request);

    PageResult<AdminWrongQuestionRecordVO> pageWrongQuestions(AdminRecordQueryRequest request);

    PageResult<AdminAnswerRecordVO> pageAnswers(AdminRecordQueryRequest request);

    PageResult<AdminAiInterviewRecordVO> pageAiInterviews(AdminRecordQueryRequest request);

    AdminAiInterviewDetailVO getAiInterviewDetail(Long id);
}
