package com.backend.presentation.dto.response;

/**
 * Response DTO for generated media variant.
 * Returns details about the newly created variant.
 */
public record MediaVariantResponse(
    Long id,
    String uid,
    String formatCode,
    String formatName,
    Integer width,
    Integer height,
    Long fileSize,
    String fileSizeFormatted,
    String mimeType,
    String publicUrl) {
}
