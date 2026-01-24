package com.backend.presentation.dto.response;

import com.backend.domain.entity.ProductAttributeDefinition;
import com.backend.domain.enums.ProductFieldType;

import java.time.LocalDateTime;

public record AttributeDefinitionResponse(
        Long id,
        String uuid,
        String code,
        String name,
        ProductFieldType fieldType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AttributeDefinitionResponse from(ProductAttributeDefinition entity) {
        if (entity == null) {
            throw new IllegalArgumentException("ProductAttributeDefinition entity cannot be null");
        }
        return new AttributeDefinitionResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getCode(),
                entity.getName(),
                entity.getFieldType(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
