package com.backend.presentation.dto.response;

import com.backend.domain.entity.Component;
import com.backend.domain.enums.ComponentStatus;

public record ComponentListItemResponse(
        Long id,
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
                component.getName(),
                component.getComponentTypeId(),
                typeName,
                component.getStatus(),
                entryCount != null ? entryCount : 0);
    }
}
