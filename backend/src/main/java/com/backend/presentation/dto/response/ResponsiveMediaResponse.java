package com.backend.presentation.dto.response;

import com.backend.domain.entity.Media;
import com.backend.domain.entity.ResponsiveMediaSet;

public record ResponsiveMediaResponse(
        Long id,
        String uid,
        String code,
        MediaInfo desktop,
        MediaInfo mobile
) {
    public static ResponsiveMediaResponse from(ResponsiveMediaSet entity) {
        if (entity == null) {
            return null;
        }
        return new ResponsiveMediaResponse(
                entity.getId(),
                entity.getUid(),
                entity.getCode(),
                entity.getDesktopMedia() != null ? MediaInfo.from(entity.getDesktopMedia()) : null,
                entity.getMobileMedia() != null ? MediaInfo.from(entity.getMobileMedia()) : null
        );
    }

    public record MediaInfo(
            Long id,
            String uid,
            String fileName,
            String originalName,
            String mimeType,
            String publicUrl,
            Integer width,
            Integer height
    ) {
        public static MediaInfo from(Media media) {
            if (media == null) return null;
            return new MediaInfo(
                    media.getId(),
                    media.getUid(),
                    media.getFileName(),
                    media.getOriginalName(),
                    media.getMimeType(),
                    "/api/media/files/" + media.getFileName(),
                    media.getWidth(),
                    media.getHeight()
            );
        }
    }
}
