package com.backend.presentation.dto.response;

import com.backend.domain.entity.ProductAttribute;
import com.backend.domain.enums.ProductFieldType;

public record ProductAttributeValueResponse(
        Long id,
        String code,
        String name,
        ProductFieldType fieldType,
        Object value
) {
    public static ProductAttributeValueResponse from(ProductAttribute entity) {
        if (entity == null || entity.getAttributeDefinition() == null) {
            throw new IllegalArgumentException("ProductAttribute entity cannot be null");
        }
        return new ProductAttributeValueResponse(
                entity.getId(),
                entity.getAttributeDefinition().getCode(),
                entity.getAttributeDefinition().getName(),
                entity.getAttributeDefinition().getFieldType(),
                entity.getValue()
        );
    }
}
