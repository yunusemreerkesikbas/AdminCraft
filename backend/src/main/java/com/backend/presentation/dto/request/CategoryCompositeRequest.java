package com.backend.presentation.dto.request;

import com.backend.domain.enums.Language;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record CategoryCompositeRequest(
        @NotBlank(message = "validation.category.code.required")
        @Size(max = 50, message = "validation.category.code.size")
        @Pattern(regexp = "^[a-z][a-z0-9_-]*$", message = "validation.category.code.pattern")
        String code,

        Long parentId,

        Integer sortOrder,

        Boolean isVisible,

        @NotEmpty(message = "validation.category.translations.required")
        @Valid
        Map<Language, CategoryI18nRequest> translations
) {
    public CategoryCompositeRequest {
        if (sortOrder == null) sortOrder = 0;
        if (isVisible == null) isVisible = true;
    }
}
