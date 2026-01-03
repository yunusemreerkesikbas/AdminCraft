package com.backend.application.dto;

/**
 * Represents a processed media format variant.
 *
 * @param formatCode     the code identifying the format (e.g., "thumbnail",
 *                       "medium")
 * @param variantMediaId the ID of the generated variant media
 * @param filePath       the file path where the variant is stored
 */
public record ProcessedFormat(
    String formatCode,
    Long variantMediaId,
    String filePath) {
}
