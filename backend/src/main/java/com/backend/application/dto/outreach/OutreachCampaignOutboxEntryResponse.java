package com.backend.application.dto.outreach;

import com.backend.infrastructure.persistence.platform.entity.PlatformOutreachCampaignContact;
import com.backend.shared.util.ResponseValueFilter;

public record OutreachCampaignOutboxEntryResponse(
    Long id,
    String contactEmail,
    String contactName,
    String renderedSubject,
    String status,
    String providerMessageId,
    String errorMessage,
    String createdAt
) {
    public static OutreachCampaignOutboxEntryResponse from(PlatformOutreachCampaignContact cc) {
        return new OutreachCampaignOutboxEntryResponse(
            cc.getId(),
            cc.getContact() != null ? cc.getContact().getEmail() : null,
            cc.getContact() != null ? cc.getContact().getFullName() : null,
            cc.getRenderedSubject(),
            cc.getStatus() != null ? cc.getStatus().name() : null,
            ResponseValueFilter.filterEmptyString(cc.getProviderMessageId()),
            ResponseValueFilter.filterEmptyString(cc.getErrorMessage()),
            cc.getCreatedAt() != null ? cc.getCreatedAt().toString() : null
        );
    }
}
