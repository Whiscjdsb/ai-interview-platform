package com.example.aiinterview.module.ai.controller;

import static org.hamcrest.Matchers.lessThanOrEqualTo;
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
    void reviewInvalidAnswerScoresAtMost15() throws Exception {
        String token = registerAndLogin("aiInvalidReviewUser");

        mockMvc.perform(post("/api/ai/review-answer")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewBody(2, "\u6211\u4e0d\u4f1a\u56de\u7b54\u8fd9\u4e2a\u95ee\u9898\uff0c\u4e0d\u77e5\u9053\u600e\u4e48\u7b54\uff0c\u4e5f\u6ca1\u6709\u5b66\u8fc7\u76f8\u5173\u5185\u5bb9\u3002")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.score", lessThanOrEqualTo(15)));
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
    void submitInterviewBlankAnswerScoresAtMost5() throws Exception {
        String token = registerAndLogin("aiBlankInterviewUser");
        long interviewId = createInterview(token, 1);

        mockMvc.perform(post("/api/ai/interviews/" + interviewId + "/submit")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "answers": [
                                    { "questionNo": 1, "answer": "" }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.totalScore", lessThanOrEqualTo(5)))
                .andExpect(jsonPath("$.data.questionResults[0].score", lessThanOrEqualTo(5)));
    }

    @Test
    void submitInterviewWoBuHuiAnswerScoresAtMost15() throws Exception {
        assertSingleQuestionSubmitScoreAtMost("aiWoBuHuiInterviewUser", "\u6211\u4e0d\u4f1a", 15);
    }

    @Test
    void submitInterviewHmmAnswerScoresAtMost15() throws Exception {
        assertSingleQuestionSubmitScoreAtMost("aiHmmInterviewUser", "\u55ef", 15);
    }

    @Test
    void submitInterviewDuiAnswerScoresAtMost15() throws Exception {
        assertSingleQuestionSubmitScoreAtMost("aiDuiInterviewUser", "\u5bf9", 15);
    }

    @Test
    void submitInterviewLowQualityAnswersTotalScoreAtMost15() throws Exception {
        String token = registerAndLogin("aiAllLowQualityInterviewUser");
        long interviewId = createInterview(token, 3);

        mockMvc.perform(post("/api/ai/interviews/" + interviewId + "/submit")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "answers": [
                                    { "questionNo": 1, "answer": "\u6211\u4e0d\u4f1a" },
                                    { "questionNo": 2, "answer": "\u55ef" },
                                    { "questionNo": 3, "answer": "\u5bf9" }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.totalScore", lessThanOrEqualTo(15)))
                .andExpect(jsonPath("$.data.questionResults[0].score", lessThanOrEqualTo(15)))
                .andExpect(jsonPath("$.data.questionResults[1].score", lessThanOrEqualTo(15)))
                .andExpect(jsonPath("$.data.questionResults[2].score", lessThanOrEqualTo(15)));
    }

    @Test
    void submitInterviewGenericAnswerScoresAtMost50() throws Exception {
        String token = registerAndLogin("aiGenericInterviewUser");
        long interviewId = createInterview(token, 1);

        mockMvc.perform(post("/api/ai/interviews/" + interviewId + "/submit")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "answers": [
                                    {
                                      "questionNo": 1,
                                      "answer": "\u8fd9\u4e2a\u95ee\u9898\u9700\u8981\u7ed3\u5408\u5b9e\u9645\u60c5\u51b5\u5206\u6790\uff0c\u4e3b\u8981\u662f\u63d0\u5347\u7cfb\u7edf\u6027\u80fd\u548c\u7a33\u5b9a\u6027\uff0c\u5177\u4f53\u8981\u770b\u9879\u76ee\u60c5\u51b5\u3002"
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.totalScore", lessThanOrEqualTo(50)))
                .andExpect(jsonPath("$.data.questionResults[0].score", lessThanOrEqualTo(50)));
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

    private long createInterview(String token, int questionCount) throws Exception {
        String response = mockMvc.perform(post("/api/ai/generate-interview")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "position": "Java Backend Engineer",
                                  "difficulty": "MEDIUM",
                                  "focusTags": ["Java", "Spring Boot", "MySQL", "Redis"],
                                  "questionCount": %d
                                }
                                """.formatted(questionCount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode root = objectMapper.readTree(response);
        return root.path("data").path("id").asLong();
    }

    private void assertSingleQuestionSubmitScoreAtMost(String username, String answer, int maxScore) throws Exception {
        String token = registerAndLogin(username);
        long interviewId = createInterview(token, 1);

        mockMvc.perform(post("/api/ai/interviews/" + interviewId + "/submit")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "answers": [
                                    { "questionNo": 1, "answer": "%s" }
                                  ]
                                }
                                """.formatted(answer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.totalScore", lessThanOrEqualTo(maxScore)))
                .andExpect(jsonPath("$.data.questionResults[0].score", lessThanOrEqualTo(maxScore)));
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
