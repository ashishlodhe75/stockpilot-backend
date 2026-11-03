package com.stockpilot.product.dto;

import java.util.List;

public record ProductPageResponse(
        List<ProductResponse> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean last
) {
}
