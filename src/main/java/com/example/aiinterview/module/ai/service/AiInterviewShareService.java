package com.example.aiinterview.module.ai.service;

import com.example.aiinterview.module.ai.vo.AiInterviewShareLinkVO;
import com.example.aiinterview.module.ai.vo.AiInterviewSubmitResponseVO;

public interface AiInterviewShareService {

    AiInterviewShareLinkVO generateShareLink(Long userId, Long interviewId, String baseUrl);

    AiInterviewSubmitResponseVO getSharedInterview(String token);
}
