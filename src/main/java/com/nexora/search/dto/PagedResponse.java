package com.nexora.search.dto;

import java.io.Serializable;
import java.util.List;

public record PagedResponse<T>(
        int page,
        int size,
        long totalElements,
        int totalPages,
        List<T> items
) implements Serializable {
}
