package com.backend.application.dto.request;

import java.util.Map;

import com.backend.domain.enums.ProductFieldType;
import com.backend.presentation.validation.Code;
import com.backend.presentation.validation.Uid;
import com.backend.shared.constants.ValidationConstants;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new product field definition.
 * Uses project validation annotations for consistent pattern enforcement.
 */
public record CreateProductFieldRequest(
    @Uid(maxLength = ValidationConstants.CODE_MAX_LENGTH)
    String uid,

    @Code(maxLength = ValidationConstants.CODE_MAX_LENGTH)
    String code,

    @NotBlank(message = "{validation.field.name.required}")
    @Size(max = ValidationConstants.ATTRIBUTE_NAME_MAX_LENGTH, message = "{validation.field.name.maxLength}")
    String name,

    @NotNull(message = "{validation.field.fieldType.required}")
    ProductFieldType fieldType,

    Boolean isRequired,
    Boolean isVisibleInList,
    Integer sortOrder,
    String defaultValue,
    Map<String, Object> validationConfig) {

    /**
     * Compact constructor for input sanitization.
     */
    public CreateProductFieldRequest {
        uid = uid != null ? uid.trim() : null;
        code = code != null ? code.trim() : null;
        name = name != null ? name.trim() : null;
    }
}
