package com.example.aiinterview.module.ai.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AiAssistantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void anonymousReviewAnswerFails() throws Exception {
        mockMvc.perform(post("/api/ai/review-answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewBody(2, "Spring Boot auto configuration uses conditions and beans.")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void reviewNonSubjectiveQuestionFails() throws Exception {
        String token = registerAndLogin("aiNonSubjectiveUser");

        mockMvc.perform(post("/api/ai/review-answer")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewBody(1, "This answer is long enough but the question is single choice.")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Only SHORT_ANSWER or CODING questions can be reviewed"));
    }

    @Test
    void reviewShortAnswerSuccessAndHistorySaved() throws Exception {
        String token = registerAndLogin("aiReviewUser");

        mockMvc.perform(post("/api/ai/review-answer")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewBody(2, "Spring Boot auto configuration relies on conditional annotations, beans, and classpath based configuration.")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.score").isNumber())
                .andExpect(jsonPath("$.data.modelName").value("MOCK"));

        mockMvc.perform(get("/api/ai/history")
                        .header("Authorization", "Bearer " + token)
                        .param("recordType", "ANSWER_REVIEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].recordType").value("ANSWER_REVIEW"))
                .andExpect(jsonPath("$.data.records[0].questionTitle").value("Spring Boot auto configuration"));
    }

    @Test
    void generateInterviewQuestionCountMatchesRequest() throws Exception {
        String token = registerAndLogin("aiInterviewUser");

        mockMvc.perform(post("/api/ai/generate-interview")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "position": "Java Backend Engineer",
                                  "difficulty": "MEDIUM",
                                  "focusTags": ["Java", "Spring Boot", "MySQL", "Redis"],
                                  "questionCount": 4
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.questions.length()").value(4))
                .andExpect(jsonPath("$.data.modelName").value("MOCK"));
    }

    @Test
    void weaknessSummaryWithoutLearningDataReturnsFriendlyResult() throws Exception {
        String token = registerAndLogin("aiWeaknessEmptyUser");

        mockMvc.perform(post("/api/ai/weakness-summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.modelName").value("MOCK"))
                .andExpect(jsonPath("$.data.weaknesses.length()").value(0))
                .andExpect(jsonPath("$.data.studyPlan.length()").value(3));
    }

    @Test
    void userCannotReadOtherUsersAiHistory() throws Exception {
        String ownerToken = registerAndLogin("aiHistoryOwner");
        String otherToken = registerAndLogin("aiHistoryOther");
        createReview(ownerToken);
        long historyId = firstHistoryId(ownerToken);

        mockMvc.perform(get("/api/ai/history/" + historyId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("Access is denied"));
    }

    @Test
    void userDeletesOwnAiHistorySuccess() throws Exception {
        String token = registerAndLogin("aiDeleteHistoryUser");
        createReview(token);
        long historyId = firstHistoryId(token);

        mockMvc.perform(delete("/api/ai/history/" + historyId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/ai/history/" + historyId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    private void createReview(String token) throws Exception {
        mockMvc.perform(post("/api/ai/review-answer")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewBody(2, "Spring Boot auto configuration uses conditional annotations, beans, and classpath based matching.")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    private long firstHistoryId(String token) throws Exception {
        String response = mockMvc.perform(get("/api/ai/history")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode root = objectMapper.readTree(response);
        return root.path("data").path("records").path(0).path("id").asLong();
    }

    private String registerAndLogin(String username) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "test1234",
                                  "confirmPassword": "test1234"
                                }
                                """.formatted(username)))
                .andExpect(status().isOk());

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "test1234"
                                }
                                """.formatted(username)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode root = objectMapper.readTree(response);
        return root.path("data").path("token").asText();
    }

    private String reviewBody(long questionId, String answer) {
        return """
                {
                  "questionId": %d,
                  "answer": "%s"
                }
                """.formatted(questionId, answer);
    }
}
