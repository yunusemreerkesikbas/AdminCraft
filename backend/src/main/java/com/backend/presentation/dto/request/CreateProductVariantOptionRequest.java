package com.backend.presentation.dto.request;

import static com.backend.shared.constants.ValidationConstants.ATTRIBUTE_NAME_MAX_LENGTH;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateProductVariantOptionRequest(
        @NotBlank(message = "{validation.field.name.required}") @Size(max = ATTRIBUTE_NAME_MAX_LENGTH) String name,
        @NotBlank(message = "{validation.product.variant.option.displayType.required}") String displayType,
        @Min(0) Integer sortOrder,
        Boolean active,
        @NotNull @Valid List<@NotNull ProductVariantOptionValueRequest> values) {
    public CreateProductVariantOptionRequest {
        name = name != null ? name.trim() : null;
        displayType = displayType != null ? displayType.trim() : null;
    }
}

