package com.backend.presentation.dto.response;

import com.backend.domain.entity.Media;
import com.backend.domain.entity.ProductMedia;

public record ProductMediaResponse(
        Long id,
        Long mediaId,
        String mediaType,
        Integer sortOrder,
        ResponsiveMediaResponse media) {
    public static ProductMediaResponse from(ProductMedia entity) {
        if (entity == null) {
            throw new IllegalArgumentException("ProductMedia entity cannot be null");
        }
        ResponsiveMediaResponse mediaResponse = entity.getResponsiveMediaSet() != null
                ? ResponsiveMediaResponse.from(entity.getResponsiveMediaSet())
                : null;

        return new ProductMediaResponse(
                entity.getId(),
                entity.getResponsiveMediaSet() != null ? entity.getResponsiveMediaSet().getId() : null,
                entity.getMediaType() != null ? entity.getMediaType().name() : null,
                entity.getSortOrder(),
                mediaResponse);
    }

    public record MediaSummaryResponse(
            Long id,
            String uid,
            String fileName,
            String originalName,
            String mimeType,
            String publicUrl,
            Integer width,
            Integer height,
            String fileSizeFormatted) {
        public static MediaSummaryResponse from(Media media) {
            if (media == null)
                return null;
            return new MediaSummaryResponse(
                    media.getId(),
                    media.getUid(),
                    media.getFileName(),
                    media.getOriginalName(),
                    media.getMimeType(),
                    "/api/media/files/" + media.getFileName(),
                    media.getWidth(),
                    media.getHeight(),
                    formatFileSize(media.getFileSize()));
        }

        private static String formatFileSize(Long bytes) {
            if (bytes == null)
                return "0 B";
            if (bytes < 1024)
                return bytes + " B";
            if (bytes < 1024 * 1024)
                return String.format("%.1f KB", bytes / 1024.0);
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }
}
