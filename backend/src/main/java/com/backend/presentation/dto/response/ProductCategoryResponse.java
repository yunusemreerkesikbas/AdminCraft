package com.backend.presentation.dto.response;

import com.backend.domain.entity.Category;
import com.backend.domain.entity.CategoryI18n;
import com.backend.domain.entity.ProductCategoryLink;
import com.backend.domain.enums.Language;

public record ProductCategoryResponse(
        Long id,
        String uid,
        String code,
        String name,
        Boolean isPrimary,
        Long parentId
) {
    public static ProductCategoryResponse from(ProductCategoryLink link, Language language) {
        if (link == null || link.getCategory() == null) {
            throw new IllegalArgumentException("ProductCategoryLink cannot be null");
        }
        Category category = link.getCategory();
        String name = category.getI18nContent() != null
                ? category.getI18nContent().stream()
                    .filter(i -> i.getLanguage() == language)
                    .findFirst()
                    .map(CategoryI18n::getName)
                    .orElse(category.getCode())
                : category.getCode();

        return new ProductCategoryResponse(
                category.getId(),
                category.getUid(),
                category.getCode(),
                name,
                link.getIsPrimary(),
                category.getParentId()
        );
    }

    public static ProductCategoryResponse from(ProductCategoryLink link) {
        return from(link, Language.TR);
    }
}
