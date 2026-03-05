package com.backend.application.dto.mail;

import com.backend.domain.enums.MailCampaignStatus;

public record MailCampaignDto(
    Long id,
    MailCampaignStatus status,
    Integer totalCount,
    Integer sentCount,
    Integer failedCount
) {
}
