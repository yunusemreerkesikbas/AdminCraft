package com.backend.presentation.dto.response;

public record ProductVariantOptionValueResponse(
        Long id,
        String uid,
        String code,
        String label,
        String swatchValue,
        Integer sortOrder,
        Boolean active) {
    public static ProductVariantOptionValueResponse from(
            com.backend.application.dto.response.ProductVariantOptionValueResponse source) {
        return new ProductVariantOptionValueResponse(
                source.id(),
                source.uid(),
                source.code(),
                source.label(),
                source.swatchValue(),
                source.sortOrder(),
                source.active());
    }
}

