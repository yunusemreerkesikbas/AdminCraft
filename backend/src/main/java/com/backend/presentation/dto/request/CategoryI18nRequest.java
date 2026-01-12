package com.backend.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryI18nRequest(
        @NotBlank(message = "validation.category.name.required")
        @Size(max = 200, message = "validation.category.name.size")
        String name,

        String description
) {
}
