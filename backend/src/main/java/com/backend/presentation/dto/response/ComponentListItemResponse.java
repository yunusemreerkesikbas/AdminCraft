package com.backend.presentation.dto.response;

import com.backend.domain.entity.Component;
import com.backend.domain.enums.ComponentStatus;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;

public record ComponentListItemResponse(
        Long id,
        String code,
        String name,
        Long componentTypeId,
        String componentTypeName,
        ComponentStatus status,
        Integer entryCount) {
    public static ComponentListItemResponse from(Component component, String typeName, Integer entryCount) {
        if (component == null) {
            throw new IllegalArgumentException("Component entity cannot be null");
        }
        return new ComponentListItemResponse(
                component.getId(),
                component.getCode(),
                component.getName(),
                component.getComponentTypeId(),
                typeName,
                component.getStatus(),
                entryCount != null ? entryCount : 0);
    }
}
