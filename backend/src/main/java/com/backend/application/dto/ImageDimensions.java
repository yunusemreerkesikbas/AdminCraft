package com.backend.application.dto;

/**
 * Represents the dimensions of an image.
 *
 * @param width  the width of the image in pixels
 * @param height the height of the image in pixels
 */
public record ImageDimensions(
    int width,
    int height) {
}
