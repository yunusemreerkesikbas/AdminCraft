package com.backend.application.dto.platform;

import java.time.LocalDateTime;

public record PlatformDemoRequestAdminDto(
    Long id,
    String uid,
    String fullName,
    String email,
    String phone,
    String message,
    String messagePreview,
    String locale,
    String source,
    String clientIp,
    String userAgent,
    LocalDateTime createdAt
) {
}
