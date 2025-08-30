package com.backend.presentation.dto.response;

import com.backend.domain.enums.CategoryStatus;

public record PageCategoryDto(
    Long id,
    Long tenantId,
    Long parentId,
    String name,
    String slug,
    String path,
    Integer level,
    Integer sortOrder,
    CategoryStatus status) {
}
