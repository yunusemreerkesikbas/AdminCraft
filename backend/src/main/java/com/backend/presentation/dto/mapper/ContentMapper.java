package com.backend.presentation.dto.mapper;

import com.backend.domain.entity.Content;
import com.backend.domain.entity.ContentType;
import com.backend.presentation.dto.request.CreateContentRequest;
import com.backend.presentation.dto.request.UpdateContentRequest;
import com.backend.presentation.dto.response.ContentResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ContentMapper {
    
    public Content toEntity(CreateContentRequest request) {
        Content content = new Content();
        content.setTitle(request.title());
        content.setSlug(request.slug());
        content.setExcerpt(request.excerpt());
        content.setData(request.data());
        content.setLanguage(request.language());
        content.setParentContentId(request.parentContentId());
        content.setContentTypeId(request.contentTypeId());
        content.setMetaTitle(request.metaTitle());
        content.setMetaDescription(request.metaDescription());
        content.setMetaKeywords(request.metaKeywords());
        content.setIsFeatured(request.isFeatured() != null ? request.isFeatured() : false);
        content.setIsSticky(request.isSticky() != null ? request.isSticky() : false);
        content.setRequiresLogin(request.requiresLogin() != null ? request.requiresLogin() : false);
        // Note: Password field removed as it doesn't exist in Content entity
        
        // Set defaults
        content.setStatus(com.backend.domain.enums.ContentStatus.DRAFT);
        content.setViewCount(0L);
        content.setLikeCount(0);
        content.setCommentCount(0);
        content.setCreatedAt(LocalDateTime.now());
        content.setUpdatedAt(LocalDateTime.now());
        
        return content;
    }
    
    public Content toEntity(UpdateContentRequest request, Content existingContent) {
        existingContent.setTitle(request.title());
        existingContent.setSlug(request.slug());
        existingContent.setExcerpt(request.excerpt());
        existingContent.setData(request.data());
        existingContent.setMetaTitle(request.metaTitle());
        existingContent.setMetaDescription(request.metaDescription());
        existingContent.setMetaKeywords(request.metaKeywords());
        existingContent.setIsFeatured(request.isFeatured() != null ? request.isFeatured() : false);
        existingContent.setIsSticky(request.isSticky() != null ? request.isSticky() : false);
        existingContent.setRequiresLogin(request.requiresLogin() != null ? request.requiresLogin() : false);
        // Note: Password field removed as it doesn't exist in Content entity
        existingContent.setUpdatedAt(LocalDateTime.now());
        
        return existingContent;
    }
    
    public ContentResponse toResponse(Content content) {
        return toResponse(content, null, null, null);
    }
    
    public ContentResponse toResponse(Content content, ContentType contentType, String authorName, String createdByName) {
        return new ContentResponse(
            content.getId(),
            content.getTitle(),
            content.getSlug(),
            content.getExcerpt(),
            content.getData(),
            content.getStatus(),
            content.getLanguage(),
            content.getParentContentId(),
            content.getContentTypeId(),
            contentType != null ? contentType.getDisplayName() : null,
            content.getTenantId(),
            content.getMetaTitle(),
            content.getMetaDescription(),
            content.getMetaKeywords(),
            content.getIsFeatured(),
            content.getIsSticky(),
            content.getRequiresLogin(),
            content.getViewCount(),
            content.getLikeCount() != null ? content.getLikeCount().longValue() : 0L,
            content.getCommentCount() != null ? content.getCommentCount().longValue() : 0L,
            content.getCreatedAt(),
            content.getUpdatedAt(),
            content.getPublishedAt(),
            content.getScheduledAt(),
            content.getExpiresAt(),
            content.getCreatedBy(),
            authorName,
            content.getCreatedBy(),
            content.getUpdatedBy(),
            content.getPublishedBy()
        );
    }
}