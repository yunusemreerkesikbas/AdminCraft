package com.backend.presentation.dto.request;

/**
 * Request DTO for assigning responsive media to a component.
 * The responsiveMediaId is nullable to allow removal of the media assignment.
 */
public record AssignResponsiveMediaRequest(
    Long responsiveMediaId
) {}
