package com.backend.presentation.dto.response;

import com.backend.domain.entity.ProductI18n;
import com.backend.domain.enums.Language;

import java.time.LocalDateTime;

public record ProductI18nResponse(
        Long id,
        String uuid,
        String uid,
        Language language,
        String name,
        String shortDescription,
        String description,
        String seoTitle,
        String seoDescription,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProductI18nResponse from(ProductI18n entity) {
        if (entity == null) {
            throw new IllegalArgumentException("ProductI18n entity cannot be null");
        }
        return new ProductI18nResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getUid(),
                entity.getLanguage(),
                entity.getName(),
                entity.getShortDescription(),
                entity.getDescription(),
                entity.getSeoTitle(),
                entity.getSeoDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
