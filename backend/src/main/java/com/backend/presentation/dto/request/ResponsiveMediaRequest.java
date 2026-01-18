package com.backend.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

public record ResponsiveMediaRequest(
    @NotNull(message = "validation.responsiveMedia.desktopMediaId.required") Long desktopMediaId,
    Long mobileMediaId) {
}
