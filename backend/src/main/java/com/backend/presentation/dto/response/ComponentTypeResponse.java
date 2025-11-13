package com.backend.presentation.dto.response;

import com.backend.domain.entity.ComponentType;
import java.time.LocalDateTime;

public record ComponentTypeResponse(
    Long id,
    String uuid,
    String uid,
    String code,
    String name,
    String category,
    String icon,
    Boolean isSystem,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ComponentTypeResponse from(ComponentType entity) {
        if (entity == null) {
            throw new IllegalArgumentException("ComponentType entity cannot be null");
        }
        return new ComponentTypeResponse(
            entity.getId(),
            entity.getUuid(),
            entity.getUid(),
            entity.getCode(),
            entity.getName(),
            entity.getCategory(),
            entity.getIcon(),
            entity.getIsSystem(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
