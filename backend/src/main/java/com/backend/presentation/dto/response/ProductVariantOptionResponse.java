package com.backend.presentation.dto.response;

import java.util.List;

public record ProductVariantOptionResponse(
        Long id,
        String uid,
        String code,
        String name,
        String displayType,
        Integer sortOrder,
        Boolean active,
        List<ProductVariantOptionValueResponse> values) {
    public static ProductVariantOptionResponse from(com.backend.application.dto.response.ProductVariantOptionResponse source) {
        return new ProductVariantOptionResponse(
                source.id(),
                source.uid(),
                source.code(),
                source.name(),
                source.displayType() == null ? null : source.displayType().toValue(),
                source.sortOrder(),
                source.active(),
                source.values() == null
                        ? List.of()
                        : source.values().stream()
                                .map(ProductVariantOptionValueResponse::from)
                                .toList());
    }
}

