package com.backend.presentation.dto.response.outreach;

import com.backend.infrastructure.persistence.platform.entity.PlatformOutreachTemplate;

public record OutreachTemplateSummaryResponse(
    Long id,
    String uuid,
    String name,
    String language,
    Boolean isActive
) {
    public static OutreachTemplateSummaryResponse from(PlatformOutreachTemplate t) {
        return new OutreachTemplateSummaryResponse(
            t.getId(),
            t.getUuid(),
            t.getName(),
            t.getLanguage(),
            t.getIsActive()
        );
    }
}
