package com.backend.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.backend.domain.exception.OtpRateLimitExceededException;

class OtpRateLimitServiceTest {

    private OtpRateLimitService service;

    @BeforeEach
    void setUp() {
        service = new OtpRateLimitService();
    }

    @Test
    @DisplayName("enforceResendCooldown allows first send")
    void enforceResendCooldown_ShouldAllowFirstSend() {
        assertThatCode(() -> service.enforceResendCooldown("user@test.com", "1", 180))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("enforceResendCooldown blocks immediate resend")
    void enforceResendCooldown_ShouldBlockImmediateResend() {
        service.recordOtpSend("user@test.com", "1");

        assertThatThrownBy(() -> service.enforceResendCooldown("user@test.com", "1", 180))
                .isInstanceOf(OtpRateLimitExceededException.class)
                .satisfies(ex -> {
                    OtpRateLimitExceededException rateLimit = (OtpRateLimitExceededException) ex;
                    assert rateLimit.getRetryAfterSeconds() > 0;
                    assert rateLimit.getRetryAfterSeconds() <= 180;
                });
    }

    @Test
    @DisplayName("enforceResendCooldown isolates scopes")
    void enforceResendCooldown_ShouldIsolateScopes() {
        service.recordOtpSend("user@test.com", "1:login");

        assertThatCode(() -> service.enforceResendCooldown("user@test.com", "1:operation", 180))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("enforceResendCooldown normalizes email case")
    void enforceResendCooldown_ShouldNormalizeEmail() {
        service.recordOtpSend("User@Test.com", "1");

        assertThatThrownBy(() -> service.enforceResendCooldown("user@test.com", "1", 180))
                .isInstanceOf(OtpRateLimitExceededException.class);
    }

    @Test
    @DisplayName("enforceResendCooldown uses default when cooldown misconfigured")
    void enforceResendCooldown_ShouldUseDefaultWhenCooldownZero() {
        service.recordOtpSend("user@test.com", "1");

        assertThatThrownBy(() -> service.enforceResendCooldown("user@test.com", "1", 0))
                .isInstanceOf(OtpRateLimitExceededException.class)
                .satisfies(ex -> {
                    OtpRateLimitExceededException rateLimit = (OtpRateLimitExceededException) ex;
                    assertThat(rateLimit.getRetryAfterSeconds())
                            .isLessThanOrEqualTo(OtpResendCooldownService.DEFAULT_COOLDOWN_SECONDS);
                });
    }
}
