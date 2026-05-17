package com.nexora.search.dto;

import java.io.Serializable;

public record UserSearchItem(
        String id,
        String username,
        String fullName,
        boolean verified,
        double score
) implements Serializable {
}
