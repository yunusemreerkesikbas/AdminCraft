package com.backend.presentation.dto.response;

import com.backend.domain.entity.ProductVariantOptionValue;
import com.backend.domain.enums.ProductVariantOptionDisplayType;

public record ProductVariantOptionValueSelectionResponse(
        Long optionId,
        String optionCode,
        String optionName,
        ProductVariantOptionDisplayType displayType,
        Long valueId,
        String valueCode,
        String valueLabel,
        String swatchValue) {
    public static ProductVariantOptionValueSelectionResponse from(ProductVariantOptionValue value) {
        return new ProductVariantOptionValueSelectionResponse(
                value.getOption().getId(),
                value.getOption().getCode(),
                value.getOption().getName(),
                value.getOption().getDisplayType(),
                value.getId(),
                value.getCode(),
                value.getLabel(),
                value.getSwatchValue());
    }
}
