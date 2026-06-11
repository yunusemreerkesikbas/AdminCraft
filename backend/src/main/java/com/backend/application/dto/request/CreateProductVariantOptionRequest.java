package com.backend.application.dto.request;

import static com.backend.shared.constants.ValidationConstants.ATTRIBUTE_NAME_MAX_LENGTH;

import java.util.List;

import com.backend.domain.enums.ProductVariantOptionDisplayType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateProductVariantOptionRequest(
        @NotBlank(message = "{validation.field.name.required}") @Size(max = ATTRIBUTE_NAME_MAX_LENGTH) String name,
        @NotNull ProductVariantOptionDisplayType displayType,
        @Min(0) Integer sortOrder,
        Boolean active,
		@NotNull @Valid List<@NotNull ProductVariantOptionValueRequest> values) {
    public CreateProductVariantOptionRequest {
        name = name != null ? name.trim() : null;
        if (displayType == null) {
            displayType = ProductVariantOptionDisplayType.TEXT;
        }
        if (sortOrder == null) {
            sortOrder = 0;
        }
        if (active == null) {
            active = true;
        }
    }
}
