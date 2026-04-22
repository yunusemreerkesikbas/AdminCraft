package com.backend.domain.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformRefreshToken {

    private Long id;
    private Long userId;
    private String tokenHash;
    private LocalDateTime expiresAt;
    private LocalDateTime revokedAt;
    private LocalDateTime createdAt;

    public boolean isValid() {
        return revokedAt == null && LocalDateTime.now().isBefore(expiresAt);
    }

    public void revoke() {
        revokedAt = LocalDateTime.now();
    }
}
