package com.backend.application.dto.contact;

public record ContactRequestSubmitCommand(
    String fullName,
    String subject,
    String message,
    String locale,
    String recaptchaToken,
    String clientIp,
    String userAgent
) {
}
