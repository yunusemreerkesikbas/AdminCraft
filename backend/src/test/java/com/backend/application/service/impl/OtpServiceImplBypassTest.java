package com.backend.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.core.env.Environment;

import com.backend.application.service.config.GlobalRuntimeConfigService;
import com.backend.domain.port.OtpConfig;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.VerificationTokenRepository;
import com.backend.infrastructure.email.EmailVerificationProperties;
import com.backend.infrastructure.email.PasswordResetProperties;

@ExtendWith(MockitoExtension.class)
class OtpServiceImplBypassTest {

    @Mock
    private VerificationTokenRepository tokenRepository;

    @Mock
    private OtpConfig otpConfig;

    @Mock
    private PasswordResetProperties passwordResetProperties;

    @Mock
    private EmailVerificationProperties emailVerificationProperties;

    @Mock
    private TenantContextPort tenantContext;

    @Mock
    private GlobalRuntimeConfigService globalRuntimeConfigService;

    @Mock
    private Environment environment;

    @InjectMocks
    private OtpServiceImpl service;

    private static final String SOME_HASH = "abc123hash";

    @BeforeEach
    void setActiveProfilesNonProd() {
        when(environment.getActiveProfiles()).thenReturn(new String[] { "test" });
    }

    @Test
    @DisplayName("validateOtp returns true when config-panel bypass enabled and code matches")
    void validateOtp_ReturnsTrue_WhenConfigPanelBypassMatches() {
        when(globalRuntimeConfigService.getOtpBypassEnabled()).thenReturn(true);
        when(globalRuntimeConfigService.getOtpBypassCodeDecrypted()).thenReturn("magic-code");

        assertThat(service.validateOtp(SOME_HASH, "magic-code")).isTrue();
        verify(tokenRepository, never()).findByTokenHash(any());
        verify(otpConfig, never()).getBypassCode();
    }

    @Test
    @DisplayName("validateOtp falls through to token check when config-panel bypass disabled")
    void validateOtp_FallsThrough_WhenConfigPanelBypassDisabled() {
        when(globalRuntimeConfigService.getOtpBypassEnabled()).thenReturn(false);
        when(otpConfig.getBypassCode()).thenReturn(null);
        when(tokenRepository.findByTokenHash(SOME_HASH)).thenReturn(Optional.empty());

        assertThat(service.validateOtp(SOME_HASH, "any-code")).isFalse();
        verify(globalRuntimeConfigService, never()).getOtpBypassCodeDecrypted();
        verify(tokenRepository).findByTokenHash(SOME_HASH);
    }

    @Test
    @DisplayName("validateOtp does NOT bypass when config-panel enabled but submitted code differs")
    void validateOtp_DoesNotBypass_WhenConfigPanelEnabledButCodeMismatches() {
        when(globalRuntimeConfigService.getOtpBypassEnabled()).thenReturn(true);
        when(globalRuntimeConfigService.getOtpBypassCodeDecrypted()).thenReturn("magic-code");
        when(otpConfig.getBypassCode()).thenReturn(null);
        when(tokenRepository.findByTokenHash(SOME_HASH)).thenReturn(Optional.empty());

        assertThat(service.validateOtp(SOME_HASH, "wrong-code")).isFalse();
        verify(tokenRepository).findByTokenHash(SOME_HASH);
    }

    @Test
    @DisplayName("validateOtp does NOT bypass when config-panel enabled but bypass code is null")
    void validateOtp_DoesNotBypass_WhenConfigPanelEnabledButCodeNull() {
        when(globalRuntimeConfigService.getOtpBypassEnabled()).thenReturn(true);
        when(globalRuntimeConfigService.getOtpBypassCodeDecrypted()).thenReturn(null);
        when(otpConfig.getBypassCode()).thenReturn(null);
        when(tokenRepository.findByTokenHash(SOME_HASH)).thenReturn(Optional.empty());

        assertThat(service.validateOtp(SOME_HASH, "any-code")).isFalse();
        verify(tokenRepository).findByTokenHash(SOME_HASH);
    }

    @Test
    @DisplayName("validateOtp respects env-var bypass when config-panel disabled (dev/stage profile)")
    void validateOtp_EnvVarBypass_StillWorksWhenConfigPanelDisabled() {
        when(globalRuntimeConfigService.getOtpBypassEnabled()).thenReturn(false);
        when(otpConfig.getBypassCode()).thenReturn("env-bypass");

        assertThat(service.validateOtp(SOME_HASH, "env-bypass")).isTrue();
        verify(tokenRepository, never()).findByTokenHash(any());
    }

    @Test
    @DisplayName("validateOtp config-panel bypass takes precedence over env-var bypass")
    void validateOtp_ConfigPanel_TakesPrecedenceOverEnvVar() {
        when(globalRuntimeConfigService.getOtpBypassEnabled()).thenReturn(true);
        when(globalRuntimeConfigService.getOtpBypassCodeDecrypted()).thenReturn("config-code");

        assertThat(service.validateOtp(SOME_HASH, "config-code")).isTrue();
        verify(otpConfig, never()).getBypassCode();
        verify(tokenRepository, never()).findByTokenHash(any());
    }
}
