package com.backend.presentation.dto.response;

import java.time.LocalDateTime;

public record ContentTypeResponse(
    Long id,
    String name,
    String displayName,
    String fields,
    Long tenantId,
    Boolean supportsMultiLanguage,
    Long contentCount,
    Long publishedContentCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}