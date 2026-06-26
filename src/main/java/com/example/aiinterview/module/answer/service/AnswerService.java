package com.example.aiinterview.module.answer.service;

import com.example.aiinterview.common.PageResult;
import com.example.aiinterview.module.answer.dto.AnswerHistoryQueryRequest;
import com.example.aiinterview.module.answer.dto.AnswerSubmitRequest;
import com.example.aiinterview.module.answer.vo.AnswerHistoryDetailVO;
import com.example.aiinterview.module.answer.vo.AnswerHistoryItemVO;
import com.example.aiinterview.module.answer.vo.AnswerSubmitVO;

public interface AnswerService {

    AnswerSubmitVO submitAnswer(Long userId, AnswerSubmitRequest request);

    PageResult<AnswerHistoryItemVO> pageHistory(Long userId, AnswerHistoryQueryRequest request);

    AnswerHistoryDetailVO getHistoryDetail(Long userId, Long id);
}
