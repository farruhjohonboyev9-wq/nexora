package com.nexora.search.service;

public interface QueryAnalyticsService {

    void logQuery(String endpoint, String q, long resultCount, long durationMs, boolean cacheHit);

    void trackPopularQuery(String endpoint, String q);
} 

