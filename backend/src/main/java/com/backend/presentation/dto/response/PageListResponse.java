package com.backend.presentation.dto.response;

import com.backend.domain.entity.Page;
import com.backend.domain.entity.PageI18n;
import com.backend.domain.enums.PageStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Lightweight DTO for page listing with translation summary.
 * The translations map contains language codes as keys and publication status as values.
 * Example: {"TR": true, "EN": false} means TR translation is published, EN is not.
 */
public record PageListResponse(
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
    Map<String, Boolean> translations) {

    /**
     * Creates PageListResponse from Page entity and its translations.
     * @param page The page entity
     * @param pageI18nList List of translations for this page
     * @return PageListResponse with translation publication status
     */
    public static PageListResponse from(Page page, List<PageI18n> pageI18nList) {
        if (page == null) {
            throw new IllegalArgumentException("Page entity cannot be null");
        }

        Map<String, Boolean> translationMap = pageI18nList == null ? Map.of()
            : pageI18nList.stream()
                .filter(i18n -> i18n.getLanguage() != null)
                .collect(Collectors.toMap(
                    i18n -> i18n.getLanguage().getCode().toUpperCase(),
                    i18n -> i18n.getStatus() == PageStatus.PUBLISHED,
                    (existing, replacement) -> replacement
                ));

        return new PageListResponse(
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
            Map.copyOf(translationMap)
        );
    }
}
