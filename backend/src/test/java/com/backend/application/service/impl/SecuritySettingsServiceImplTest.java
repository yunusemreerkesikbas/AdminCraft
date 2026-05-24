package com.backend.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.backend.application.dto.TwoFactorPolicyChangeRequestResult;
import com.backend.application.service.EmailService;
import com.backend.application.service.OtpBypassVerifier;
import com.backend.application.service.OtpRateLimitService;
import com.backend.application.service.OtpResendCooldownService;
import com.backend.application.service.OtpService;
import com.backend.application.service.TwoFactorPolicyChangeMetadata;
import com.backend.domain.entity.Tenant;
import com.backend.domain.entity.User;
import com.backend.domain.entity.VerificationToken;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.TenantStatus;
import com.backend.domain.enums.TokenStatus;
import com.backend.domain.enums.TokenType;
import com.backend.domain.enums.TwoFactorPolicy;
import com.backend.domain.enums.UserRole;
import com.backend.domain.exception.TwoFactorPolicyVerificationRequiredException;
import com.backend.domain.port.OtpConfig;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.TenantRepository;
import com.backend.domain.repository.UserRepository;
import com.backend.domain.repository.VerificationTokenRepository;
import com.backend.shared.common.SecurityHelper;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SecuritySettingsServiceImplTest {

    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private TenantContextPort tenantContext;
    @Mock
    private UserRepository userRepository;
    @Mock
    private VerificationTokenRepository verificationTokenRepository;
    @Mock
    private SecurityHelper securityHelper;
    @Mock
    private OtpService otpService;
    @Mock
    private EmailService emailService;
    @Mock
    private OtpRateLimitService otpRateLimitService;
    @Mock
    private OtpResendCooldownService otpResendCooldownService;
    @Mock
    private OtpBypassVerifier otpBypassVerifier;
    @Mock
    private OtpConfig otpConfig;

    @InjectMocks
    private SecuritySettingsServiceImpl service;

    private Tenant tenant;
    private User admin;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setId(1L);
        tenant.setSubdomain("acme");
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setTwoFactorPolicy(TwoFactorPolicy.DISABLED);
        tenant.setDefaultLanguage(Language.EN);

        admin = new User();
        admin.setId(10L);
        admin.setEmail("admin@acme.test");
        admin.setRole(UserRole.TENANT_ADMIN);

        when(tenantContext.getTenantId()).thenReturn("1");
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(securityHelper.getCurrentUserId()).thenReturn(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(admin));
        when(otpConfig.getExpirySeconds()).thenReturn(300);
        when(otpResendCooldownService.resolveTenantCooldownSeconds(tenant)).thenReturn(180);
    }

    @Test
    @DisplayName("updateSecuritySettings rejects direct twoFactorPolicy patch")
    void updateSecuritySettings_ShouldRequireVerification() {
        assertThatThrownBy(() -> service.updateSecuritySettings(
                new com.backend.application.dto.UpdateSecuritySettingsCommand(TwoFactorPolicy.REQUIRED)))
                .isInstanceOf(TwoFactorPolicyVerificationRequiredException.class);

        verify(tenantRepository, never()).save(any());
    }

    @Test
    @DisplayName("requestTwoFactorPolicyChange sends operation OTP email")
    void requestTwoFactorPolicyChange_ShouldSendEmail() {
        when(otpService.createOperationOtpToken(eq(admin), eq(TwoFactorPolicy.REQUIRED), any(), any()))
                .thenReturn(new OtpService.OperationOtpResult("123456", "pending-session"));

        TwoFactorPolicyChangeRequestResult result = service.requestTwoFactorPolicyChange(
                TwoFactorPolicy.REQUIRED,
                "127.0.0.1",
                "JUnit");

        assertThat(result.pendingChangeId()).isEqualTo("pending-session");
        assertThat(result.targetPolicy()).isEqualTo(TwoFactorPolicy.REQUIRED);
        assertThat(result.resendCooldownSeconds()).isEqualTo(180);
        assertThat(result.emailSent()).isTrue();
        verify(otpRateLimitService).enforceResendCooldown("admin@acme.test", "1:operation", 180);
        verify(otpRateLimitService).recordOtpSend("admin@acme.test", "1:operation");
        verify(emailService).sendOperationOtpEmail("admin@acme.test", "123456", Language.EN);
    }

    @Test
    @DisplayName("requestTwoFactorPolicyChange rejects inactive tenants")
    void requestTwoFactorPolicyChange_ShouldRejectInactiveTenant() {
        tenant.setStatus(TenantStatus.SUSPENDED);

        assertThatThrownBy(() -> service.requestTwoFactorPolicyChange(
                TwoFactorPolicy.REQUIRED,
                "127.0.0.1",
                "JUnit"))
                .isInstanceOf(com.backend.domain.exception.TenantNotFoundException.class);

        verify(otpRateLimitService, never()).enforceResendCooldown(any(), any(), anyInt());
        verify(emailService, never()).sendOperationOtpEmail(any(), any(), any());
    }

    @Test
    @DisplayName("requestTwoFactorPolicyChange does not record cooldown when email fails")
    void requestTwoFactorPolicyChange_ShouldNotRecordCooldownWhenEmailFails() {
        when(otpService.createOperationOtpToken(eq(admin), eq(TwoFactorPolicy.REQUIRED), any(), any()))
                .thenReturn(new OtpService.OperationOtpResult("123456", "pending-session"));
        org.mockito.Mockito.doThrow(new RuntimeException("smtp down"))
                .when(emailService)
                .sendOperationOtpEmail("admin@acme.test", "123456", Language.EN);

        assertThatThrownBy(() -> service.requestTwoFactorPolicyChange(
                TwoFactorPolicy.REQUIRED,
                "127.0.0.1",
                "JUnit"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("smtp down");

        verify(otpRateLimitService).enforceResendCooldown("admin@acme.test", "1:operation", 180);
        verify(otpRateLimitService, never()).recordOtpSend(any(), any());
    }

    @Test
    @DisplayName("confirmTwoFactorPolicyChange applies policy when OTP is valid")
    void confirmTwoFactorPolicyChange_ShouldApplyPolicy() {
        VerificationToken token = VerificationToken.builder()
                .user(admin)
                .tokenType(TokenType.OPERATION_OTP)
                .status(TokenStatus.ACTIVE)
                .targetValue("otp-hash")
                .userAgent(TwoFactorPolicyChangeMetadata.format(TwoFactorPolicy.REQUIRED))
                .attemptCount(0)
                .maxAttempts(5)
                .expiresAt(java.time.LocalDateTime.now().plusMinutes(5))
                .build();

        when(otpService.hashToken("pending-session")).thenReturn("session-hash");
        when(otpService.hashToken("123456")).thenReturn("otp-hash");
        when(verificationTokenRepository.findByTokenHash("session-hash")).thenReturn(Optional.of(token));
        when(otpBypassVerifier.isBypassCode("123456")).thenReturn(false);

        var result = service.confirmTwoFactorPolicyChange("pending-session", "123456");

        assertThat(result.policy()).isEqualTo(TwoFactorPolicy.REQUIRED);
        assertThat(tenant.getTwoFactorPolicy()).isEqualTo(TwoFactorPolicy.REQUIRED);
        verify(tenantRepository).save(tenant);
        verify(verificationTokenRepository).save(token);
    }

    @Test
    @DisplayName("confirmTwoFactorPolicyChange rejects inactive tenants before saving")
    void confirmTwoFactorPolicyChange_ShouldRejectInactiveTenant() {
        tenant.setStatus(TenantStatus.SUSPENDED);
        VerificationToken token = VerificationToken.builder()
                .user(admin)
                .tokenType(TokenType.OPERATION_OTP)
                .status(TokenStatus.ACTIVE)
                .targetValue("otp-hash")
                .userAgent(TwoFactorPolicyChangeMetadata.format(TwoFactorPolicy.REQUIRED))
                .attemptCount(0)
                .maxAttempts(5)
                .expiresAt(java.time.LocalDateTime.now().plusMinutes(5))
                .build();

        when(otpService.hashToken("pending-session")).thenReturn("session-hash");
        when(otpService.hashToken("123456")).thenReturn("otp-hash");
        when(verificationTokenRepository.findByTokenHash("session-hash")).thenReturn(Optional.of(token));
        when(otpBypassVerifier.isBypassCode("123456")).thenReturn(false);

        assertThatThrownBy(() -> service.confirmTwoFactorPolicyChange("pending-session", "123456"))
                .isInstanceOf(com.backend.domain.exception.TenantNotFoundException.class);

        verify(tenantRepository, never()).save(tenant);
        verify(verificationTokenRepository, never()).save(token);
    }
}
