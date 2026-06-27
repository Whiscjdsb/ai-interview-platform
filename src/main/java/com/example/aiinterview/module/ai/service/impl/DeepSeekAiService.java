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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
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
    public boolean isAvailable() {
        return StringUtils.hasText(apiKey);
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
            log.warn("DeepSeek request skipped because apiKey is not configured");
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
            log.info("DeepSeek request sending: url={}, model={}, promptChars={}", API_URL, MODEL_NAME, prompt.length());
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                    API_URL,
                    new HttpEntity<>(body, headers),
                    String.class);
            String response = responseEntity.getBody() == null ? "" : responseEntity.getBody();
            log.info("DeepSeek response received: status={}, bodyChars={}",
                    responseEntity.getStatusCode().value(),
                    response.length());
            return response;
        } catch (HttpStatusCodeException ex) {
            log.warn("DeepSeek response failed: status={}, bodyChars={}",
                    ex.getStatusCode().value(),
                    ex.getResponseBodyAsString() == null ? 0 : ex.getResponseBodyAsString().length());
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "DeepSeek request failed");
        } catch (RestClientException ex) {
            log.warn("DeepSeek request failed before valid response: {}", ex.getMessage());
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
                You must grade strictly as a senior Java interviewer. Do not give comforting high scores.
                If the user answer is empty, whitespace only, or has no content, score must be 0-5.
                If the answer is clearly invalid, such as "我不会", "不会", "不知道", "不清楚", "不了解", "没学过", "不会答", "不知道怎么答", "嗯", "哦", "对", "是", "不是", "不知道了", or "没思路", score must be 0-15.
                If the answer has fewer than 10 meaningful characters and no technical keyword, score must not exceed 15.
                If the answer has fewer than 20 meaningful characters and no technical keyword, score must not exceed 25.
                If the answer is generic, vague, or lacks key technical points, score must not exceed 50.
                Only answers with valid technical keywords plus explanation, principles, concrete cases, or complete analysis may score above 40.
                Only answers with core concepts, key principles, concrete cases, or complete analysis may score above 70.
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
