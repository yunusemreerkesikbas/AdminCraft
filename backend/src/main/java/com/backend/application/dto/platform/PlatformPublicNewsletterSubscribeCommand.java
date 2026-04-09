package com.backend.application.dto.platform;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlatformPublicNewsletterSubscribeCommand(
    @NotBlank @Email String email,
    @NotBlank String source,
    @NotBlank String templateType,
    @NotBlank String locale,
    String honeypot,
    @NotNull Long formStartedAt
) {
}
