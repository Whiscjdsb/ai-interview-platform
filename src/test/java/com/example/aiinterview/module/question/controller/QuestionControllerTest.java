package com.example.aiinterview.module.question.controller;

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
class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void anonymousPageQuestionsSuccess() throws Exception {
        mockMvc.perform(get("/api/questions")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").isNumber())
                .andExpect(jsonPath("$.data.records[0].title").isNotEmpty())
                .andExpect(jsonPath("$.data.records[0].correctAnswer").doesNotExist())
                .andExpect(jsonPath("$.data.records[0].analysis").doesNotExist());
    }

    @Test
    void userCreateQuestionForbidden() throws Exception {
        String token = registerAndLoginUser("questionUserForbidden");

        mockMvc.perform(post("/api/admin/questions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(questionBody("USER should not create question", "USER token should be rejected.", "SHORT_ANSWER", "EASY", "Forbidden")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("Access is denied"));
    }

    @Test
    void adminCreateQuestionSuccess() throws Exception {
        String token = login("admin", "admin123");

        mockMvc.perform(post("/api/admin/questions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(questionBody("Admin created question", "Created by admin test.", "SHORT_ANSWER", "MEDIUM", "Admin answer")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.title").value("Admin created question"))
                .andExpect(jsonPath("$.data.correctAnswer").value("Admin answer"))
                .andExpect(jsonPath("$.data.tags[0].tagName").value("Java"));
    }

    @Test
    void filterQuestionsByTagSuccess() throws Exception {
        mockMvc.perform(get("/api/questions")
                        .param("tagId", "3")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].title").value("MySQL index usage"))
                .andExpect(jsonPath("$.data.records[0].tags[0].tagName").value("MySQL"));
    }

    @Test
    void deleteTagWithRelatedQuestionFails() throws Exception {
        String token = login("admin", "admin123");

        mockMvc.perform(delete("/api/admin/tags/3")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("Tag is used by questions and cannot be deleted"));
    }

    private String registerAndLoginUser(String username) throws Exception {
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
        return login(username, "test1234");
    }

    private String login(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode root = objectMapper.readTree(response);
        return root.path("data").path("token").asText();
    }

    private String questionBody(String title, String content, String type, String difficulty, String answer) {
        return """
                {
                  "title": "%s",
                  "content": "%s",
                  "questionType": "%s",
                  "difficulty": "%s",
                  "correctAnswer": "%s",
                  "analysis": "Generated in integration test.",
                  "tagIds": [1, 2]
                }
                """.formatted(title, content, type, difficulty, answer);
    }
}
