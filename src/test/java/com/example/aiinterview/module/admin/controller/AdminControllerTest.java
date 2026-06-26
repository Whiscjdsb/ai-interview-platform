package com.example.aiinterview.module.admin.controller;

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

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
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void normalUserAccessAdminStatisticsFails() throws Exception {
        LoginResult user = registerAndLogin("adminForbiddenUser");

        mockMvc.perform(get("/api/admin/statistics/overview")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("Access is denied"));
    }

    @Test
    void adminQueriesUserListSuccess() throws Exception {
        String adminToken = adminLogin().token();

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.records[0].id").isNumber())
                .andExpect(jsonPath("$.data.records[0].password").doesNotExist())
                .andExpect(jsonPath("$.data.records[0].roles").isArray());
    }

    @Test
    void adminDisablesNormalUserSuccess() throws Exception {
        String adminToken = adminLogin().token();
        LoginResult user = registerAndLogin("adminDisableUser");

        mockMvc.perform(put("/api/admin/users/" + user.userId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/admin/users/" + user.userId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(0));
    }

    @Test
    void disabledUserCannotLogin() throws Exception {
        String adminToken = adminLogin().token();
        LoginResult user = registerAndLogin("adminDisabledLoginUser");

        mockMvc.perform(put("/api/admin/users/" + user.userId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "adminDisabledLoginUser",
                                  "password": "test1234"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("User account is disabled"));
    }

    @Test
    void adminCannotDisableSelf() throws Exception {
        LoginResult admin = adminLogin();

        mockMvc.perform(put("/api/admin/users/" + admin.userId() + "/status")
                        .header("Authorization", "Bearer " + admin.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Cannot disable current administrator"));
    }

    @Test
    void adminCannotRemoveLastAdminRole() throws Exception {
        LoginResult admin = adminLogin();

        mockMvc.perform(put("/api/admin/users/" + admin.userId() + "/roles")
                        .header("Authorization", "Bearer " + admin.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleCodes\":[\"USER\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("Cannot remove the last ADMIN role"));
    }

    @Test
    void adminStatisticsOverviewReturnsStructure() throws Exception {
        String adminToken = adminLogin().token();

        mockMvc.perform(get("/api/admin/statistics/overview")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.totalUserCount").isNumber())
                .andExpect(jsonPath("$.data.todayNewUserCount").isNumber())
                .andExpect(jsonPath("$.data.totalQuestionCount").isNumber())
                .andExpect(jsonPath("$.data.averageAccuracyRate").isNumber())
                .andExpect(jsonPath("$.data.totalAiReviewCount").isNumber());
    }

    @Test
    void adminTrendFillsMissingDates() throws Exception {
        String adminToken = adminLogin().token();

        mockMvc.perform(get("/api/admin/statistics/trend")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("days", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].newUserCount").value(0))
                .andExpect(jsonPath("$.data[0].answerCount").value(0))
                .andExpect(jsonPath("$.data[1].newUserCount").value(0))
                .andExpect(jsonPath("$.data[1].answerCount").value(0));
    }

    @Test
    void submitAnswerEvictsAdminStatisticsCache() throws Exception {
        LoginResult user = registerAndLogin("adminStatsCacheUser");
        Set<String> cacheKeys = Set.of("ai-interview:statistics:admin:overview");
        when(stringRedisTemplate.keys("ai-interview:statistics:admin:*")).thenReturn(cacheKeys);
        clearInvocations(stringRedisTemplate);

        mockMvc.perform(post("/api/answers/submit")
                        .header("Authorization", "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "questionId": 1,
                                  "userAnswer": "A",
                                  "answerDuration": 30
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(stringRedisTemplate).keys("ai-interview:statistics:admin:*");
        verify(stringRedisTemplate).delete(cacheKeys);
    }

    private LoginResult adminLogin() throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "admin123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode root = objectMapper.readTree(response);
        JsonNode data = root.path("data");
        return new LoginResult(data.path("id").asLong(), data.path("token").asText());
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
