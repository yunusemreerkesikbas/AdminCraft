package com.backend.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

import com.backend.domain.entity.ComponentEntryI18n;
import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.Language;

public record ComponentEntryI18nResponse(
        Long id,
        String uuid,
        String uid,
        Long entryId,
        Language language,
        String title,
        String description,
        ComponentStatus status,
        LocalDateTime publishedAt,
        LocalDateTime updatedAt,
        Map<String, Object> customFields) {
    public static ComponentEntryI18nResponse from(ComponentEntryI18n entity, Map<String, Object> customFields) {
        return new ComponentEntryI18nResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getUid(),
                entity.getEntryId(),
                entity.getLanguage(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getPublishedAt(),
                entity.getUpdatedAt(),
                customFields);
    }
}
