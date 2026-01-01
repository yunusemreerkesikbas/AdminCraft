package com.backend.presentation.dto.request;

import jakarta.validation.constraints.Size;

/**
 * Request DTO for media i18n metadata (alt text, title, description).
 */
public record MediaI18nRequest(
    @Size(max = 500, message = "Alt text must be at most 500 characters") String altText,

    @Size(max = 255, message = "Title must be at most 255 characters") String title,

    @Size(max = 5000, message = "Description must be at most 5000 characters") String description) {
}
