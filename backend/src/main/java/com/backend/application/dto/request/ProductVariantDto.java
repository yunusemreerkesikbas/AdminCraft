package com.backend.application.dto.request;

import java.math.BigDecimal;
import java.util.List;

public record ProductVariantDto(
        String sku,
        BigDecimal price,
        BigDecimal firstPrice,
        BigDecimal vatRate,
        Integer stockQuantity,
        Boolean active,
        Long responsiveMediaId,
        List<Long> optionValueIds) {
    public ProductVariantDto {
        sku = sku != null ? sku.trim() : null;
    }
}
