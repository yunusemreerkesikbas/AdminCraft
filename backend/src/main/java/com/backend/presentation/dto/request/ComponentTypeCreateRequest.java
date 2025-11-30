package com.backend.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComponentTypeCreateRequest(
        @NotBlank(message = "validation.component.type.name.required") @Size(max = 100, message = "validation.component.type.name.size") String name,

        @Size(max = 50, message = "validation.component.type.category.size") String category) {
    public ComponentTypeCreateRequest {
        if (name != null) {
            name = name.trim();
        }
        if (category != null) {
            category = category.trim();
        }
    }
}
