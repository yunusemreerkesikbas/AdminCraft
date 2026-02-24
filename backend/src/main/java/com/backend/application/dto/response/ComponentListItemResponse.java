package com.backend.application.dto.response;

import com.backend.domain.entity.Component;

public record ComponentListItemResponse(
        Long id,
        String uid,
        Long componentTypeId,
        String componentTypeName,
        Boolean isVisible,
        Integer entryCount) {
    public static ComponentListItemResponse from(Component component, String typeName, Integer entryCount) {
        if (component == null) {
            throw new IllegalArgumentException("Component entity cannot be null");
        }
        return new ComponentListItemResponse(
                component.getId(),
                component.getUid(),
                component.getComponentTypeId(),
                typeName,
                component.getIsVisible(),
                entryCount != null ? entryCount : 0);
    }
}
