package com.backend.presentation.dto.response;

import com.backend.domain.enums.Language;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public record SiteResponse(
    Long id,
    String siteName,
    String description,
    Set<Language> enabledLanguages,
    Language defaultLanguage,
    Long tenantId,
    String tenantName,
    String domain,
    Boolean isActive,
    String theme,
    String logoUrl,
    String faviconUrl,
    String primaryColor,
    String secondaryColor,
    String fontFamily,
    String metaTitle,
    String metaDescription,
    String metaKeywords,
    String googleAnalyticsId,
    String customCode,
    Boolean isPublished,
    List<Object> menus,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime publishedAt
) {}