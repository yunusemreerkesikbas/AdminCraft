package com.backend.presentation.dto.response;

import com.backend.domain.entity.Component;
import com.backend.domain.enums.ComponentStatus;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;

public record ComponentListItemResponse(
    Long id,
    String uuid,
    String uid,
    Long componentTypeId,
    String componentTypeName,
    String code,
    String name,
    JsonNode baseData,
    ComponentStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ComponentListItemResponse from(Component component, String typeName) {
        if (component == null) {
            throw new IllegalArgumentException("Component entity cannot be null");
        }
        return new ComponentListItemResponse(
            component.getId(),
            component.getUuid(),
            component.getUid(),
            component.getComponentTypeId(),
            typeName,
            component.getCode(),
            component.getName(),
            component.getBaseData(),
            component.getStatus(),
            component.getCreatedAt(),
            component.getUpdatedAt()
        );
    }
}

