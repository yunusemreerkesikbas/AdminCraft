package com.backend.presentation.dto.request;

import com.backend.domain.enums.ComponentStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ComponentCreateRequest(
        @NotNull(message = "validation.component.type.id.required") Long componentTypeId,

        @NotBlank(message = "validation.component.name.required") @Size(max = 100, message = "validation.component.name.size") String name,

        Integer displayOrder,

        Boolean isVisible,

        @Size(max = 500) String styleClasses,

        ComponentStatus status) {
    public ComponentCreateRequest {
        if (name != null) {
            name = name.trim();
        }
    }
}
