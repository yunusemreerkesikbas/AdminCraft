package com.backend.presentation.dto.request;

import jakarta.validation.constraints.Size;
import java.util.Map;

import static com.backend.shared.constants.ValidationConstants.COMPONENT_ENTRY_BUTTON_TEXT_MAX_LENGTH;
import static com.backend.shared.constants.ValidationConstants.COMPONENT_ENTRY_BUTTON_URL_MAX_LENGTH;
import static com.backend.shared.constants.ValidationConstants.COMPONENT_ENTRY_IMAGE_URL_MAX_LENGTH;
import static com.backend.shared.constants.ValidationConstants.COMPONENT_ENTRY_TITLE_MAX_LENGTH;

public record EntryI18nRequest(
    @Size(max = COMPONENT_ENTRY_TITLE_MAX_LENGTH, message = "{validation.component.entry.title.size}")
    String title,

    String description,

    @Size(max = COMPONENT_ENTRY_IMAGE_URL_MAX_LENGTH, message = "{validation.component.entry.image.url.size}")
    String imageUrl,

    @Size(max = COMPONENT_ENTRY_BUTTON_TEXT_MAX_LENGTH, message = "{validation.component.entry.button.text.size}")
    String buttonText,

    @Size(max = COMPONENT_ENTRY_BUTTON_URL_MAX_LENGTH, message = "{validation.component.entry.button.url.size}")
    String buttonUrl,

    Map<String, Object> customFields
) {}


