package com.backend.presentation.dto.response;

import com.backend.domain.entity.Component;
import com.backend.domain.enums.ComponentStatus;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ComponentDetailResponse(
        Long id,
        String uuid,
        String uid,
        String name,
        Long componentTypeId,
        String componentTypeName,
        JsonNode baseData,
        ComponentStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Map<String, ComponentI18nResponse> translations,
        Metadata metadata) {

    public record Metadata(int translationCount, int publishedTranslationCount) {}

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
                component.getBaseData(),
                component.getStatus(),
                component.getCreatedAt(),
                component.getUpdatedAt(),
                translations,
                metadata);
    }
}
