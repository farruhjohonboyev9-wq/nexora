package com.nexora.search.dto;

import java.io.Serializable;

public record HashtagSearchItem(
        String id,
        String tag,
        long usageCount,
        double rankScore
) implements Serializable {
}
