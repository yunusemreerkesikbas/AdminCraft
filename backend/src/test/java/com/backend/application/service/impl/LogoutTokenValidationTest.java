package com.backend.application.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;

import com.backend.application.service.EmailService;
import com.backend.application.service.OtpService;
import com.backend.application.service.TrustedDeviceService;
import com.backend.domain.port.JwtProviderPort;
import com.backend.domain.port.OtpConfig;
import com.backend.domain.port.PlatformSettingsPort;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.PlatformAdminUserRepository;
import com.backend.domain.repository.PlatformRefreshTokenRepository;
import com.backend.domain.repository.PlatformVerificationTokenRepository;
import com.backend.domain.repository.RefreshTokenRepository;
import com.backend.domain.repository.TenantRepository;
import com.backend.domain.repository.UserRepository;
import com.backend.domain.repository.VerificationTokenRepository;

/**
 * SEC-007: verifies logout silently skips revocation for invalid or non-refresh tokens.
 */
@ExtendWith(MockitoExtension.class)
class LogoutTokenValidationTest {

    private static final String TOKEN = "eyJhbGciOiJIUzI1NiJ9.test.logout";

    @Mock private UserRepository userRepository;
    @Mock private JwtProviderPort jwtProviderPort;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TenantRepository tenantRepository;
    @Mock private PlatformAdminUserRepository platformAdminUserRepository;
    @Mock private PlatformSettingsPort platformSettingsPort;
    @Mock private PlatformVerificationTokenRepository platformVerificationTokenRepository;
    @Mock private TenantContextPort tenantContext;
    @Mock private OtpService otpService;
    @Mock private EmailService emailService;
    @Mock private TrustedDeviceService trustedDeviceService;
    @Mock private VerificationTokenRepository verificationTokenRepository;
    @Mock private OtpConfig otpConfig;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PlatformRefreshTokenRepository platformRefreshTokenRepository;
    @Mock private PlatformTransactionManager tenantTransactionManager;
    @Mock private PlatformTransactionManager platformTransactionManager;

    private AuthenticationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuthenticationServiceImpl(
                userRepository, jwtProviderPort, passwordEncoder,
                tenantRepository, platformAdminUserRepository, platformSettingsPort,
                platformVerificationTokenRepository, tenantContext,
                otpService, emailService, trustedDeviceService, verificationTokenRepository,
                otpConfig, refreshTokenRepository, platformRefreshTokenRepository,
                tenantTransactionManager, platformTransactionManager);
    }

    @Test
    @DisplayName("Logout with invalid signature → revocation skipped silently")
    void logout_withInvalidSignature_silentlySkipsRevoke() {
        when(jwtProviderPort.validateToken(TOKEN)).thenReturn(false);

        service.logout(null, TOKEN);

        verify(platformRefreshTokenRepository, never()).revokeByTokenHash(any());
        verify(refreshTokenRepository, never()).revokeByTokenHash(any());
    }

    @Test
    @DisplayName("Logout with access token in refresh position → revocation skipped silently")
    void logout_withAccessToken_silentlySkipsRevoke() {
        when(jwtProviderPort.validateToken(TOKEN)).thenReturn(true);
        when(jwtProviderPort.isRefreshToken(TOKEN)).thenReturn(false);

        service.logout(null, TOKEN);

        verify(platformRefreshTokenRepository, never()).revokeByTokenHash(any());
        verify(refreshTokenRepository, never()).revokeByTokenHash(any());
    }

    @Test
    @DisplayName("Logout with valid refresh token → token revoked")
    void logout_withValidRefreshToken_revokes() {
        when(jwtProviderPort.validateToken(TOKEN)).thenReturn(true);
        when(jwtProviderPort.isRefreshToken(TOKEN)).thenReturn(true);
        when(jwtProviderPort.getRoleFromToken(TOKEN)).thenReturn("SUPER_ADMIN");

        service.logout(null, TOKEN);

        verify(platformRefreshTokenRepository).revokeByTokenHash(any());
    }
}
