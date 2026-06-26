package com.example.aiinterview.module.admin.service.impl;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import com.example.aiinterview.module.admin.service.AdminStatisticsCacheService;
import com.example.aiinterview.module.admin.vo.AdminPopularQuestionVO;
import com.example.aiinterview.module.admin.vo.AdminPopularTagVO;
import com.example.aiinterview.module.admin.vo.AdminStatisticsOverviewVO;
import com.example.aiinterview.module.admin.vo.AdminTrendVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class RedisAdminStatisticsCacheServiceImpl implements AdminStatisticsCacheService {

    private static final String PREFIX = "ai-interview:statistics:admin:";
    private static final String OVERVIEW_KEY = PREFIX + "overview";
    private static final String TREND_KEY_FORMAT = PREFIX + "trend:%d";
    private static final String POPULAR_QUESTIONS_KEY_FORMAT = PREFIX + "popular-questions:%d";
    private static final String POPULAR_TAGS_KEY_FORMAT = PREFIX + "popular-tags:%d";
    private static final Duration TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public AdminStatisticsOverviewVO getOverview() {
        return read(OVERVIEW_KEY, AdminStatisticsOverviewVO.class);
    }

    @Override
    public void setOverview(AdminStatisticsOverviewVO overview) {
        write(OVERVIEW_KEY, overview);
    }

    @Override
    public List<AdminTrendVO> getTrend(int days) {
        return readList(TREND_KEY_FORMAT.formatted(days), new TypeReference<>() {
        });
    }

    @Override
    public void setTrend(int days, List<AdminTrendVO> trend) {
        write(TREND_KEY_FORMAT.formatted(days), trend);
    }

    @Override
    public List<AdminPopularQuestionVO> getPopularQuestions(int limit) {
        return readList(POPULAR_QUESTIONS_KEY_FORMAT.formatted(limit), new TypeReference<>() {
        });
    }

    @Override
    public void setPopularQuestions(int limit, List<AdminPopularQuestionVO> questions) {
        write(POPULAR_QUESTIONS_KEY_FORMAT.formatted(limit), questions);
    }

    @Override
    public List<AdminPopularTagVO> getPopularTags(int limit) {
        return readList(POPULAR_TAGS_KEY_FORMAT.formatted(limit), new TypeReference<>() {
        });
    }

    @Override
    public void setPopularTags(int limit, List<AdminPopularTagVO> tags) {
        write(POPULAR_TAGS_KEY_FORMAT.formatted(limit), tags);
    }

    @Override
    public void evictAll() {
        try {
            Set<String> keys = stringRedisTemplate.keys(PREFIX + "*");
            if (!CollectionUtils.isEmpty(keys)) {
                stringRedisTemplate.delete(keys);
            }
        } catch (RuntimeException ex) {
            // Admin statistics must remain available from the database if Redis is unavailable.
        }
    }

    private <T> T read(String key, Class<T> type) {
        try {
            String cached = stringRedisTemplate.opsForValue().get(key);
            if (!StringUtils.hasText(cached)) {
                return null;
            }
            return objectMapper.readValue(cached, type);
        } catch (Exception ex) {
            return null;
        }
    }

    private <T> List<T> readList(String key, TypeReference<List<T>> typeReference) {
        try {
            String cached = stringRedisTemplate.opsForValue().get(key);
            if (!StringUtils.hasText(cached)) {
                return null;
            }
            return objectMapper.readValue(cached, typeReference);
        } catch (Exception ex) {
            return null;
        }
    }

    private void write(String key, Object value) {
        try {
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), TTL);
        } catch (Exception ex) {
            // Cache write failures should not affect admin APIs.
        }
    }
}
