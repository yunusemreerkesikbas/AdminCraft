package com.backend.application.dto.mail;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MailSubscriberSubscriptionDto(
    @NotBlank String templateType,
    String source,
    @NotBlank String preferredLanguage,
    @NotNull Boolean permission
) {
}
