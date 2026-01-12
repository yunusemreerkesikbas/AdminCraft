package com.backend.presentation.dto.response;

import com.backend.domain.entity.ProductType;

import java.time.LocalDateTime;
import java.util.List;

public record ProductTypeDetailResponse(
        Long id,
        String uuid,
        String uid,
        String code,
        String name,
        String category,
        List<AttributeDefinitionResponse> attributes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProductTypeDetailResponse from(ProductType entity) {
        if (entity == null) {
            throw new IllegalArgumentException("ProductType entity cannot be null");
        }
        List<AttributeDefinitionResponse> attrs = entity.getAttributeDefinitions() != null
                ? entity.getAttributeDefinitions().stream()
                    .map(AttributeDefinitionResponse::from)
                    .toList()
                : List.of();
        return new ProductTypeDetailResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getUid(),
                entity.getCode(),
                entity.getName(),
                entity.getCategory(),
                attrs,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
