package com.backend.presentation.dto.request;

import static com.backend.shared.constants.ValidationConstants.ATTRIBUTE_CODE_MAX_LENGTH;
import static com.backend.shared.constants.ValidationConstants.ATTRIBUTE_NAME_MAX_LENGTH;

import com.backend.domain.enums.ProductFieldType;
import com.backend.shared.validation.Code;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AttributeDefinitionCreateRequest(
        @Code(maxLength = ATTRIBUTE_CODE_MAX_LENGTH) String code,

        @NotBlank(message = "validation.attribute.name.required") @Size(max = ATTRIBUTE_NAME_MAX_LENGTH, message = "validation.attribute.name.size") String name,

        @NotNull(message = "validation.attribute.fieldType.required") ProductFieldType fieldType,

        Boolean isRequired,

        Boolean isSearchable,

        Integer sortOrder,

        String validationConfig) {
    public AttributeDefinitionCreateRequest {
        code = code != null ? code.trim() : null;
        name = name != null ? name.trim() : null;
        if (isRequired == null)
            isRequired = false;
        if (isSearchable == null)
            isSearchable = false;
        if (sortOrder == null)
            sortOrder = 0;
    }
}
