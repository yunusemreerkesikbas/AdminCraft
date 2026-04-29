package com.backend.application.dto.request;

import jakarta.validation.constraints.Size;

public record ComponentI18nCommand(
    @Size(max = 200, message = "{validation.component.title.size}") String title,

    @Size(max = 200, message = "{validation.component.subtitle.size}") String subtitle,

    @Size(max = 5000, message = "{validation.component.description.size}") String description) {
}
