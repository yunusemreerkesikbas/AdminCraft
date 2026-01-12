package com.backend.presentation.dto.response;

import com.backend.domain.entity.ProductType;

import java.time.LocalDateTime;

public record ProductTypeResponse(
        Long id,
        String uuid,
        String uid,
        String code,
        String name,
        String category,
        int attributeCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProductTypeResponse from(ProductType entity) {
        if (entity == null) {
            throw new IllegalArgumentException("ProductType entity cannot be null");
        }
        int attrCount = entity.getAttributeDefinitions() != null 
                ? entity.getAttributeDefinitions().size() 
                : 0;
        return new ProductTypeResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getUid(),
                entity.getCode(),
                entity.getName(),
                entity.getCategory(),
                attrCount,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static ProductTypeResponse fromWithoutCount(ProductType entity) {
        if (entity == null) {
            throw new IllegalArgumentException("ProductType entity cannot be null");
        }
        return new ProductTypeResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getUid(),
                entity.getCode(),
                entity.getName(),
                entity.getCategory(),
                0,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
