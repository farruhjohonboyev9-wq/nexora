package com.nexora.search.config;

import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.Map;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5));

        Map<String, RedisCacheConfiguration> perCache = Map.of(
                "search-users", defaultConfig.entryTtl(Duration.ofMinutes(2)),
                "search-posts", defaultConfig.entryTtl(Duration.ofMinutes(2)),
                "search-hashtags", defaultConfig.entryTtl(Duration.ofMinutes(2)),
                "search-suggest", defaultConfig.entryTtl(Duration.ofSeconds(30))
        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(perCache)
                .build();
    }
}
