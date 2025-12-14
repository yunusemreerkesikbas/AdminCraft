package com.backend.presentation.dto.request;

import com.backend.domain.enums.PageStatus;

import jakarta.validation.constraints.Size;

public record PageCreateRequest(
        Long categoryId,

        Long templateId,

        PageStatus status,

        @Size(max = 500, message = "validation.featured.image.size") String featuredImage,

        @Size(max = 255, message = "validation.style.classes.size") String styleClasses,

        Integer sortOrder) {

    public PageCreateRequest {
        if (featuredImage != null) {
            featuredImage = featuredImage.trim();
        }

        if (styleClasses != null) {
            styleClasses = styleClasses.trim();
        }
    }
}
