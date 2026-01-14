package com.backend.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import static com.backend.shared.constants.ValidationConstants.MEDIA_CODE_MAX_LENGTH;
import static com.backend.shared.constants.ValidationConstants.MEDIA_CODE_PATTERN;
import static com.backend.shared.constants.ValidationConstants.MSG_MEDIA_CODE_PATTERN;
import static com.backend.shared.constants.ValidationConstants.MSG_MEDIA_CODE_REQUIRED;
import static com.backend.shared.constants.ValidationConstants.MSG_MEDIA_CODE_SIZE;

/**
 * Request DTO for creating/updating a ResponsiveMediaSet.
 * Both desktopMediaId and mobileMediaId are nullable to allow clearing media.
 */
public record ResponsiveMediaRequest(
        @NotBlank(message = MSG_MEDIA_CODE_REQUIRED)
        @Size(max = MEDIA_CODE_MAX_LENGTH, message = MSG_MEDIA_CODE_SIZE)
        @Pattern(regexp = MEDIA_CODE_PATTERN, message = MSG_MEDIA_CODE_PATTERN)
        String code,

        Long desktopMediaId,

                Long mobileMediaId) {
        public ResponsiveMediaRequest {
                if (code != null) {
                        code = code.trim();
                }
        }
}
