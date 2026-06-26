package com.example.aiinterview.module.statistics.service.impl;

import java.time.Duration;

import com.example.aiinterview.module.statistics.service.StatisticsCacheService;
import com.example.aiinterview.module.statistics.vo.UserStatisticsOverviewVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class RedisStatisticsCacheServiceImpl implements StatisticsCacheService {

    private static final String OVERVIEW_KEY_FORMAT = "ai-interview:statistics:user:%d:overview";
    private static final Duration OVERVIEW_TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public UserStatisticsOverviewVO getOverview(Long userId) {
        try {
            String cached = stringRedisTemplate.opsForValue().get(overviewKey(userId));
            if (!StringUtils.hasText(cached)) {
                return null;
            }
            return objectMapper.readValue(cached, UserStatisticsOverviewVO.class);
        } catch (RuntimeException ex) {
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public void setOverview(Long userId, UserStatisticsOverviewVO overview) {
        try {
            stringRedisTemplate.opsForValue().set(overviewKey(userId), objectMapper.writeValueAsString(overview), OVERVIEW_TTL);
        } catch (Exception ex) {
            // Statistics can still be served from the database if Redis is unavailable.
        }
    }

    @Override
    public void evictOverview(Long userId) {
        try {
            stringRedisTemplate.delete(overviewKey(userId));
        } catch (RuntimeException ex) {
            // Cache eviction failure must not break answer or favorite workflows.
        }
    }

    private String overviewKey(Long userId) {
        return OVERVIEW_KEY_FORMAT.formatted(userId);
    }
}
