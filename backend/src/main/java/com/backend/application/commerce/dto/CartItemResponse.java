package com.backend.application.commerce.dto;

import java.math.BigDecimal;

public record CartItemResponse(
        String itemUid,
        String productUid,
        String productSku,
        String variantUid,
        String variantSku,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal currentUnitPrice,
        BigDecimal vatRate,
        BigDecimal lineTotal,
        Boolean priceChanged,
        Boolean available,
        Integer stockQuantity) {
}
