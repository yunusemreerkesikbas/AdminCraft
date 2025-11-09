package com.backend.presentation.dto.response;

import com.backend.domain.entity.Component;
import com.backend.domain.enums.ComponentStatus;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;

public record ComponentResponse(
    Long id,
    String uuid,
    String uid,
    Long componentTypeId,
    String code,
    String name,
    JsonNode baseData,
    JsonNode extendedData,
    ComponentStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ComponentResponse from(Component entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Component entity cannot be null");
        }
        return new ComponentResponse(
            entity.getId(),
            entity.getUuid(),
            entity.getUid(),
            entity.getComponentTypeId(),
            entity.getCode(),
            entity.getName(),
            entity.getBaseData(),
            entity.getExtendedData(),
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
