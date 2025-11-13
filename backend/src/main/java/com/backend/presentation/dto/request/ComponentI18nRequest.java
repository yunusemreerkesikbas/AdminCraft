package com.backend.presentation.dto.request;

import com.backend.domain.enums.ComponentStatus;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;

public record ComponentI18nRequest(
    @NotNull(message = "validation.component.i18n.data.required")
    JsonNode baseLocalizedData,

    ComponentStatus status
) {
}
