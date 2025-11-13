package com.backend.presentation.dto.request;

import com.backend.domain.enums.ComponentStatus;

public record UpdateComponentEntryRequest(
    Integer sortOrder,
    Boolean isVisible,
    String styleClasses,
    ComponentStatus status
) {}



