package com.backend.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.backend.domain.entity.Page;
import com.backend.domain.entity.PageI18n;
import com.backend.domain.enums.PageStatus;
import com.backend.domain.enums.RobotTag;

public record PageListResponse(
        Long id,
        String uuid,
        String uid,
        Long templateId,
        PageStatus status,
        String styleClasses,
        RobotTag robotTag,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Map<String, Boolean> translations) {

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
                                (existing, replacement) -> replacement));

        return new PageListResponse(
                page.getId(),
                page.getUuid(),
                page.getUid(),
                page.getTemplateId(),
                page.getStatus(),
                page.getStyleClasses(),
                page.getRobotTag(),
                page.getCreatedAt(),
                page.getUpdatedAt(),
                Map.copyOf(translationMap));
    }
}
