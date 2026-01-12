package com.backend.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductI18nRequest(
        @NotBlank(message = "validation.product.name.required")
        @Size(max = 200, message = "validation.product.name.size")
        String name,

        @Size(max = 500, message = "validation.product.shortDescription.size")
        String shortDescription,

        String description,

        @Size(max = 200, message = "validation.product.seoTitle.size")
        String seoTitle,

        @Size(max = 500, message = "validation.product.seoDescription.size")
        String seoDescription
) {
}
