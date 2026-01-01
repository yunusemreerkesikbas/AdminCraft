package com.backend.presentation.dto.request;

import com.backend.domain.enums.CropMode;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating a custom media format.
 * System formats cannot be updated.
 */
public record MediaFormatUpdateRequest(
    @Size(max = 100, message = "Name must be at most 100 characters") String name,

    @Min(value = 1, message = "Width must be at least 1") @Max(value = 10000, message = "Width must be at most 10000") Integer width,

    @Min(value = 1, message = "Height must be at least 1") @Max(value = 10000, message = "Height must be at most 10000") Integer height,

    @Min(value = 1, message = "Quality must be at least 1") @Max(value = 100, message = "Quality must be at most 100") Integer quality,

    CropMode cropMode) {
}
