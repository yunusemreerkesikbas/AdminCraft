package com.backend.presentation.dto.response;

import com.backend.domain.entity.ComponentI18n;
import com.backend.domain.enums.ComponentStatus;

public record ComponentI18nContentResponse(
    String title,
    String subtitle,
    String description,
    ComponentStatus status
) {
    public static ComponentI18nContentResponse from(ComponentI18n entity) {
        if (entity == null) {
            throw new IllegalArgumentException("ComponentI18n entity cannot be null");
        }
        return new ComponentI18nContentResponse(
            entity.getTitle(),
            entity.getSubtitle(),
            entity.getDescription(),
            entity.getStatus()
        );
    }
}
