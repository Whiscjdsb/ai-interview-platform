package com.example.aiinterview.module.ai.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.example.aiinterview.module.ai.dto.AiScoreContext;
import org.junit.jupiter.api.Test;

class ScoreEngineTest {

    private final ScoreEngine scoreEngine = new ScoreEngine();

    @Test
    void invalidAnswerScoresAtMost15() {
        int score = scoreEngine.calculateScore(AiScoreContext.builder()
                .deepseekScore(90)
                .userAnswer("\u6211\u4e0d\u4f1a")
                .questionType("SHORT_ANSWER")
                .fallback(false)
                .referencePoints(List.of("Spring Boot", "AutoConfiguration"))
                .build());

        assertThat(score).isLessThanOrEqualTo(15);
    }

    @Test
    void blankAnswerScoresAtMost5() {
        int score = scoreEngine.calculateScore(AiScoreContext.builder()
                .deepseekScore(90)
                .userAnswer("   ")
                .questionType("SHORT_ANSWER")
                .fallback(false)
                .referencePoints(List.of("Spring Boot"))
                .build());

        assertThat(score).isLessThanOrEqualTo(5);
    }

    @Test
    void genericAnswerScoresAtMost50() {
        int score = scoreEngine.calculateScore(AiScoreContext.builder()
                .deepseekScore(90)
                .userAnswer("\u8fd9\u4e2a\u95ee\u9898\u9700\u8981\u7ed3\u5408\u5b9e\u9645\u60c5\u51b5\u5206\u6790\uff0c\u4e3b\u8981\u662f\u63d0\u5347\u7cfb\u7edf\u6027\u80fd\u548c\u7a33\u5b9a\u6027\uff0c\u5177\u4f53\u8981\u770b\u9879\u76ee\u60c5\u51b5\u3002")
                .questionType("SHORT_ANSWER")
                .fallback(false)
                .referencePoints(List.of("Spring Boot", "Redis"))
                .build());

        assertThat(score).isLessThanOrEqualTo(50);
    }

    @Test
    void technicalAnswerCanScoreAtLeast70() {
        int score = scoreEngine.calculateScore(AiScoreContext.builder()
                .deepseekScore(85)
                .userAnswer("Spring Boot auto configuration relies on conditional annotations, AutoConfiguration classes, classpath matching, and Bean conditions because it only loads components when required dependencies and properties exist.")
                .questionType("SHORT_ANSWER")
                .fallback(false)
                .referencePoints(List.of("Spring Boot", "AutoConfiguration", "conditional annotations", "Bean"))
                .build());

        assertThat(score).isGreaterThanOrEqualTo(70);
    }
}
