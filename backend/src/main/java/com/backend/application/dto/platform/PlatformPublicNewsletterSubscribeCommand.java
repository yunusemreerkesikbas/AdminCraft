package com.backend.application.dto.platform;

public record PlatformPublicNewsletterSubscribeCommand(
    String email,
    String source,
    String templateType,
    String locale,
    String honeypot,
    Long formStartedAt
) {
}

