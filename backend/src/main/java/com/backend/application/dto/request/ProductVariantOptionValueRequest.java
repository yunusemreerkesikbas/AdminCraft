package com.backend.application.dto.request;

import static com.backend.shared.constants.ValidationConstants.ATTRIBUTE_NAME_MAX_LENGTH;
import static com.backend.shared.constants.ValidationConstants.CODE_MAX_LENGTH;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductVariantOptionValueRequest(
        Long id,
        @NotBlank(message = "{validation.field.name.required}") @Size(max = ATTRIBUTE_NAME_MAX_LENGTH) String label,
        @Size(max = CODE_MAX_LENGTH) String swatchValue,
        @Min(0) Integer sortOrder,
        Boolean active) {
    public ProductVariantOptionValueRequest {
        label = label != null ? label.trim() : null;
        swatchValue = swatchValue != null ? swatchValue.trim() : null;
    }
}
