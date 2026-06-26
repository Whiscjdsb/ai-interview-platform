package com.example.aiinterview.module.ai.service;

import com.example.aiinterview.common.PageResult;
import com.example.aiinterview.module.ai.dto.AiHistoryQueryRequest;
import com.example.aiinterview.module.ai.dto.AiReviewAnswerRequest;
import com.example.aiinterview.module.ai.dto.GenerateInterviewRequest;
import com.example.aiinterview.module.ai.vo.AiAnswerReviewVO;
import com.example.aiinterview.module.ai.vo.AiHistoryDetailVO;
import com.example.aiinterview.module.ai.vo.AiHistoryItemVO;
import com.example.aiinterview.module.ai.vo.GenerateInterviewVO;
import com.example.aiinterview.module.ai.vo.WeaknessSummaryVO;

public interface AiAssistantService {

    AiAnswerReviewVO reviewAnswer(Long userId, AiReviewAnswerRequest request);

    GenerateInterviewVO generateInterview(Long userId, GenerateInterviewRequest request);

    WeaknessSummaryVO summarizeWeakness(Long userId);

    PageResult<AiHistoryItemVO> pageHistory(Long userId, AiHistoryQueryRequest request);

    AiHistoryDetailVO getHistoryDetail(Long userId, Long id);

    void deleteHistory(Long userId, Long id);
}
