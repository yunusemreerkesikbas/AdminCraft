package com.backend.presentation.dto.response;

import java.time.LocalDateTime;

import com.backend.domain.entity.ComponentType;
import com.backend.domain.enums.ComponentNavigationProfile;

public record ComponentTypeResponse(
        Long id,
        String uuid,
        String uid,
        String name,
        String category,
        ComponentNavigationProfile navigationProfile,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
    public static ComponentTypeResponse from(ComponentType entity) {
        if (entity == null) {
            throw new IllegalArgumentException("ComponentType entity cannot be null");
        }
        return new ComponentTypeResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getUid(),
                entity.getName(),
                entity.getCategory(),
                entity.getNavigationProfile(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
