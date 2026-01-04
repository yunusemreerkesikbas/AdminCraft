package com.backend.presentation.dto.request;

import jakarta.validation.constraints.Positive;

/**
 * Request DTO for assigning responsive media to a component.
 * The responsiveMediaId is nullable to allow removal of the media assignment.
 */
public record AssignResponsiveMediaRequest(
    @Positive(message = "validation.responsive.media.id.positive")
    Long responsiveMediaId
) {}
