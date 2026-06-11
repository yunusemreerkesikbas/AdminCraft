package com.backend.presentation.dto.request;

import static com.backend.shared.constants.ValidationConstants.ATTRIBUTE_NAME_MAX_LENGTH;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateProductVariantOptionRequest(
        @Size(max = ATTRIBUTE_NAME_MAX_LENGTH) String name,
        String displayType,
        @Min(0) Integer sortOrder,
        Boolean active,
        @Valid List<@NotNull ProductVariantOptionValueRequest> values) {
    public UpdateProductVariantOptionRequest {
        name = name != null ? name.trim() : null;
        displayType = displayType != null ? displayType.trim() : null;
    }
}

