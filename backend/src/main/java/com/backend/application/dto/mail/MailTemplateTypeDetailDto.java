package com.backend.application.dto.mail;

import java.util.List;

public record MailTemplateTypeDetailDto(
    String templateType,
    List<MailTemplateDto> templates,
    MailCampaignDto lastCampaign,
    Long subscriberCount
) {
}

