package com.backend.presentation.dto.request;

import jakarta.validation.constraints.*;

public record CreatePageCategoryRequest(
        String uid,

        @Min(value = 1, message = "Parent ID must be positive") Long parentId,

        Boolean active,

        String styleClasses,

        @Min(value = 0, message = "Sort order cannot be negative") @Max(value = 9999, message = "Sort order cannot exceed 9999") Integer sortOrder) {

    public CreatePageCategoryRequest {
        if (uid != null) {
            uid = uid.trim();
            if (uid.contains("<") || uid.contains(">")) {
                throw new IllegalArgumentException("UID cannot contain HTML tags");
            }
        }

        if (active == null) {
            active = true;
        }

        if (sortOrder == null) {
            sortOrder = 0;
        }
    }
}
