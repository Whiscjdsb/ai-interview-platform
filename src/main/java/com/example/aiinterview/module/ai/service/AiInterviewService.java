package com.example.aiinterview.module.ai.service;

import com.example.aiinterview.module.ai.dto.AiInterviewSubmitRequest;
import com.example.aiinterview.module.ai.vo.AiInterviewDetailVO;
import com.example.aiinterview.module.ai.vo.AiInterviewSubmitResponseVO;

public interface AiInterviewService {

    AiInterviewDetailVO getInterview(Long userId, Long id);

    AiInterviewSubmitResponseVO submitInterview(Long userId, Long id, AiInterviewSubmitRequest request);
}
