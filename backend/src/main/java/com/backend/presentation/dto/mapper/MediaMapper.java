package com.backend.presentation.dto.mapper;

import com.backend.domain.entity.MediaFile;
import com.backend.domain.entity.User;
import com.backend.presentation.dto.request.CreateMediaRequest;
import com.backend.presentation.dto.request.UpdateMediaRequest;
import com.backend.presentation.dto.response.MediaResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MediaMapper {
    
    public MediaFile toEntity(CreateMediaRequest request, MediaFile existingMedia) {
        if (request.altTextTr() != null) existingMedia.setAltTextTr(request.altTextTr());
        if (request.altTextEn() != null) existingMedia.setAltTextEn(request.altTextEn());
        if (request.descriptionTr() != null) existingMedia.setDescriptionTr(request.descriptionTr());
        if (request.descriptionEn() != null) existingMedia.setDescriptionEn(request.descriptionEn());
        if (request.titleTr() != null) existingMedia.setTitleTr(request.titleTr());
        if (request.titleEn() != null) existingMedia.setTitleEn(request.titleEn());
        if (request.folder() != null) existingMedia.setFolder(request.folder());
        if (request.category() != null) existingMedia.setCategory(request.category());
        if (request.tags() != null) existingMedia.setTags(request.tags());
        if (request.isPublic() != null) existingMedia.setIsPublic(request.isPublic());
        
        existingMedia.setUpdatedAt(LocalDateTime.now());
        
        return existingMedia;
    }
    
    public MediaFile toEntity(UpdateMediaRequest request, MediaFile existingMedia) {
        if (request.altTextTr() != null) existingMedia.setAltTextTr(request.altTextTr());
        if (request.altTextEn() != null) existingMedia.setAltTextEn(request.altTextEn());
        if (request.descriptionTr() != null) existingMedia.setDescriptionTr(request.descriptionTr());
        if (request.descriptionEn() != null) existingMedia.setDescriptionEn(request.descriptionEn());
        if (request.titleTr() != null) existingMedia.setTitleTr(request.titleTr());
        if (request.titleEn() != null) existingMedia.setTitleEn(request.titleEn());
        if (request.folder() != null) existingMedia.setFolder(request.folder());
        if (request.category() != null) existingMedia.setCategory(request.category());
        if (request.tags() != null) existingMedia.setTags(request.tags());
        if (request.isPublic() != null) existingMedia.setIsPublic(request.isPublic());
        
        existingMedia.setUpdatedAt(LocalDateTime.now());
        
        return existingMedia;
    }
    
    public MediaResponse toResponse(MediaFile media) {
        return toResponse(media, null);
    }
    
    public MediaResponse toResponse(MediaFile media, User uploader) {
        return new MediaResponse(
            media.getId(),
            media.getOriginalName(),
            media.getFileName(),
            media.getFilePath(),
            media.getMimeType(),
            media.getFileSize(),
            media.getWidth(),
            media.getHeight(),
            media.getAltTextTr(),
            media.getAltTextEn(),
            media.getDescriptionTr(),
            media.getDescriptionEn(),
            media.getTitleTr(),
            media.getTitleEn(),
            media.getFolder(),
            media.getCategory(),
            media.getTags(),
            media.getIsPublic(),
            media.getUsageCount() != null ? media.getUsageCount().longValue() : 0L,
            generateFileUrl(media),
            generateThumbnailUrl(media),
            media.getTenantId(),
            media.getUploadedBy(),
            uploader != null ? uploader.getFullName() : null,
            media.getCreatedAt(),
            media.getUpdatedAt()
        );
    }
    
    private String generateFileUrl(MediaFile media) {
        return "/api/media/files/" + media.getId();
    }
    
    private String generateThumbnailUrl(MediaFile media) {
        return "/api/media/thumbnails/" + media.getId();
    }
}