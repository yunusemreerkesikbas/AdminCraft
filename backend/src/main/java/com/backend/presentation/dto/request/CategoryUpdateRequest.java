package com.backend.presentation.dto.request;

import com.backend.domain.enums.Language;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.Map;

public record CategoryUpdateRequest(
        Long parentId,

        Integer sortOrder,

        Boolean isVisible,

        @NotEmpty(message = "validation.category.translations.required")
        @Valid
        Map<Language, CategoryI18nRequest> translations
) {
}
