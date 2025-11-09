package com.backend.presentation.dto.response;

import com.backend.domain.entity.ComponentI18n;
import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.Language;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;

public record ComponentI18nResponse(
    Long id,
    String uuid,
    String uid,
    Long componentId,
    Language language,
    JsonNode baseLocalizedData,
    JsonNode extendedLocalizedData,
    ComponentStatus status,
    LocalDateTime publishedAt,
    LocalDateTime updatedAt
) {
    public static ComponentI18nResponse from(ComponentI18n entity) {
        if (entity == null) {
            throw new IllegalArgumentException("ComponentI18n entity cannot be null");
        }
        return new ComponentI18nResponse(
            entity.getId(),
            entity.getUuid(),
            entity.getUid(),
            entity.getComponentId(),
            entity.getLanguage(),
            entity.getBaseLocalizedData(),
            entity.getExtendedLocalizedData(),
            entity.getStatus(),
            entity.getPublishedAt(),
            entity.getUpdatedAt()
        );
    }
}
