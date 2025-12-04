package com.backend.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

import com.backend.domain.entity.Component;
import com.backend.domain.enums.ComponentStatus;

public record ComponentDetailResponse(
        Long id,
        String uuid,
        String uid,
        String name,
        Long componentTypeId,
        String componentTypeName,
        Integer displayOrder,
        Boolean isVisible,
        String styleClasses,
        ComponentStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Map<String, ComponentI18nResponse> translations,
        Metadata metadata) {

    public record Metadata(int translationCount, int publishedTranslationCount) {
    }

    public static ComponentDetailResponse from(
            Component component,
            String componentTypeName,
            Map<String, ComponentI18nResponse> translations,
            Metadata metadata) {

        return new ComponentDetailResponse(
                component.getId(),
                component.getUuid(),
                component.getUid(),
                component.getName(),
                component.getComponentTypeId(),
                componentTypeName,
                component.getDisplayOrder(),
                component.getIsVisible(),
                component.getStyleClasses(),
                component.getStatus(),
                component.getCreatedAt(),
                component.getUpdatedAt(),
                translations,
                metadata);
    }
}
