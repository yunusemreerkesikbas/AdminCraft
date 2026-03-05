package com.backend.application.dto.mail;

public record MailProviderConfigDto(
    String provider,
    String fromEmail,
    String fromName,
    Boolean active,
    Boolean tokenConfigured
) {
}
