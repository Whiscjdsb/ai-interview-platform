package com.example.aiinterview.module.ai.service.impl;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;

import com.example.aiinterview.module.ai.dto.AiScoreContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class ScoreEngine {

    public int calculateScore(AiScoreContext context) {
        String traceId = UUID.randomUUID().toString();
        int inputScore = resolveInputScore(context);
        QualityLimit limit = evaluateQualityLimit(context.getUserAnswer(), context.getReferencePoints());
        int finalScore = Math.max(0, Math.min(inputScore, limit.maxScore()));
        String reason = finalScore < inputScore
                ? "capped by " + limit.reason()
                : (context.isFallback() && context.getDeepseekScore() == null
                        ? "fallback raw score generated and accepted"
                        : "accepted");
        log.info("AI score calculated: traceId={}, questionType={}, isFallback={}, inputScore={}, finalScore={}, correctionReason={}",
                traceId,
                context.getQuestionType(),
                context.isFallback(),
                inputScore,
                finalScore,
                reason);
        return finalScore;
    }

    private int resolveInputScore(AiScoreContext context) {
        if (context.isFallback() && context.getDeepseekScore() == null) {
            return fallbackRawScore(context.getUserAnswer(), context.getReferencePoints());
        }
        Integer score = context.getDeepseekScore();
        if (score == null) {
            return 0;
        }
        return Math.max(0, Math.min(100, score));
    }

    private int fallbackRawScore(String answer, List<String> referencePoints) {
        String normalized = normalize(answer);
        if (!StringUtils.hasText(normalized)) {
            return 0;
        }
        int length = meaningfulLength(answer);
        int technicalHits = technicalHitCount(normalized, referencePoints);
        int lengthScore = Math.min(30, length / 8);
        int technicalScore = Math.min(30, technicalHits * 10);
        int explanationScore = hasExplanationContent(normalized, length) ? 15 : 0;
        int baseScore = technicalHits > 0 ? 45 : 35;
        return Math.max(0, Math.min(100, baseScore + lengthScore + technicalScore + explanationScore));
    }

    private QualityLimit evaluateQualityLimit(String answer, List<String> referencePoints) {
        String normalized = normalize(answer);
        int length = meaningfulLength(answer);
        if (!StringUtils.hasText(normalized)) {
            return new QualityLimit(5, "empty answer");
        }
        if (isClearlyInvalid(normalized, length)) {
            return new QualityLimit(15, "clearly invalid answer");
        }

        int technicalHits = technicalHitCount(normalized, referencePoints);
        boolean hasTechnicalContent = technicalHits > 0;
        if (!hasTechnicalContent) {
            if (length < 10) {
                return new QualityLimit(15, "short answer without technical keyword");
            }
            if (length < 20) {
                return new QualityLimit(25, "brief answer without technical keyword");
            }
            return new QualityLimit(50, "generic answer without key technical points");
        }

        if (length < 10) {
            return new QualityLimit(15, "short answer with insufficient explanation");
        }
        if (length < 20) {
            return new QualityLimit(25, "brief answer with insufficient explanation");
        }
        if (!hasExplanationContent(normalized, length)) {
            return new QualityLimit(40, "technical keywords without explanation");
        }
        if (length < 50 && technicalHits < 2) {
            return new QualityLimit(65, "limited technical coverage");
        }
        return new QualityLimit(100, "sufficient technical coverage");
    }

    private boolean isClearlyInvalid(String normalized, int length) {
        return AiAnswerQualityEvaluator.INVALID_PHRASES.stream().anyMatch(phrase -> {
            String normalizedPhrase = normalize(phrase);
            return normalized.equals(normalizedPhrase)
                    || (length <= 50 && normalized.contains(normalizedPhrase));
        });
    }

    private boolean hasExplanationContent(String normalized, int length) {
        return length >= 30 && AiAnswerQualityEvaluator.EXPLANATION_MARKERS.stream()
                .map(this::normalize)
                .anyMatch(normalized::contains);
    }

    private int technicalHitCount(String normalized, List<String> referencePoints) {
        long keywordHits = AiAnswerQualityEvaluator.TECHNICAL_KEYWORDS.stream()
                .map(this::normalize)
                .filter(normalized::contains)
                .count();
        long referenceHits = safeReferencePoints(referencePoints)
                .map(this::normalize)
                .filter(normalized::contains)
                .count();
        return (int) Math.min(Integer.MAX_VALUE, keywordHits + referenceHits);
    }

    private Stream<String> safeReferencePoints(List<String> referencePoints) {
        if (referencePoints == null) {
            return Stream.empty();
        }
        return referencePoints.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .filter(point -> point.length() >= 2)
                .filter(point -> !AiAnswerQualityEvaluator.GENERIC_REFERENCE_POINTS.contains(point));
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT)
                .replaceAll("[\\p{Punct}\\p{IsPunctuation}\\s]+", "");
    }

    private int meaningfulLength(String value) {
        if (value == null) {
            return 0;
        }
        return value.replaceAll("[\\p{Punct}\\p{IsPunctuation}\\s]+", "").length();
    }

    private record QualityLimit(int maxScore, String reason) {
    }
}
