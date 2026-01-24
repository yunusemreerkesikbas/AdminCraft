package com.backend.presentation.dto.response;

import com.backend.domain.entity.ProductI18n;
import com.backend.domain.enums.Language;
import com.backend.shared.util.ResponseValueFilter;

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
                ResponseValueFilter.filterEmptyString(entity.getShortDescription()),
                ResponseValueFilter.filterEmptyString(entity.getDescription()),
                ResponseValueFilter.filterEmptyString(entity.getSeoTitle()),
                ResponseValueFilter.filterEmptyString(entity.getSeoDescription()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
