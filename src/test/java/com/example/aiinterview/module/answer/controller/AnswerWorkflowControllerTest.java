package com.example.aiinterview.module.answer.controller;

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
class AnswerWorkflowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void submitSingleChoiceCorrectSuccess() throws Exception {
        String token = registerAndLogin("answerSingleUser");

        mockMvc.perform(post("/api/answers/submit")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(answerBody(1, "A", 45)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.isCorrect").value(true))
                .andExpect(jsonPath("$.data.score").value(100.00))
                .andExpect(jsonPath("$.data.correctAnswer").value("A"))
                .andExpect(jsonPath("$.data.answerRecordId").isNumber());
    }

    @Test
    void submitMultipleChoiceDifferentOrderStillCorrect() throws Exception {
        String token = registerAndLogin("answerMultiUser");

        mockMvc.perform(post("/api/answers/submit")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(answerBody(3, "C, A, B", 60)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.isCorrect").value(true))
                .andExpect(jsonPath("$.data.score").value(100.00));
    }

    @Test
    void wrongAnswerAddedToWrongQuestionBook() throws Exception {
        String token = registerAndLogin("wrongBookUser");

        mockMvc.perform(post("/api/answers/submit")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(answerBody(1, "B", 30)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isCorrect").value(false));

        mockMvc.perform(get("/api/wrong-questions")
                        .header("Authorization", "Bearer " + token)
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.records[0].questionId").value(1))
                .andExpect(jsonPath("$.data.records[0].wrongCount").value(1))
                .andExpect(jsonPath("$.data.records[0].status").value("ACTIVE"));
    }

    @Test
    void correctAnswerResolvesExistingWrongQuestion() throws Exception {
        String token = registerAndLogin("resolvedWrongUser");

        mockMvc.perform(post("/api/answers/submit")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(answerBody(1, "B", 30)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/answers/submit")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(answerBody(1, "A", 20)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isCorrect").value(true));

        mockMvc.perform(get("/api/wrong-questions")
                        .header("Authorization", "Bearer " + token)
                        .param("status", "RESOLVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.records[0].questionId").value(1))
                .andExpect(jsonPath("$.data.records[0].status").value("RESOLVED"));
    }

    @Test
    void duplicateFavoriteFails() throws Exception {
        String token = registerAndLogin("favoriteDuplicateUser");

        mockMvc.perform(post("/api/favorites/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/api/favorites/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("Question is already in favorites"));
    }

    @Test
    void userCannotReadOtherUsersAnswerRecord() throws Exception {
        String ownerToken = registerAndLogin("answerOwnerUser");
        String otherToken = registerAndLogin("answerOtherUser");

        String submitResponse = mockMvc.perform(post("/api/answers/submit")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(answerBody(1, "A", 45)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode root = objectMapper.readTree(submitResponse);
        long recordId = root.path("data").path("answerRecordId").asLong();

        mockMvc.perform(get("/api/answers/history/" + recordId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("Access is denied"));
    }

    @Test
    void anonymousSubmitAnswerFails() throws Exception {
        mockMvc.perform(post("/api/answers/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(answerBody(1, "A", 45)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("Authentication is required"));
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

    private String answerBody(long questionId, String userAnswer, int answerDuration) {
        return """
                {
                  "questionId": %d,
                  "userAnswer": "%s",
                  "answerDuration": %d
                }
                """.formatted(questionId, userAnswer, answerDuration);
    }
}
