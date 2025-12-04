package com.backend.presentation.dto.response;

import java.time.LocalDateTime;

import com.backend.domain.entity.Component;
import com.backend.domain.enums.ComponentStatus;

public record ComponentResponse(
        Long id,
        String uuid,
        String uid,
        Long componentTypeId,
        String name,
        Integer displayOrder,
        Boolean isVisible,
        String styleClasses,
        ComponentStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
    public static ComponentResponse from(Component entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Component entity cannot be null");
        }
        return new ComponentResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getUid(),
                entity.getComponentTypeId(),
                entity.getName(),
                entity.getDisplayOrder(),
                entity.getIsVisible(),
                entity.getStyleClasses(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
