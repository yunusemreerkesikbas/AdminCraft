package com.backend.application.dto.mail;

import java.time.LocalDateTime;
import java.util.List;

import com.backend.domain.enums.MailSubscriberStatus;

public record MailSubscriberAdminDto(
    Long id,
    String email,
    MailSubscriberStatus status,
    List<MailSubscriberSubscriptionDto> subscriptions,
    LocalDateTime createdAt,
    LocalDateTime confirmedAt,
    LocalDateTime unsubscribedAt
) {
}
