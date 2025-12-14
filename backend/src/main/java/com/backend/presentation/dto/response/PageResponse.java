package com.backend.presentation.dto.response;

import java.time.LocalDateTime;

import com.backend.domain.entity.Page;
import com.backend.domain.enums.PageStatus;

public record PageResponse(
        Long id,
        String uuid,
        String uid,
        Long templateId,
        PageStatus status,
        String featuredImage,
        String styleClasses,
        Integer sortOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static PageResponse from(Page page) {
        if (page == null) {
            throw new IllegalArgumentException("Page entity cannot be null");
        }

        return new PageResponse(
                page.getId(),
                page.getUuid(),
                page.getUid(),
                page.getTemplateId(),
                page.getStatus(),
                page.getFeaturedImage(),
                page.getStyleClasses(),
                page.getSortOrder(),
                page.getCreatedAt(),
                page.getUpdatedAt());
    }

}
