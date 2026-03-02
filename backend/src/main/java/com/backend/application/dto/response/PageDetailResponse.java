package com.backend.application.dto.response;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.backend.domain.entity.Page;
import com.backend.domain.entity.PageI18n;
import com.backend.domain.enums.PageStatus;
import com.backend.domain.enums.RobotTag;

public record PageDetailResponse(
                Long id,
                String uuid,
                String uid,
                Long templateId,
                String templateUid,
                PageStatus status,
                String styleClasses,
                RobotTag robotTag,
                LocalDateTime publishedAt,
                LocalDateTime scheduledAt,
                LocalDateTime createdAt,
                LocalDateTime updatedAt,
                Map<String, PageI18nResponse> translations) {

        public static PageDetailResponse from(Page page, List<PageI18n> pageI18nList, String templateUid) {
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

                return new PageDetailResponse(
                                page.getId(),
                                page.getUuid(),
                                page.getUid(),
                                page.getTemplateId(),
                                templateUid,
                                page.getStatus(),
                                page.getStyleClasses(),
                                page.getRobotTag(),
                                page.getPublishedAt(),
                                page.getScheduledAt(),
                                page.getCreatedAt(),
                                page.getUpdatedAt(),
                                Map.copyOf(translationsMap));
        }
}
