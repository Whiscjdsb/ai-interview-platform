package com.example.aiinterview.module.ai.service;

import java.util.List;

import com.example.aiinterview.module.ai.dto.InterviewTemplateCreateRequest;
import com.example.aiinterview.module.ai.enums.EnterpriseInterviewerType;
import com.example.aiinterview.module.ai.vo.EnterpriseFitAnalysisVO;
import com.example.aiinterview.module.ai.vo.InterviewTemplateVO;

public interface EnterpriseInterviewService {

    InterviewTemplateVO createTemplate(Long userId, InterviewTemplateCreateRequest request);

    List<InterviewTemplateVO> listTemplates(Long userId);

    EnterpriseFitAnalysisVO analyzeFit(Long userId, String positionModel, EnterpriseInterviewerType companyType);
}
