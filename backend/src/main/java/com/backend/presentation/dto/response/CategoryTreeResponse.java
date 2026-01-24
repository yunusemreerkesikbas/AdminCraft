package com.backend.presentation.dto.response;

import com.backend.domain.entity.Category;
import com.backend.domain.entity.CategoryI18n;
import com.backend.domain.enums.Language;
import com.backend.shared.util.ResponseValueFilter;

import java.util.List;

public record CategoryTreeResponse(
        Long id,
        String uuid,
        String uid,
        String code,
        String name,
        Boolean isVisible,
        Integer sortOrder,
        List<CategoryTreeResponse> children
) {
    public static CategoryTreeResponse from(Category entity, Language language) {
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

        List<CategoryTreeResponse> childList = ResponseValueFilter.filterEmptyList(
                entity.getChildren() != null
                        ? entity.getChildren().stream()
                                .filter(c -> c.getIsVisible() != null && c.getIsVisible())
                                .map(c -> CategoryTreeResponse.from(c, language))
                                .toList()
                        : null);

        return new CategoryTreeResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getUid(),
                entity.getCode(),
                name,
                entity.getIsVisible(),
                entity.getSortOrder(),
                childList
        );
    }

    public static CategoryTreeResponse fromAll(Category entity, Language language) {
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

        List<CategoryTreeResponse> childList = ResponseValueFilter.filterEmptyList(
                entity.getChildren() != null
                        ? entity.getChildren().stream()
                                .map(c -> CategoryTreeResponse.fromAll(c, language))
                                .toList()
                        : null);

        return new CategoryTreeResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getUid(),
                entity.getCode(),
                name,
                entity.getIsVisible(),
                entity.getSortOrder(),
                childList
        );
    }
}
