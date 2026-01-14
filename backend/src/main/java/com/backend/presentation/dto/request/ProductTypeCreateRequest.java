package com.backend.presentation.dto.request;

import com.backend.presentation.validation.Code;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static com.backend.shared.constants.ValidationConstants.PRODUCT_TYPE_CATEGORY_MAX_LENGTH;
import static com.backend.shared.constants.ValidationConstants.PRODUCT_TYPE_CODE_MAX_LENGTH;
import static com.backend.shared.constants.ValidationConstants.PRODUCT_TYPE_NAME_MAX_LENGTH;

public record ProductTypeCreateRequest(
        @Code(maxLength = PRODUCT_TYPE_CODE_MAX_LENGTH)
        String code,

        @NotBlank(message = "validation.product.type.name.required")
        @Size(max = PRODUCT_TYPE_NAME_MAX_LENGTH, message = "validation.product.type.name.size")
        String name,

        @Size(max = PRODUCT_TYPE_CATEGORY_MAX_LENGTH, message = "validation.product.type.category.size")
        String category
) {
    public ProductTypeCreateRequest {
        code = code != null ? code.trim() : null;
        name = name != null ? name.trim() : null;
        category = category != null ? category.trim() : null;
    }
}
