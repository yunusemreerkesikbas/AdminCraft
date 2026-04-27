package com.backend.application.dto.contact;

import java.time.LocalDateTime;

public record ContactRequestAdminDto(
    Long id,
    String fullName,
    String subject,
    String message,
    String messagePreview,
    String locale,
    String source,
    String clientIp,
    String userAgent,
    LocalDateTime createdAt
) {
}
