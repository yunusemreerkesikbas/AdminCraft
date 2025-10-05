package com.backend.presentation.dto.response;

import com.backend.domain.entity.Page;
import com.backend.domain.entity.PageI18n;
import com.backend.domain.enums.PageStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Detailed page response with all translations.
 * Used when client needs full page data including all language versions.
 */
public record PageDetailResponse(
        Long id,
        String uuid,
        String uid,
        Long tenantId,
        Long categoryId,
        PageStatus status,
        String featuredImage,
        String styleClasses,
        Boolean isHome,
        Integer sortOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Map<String, PageI18nResponse> translations,
        Metadata metadata) {

    public record Metadata(int translationCount, int publishedTranslationCount) {
    }

    public static PageDetailResponse from(Page page, List<PageI18n> pageI18nList) {
        if (page == null) {
            throw new IllegalArgumentException("Page entity cannot be null");
        }

        Map<String, PageI18nResponse> translationsMap = pageI18nList == null || pageI18nList.isEmpty()
                ? new HashMap<>()
                : pageI18nList.stream()
                        .collect(Collectors.toMap(
                                i18n -> i18n.getLanguage().getCode().toUpperCase(),
                                PageI18nResponse::from,
                                (existing, replacement) -> replacement));

        int translationCount = translationsMap.size();
        int publishedCount = (int) translationsMap.values().stream()
                .filter(i18n -> i18n.status() == PageStatus.PUBLISHED)
                .count();

        return new PageDetailResponse(
                page.getId(),
                page.getUuid(),
                page.getUid(),
                page.getTenantId(),
                page.getCategoryId(),
                page.getStatus(),
                page.getFeaturedImage(),
                page.getStyleClasses(),
                page.getIsHome(),
                page.getSortOrder(),
                page.getCreatedAt(),
                page.getUpdatedAt(),
                Map.copyOf(translationsMap),
                new Metadata(translationCount, publishedCount));
    }
}
