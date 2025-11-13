package com.backend.presentation.dto.request;

import com.backend.domain.enums.ComponentStatus;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ComponentCreateRequest(
    @NotNull(message = "validation.component.type.id.required")
    Long componentTypeId,

    @NotBlank(message = "validation.component.code.required")
    @Size(max = 50, message = "validation.component.code.size")
    String code,

    @NotBlank(message = "validation.component.name.required")
    @Size(max = 100, message = "validation.component.name.size")
    String name,

    JsonNode baseData,

    ComponentStatus status
) {
    public ComponentCreateRequest {
        if (code != null) {
            code = code.trim();
        }
        if (name != null) {
            name = name.trim();
        }
    }
}
