package com.backend.presentation.dto.response;

import com.backend.domain.enums.Language;
import com.backend.domain.enums.PageStatus;

import java.time.LocalDateTime;

public record PageResponse(
    Long id,
    Long tenantId,
    String title,
    String slug,
    PageStatus status,
    Language language,
    Long categoryId,
    String metaTitle,
    String metaDescription,
    String canonicalUrl,
    String subtitle,
    String styleClasses,
    String description,
    String descriptionHtml,
    String featuredImage,
    LocalDateTime publishedAt,
    LocalDateTime scheduledAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {
}
