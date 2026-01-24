package com.backend.presentation.dto.request;

import com.backend.domain.enums.ProductFieldType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AttributeDefinitionUpdateRequest(
        @NotBlank(message = "validation.attribute.name.required")
        @Size(max = 100, message = "validation.attribute.name.size")
        String name,

        @NotNull(message = "validation.attribute.fieldType.required")
        ProductFieldType fieldType
) {
}
