package com.backend.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

public record PageCategoryListResponse(
    Long id,
    String uuid,
    String uid,
    Long parentId,
    Boolean active,
    String styleClasses,
    Integer sortOrder,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Map<String, Boolean> translations) {
}
