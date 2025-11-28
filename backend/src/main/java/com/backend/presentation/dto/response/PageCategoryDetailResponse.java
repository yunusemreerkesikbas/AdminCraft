package com.backend.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

public record PageCategoryDetailResponse(
    Long id,
    String uuid,
    String uid,
    Long parentId,
    Boolean active,
    String styleClasses,
    Integer sortOrder,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Map<String, PageCategoryI18nResponse> translations,
    PageCategoryMetadataResponse metadata) {
}
