package com.backend.presentation.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComponentTypeCreateRequest(
    @NotBlank(message = "validation.component.type.code.required")
    @Size(max = 50, message = "validation.component.type.code.size")
    String code,

    @NotBlank(message = "validation.component.type.name.required")
    @Size(max = 100, message = "validation.component.type.name.size")
    String name,

    @Size(max = 50, message = "validation.component.type.category.size")
    String category,

    @Size(max = 50, message = "validation.component.type.icon.size")
    String icon,

    JsonNode extendedFieldsSchema
) {
    public ComponentTypeCreateRequest {
        if (code != null) {
            code = code.trim();
        }
        if (name != null) {
            name = name.trim();
        }
        if (category != null) {
            category = category.trim();
        }
        if (icon != null) {
            icon = icon.trim();
        }
    }
}
