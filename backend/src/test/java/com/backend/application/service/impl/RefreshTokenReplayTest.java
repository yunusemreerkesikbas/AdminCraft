package com.backend.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;

import com.backend.application.dto.AuthResult;
import com.backend.application.service.EmailService;
import com.backend.application.service.OtpBypassVerifier;
import com.backend.application.service.OtpService;
import com.backend.application.service.TrustedDeviceService;
import com.backend.domain.entity.Tenant;
import com.backend.domain.entity.User;
import com.backend.domain.entity.PlatformAdminUser;
import com.backend.domain.enums.TenantStatus;
import com.backend.domain.enums.TwoFactorPolicy;
import com.backend.domain.enums.UserRole;
import com.backend.domain.exception.InvalidTokenException;
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
 * SEC-008: verifies refresh token replay protection — second use must throw.
 */
@ExtendWith(MockitoExtension.class)
class RefreshTokenReplayTest {

    private static final String TOKEN = "eyJhbGciOiJIUzI1NiJ9.test.replay";

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
    @Mock private OtpBypassVerifier otpBypassVerifier;
    @Mock private com.backend.application.service.OtpRateLimitService otpRateLimitService;
    @Mock private com.backend.application.service.OtpResendCooldownService otpResendCooldownService;
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
                otpConfig, otpBypassVerifier, otpRateLimitService, otpResendCooldownService,
                refreshTokenRepository, platformRefreshTokenRepository,
                tenantTransactionManager, platformTransactionManager);
    }

    @Test
    @DisplayName("Replay: second use of same refresh token → InvalidTokenException")
    void replay_secondUse_throwsInvalidToken() {
        setupSuperAdminToken();
        when(platformRefreshTokenRepository.revokeByTokenHash(any())).thenReturn(0);

        assertThatThrownBy(() -> service.refreshToken(TOKEN, null, null, null))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    @DisplayName("First use: valid refresh token → new tokens issued")
    void firstUse_valid_issuesNewToken() {
        setupSuperAdminToken();
        when(platformRefreshTokenRepository.revokeByTokenHash(any())).thenReturn(1);
        when(jwtProviderPort.createAccessToken(any(), any(), any(), any())).thenReturn("new.access");
        when(jwtProviderPort.createRefreshToken(any(), any(), any(), any(), anyBoolean())).thenReturn("new.refresh");
        when(jwtProviderPort.getAccessTokenExpiration()).thenReturn(3_600_000L);
        when(jwtProviderPort.getRefreshTokenExpiration(anyBoolean())).thenReturn(86_400_000L);
        when(platformRefreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuthResult result = service.refreshToken(TOKEN, null, null, null);

        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo("new.access");
    }

    @Test
    @DisplayName("Platform refresh: existing session is not challenged for 2FA again")
    void platformRefresh_doesNotRechallengeExistingSessionForTwoFactor() {
        setupSuperAdminToken();
        when(platformRefreshTokenRepository.revokeByTokenHash(any())).thenReturn(1);
        when(jwtProviderPort.createAccessToken(any(), any(), any(), any())).thenReturn("new.access");
        when(jwtProviderPort.createRefreshToken(any(), any(), any(), any(), anyBoolean())).thenReturn("new.refresh");
        when(jwtProviderPort.getAccessTokenExpiration()).thenReturn(3_600_000L);
        when(jwtProviderPort.getRefreshTokenExpiration(anyBoolean())).thenReturn(86_400_000L);
        when(platformRefreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuthResult result = service.refreshToken(TOKEN, null, null, null);

        assertThat(result.requires2FA()).isFalse();
        assertThat(result.accessToken()).isEqualTo("new.access");
        verifyNoInteractions(
                platformSettingsPort,
                platformVerificationTokenRepository,
                otpRateLimitService,
                emailService);
    }

    @Test
    @DisplayName("Tenant refresh: REQUIRED 2FA policy preserves existing session")
    void tenantRefresh_requiredTwoFactorPolicyWithoutTrustedDevice_issuesNewToken() {
        setupTenantToken();
        when(refreshTokenRepository.revokeByTokenHash(any())).thenReturn(1);
        when(jwtProviderPort.createAccessToken(any(), any(), any(), any())).thenReturn("tenant.access");
        when(jwtProviderPort.createRefreshToken(any(), any(), any(), any(), anyBoolean())).thenReturn("tenant.refresh");
        when(jwtProviderPort.getAccessTokenExpiration()).thenReturn(3_600_000L);
        when(jwtProviderPort.getRefreshTokenExpiration(anyBoolean())).thenReturn(86_400_000L);
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuthResult result = service.refreshToken(TOKEN, null, null, null);

        assertThat(result.requires2FA()).isFalse();
        assertThat(result.accessToken()).isEqualTo("tenant.access");
    }

    private void setupSuperAdminToken() {
        when(jwtProviderPort.validateToken(TOKEN)).thenReturn(true);
        when(jwtProviderPort.isRefreshToken(TOKEN)).thenReturn(true);
        when(jwtProviderPort.getRoleFromToken(TOKEN)).thenReturn("SUPER_ADMIN");
        when(jwtProviderPort.getEmailFromToken(TOKEN)).thenReturn("admin@example.com");
        when(jwtProviderPort.getTenantIdFromToken(TOKEN)).thenReturn(null);
        when(jwtProviderPort.isRememberMeToken(TOKEN)).thenReturn(false);

        PlatformAdminUser admin = PlatformAdminUser.builder()
                .id(1L).email("admin@example.com").fullName("Admin").build();
        when(platformAdminUserRepository.findByEmailAndIsActiveTrue("admin@example.com"))
                .thenReturn(Optional.of(admin));
    }

    private void setupTenantToken() {
        when(jwtProviderPort.validateToken(TOKEN)).thenReturn(true);
        when(jwtProviderPort.isRefreshToken(TOKEN)).thenReturn(true);
        when(jwtProviderPort.getRoleFromToken(TOKEN)).thenReturn("TENANT_ADMIN");
        when(jwtProviderPort.getEmailFromToken(TOKEN)).thenReturn("owner@example.com");
        when(jwtProviderPort.getTenantIdFromToken(TOKEN)).thenReturn(10L);
        when(jwtProviderPort.isRememberMeToken(TOKEN)).thenReturn(false);

        Tenant tenant = new Tenant();
        tenant.setId(10L);
        tenant.setSubdomain("acme");
        tenant.setCompanyName("Acme");
        tenant.setDatabaseName("ac_acme_10010");
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setTwoFactorPolicy(TwoFactorPolicy.REQUIRED);
        when(tenantRepository.findById(10L)).thenReturn(Optional.of(tenant));

        User user = new User();
        user.setId(20L);
        user.setEmail("owner@example.com");
        user.setFullName("Tenant Owner");
        user.setRole(UserRole.TENANT_ADMIN);
        user.setIsActive(true);
        user.setFailedLoginAttempts(0);
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(user));
    }
}
