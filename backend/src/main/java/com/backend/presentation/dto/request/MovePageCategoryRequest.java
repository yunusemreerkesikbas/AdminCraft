package com.backend.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

public record MovePageCategoryRequest(
    @NotNull Long tenantId,
    @NotNull Long categoryId,
    Long newParentId) {
}
