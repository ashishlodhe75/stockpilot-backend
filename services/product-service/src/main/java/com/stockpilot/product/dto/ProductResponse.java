package com.stockpilot.product.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String name,
        String sku,
        String description,
        BigDecimal price,
        Integer quantity,
        Instant createdAt,
        Instant updatedAt
) {
}
