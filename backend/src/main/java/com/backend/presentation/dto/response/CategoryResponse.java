package com.backend.presentation.dto.response;

import com.backend.domain.entity.Category;
import com.backend.domain.entity.CategoryI18n;
import com.backend.domain.enums.Language;

import java.time.LocalDateTime;

public record CategoryResponse(
        Long id,
        String uuid,
        String uid,
        String code,
        Long parentId,
        Integer sortOrder,
        Boolean isVisible,
        String name,
        int childCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CategoryResponse from(Category entity, Language language) {
        if (entity == null) {
            throw new IllegalArgumentException("Category entity cannot be null");
        }
        String name = entity.getI18nContent() != null
                ? entity.getI18nContent().stream()
                    .filter(i -> i.getLanguage() == language)
                    .findFirst()
                    .map(CategoryI18n::getName)
                    .orElse(entity.getCode())
                : entity.getCode();
        int childCount = entity.getChildren() != null ? entity.getChildren().size() : 0;

        return new CategoryResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getUid(),
                entity.getCode(),
                entity.getParentId(),
                entity.getSortOrder(),
                entity.getIsVisible(),
                name,
                childCount,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static CategoryResponse from(Category entity) {
        return from(entity, Language.TR);
    }
}
