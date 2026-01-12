package com.backend.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProductTypeCreateRequest(
        @NotBlank(message = "validation.product.type.code.required")
        @Size(max = 50, message = "validation.product.type.code.size")
        @Pattern(regexp = "^[a-z][a-z0-9_]*$", message = "validation.product.type.code.pattern")
        String code,

        @NotBlank(message = "validation.product.type.name.required")
        @Size(max = 100, message = "validation.product.type.name.size")
        String name,

        @Size(max = 50, message = "validation.product.type.category.size")
        String category
) {
}
