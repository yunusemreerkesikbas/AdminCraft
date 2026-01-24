package com.backend.application.dto.request;

import com.backend.domain.enums.ProductFieldType;
import com.backend.shared.constants.ValidationConstants;
import com.backend.shared.validation.Uid;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new product field definition.
 * Code is automatically generated from name in the service layer.
 */
public record CreateProductFieldRequest(
        @Uid(maxLength = ValidationConstants.UID_MAX_LENGTH) String uid,

        @NotBlank(message = "{validation.field.name.required}") @Size(max = ValidationConstants.ATTRIBUTE_NAME_MAX_LENGTH, message = "{validation.field.name.maxLength}") String name,

        @NotNull(message = "{validation.field.fieldType.required}") ProductFieldType fieldType) {

    /**
     * Compact constructor for input sanitization.
     */
    public CreateProductFieldRequest {
        uid = uid != null ? uid.trim() : null;
        name = name != null ? name.trim() : null;
    }
}
