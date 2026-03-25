package com.backend.application.dto.platform;

public record PlatformDemoRequestSubmitCommand(
    String fullName,
    String email,
    String phone,
    String message,
    String locale,
    String recaptchaToken,
    String clientIp,
    String userAgent
) {
}
