package com.backend.application.dto.media;

public record MediaFileResponseDto(
    Long id,
    String originalName,
    String fileName,
    String mimeType,
    Long fileSize,
    Integer width,
    Integer height,
    String url,
    String thumbnailUrl,
    String variants) {
}
