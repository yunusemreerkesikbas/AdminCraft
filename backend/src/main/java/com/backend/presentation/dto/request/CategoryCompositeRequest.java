package com.backend.presentation.dto.request;

import static com.backend.shared.constants.ValidationConstants.CATEGORY_CODE_MAX_LENGTH;

import java.util.Map;

import com.backend.domain.enums.Language;
import com.backend.shared.validation.CategoryCode;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record CategoryCompositeRequest(
        @CategoryCode(maxLength = CATEGORY_CODE_MAX_LENGTH) String code,

        Long parentId,

        Integer sortOrder,

        Boolean isVisible,

        @NotEmpty(message = "validation.category.translations.required") @Valid Map<Language, CategoryI18nRequest> translations) {
    public CategoryCompositeRequest {
        code = code != null ? code.trim() : null;
        if (sortOrder == null)
            sortOrder = 0;
        if (isVisible == null)
            isVisible = true;
    }
}
