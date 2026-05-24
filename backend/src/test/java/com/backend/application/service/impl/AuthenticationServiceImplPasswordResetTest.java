package com.backend.application.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;

import com.backend.application.service.EmailService;
import com.backend.application.service.OtpBypassVerifier;
import com.backend.application.service.OtpService;
import com.backend.application.service.TrustedDeviceService;
import com.backend.domain.entity.Tenant;
import com.backend.domain.entity.User;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.TenantStatus;
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

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplPasswordResetTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtProviderPort jwtProviderPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private PlatformAdminUserRepository platformAdminUserRepository;

    @Mock
    private PlatformSettingsPort platformSettingsPort;

    @Mock
    private PlatformVerificationTokenRepository platformVerificationTokenRepository;

    @Mock
    private TenantContextPort tenantContext;

    @Mock
    private OtpService otpService;

    @Mock
    private EmailService emailService;

    @Mock
    private TrustedDeviceService trustedDeviceService;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private OtpConfig otpConfig;

    @Mock
    private OtpBypassVerifier otpBypassVerifier;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PlatformRefreshTokenRepository platformRefreshTokenRepository;

    @Mock
    private PlatformTransactionManager tenantTransactionManager;

    @Mock
    private PlatformTransactionManager platformTransactionManager;

    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationServiceImpl(
                userRepository,
                jwtProviderPort,
                passwordEncoder,
                tenantRepository,
                platformAdminUserRepository,
                platformSettingsPort,
                platformVerificationTokenRepository,
                tenantContext,
                otpService,
                emailService,
                trustedDeviceService,
                verificationTokenRepository,
                otpConfig,
                otpBypassVerifier,
                refreshTokenRepository,
                platformRefreshTokenRepository,
                tenantTransactionManager,
                platformTransactionManager);
    }

    @Test
    void requestPasswordResetUsesTenantContextFallbackWhenNoExplicitTenantIdentifier() {
        Tenant tenant = activeTenant();
        User user = user();
        when(tenantContext.isSet()).thenReturn(true);
        when(tenantContext.getTenantId()).thenReturn("1");
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(tenantTransactionManager.getTransaction(any(TransactionDefinition.class)))
                .thenReturn(new SimpleTransactionStatus());
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(otpService.createPasswordResetToken(user, "127.0.0.1", "agent"))
                .thenReturn(new OtpService.PasswordResetTokenResult(null, "plain-token"));

        authenticationService.requestPasswordReset(
                "user@example.com",
                null,
                null,
                "127.0.0.1",
                "agent",
                Language.TR);

        verify(emailService).sendPasswordResetEmail(
                "user@example.com",
                "plain-token",
                "acme",
                Language.TR);
        verify(tenantContext).clear();
    }

    @Test
    void requestPasswordResetDoesNotSendMailWhenTenantIdAndSubdomainMismatch() {
        Tenant tenant = activeTenant();
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));

        authenticationService.requestPasswordReset(
                "user@example.com",
                1L,
                "other",
                "127.0.0.1",
                "agent",
                Language.TR);

        verify(emailService, never()).sendPasswordResetEmail(any(), any(), any(), any());
        verify(tenantTransactionManager, never()).getTransaction(any(TransactionDefinition.class));
    }

    @Test
    void requestPasswordResetDoesNotSendMailForInactiveTenant() {
        Tenant tenant = activeTenant();
        tenant.setStatus(TenantStatus.SUSPENDED);
        when(tenantRepository.findBySubdomain("acme")).thenReturn(Optional.of(tenant));

        authenticationService.requestPasswordReset(
                "user@example.com",
                null,
                " acme ",
                "127.0.0.1",
                "agent",
                Language.TR);

        verify(emailService, never()).sendPasswordResetEmail(any(), any(), any(), any());
        verify(tenantTransactionManager, never()).getTransaction(any(TransactionDefinition.class));
    }

    private Tenant activeTenant() {
        Tenant tenant = new Tenant();
        tenant.setId(1L);
        tenant.setSubdomain("acme");
        tenant.setDatabaseName("ac_acme_1");
        tenant.setStatus(TenantStatus.ACTIVE);
        return tenant;
    }

    private User user() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setFullName("Test User");
        return user;
    }
}
