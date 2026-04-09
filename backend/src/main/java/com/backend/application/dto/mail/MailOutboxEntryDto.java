package com.backend.application.dto.mail;

import com.backend.domain.enums.MailOutboxStatus;

public record MailOutboxEntryDto(
    Long id,
    String toEmail,
    MailOutboxStatus status,
    String errorMessage,
    String providerMessageId
) {
}
