package com.backend.presentation.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static com.backend.shared.constants.ValidationConstants.MEDIA_TAGS_MAX;

/**
 * Request DTO for updating media metadata.
 */
public record MediaUpdateRequest(
    Boolean isPublic,
    @Size(max = MEDIA_TAGS_MAX, message = "validation.media.tags.size")
    List<@NotBlank(message = "validation.media.tag.required") String> tags) {
}
