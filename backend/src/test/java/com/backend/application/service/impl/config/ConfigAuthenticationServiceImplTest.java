package com.backend.application.service.impl.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;

import com.backend.application.config.ConfigAuthProperties;
import com.backend.application.dto.config.ConfigPrincipal;
import com.backend.application.service.EmailService;
import com.backend.application.service.OtpService;
import com.backend.domain.entity.PlatformAdminUser;
import com.backend.domain.entity.Tenant;
import com.backend.domain.entity.User;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.TenantStatus;
import com.backend.domain.enums.UserRole;
import com.backend.domain.port.JwtProviderPort;
import com.backend.domain.port.OtpConfig;
import com.backend.domain.port.PlatformSettingsPort;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.PlatformAdminUserRepository;
import com.backend.domain.repository.PlatformVerificationTokenRepository;
import com.backend.domain.repository.TenantRepository;
import com.backend.domain.repository.UserRepository;
import com.backend.domain.repository.VerificationTokenRepository;

@ExtendWith(MockitoExtension.class)
class ConfigAuthenticationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

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
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProviderPort jwtProviderPort;

    @Mock
    private OtpConfig otpConfig;

    @Mock
    private PlatformTransactionManager tenantTransactionManager;

    @Test
    void loginTenantAdminReturnsSessionWithoutOtpWhenConfigOtpDisabled() {
        ConfigAuthenticationServiceImpl service = service(false);
        Tenant tenant = activeTenant();
        User user = tenantAdmin();
        when(tenantRepository.findBySubdomain("acme")).thenReturn(Optional.of(tenant));
        when(tenantTransactionManager.getTransaction(any(TransactionDefinition.class)))
                .thenReturn(new SimpleTransactionStatus());
        when(userRepository.findByEmail("admin@acme.test")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);
        when(jwtProviderPort.createAccessToken(
                "admin@acme.test",
                ConfigPrincipal.ROLE_CONFIG_TENANT_ADMIN,
                10L,
                1L)).thenReturn("access-token");
        when(jwtProviderPort.createRefreshToken(
                "admin@acme.test",
                ConfigPrincipal.ROLE_CONFIG_TENANT_ADMIN,
                10L,
                1L)).thenReturn("refresh-token");
        when(jwtProviderPort.getAccessTokenExpiration()).thenReturn(86_400_000L);

        var result = service.login(
                "admin@acme.test",
                "secret",
                null,
                "acme",
                "127.0.0.1",
                "agent");

        assertThat(result.requiresOtp()).isFalse();
        assertThat(result.challenge()).isNull();
        assertThat(result.session()).isNotNull();
        assertThat(result.session().accessToken()).isEqualTo("access-token");
        assertThat(result.session().refreshToken()).isEqualTo("refresh-token");
        assertThat(result.session().role()).isEqualTo(ConfigPrincipal.ROLE_CONFIG_TENANT_ADMIN);
        verify(otpService, never()).createLoginOtpToken(any(), any(), any());
        verify(emailService, never()).sendOtpEmail(any(), any(), any());
        verify(userRepository).save(user);
    }

    @Test
    void loginTenantAdminReturnsChallengeWhenConfigOtpEnabled() {
        ConfigAuthenticationServiceImpl service = service(true);
        Tenant tenant = activeTenant();
        User user = tenantAdmin();
        when(tenantRepository.findBySubdomain("acme")).thenReturn(Optional.of(tenant));
        when(tenantTransactionManager.getTransaction(any(TransactionDefinition.class)))
                .thenReturn(new SimpleTransactionStatus());
        when(userRepository.findByEmail("admin@acme.test")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);
        when(otpService.createLoginOtpToken(user, "127.0.0.1", "agent"))
                .thenReturn(new OtpService.LoginOtpResult("654321", "pending-token"));

        var result = service.login(
                "admin@acme.test",
                "secret",
                null,
                "acme",
                "127.0.0.1",
                "agent");

        assertThat(result.requiresOtp()).isTrue();
        assertThat(result.session()).isNull();
        assertThat(result.challenge()).isNotNull();
        assertThat(result.challenge().pendingToken()).isEqualTo("pending-token");
        assertThat(result.challenge().role()).isEqualTo(ConfigPrincipal.ROLE_CONFIG_TENANT_ADMIN);
        verify(emailService).sendOtpEmail("admin@acme.test", "654321", Language.TR);
        verify(jwtProviderPort, never()).createAccessToken(any(), any(), any(), any());
        verify(jwtProviderPort, never()).createRefreshToken(any(), any(), any(), any());
    }

    @Test
    void loginPlatformAdminReturnsSessionWithoutOtpWhenConfigOtpDisabled() {
        ConfigAuthenticationServiceImpl service = service(false);
        PlatformAdminUser admin = PlatformAdminUser.builder()
                .id(99L)
                .email("root@craftive.test")
                .passwordHash("hash")
                .fullName("Root Admin")
                .isActive(true)
                .failedLoginAttempts(0)
                .build();
        when(platformAdminUserRepository.findByEmailAndIsActiveTrue("root@craftive.test"))
                .thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);
        when(jwtProviderPort.createAccessToken(
                "root@craftive.test",
                ConfigPrincipal.ROLE_CONFIG_SUPER_ADMIN,
                99L,
                null)).thenReturn("platform-access-token");
        when(jwtProviderPort.getAccessTokenExpiration()).thenReturn(86_400_000L);

        var result = service.login(
                "root@craftive.test",
                "secret",
                null,
                null,
                "127.0.0.1",
                "agent");

        assertThat(result.requiresOtp()).isFalse();
        assertThat(result.challenge()).isNull();
        assertThat(result.session()).isNotNull();
        assertThat(result.session().accessToken()).isEqualTo("platform-access-token");
        assertThat(result.session().refreshToken()).isNull();
        assertThat(result.session().role()).isEqualTo(ConfigPrincipal.ROLE_CONFIG_SUPER_ADMIN);
        verify(otpService, never()).generateOtp();
        verify(emailService, never()).sendOtpEmail(any(), any(), any());
        verify(platformAdminUserRepository).save(admin);
    }

    private ConfigAuthenticationServiceImpl service(boolean otpEnabled) {
        return new ConfigAuthenticationServiceImpl(
                userRepository,
                tenantRepository,
                verificationTokenRepository,
                platformAdminUserRepository,
                platformSettingsPort,
                platformVerificationTokenRepository,
                tenantContext,
                otpService,
                emailService,
                passwordEncoder,
                jwtProviderPort,
                otpConfig,
                new ConfigAuthProperties(otpEnabled),
                tenantTransactionManager);
    }

    private Tenant activeTenant() {
        Tenant tenant = new Tenant();
        tenant.setId(1L);
        tenant.setSubdomain("acme");
        tenant.setDatabaseName("ac_acme_10001");
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setDefaultLanguage(Language.TR);
        return tenant;
    }

    private User tenantAdmin() {
        User user = new User();
        user.setId(10L);
        user.setEmail("admin@acme.test");
        user.setPasswordHash("hash");
        user.setFullName("Acme Admin");
        user.setRole(UserRole.TENANT_ADMIN);
        user.setIsActive(true);
        user.setFailedLoginAttempts(0);
        return user;
    }
}
