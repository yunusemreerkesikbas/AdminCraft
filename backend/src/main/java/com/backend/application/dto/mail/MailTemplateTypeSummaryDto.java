package com.backend.application.dto.mail;

import java.time.LocalDateTime;
import java.util.List;

public record MailTemplateTypeSummaryDto(
    String templateType,
    Boolean active,
    List<String> languages,
    Long subscriberCount,
    LocalDateTime lastCampaignAt
) {
}

