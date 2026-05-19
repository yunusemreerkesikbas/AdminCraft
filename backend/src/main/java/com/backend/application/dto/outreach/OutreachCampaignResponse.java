package com.backend.application.dto.outreach;

import com.backend.infrastructure.persistence.platform.entity.PlatformOutreachCampaign;

public record OutreachCampaignResponse(
    Long id,
    String uuid,
    String name,
    String subject,
    String status,
    Integer totalCount,
    Integer sentCount,
    Integer failedCount,
    String createdByEmail,
    String createdAt,
    OutreachTemplateSummaryResponse template
) {
    public static OutreachCampaignResponse from(PlatformOutreachCampaign campaign) {
        return new OutreachCampaignResponse(
            campaign.getId(),
            campaign.getUuid(),
            campaign.getName(),
            campaign.getSubject(),
            campaign.getStatus() != null ? campaign.getStatus().name() : null,
            campaign.getTotalCount(),
            campaign.getSentCount(),
            campaign.getFailedCount(),
            campaign.getCreatedByEmail(),
            campaign.getCreatedAt() != null ? campaign.getCreatedAt().toString() : null,
            campaign.getTemplate() != null ? OutreachTemplateSummaryResponse.from(campaign.getTemplate()) : null
        );
    }
}
