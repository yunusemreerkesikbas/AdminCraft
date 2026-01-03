package com.backend.presentation.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating media focal point.
 * Focal point is used for smart cropping to keep important areas visible.
 */
public record FocalPointRequest(
    @NotNull(message = "validation.media.focal.x.required") @DecimalMin(value = "0.0", message = "validation.media.focal.x.min") @DecimalMax(value = "1.0", message = "validation.media.focal.x.max") Double x,

    @NotNull(message = "validation.media.focal.y.required") @DecimalMin(value = "0.0", message = "validation.media.focal.y.min") @DecimalMax(value = "1.0", message = "validation.media.focal.y.max") Double y) {
}
