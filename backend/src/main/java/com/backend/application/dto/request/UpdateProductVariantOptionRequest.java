package com.backend.application.dto.request;

import static com.backend.shared.constants.ValidationConstants.ATTRIBUTE_NAME_MAX_LENGTH;

import java.util.List;

import com.backend.domain.enums.ProductVariantOptionDisplayType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateProductVariantOptionRequest(
        @Size(max = ATTRIBUTE_NAME_MAX_LENGTH) String name,
        ProductVariantOptionDisplayType displayType,
        @Min(0) Integer sortOrder,
        Boolean active,
        @Valid List<ProductVariantOptionValueRequest> values) {
    public UpdateProductVariantOptionRequest {
        name = name != null ? name.trim() : null;
    }
}
