package com.backend.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating/updating a ResponsiveMediaSet.
 */
public record ResponsiveMediaRequest(
        @NotBlank(message = "validation.responsive.media.code.required") @Size(max = 100, message = "validation.responsive.media.code.size") String code,

        @Positive(message = "validation.responsive.media.desktop.positive") Long desktopMediaId,

        @Positive(message = "validation.responsive.media.mobile.positive") Long mobileMediaId) {
}
