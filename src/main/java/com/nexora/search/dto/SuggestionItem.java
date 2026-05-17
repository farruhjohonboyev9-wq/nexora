package com.nexora.search.dto;

import java.io.Serializable;

public record SuggestionItem(
        String type,
        String value,
        double score
) implements Serializable {
}
