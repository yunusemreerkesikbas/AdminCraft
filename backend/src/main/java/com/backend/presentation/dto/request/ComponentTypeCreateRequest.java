package com.backend.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static com.backend.shared.constants.ValidationConstants.COMPONENT_TYPE_CATEGORY_MAX_LENGTH;
import static com.backend.shared.constants.ValidationConstants.COMPONENT_TYPE_NAME_MAX_LENGTH;

public record ComponentTypeCreateRequest(
        @NotBlank(message = "{validation.component.type.name.required}")
        @Size(max = COMPONENT_TYPE_NAME_MAX_LENGTH, message = "{validation.component.type.name.size}")
        String name,

        @Size(max = COMPONENT_TYPE_CATEGORY_MAX_LENGTH, message = "{validation.component.type.category.size}")
        String category,

        boolean navigationAware) {
    public ComponentTypeCreateRequest {
        if (name != null) {
            name = name.trim();
        }
        if (category != null) {
            category = category.trim();
        }
    }
}
