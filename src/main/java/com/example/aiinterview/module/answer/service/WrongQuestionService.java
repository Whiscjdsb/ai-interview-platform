package com.example.aiinterview.module.answer.service;

import com.example.aiinterview.common.PageResult;
import com.example.aiinterview.module.answer.dto.WrongQuestionQueryRequest;
import com.example.aiinterview.module.answer.vo.WrongQuestionItemVO;

public interface WrongQuestionService {

    PageResult<WrongQuestionItemVO> pageWrongQuestions(Long userId, WrongQuestionQueryRequest request);

    void removeWrongQuestion(Long userId, Long questionId);
}
