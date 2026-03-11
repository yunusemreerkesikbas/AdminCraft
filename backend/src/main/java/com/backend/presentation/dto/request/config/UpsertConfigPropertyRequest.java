package com.backend.presentation.dto.request.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpsertConfigPropertyRequest(
        String value,

        boolean secret,

        @NotBlank(message = "validation.reason.required")
        @Size(min = 5, max = 500, message = "validation.reason.size")
        String reason
) {
}
