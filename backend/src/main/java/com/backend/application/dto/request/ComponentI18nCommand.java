package com.backend.application.dto.request;

import jakarta.validation.constraints.Size;

public record ComponentI18nCommand(
    @Size(max = 200, message = "Title must be at most 200 characters") String title,

    @Size(max = 200, message = "Subtitle must be at most 200 characters") String subtitle,

    @Size(max = 5000, message = "Description must be at most 5000 characters") String description) {
}
