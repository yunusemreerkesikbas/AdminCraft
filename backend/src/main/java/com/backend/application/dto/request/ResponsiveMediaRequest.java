package com.backend.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating/updating a ResponsiveMediaSet.
 * Both desktopMediaId and mobileMediaId are nullable to allow clearing media.
 */
public record ResponsiveMediaRequest(
        @NotBlank(message = "validation.responsive.media.code.required") @Size(max = 100, message = "validation.responsive.media.code.size") String code,

        Long desktopMediaId,

        Long mobileMediaId) {
}
