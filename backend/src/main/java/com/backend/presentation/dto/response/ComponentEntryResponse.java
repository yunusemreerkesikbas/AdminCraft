package com.backend.presentation.dto.response;

import com.backend.domain.entity.ComponentEntry;
import com.backend.domain.enums.ComponentStatus;

import java.time.LocalDateTime;

public record ComponentEntryResponse(
    Long id,
    String uuid,
    String uid,
    Long componentId,
    Integer sortOrder,
    Boolean isVisible,
    String styleClasses,
    ComponentStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ComponentEntryResponse from(ComponentEntry entity) {
        return new ComponentEntryResponse(
            entity.getId(),
            entity.getUuid(),
            entity.getUid(),
            entity.getComponentId(),
            entity.getSortOrder(),
            entity.getIsVisible(),
            entity.getStyleClasses(),
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}



