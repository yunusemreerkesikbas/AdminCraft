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
        Integer order,
        Boolean isVisible,
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

        Integer order = 0;
        Boolean isVisible = true;

        if (component.getBaseData() != null) {
            if (component.getBaseData().has("order")) {
                order = component.getBaseData().get("order").asInt(0);
            }
            if (component.getBaseData().has("isVisible")) {
                isVisible = component.getBaseData().get("isVisible").asBoolean(true);
            }
        }

        return new ComponentDetailResponse(
                component.getId(),
                component.getUuid(),
                component.getUid(),
                component.getName(),
                component.getComponentTypeId(),
                componentTypeName,
                order,
                isVisible,
                component.getStatus(),
                component.getCreatedAt(),
                component.getUpdatedAt(),
                translations,
                metadata);
    }
}
