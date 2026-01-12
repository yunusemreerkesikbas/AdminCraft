package com.backend.application.dto.delivery;

import com.backend.domain.entity.Category;
import com.backend.domain.entity.CategoryI18n;
import com.backend.domain.enums.Language;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CategoryDeliveryResponse {
    private String uid;
    private String code;
    private String name;
    private String description;
    private List<CategoryDeliveryResponse> children;

    public static CategoryDeliveryResponse from(Category category, Language language) {
        if (category == null) return null;

        String name = null, description = null;
        if (category.getI18nContent() != null) {
            CategoryI18n i18n = category.getI18nContent().stream()
                    .filter(i -> i.getLanguage() == language)
                    .findFirst()
                    .orElse(null);
            if (i18n != null) {
                name = i18n.getName();
                description = i18n.getDescription();
            }
        }

        List<CategoryDeliveryResponse> children = category.getChildren() != null
                ? category.getChildren().stream()
                    .filter(c -> c.getIsVisible() != null && c.getIsVisible())
                    .map(c -> CategoryDeliveryResponse.from(c, language))
                    .toList()
                : List.of();

        return CategoryDeliveryResponse.builder()
                .uid(category.getUid())
                .code(category.getCode())
                .name(name != null ? name : category.getCode())
                .description(description)
                .children(children)
                .build();
    }
}
