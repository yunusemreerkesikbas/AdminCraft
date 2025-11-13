package com.backend.presentation.dto.request;

import com.backend.domain.enums.ComponentStatus;
import jakarta.validation.constraints.NotNull;

public record CreateComponentEntryRequest(
    @NotNull(message = "validation.component.id.required")
    Long componentId,

    Integer sortOrder,
    Boolean isVisible,
    String styleClasses,
    ComponentStatus status
) {}



