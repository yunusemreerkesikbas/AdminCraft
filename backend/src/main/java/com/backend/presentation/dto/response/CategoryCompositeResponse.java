package com.backend.presentation.dto.response;

import com.backend.domain.entity.Category;
import com.backend.domain.enums.Language;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

public record CategoryCompositeResponse(
        Long id,
        String uuid,
        String uid,
        String code,
        Long parentId,
        Integer sortOrder,
        Boolean isVisible,
        Map<Language, CategoryI18nResponse> translations,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CategoryCompositeResponse from(Category entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Category entity cannot be null");
        }
        Map<Language, CategoryI18nResponse> translations = entity.getI18nContent() != null
                ? entity.getI18nContent().stream()
                    .collect(Collectors.toMap(
                            i18n -> i18n.getLanguage(),
                            CategoryI18nResponse::from
                    ))
                : Map.of();

        return new CategoryCompositeResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getUid(),
                entity.getCode(),
                entity.getParentId(),
                entity.getSortOrder(),
                entity.getIsVisible(),
                translations,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
