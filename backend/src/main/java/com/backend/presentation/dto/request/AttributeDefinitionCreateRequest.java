package com.backend.presentation.dto.request;

import com.backend.domain.enums.ProductFieldType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AttributeDefinitionCreateRequest(
        @NotBlank(message = "validation.attribute.code.required")
        @Size(max = 50, message = "validation.attribute.code.size")
        @Pattern(regexp = "^[a-z][a-z0-9_]*$", message = "validation.attribute.code.pattern")
        String code,

        @NotBlank(message = "validation.attribute.name.required")
        @Size(max = 100, message = "validation.attribute.name.size")
        String name,

        @NotNull(message = "validation.attribute.fieldType.required")
        ProductFieldType fieldType,

        Boolean isRequired,

        Boolean isSearchable,

        Integer sortOrder,

        String validationConfig
) {
    public AttributeDefinitionCreateRequest {
        if (isRequired == null) isRequired = false;
        if (isSearchable == null) isSearchable = false;
        if (sortOrder == null) sortOrder = 0;
    }
}
