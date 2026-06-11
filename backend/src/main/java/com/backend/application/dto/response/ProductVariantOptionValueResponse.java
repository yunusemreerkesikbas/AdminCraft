package com.backend.application.dto.response;

import com.backend.domain.entity.ProductVariantOptionValue;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductVariantOptionValueResponse(
        Long id,
        String uid,
        String code,
        String label,
        String swatchValue,
        Integer sortOrder,
        Boolean active) {
    public static ProductVariantOptionValueResponse from(ProductVariantOptionValue entity) {
        return new ProductVariantOptionValueResponse(
                entity.getId(),
                entity.getUid(),
                entity.getCode(),
                entity.getLabel(),
                entity.getSwatchValue(),
                entity.getSortOrder(),
                entity.getActive());
    }
}
