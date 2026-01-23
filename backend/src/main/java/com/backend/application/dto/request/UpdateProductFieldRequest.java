package com.backend.application.dto.request;

import java.util.Map;

import com.backend.domain.enums.ProductFieldType;
import com.backend.shared.constants.ValidationConstants;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating a product field definition.
 * Note: uid and code are immutable after creation.
 */
public record UpdateProductFieldRequest(
        @NotBlank(message = "{validation.field.name.required}") @Size(max = ValidationConstants.ATTRIBUTE_NAME_MAX_LENGTH, message = "{validation.field.name.maxLength}") String name,

        @NotNull(message = "{validation.field.fieldType.required}") ProductFieldType fieldType,

        @NotNull(message = "{validation.field.isRequired.required}") Boolean isRequired,

        @NotNull(message = "{validation.field.isVisibleInList.required}") Boolean isVisibleInList,

        @NotNull(message = "{validation.field.sortOrder.required}") @Min(value = 0, message = "{validation.sortOrder.min}") Integer sortOrder,

        String defaultValue,
        Map<String, Object> validationConfig) {

    /**
     * Compact constructor for input sanitization.
     */
    public UpdateProductFieldRequest {
        name = name != null ? name.trim() : null;
    }
}
