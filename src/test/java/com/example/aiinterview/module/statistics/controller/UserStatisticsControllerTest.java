package com.example.aiinterview.module.statistics.controller;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aiinterview.module.statistics.entity.DailyLearningRecord;
import com.example.aiinterview.module.statistics.mapper.DailyLearningRecordMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserStatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DailyLearningRecordMapper dailyLearningRecordMapper;

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void submitAnswerCreatesTodayLearningRecord() throws Exception {
        LoginResult login = registerAndLogin("statsCreateUser");

        submitAnswer(login.token(), 1, "A", 45);

        DailyLearningRecord record = selectTodayRecord(login.userId());
        org.assertj.core.api.Assertions.assertThat(record).isNotNull();
        org.assertj.core.api.Assertions.assertThat(record.getAnswerCount()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(record.getCorrectCount()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(record.getStudyDuration()).isEqualTo(45);
    }

    @Test
    void sameDaySubmissionsAccumulateLearningRecord() throws Exception {
        LoginResult login = registerAndLogin("statsAccumulateUser");

        submitAnswer(login.token(), 1, "A", 30);
        submitAnswer(login.token(), 3, "C,B,A", 50);

        DailyLearningRecord record = selectTodayRecord(login.userId());
        org.assertj.core.api.Assertions.assertThat(record.getAnswerCount()).isEqualTo(2);
        org.assertj.core.api.Assertions.assertThat(record.getCorrectCount()).isEqualTo(2);
        org.assertj.core.api.Assertions.assertThat(record.getStudyDuration()).isEqualTo(80);
    }

    @Test
    void trendFillsMissingDatesWithZero() throws Exception {
        LoginResult login = registerAndLogin("statsTrendUser");

        mockMvc.perform(get("/api/statistics/user/trend")
                        .header("Authorization", "Bearer " + login.token())
                        .param("days", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].answerCount").value(0))
                .andExpect(jsonPath("$.data[1].answerCount").value(0))
                .andExpect(jsonPath("$.data[2].answerCount").value(0));
    }

    @Test
    void userOnlySeesOwnStatistics() throws Exception {
        LoginResult owner = registerAndLogin("statsOwnerUser");
        LoginResult other = registerAndLogin("statsOtherUser");
        submitAnswer(owner.token(), 1, "A", 45);

        mockMvc.perform(get("/api/statistics/user/overview")
                        .header("Authorization", "Bearer " + other.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.totalAnswerCount").value(0))
                .andExpect(jsonPath("$.data.correctAnswerCount").value(0))
                .andExpect(jsonPath("$.data.todayAnswerCount").value(0));
    }

    @Test
    void submitAnswerEvictsOverviewCache() throws Exception {
        LoginResult login = registerAndLogin("statsCacheUser");

        submitAnswer(login.token(), 1, "A", 45);

        verify(stringRedisTemplate).delete("ai-interview:statistics:user:%d:overview".formatted(login.userId()));
    }

    @Test
    void daysGreaterThanThirtyFailsValidation() throws Exception {
        LoginResult login = registerAndLogin("statsDaysUser");

        mockMvc.perform(get("/api/statistics/user/trend")
                        .header("Authorization", "Bearer " + login.token())
                        .param("days", "31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    private DailyLearningRecord selectTodayRecord(Long userId) {
        return dailyLearningRecordMapper.selectOne(new LambdaQueryWrapper<DailyLearningRecord>()
                .eq(DailyLearningRecord::getUserId, userId)
                .eq(DailyLearningRecord::getRecordDate, LocalDate.now())
                .last("LIMIT 1"));
    }

    private void submitAnswer(String token, long questionId, String userAnswer, int answerDuration) throws Exception {
        mockMvc.perform(post("/api/answers/submit")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "questionId": %d,
                                  "userAnswer": "%s",
                                  "answerDuration": %d
                                }
                                """.formatted(questionId, userAnswer, answerDuration)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    private LoginResult registerAndLogin(String username) throws Exception {
        String registerResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "test1234",
                                  "confirmPassword": "test1234"
                                }
                                """.formatted(username)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long userId = objectMapper.readTree(registerResponse).path("data").path("id").asLong();

        String loginResponse = mockMvc.perform(post("/api/auth/login")
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
        JsonNode root = objectMapper.readTree(loginResponse);
        return new LoginResult(userId, root.path("data").path("token").asText());
    }

    private record LoginResult(Long userId, String token) {
    }
}
