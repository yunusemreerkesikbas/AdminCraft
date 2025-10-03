package com.backend.presentation.dto.response;

import com.backend.domain.entity.PageI18n;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.PageStatus;
import java.time.LocalDateTime;

public record PageI18nResponse(
        Long id,
        String uuid,
        String uid,
        Long pageId,
        Long tenantId,
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
        Boolean isFallbackLanguage // indicates if this is fallback content
) {
    public static PageI18nResponse from(PageI18n pageI18n, boolean isFallback) {
        if (pageI18n == null) {
            throw new IllegalArgumentException("PageI18n entity cannot be null");
        }

        return new PageI18nResponse(
                pageI18n.getId(),
                pageI18n.getUuid(),
                pageI18n.getUid(),
                pageI18n.getPageId(),
                pageI18n.getTenantId(),
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

    public boolean isPublished() {
        return status == PageStatus.PUBLISHED;
    }

    public boolean isScheduled() {
        return status == PageStatus.SCHEDULED;
    }

    public boolean isDraft() {
        return status == PageStatus.DRAFT;
    }

    public boolean isArchived() {
        return status == PageStatus.ARCHIVED;
    }

    public boolean isFallback() {
        return Boolean.TRUE.equals(isFallbackLanguage);
    }

    public String getEffectiveMetaTitle() {
        return (metaTitle != null && !metaTitle.trim().isEmpty())
                ? metaTitle
                : title;
    }

    public boolean hasCustomMetaTitle() {
        return metaTitle != null &&
                !metaTitle.trim().isEmpty() &&
                !metaTitle.equals(title);
    }

    public boolean hasUrlPath() {
        return urlPath != null && !urlPath.trim().isEmpty();
    }

    public boolean hasTitle() {
        return title != null && !title.trim().isEmpty();
    }

    public boolean isReadyForPublication() {
        return hasTitle() && hasUrlPath();
    }
}
