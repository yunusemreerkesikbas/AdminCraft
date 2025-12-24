package com.backend.application.dto.request;

import com.backend.domain.enums.ComponentStatus;

import jakarta.validation.constraints.Size;

public record ComponentI18nCommand(
    @Size(max = 200, message = "Title must be at most 200 characters") String title,

    @Size(max = 200, message = "Subtitle must be at most 200 characters") String subtitle,

    String description,

    ComponentStatus status) {
}
