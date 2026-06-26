package com.example.aiinterview.module.ai.service;

import java.util.List;

import com.example.aiinterview.module.ai.dto.GenerateInterviewRequest;
import com.example.aiinterview.module.ai.vo.AiAnswerReviewVO;
import com.example.aiinterview.module.ai.vo.GenerateInterviewVO;
import com.example.aiinterview.module.ai.vo.WeaknessSummaryVO;
import com.example.aiinterview.module.question.entity.Question;
import com.example.aiinterview.module.question.vo.TagVO;
import com.example.aiinterview.module.statistics.vo.TagAccuracyVO;
import com.example.aiinterview.module.statistics.vo.WrongAnalysisVO;

public interface AiService {

    String provider();

    String modelName();

    AiAnswerReviewVO reviewAnswer(Question question, List<TagVO> tags, String answer);

    GenerateInterviewVO generateInterview(GenerateInterviewRequest request, List<Question> seedQuestions);

    WeaknessSummaryVO summarizeWeakness(List<TagAccuracyVO> tagAccuracy, List<WrongAnalysisVO> wrongAnalysis);
}
