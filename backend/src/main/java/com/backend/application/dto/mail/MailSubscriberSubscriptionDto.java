package com.backend.application.dto.mail;

public record MailSubscriberSubscriptionDto(
    String templateType,
    String source,
    String preferredLanguage,
    Boolean permission
) {
}
