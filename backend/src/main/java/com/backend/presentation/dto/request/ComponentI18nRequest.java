package com.backend.presentation.dto.request;

import com.backend.domain.enums.ComponentStatus;

public record ComponentI18nRequest(
    String title,
    String subtitle,
    String description,
    ComponentStatus status
) {
}
