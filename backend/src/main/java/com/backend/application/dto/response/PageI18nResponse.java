package com.backend.application.dto.response;

import java.time.LocalDateTime;

import com.backend.domain.entity.PageI18n;

public record PageI18nResponse(
        Long id,
        String uuid,
        String uid,
        String name,
        String title,
        String canonicalUrl,
        String description,
        LocalDateTime updatedAt) {

    public static PageI18nResponse from(PageI18n pageI18n) {
        if (pageI18n == null) {
            throw new IllegalArgumentException("PageI18n entity cannot be null");
        }

        return new PageI18nResponse(
                pageI18n.getId(),
                pageI18n.getUuid(),
                pageI18n.getUid(),
                pageI18n.getName(),
                pageI18n.getTitle(),
                pageI18n.getCanonicalUrl(),
                pageI18n.getDescription(),
                pageI18n.getUpdatedAt());
    }
}
