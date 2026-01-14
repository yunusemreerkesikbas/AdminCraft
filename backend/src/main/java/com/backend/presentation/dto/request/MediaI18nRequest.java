package com.backend.presentation.dto.request;

import jakarta.validation.constraints.Size;

import static com.backend.shared.constants.ValidationConstants.MEDIA_I18N_ALT_TEXT_MAX_LENGTH;
import static com.backend.shared.constants.ValidationConstants.MEDIA_I18N_DESCRIPTION_MAX_LENGTH;
import static com.backend.shared.constants.ValidationConstants.MEDIA_I18N_TITLE_MAX_LENGTH;

/**
 * Request DTO for media i18n metadata (alt text, title, description).
 */
public record MediaI18nRequest(
    @Size(max = MEDIA_I18N_ALT_TEXT_MAX_LENGTH, message = "validation.media.altText.size")
    String altText,

    @Size(max = MEDIA_I18N_TITLE_MAX_LENGTH, message = "validation.media.title.size")
    String title,

    @Size(max = MEDIA_I18N_DESCRIPTION_MAX_LENGTH, message = "validation.media.description.size")
    String description) {
}
