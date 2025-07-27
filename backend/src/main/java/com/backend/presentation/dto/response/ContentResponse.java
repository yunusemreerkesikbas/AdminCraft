package com.backend.presentation.dto.response;

import com.backend.domain.enums.ContentStatus;
import com.backend.domain.enums.Language;

import java.time.LocalDateTime;

public record ContentResponse(
    Long id,
    String title,
    String slug,
    String excerpt,
    String data,
    ContentStatus status,
    Language language,
    Long parentContentId,
    Long contentTypeId,
    String contentTypeName,
    Long tenantId,
    String metaTitle,
    String metaDescription,
    String metaKeywords,
    Boolean isFeatured,
    Boolean isSticky,
    Boolean requiresLogin,
    Long viewCount,
    Long likeCount,
    Long commentCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime publishedAt,
    LocalDateTime scheduledAt,
    LocalDateTime expiresAt,
    Long authorId,
    String authorName,
    Long createdBy,
    Long updatedBy,
    Long publishedBy
) {}