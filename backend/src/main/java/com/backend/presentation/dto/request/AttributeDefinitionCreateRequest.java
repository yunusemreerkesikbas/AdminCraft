package com.backend.presentation.dto.request;

import static com.backend.shared.constants.ValidationConstants.ATTRIBUTE_NAME_MAX_LENGTH;

import com.backend.domain.enums.ProductFieldType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AttributeDefinitionCreateRequest(
        @NotBlank(message = "validation.attribute.name.required") @Size(max = ATTRIBUTE_NAME_MAX_LENGTH, message = "validation.attribute.name.size") String name,

        @NotNull(message = "validation.attribute.fieldType.required") ProductFieldType fieldType) {
    public AttributeDefinitionCreateRequest {
        name = name != null ? name.trim() : null;
    }
}
