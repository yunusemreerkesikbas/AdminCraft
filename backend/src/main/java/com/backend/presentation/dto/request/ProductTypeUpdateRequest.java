package com.backend.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductTypeUpdateRequest(
        @NotBlank(message = "validation.product.type.name.required")
        @Size(max = 100, message = "validation.product.type.name.size")
        String name,

        @Size(max = 50, message = "validation.product.type.category.size")
        String category
) {
}
