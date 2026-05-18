package com.backend.presentation.dto.response.outreach;

import com.backend.infrastructure.persistence.platform.entity.PlatformOutreachTemplate;

public record OutreachTemplateResponse(
    Long id,
    String uuid,
    String name,
    String subject,
    String content,
    String language,
    Boolean isActive,
    String createdAt,
    String updatedAt
) {
    public static OutreachTemplateResponse from(PlatformOutreachTemplate template) {
        return new OutreachTemplateResponse(
            template.getId(),
            template.getUuid(),
            template.getName(),
            template.getSubject(),
            template.getContent(),
            template.getLanguage(),
            template.getIsActive(),
            template.getCreatedAt() != null ? template.getCreatedAt().toString() : null,
            template.getUpdatedAt() != null ? template.getUpdatedAt().toString() : null
        );
    }
}
