package com.backend.presentation.dto.request;

import com.backend.domain.enums.PageStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PageCreateRequest(

        @NotNull(message = "validation.tenant.id.required") Long tenantId,

        Long categoryId,

        PageStatus status, // defaults to DRAFT in entity if null

        @Size(max = 500, message = "validation.featured.image.size") String featuredImage,

        @Size(max = 255, message = "validation.style.classes.size") String styleClasses,

        Boolean isHome, // defaults to false in entity if null

        Integer sortOrder // defaults to 0 in entity if null
) {

    public PageCreateRequest {
        // Trim featured image if provided
        if (featuredImage != null) {
            featuredImage = featuredImage.trim();
        }

        // Trim style classes if provided
        if (styleClasses != null) {
            styleClasses = styleClasses.trim();
        }
    }
}
