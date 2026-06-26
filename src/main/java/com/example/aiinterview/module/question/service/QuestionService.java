package com.example.aiinterview.module.question.service;

import com.example.aiinterview.common.PageResult;
import com.example.aiinterview.module.question.dto.QuestionQueryRequest;
import com.example.aiinterview.module.question.dto.QuestionSaveRequest;
import com.example.aiinterview.module.question.vo.QuestionVO;

public interface QuestionService {

    PageResult<QuestionVO> pageQuestions(QuestionQueryRequest request);

    QuestionVO getPublicDetail(Long id);

    QuestionVO getAdminDetail(Long id);

    QuestionVO createQuestion(QuestionSaveRequest request, Long creatorId);

    QuestionVO updateQuestion(Long id, QuestionSaveRequest request);

    void deleteQuestion(Long id);
}
