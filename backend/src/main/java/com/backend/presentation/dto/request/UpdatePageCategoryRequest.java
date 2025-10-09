package com.backend.presentation.dto.request;

import jakarta.validation.constraints.*;

public record UpdatePageCategoryRequest(
        @Min(value = 1, message = "Parent ID must be positive") Long parentId,

        Boolean active,

        String styleClasses,

        @Min(value = 0, message = "Sort order cannot be negative") @Max(value = 9999, message = "Sort order cannot exceed 9999") Integer sortOrder) {

    public UpdatePageCategoryRequest {
        if (sortOrder == null) {
            sortOrder = 0;
        }
    }
}
