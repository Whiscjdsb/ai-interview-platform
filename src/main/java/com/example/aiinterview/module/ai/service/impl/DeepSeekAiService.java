package com.example.aiinterview.module.ai.service.impl;

import java.util.List;
import java.util.Map;

import com.example.aiinterview.common.BusinessException;
import com.example.aiinterview.common.ErrorCode;
import com.example.aiinterview.module.ai.dto.AiInterviewResultDTO;
import com.example.aiinterview.module.ai.vo.AiAnswerReviewVO;
import com.example.aiinterview.module.question.entity.Question;
import com.example.aiinterview.module.question.vo.TagVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class DeepSeekAiService extends MockAiService {

    private static final String API_URL = "https://api.deepseek.com/chat/completions";
    private static final String MODEL_NAME = "deepseek-chat";
    private static final int MAX_SCORE = 100;
    private static final int MAX_ITEM_COUNT = 3;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    @Value("${ai.deepseek.api-key:${app.ai.deepseek.api-key:}}")
    private String apiKey;

    @Override
    public String provider() {
        return "deepseek";
    }

    @Override
    public String modelName() {
        return MODEL_NAME;
    }

    @Override
    public AiAnswerReviewVO reviewAnswer(Question question, List<TagVO> tags, String answer) {
        String prompt = buildReviewPrompt(question, answer);
        String rawResponse = chat(prompt);
        AiInterviewResultDTO structuredResult = parseStructuredResult(rawResponse);
        return toReviewVO(structuredResult, rawResponse);
    }

    public String chat(String prompt) {
        if (!StringUtils.hasText(apiKey)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "DeepSeek API key is not configured");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "model", MODEL_NAME,
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a senior Java interviewer and grade answers strictly."),
                        Map.of("role", "user", "content", prompt)),
                "temperature", 0.3);

        try {
            String response = restTemplate.postForObject(API_URL, new HttpEntity<>(body, headers), String.class);
            return response == null ? "" : response;
        } catch (RestClientException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "DeepSeek request failed");
        }
    }

    private String buildReviewPrompt(Question question, String answer) {
        return """
                Question:
                %s

                User answer:
                %s

                Scoring requirements:
                Return only one valid JSON object. Do not use Markdown, code fences, comments, or extra text.
                The JSON object must contain exactly these fields:
                {
                  "score": 0,
                  "strengths": ["up to 3 concise strengths"],
                  "weaknesses": ["up to 3 concise weaknesses"],
                  "suggestions": ["up to 3 concise improvement suggestions"],
                  "referenceAnswer": "a concise reference answer"
                }
                score must be an integer from 0 to 100.
                strengths, weaknesses, and suggestions must each contain no more than 3 items.
                """.formatted(question.getContent(), answer);
    }

    private AiAnswerReviewVO toReviewVO(AiInterviewResultDTO structuredResult, String rawResponse) {
        AiAnswerReviewVO vo = new AiAnswerReviewVO();
        vo.setModelName(MODEL_NAME);
        vo.setRawResponse(rawResponse);
        vo.setStructuredResult(structuredResult);

        if (structuredResult == null) {
            vo.setScore(0);
            vo.setSummary(rawResponse);
            vo.setAdvantages(List.of());
            vo.setImprovements(List.of());
            vo.setSuggestedAnswer(rawResponse);
            return vo;
        }

        vo.setScore(structuredResult.getScore());
        vo.setSummary("DeepSeek returned a structured interview review.");
        vo.setAdvantages(structuredResult.getStrengths());
        vo.setImprovements(structuredResult.getSuggestions());
        vo.setSuggestedAnswer(structuredResult.getReferenceAnswer());
        return vo;
    }

    private AiInterviewResultDTO parseStructuredResult(String rawResponse) {
        try {
            if (!StringUtils.hasText(rawResponse)) {
                return null;
            }
            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                return null;
            }
            String content = choices.get(0).path("message").path("content").asText();
            if (!StringUtils.hasText(content)) {
                return null;
            }
            AiInterviewResultDTO result = objectMapper.readValue(content.trim(), AiInterviewResultDTO.class);
            return normalize(result);
        } catch (Exception ex) {
            return null;
        }
    }

    private AiInterviewResultDTO normalize(AiInterviewResultDTO result) {
        if (result == null) {
            return null;
        }
        result.setScore(normalizeScore(result.getScore()));
        result.setStrengths(normalizeItems(result.getStrengths()));
        result.setWeaknesses(normalizeItems(result.getWeaknesses()));
        result.setSuggestions(normalizeItems(result.getSuggestions()));
        result.setReferenceAnswer(StringUtils.hasText(result.getReferenceAnswer()) ? result.getReferenceAnswer() : "");
        return result;
    }

    private Integer normalizeScore(Integer score) {
        if (score == null) {
            return 0;
        }
        return Math.max(0, Math.min(MAX_SCORE, score));
    }

    private List<String> normalizeItems(List<String> items) {
        if (items == null) {
            return List.of();
        }
        return items.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .limit(MAX_ITEM_COUNT)
                .toList();
    }
}
