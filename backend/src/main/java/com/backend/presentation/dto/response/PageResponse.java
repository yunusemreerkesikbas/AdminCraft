package com.backend.presentation.dto.response;

import com.backend.domain.entity.Page;
import com.backend.domain.enums.PageStatus;
import java.time.LocalDateTime;

public record PageResponse(
        Long id,
        String uuid,
        String uid,
        Long tenantId,
        Long categoryId,
        PageStatus status,
        String featuredImage,
        String styleClasses,
        Boolean isHome,
        Integer sortOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long createdBy,
        Long updatedBy) {

    public static PageResponse from(Page page) {
        if (page == null) {
            throw new IllegalArgumentException("Page entity cannot be null");
        }

        return new PageResponse(
                page.getId(),
                page.getUuid(),
                page.getUid(),
                page.getTenantId(),
                page.getCategoryId(),
                page.getStatus(),
                page.getFeaturedImage(),
                page.getStyleClasses(),
                page.getIsHome(),
                page.getSortOrder(),
                page.getCreatedAt(),
                page.getUpdatedAt(),
                page.getCreatedBy(),
                page.getUpdatedBy());
    }

    public boolean isHomePage() {
        return Boolean.TRUE.equals(isHome);
    }

    public boolean hasFeaturedImage() {
        return featuredImage != null && !featuredImage.trim().isEmpty();
    }

    public boolean hasStyleClasses() {
        return styleClasses != null && !styleClasses.trim().isEmpty();
    }

    public boolean hasCategory() {
        return categoryId != null;
    }
}
