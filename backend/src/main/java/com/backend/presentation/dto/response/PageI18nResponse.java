package com.backend.presentation.dto.response;

import java.time.LocalDateTime;

import com.backend.domain.entity.PageI18n;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.PageStatus;

public record PageI18nResponse(
        Long id,
        String uuid,
        String uid,
        Long pageId,
        Language language,
        String urlPath,
        String title,
        String subtitle,
        String metaTitle,
        String metaDescription,
        String description,
        String descriptionHtml,
        PageStatus status,
        LocalDateTime publishedAt,
        LocalDateTime scheduledAt,
        LocalDateTime updatedAt,
        Boolean fallbackLanguage) {
    public static PageI18nResponse from(PageI18n pageI18n, boolean isFallback) {
        if (pageI18n == null) {
            throw new IllegalArgumentException("PageI18n entity cannot be null");
        }

        return new PageI18nResponse(
                pageI18n.getId(),
                pageI18n.getUuid(),
                pageI18n.getUid(),
                pageI18n.getPageId(),
                pageI18n.getLanguage(),
                pageI18n.getUrlPath(),
                pageI18n.getTitle(),
                pageI18n.getSubtitle(),
                pageI18n.getMetaTitle(),
                pageI18n.getMetaDescription(),
                pageI18n.getDescription(),
                pageI18n.getDescriptionHtml(),
                pageI18n.getStatus(),
                pageI18n.getPublishedAt(),
                pageI18n.getScheduledAt(),
                pageI18n.getUpdatedAt(),
                isFallback);
    }

    public static PageI18nResponse from(PageI18n pageI18n) {
        return from(pageI18n, false);
    }
}
