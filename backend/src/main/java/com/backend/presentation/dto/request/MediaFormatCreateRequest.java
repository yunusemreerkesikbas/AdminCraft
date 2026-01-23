package com.backend.presentation.dto.request;

import static com.backend.shared.constants.ValidationConstants.MEDIA_FORMAT_CODE_MAX_LENGTH;
import static com.backend.shared.constants.ValidationConstants.MEDIA_FORMAT_DIMENSION_MAX;
import static com.backend.shared.constants.ValidationConstants.MEDIA_FORMAT_DIMENSION_MIN;
import static com.backend.shared.constants.ValidationConstants.MEDIA_FORMAT_NAME_MAX_LENGTH;
import static com.backend.shared.constants.ValidationConstants.MEDIA_FORMAT_QUALITY_MAX;
import static com.backend.shared.constants.ValidationConstants.MEDIA_FORMAT_QUALITY_MIN;

import com.backend.domain.enums.CropMode;
import com.backend.shared.validation.MediaCode;

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
        @MediaCode(maxLength = MEDIA_FORMAT_CODE_MAX_LENGTH) String code,

        @NotBlank(message = "validation.media.format.name.required") @Size(max = MEDIA_FORMAT_NAME_MAX_LENGTH, message = "validation.media.format.name.size") String name,

        @NotNull(message = "validation.media.format.width.required") @Min(value = MEDIA_FORMAT_DIMENSION_MIN, message = "validation.media.format.width.min") @Max(value = MEDIA_FORMAT_DIMENSION_MAX, message = "validation.media.format.width.max") Integer width,

        @NotNull(message = "validation.media.format.height.required") @Min(value = MEDIA_FORMAT_DIMENSION_MIN, message = "validation.media.format.height.min") @Max(value = MEDIA_FORMAT_DIMENSION_MAX, message = "validation.media.format.height.max") Integer height,

        @Min(value = MEDIA_FORMAT_QUALITY_MIN, message = "validation.media.format.quality.min") @Max(value = MEDIA_FORMAT_QUALITY_MAX, message = "validation.media.format.quality.max") Integer quality,

        CropMode cropMode // default FIT
) {
}
