package com.nexora.search.service.impl;

import com.nexora.search.entity.QueryLogEntity;
import com.nexora.search.repository.postgres.QueryLogRepository;
import com.nexora.search.service.QueryAnalyticsService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class QueryAnalyticsServiceImpl implements QueryAnalyticsService {

    private static final String QUERY_POPULARITY_KEY = "search:popular:queries";

    private final QueryLogRepository queryLogRepository;
    private final RedisTemplate<String, String> redisTemplate;

    public QueryAnalyticsServiceImpl(
            QueryLogRepository queryLogRepository,
            RedisTemplate<String, String> redisTemplate
    ) {
        this.queryLogRepository = queryLogRepository;
        this.redisTemplate = redisTemplate;
    }

    @Async
    @Override
    public void logQuery(String endpoint, String q, long resultCount, long durationMs, boolean cacheHit) {
        QueryLogEntity log = new QueryLogEntity();
        log.setEndpoint(endpoint);
        log.setQueryText(q);
        log.setResultCount(resultCount);
        log.setDurationMs(durationMs);
        log.setCacheHit(cacheHit);
        log.setCreatedAt(Instant.now());
        queryLogRepository.save(log);
    }

    @Async
    @Override
    public void trackPopularQuery(String endpoint, String q) {
        String value = endpoint + "::" + q;
        redisTemplate.opsForZSet().incrementScore(QUERY_POPULARITY_KEY, value, 1D);
    }
}
