package com.nexora.search.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

public record PostSearchItem(
        String id,
        String authorId,
        String content,
        List<String> hashtags,
        int popularity,
        Instant createdAt,
        double score
) implements Serializable {
}
