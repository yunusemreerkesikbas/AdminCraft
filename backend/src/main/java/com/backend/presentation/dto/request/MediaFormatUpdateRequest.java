package com.backend.presentation.dto.request;

import com.backend.domain.enums.CropMode;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import static com.backend.shared.constants.ValidationConstants.MEDIA_FORMAT_DIMENSION_MAX;
import static com.backend.shared.constants.ValidationConstants.MEDIA_FORMAT_DIMENSION_MIN;
import static com.backend.shared.constants.ValidationConstants.MEDIA_FORMAT_NAME_MAX_LENGTH;
import static com.backend.shared.constants.ValidationConstants.MEDIA_FORMAT_QUALITY_MAX;
import static com.backend.shared.constants.ValidationConstants.MEDIA_FORMAT_QUALITY_MIN;

/**
 * Request DTO for updating a custom media format.
 * System formats cannot be updated.
 */
public record MediaFormatUpdateRequest(
    @Size(max = MEDIA_FORMAT_NAME_MAX_LENGTH, message = "validation.media.format.name.size")
    String name,

    @Min(value = MEDIA_FORMAT_DIMENSION_MIN, message = "validation.media.format.width.min")
    @Max(value = MEDIA_FORMAT_DIMENSION_MAX, message = "validation.media.format.width.max")
    Integer width,

    @Min(value = MEDIA_FORMAT_DIMENSION_MIN, message = "validation.media.format.height.min")
    @Max(value = MEDIA_FORMAT_DIMENSION_MAX, message = "validation.media.format.height.max")
    Integer height,

    @Min(value = MEDIA_FORMAT_QUALITY_MIN, message = "validation.media.format.quality.min")
    @Max(value = MEDIA_FORMAT_QUALITY_MAX, message = "validation.media.format.quality.max")
    Integer quality,

    CropMode cropMode) {
}
