package com.backend.presentation.dto.response;

import com.backend.domain.entity.CategoryI18n;
import com.backend.domain.enums.Language;

import java.time.LocalDateTime;

public record CategoryI18nResponse(
        Long id,
        String uuid,
        String uid,
        Language language,
        String name,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CategoryI18nResponse from(CategoryI18n entity) {
        if (entity == null) {
            throw new IllegalArgumentException("CategoryI18n entity cannot be null");
        }
        return new CategoryI18nResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getUid(),
                entity.getLanguage(),
                entity.getName(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
