package com.backend.presentation.dto.request;

import com.backend.domain.enums.CropMode;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a custom media format.
 * System formats cannot be created via API.
 */
public record MediaFormatCreateRequest(
    @NotBlank(message = "Format code is required") @Size(max = 50, message = "Code must be at most 50 characters") String code,

    @NotBlank(message = "Format name is required") @Size(max = 100, message = "Name must be at most 100 characters") String name,

    @NotNull(message = "Width is required") @Min(value = 1, message = "Width must be at least 1") @Max(value = 10000, message = "Width must be at most 10000") Integer width,

    @NotNull(message = "Height is required") @Min(value = 1, message = "Height must be at least 1") @Max(value = 10000, message = "Height must be at most 10000") Integer height,

    @Min(value = 1, message = "Quality must be at least 1") @Max(value = 100, message = "Quality must be at most 100") Integer quality, // default
                                                                                                                                        // 80

    CropMode cropMode // default FIT
) {
}
