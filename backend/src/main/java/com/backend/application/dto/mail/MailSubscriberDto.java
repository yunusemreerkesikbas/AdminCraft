package com.backend.application.dto.mail;

import java.time.LocalDateTime;

import com.backend.domain.enums.MailSubscriberStatus;

public record MailSubscriberDto(
    Long id,
    String email,
    MailSubscriberStatus status,
    String source,
    String preferredLanguage,
    Boolean permission,
    LocalDateTime createdAt,
    LocalDateTime confirmedAt,
    LocalDateTime unsubscribedAt
) {
}
