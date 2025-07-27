package com.backend.presentation.dto.mapper;

import com.backend.domain.entity.ContentType;
import com.backend.presentation.dto.response.ContentTypeResponse;
import org.springframework.stereotype.Component;

@Component
public class ContentTypeMapper {
    
    public ContentTypeResponse toResponse(ContentType contentType) {
        return toResponse(contentType, 0L, 0L);
    }
    
    public ContentTypeResponse toResponse(ContentType contentType, Long contentCount, Long publishedContentCount) {
        return new ContentTypeResponse(
            contentType.getId(),
            contentType.getName(),
            contentType.getDisplayName(),
            contentType.getFields(),
            contentType.getTenantId(),
            contentType.getSupportsMultiLanguage(),
            contentCount,
            publishedContentCount,
            contentType.getCreatedAt(),
            contentType.getUpdatedAt()
        );
    }
}