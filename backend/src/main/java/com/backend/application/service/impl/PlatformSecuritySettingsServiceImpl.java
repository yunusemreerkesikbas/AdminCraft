package com.backend.application.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.TwoFactorPolicyChangeRequestResult;
import com.backend.application.dto.response.PlatformSettingsData;
import com.backend.application.service.EmailService;
import com.backend.application.service.OtpBypassVerifier;
import com.backend.application.service.OtpRateLimitService;
import com.backend.application.service.OtpService;
import com.backend.application.service.PlatformSecuritySettingsService;
import com.backend.application.service.PlatformSettingsService;
import com.backend.application.service.TwoFactorPolicyChangeMetadata;
import com.backend.domain.entity.PlatformAdminUser;
import com.backend.domain.entity.PlatformVerificationToken;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.TokenStatus;
import com.backend.domain.enums.TokenType;
import com.backend.domain.enums.TwoFactorPolicy;
import com.backend.domain.exception.InvalidCredentialsException;
import com.backend.domain.exception.InvalidTokenException;
import com.backend.domain.port.PlatformSettingsPort;
import com.backend.domain.repository.PlatformAdminUserRepository;
import com.backend.domain.repository.PlatformVerificationTokenRepository;
import com.backend.infrastructure.email.OtpProperties;
import com.backend.infrastructure.persistence.platform.entity.PlatformSettings;
import com.backend.infrastructure.persistence.platform.repository.PlatformSettingsRepository;
import com.backend.shared.common.LogSanitizer;
import com.backend.shared.common.SecurityHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformSecuritySettingsServiceImpl implements PlatformSecuritySettingsService {

    private final PlatformSettingsPort platformSettingsPort;
    private final PlatformSettingsRepository platformSettingsRepository;
    private final PlatformAdminUserRepository platformAdminUserRepository;
    private final PlatformVerificationTokenRepository platformVerificationTokenRepository;
    private final SecurityHelper securityHelper;
    private final OtpService otpService;
    private final EmailService emailService;
    private final OtpRateLimitService otpRateLimitService;
    private final OtpBypassVerifier otpBypassVerifier;
    private final OtpProperties otpProperties;
    private final PlatformSettingsService platformSettingsService;

    @Override
    @Transactional("platformTransactionManager")
    public TwoFactorPolicyChangeRequestResult requestTwoFactorPolicyChange(
            TwoFactorPolicy targetPolicy,
            String ipAddress,
            String userAgent) {
        PlatformSettings entity = platformSettingsPort.getSingleton();
        TwoFactorPolicy currentPolicy = entity.getTwoFactorPolicy() != null
                ? entity.getTwoFactorPolicy()
                : TwoFactorPolicy.DISABLED;
        if (currentPolicy == targetPolicy) {
            throw new IllegalArgumentException("Two-factor policy is already " + targetPolicy.name());
        }

        PlatformAdminUser admin = getActingAdmin();
        otpRateLimitService.checkRateLimit(admin.getEmail(), "platform");

        PlatformOperationOtpResult otpResult = createPlatformOperationOtpToken(admin, targetPolicy, ipAddress, userAgent);
        Language language = resolvePlatformLanguage(entity);
        emailService.sendOperationOtpEmail(admin.getEmail(), otpResult.otpCode(), language);

        log.info("Platform two-factor policy change OTP requested by admin {} -> {}",
                admin.getId(), targetPolicy);

        return new TwoFactorPolicyChangeRequestResult(
                otpResult.sessionToken(),
                LogSanitizer.maskEmail(admin.getEmail()),
                targetPolicy,
                otpProperties.getExpirySeconds());
    }

    @Override
    @Transactional("platformTransactionManager")
    public PlatformSettingsData confirmTwoFactorPolicyChange(String pendingChangeId, String otpCode) {
        String tokenHash = otpService.hashToken(pendingChangeId);
        PlatformVerificationToken token = platformVerificationTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired verification session"));

        if (token.getTokenType() != TokenType.OPERATION_OTP) {
            throw new InvalidTokenException("Invalid or expired verification session");
        }

        if (!token.isUsable()) {
            throw new InvalidTokenException("Verification session has expired or is no longer valid");
        }

        PlatformAdminUser actingAdmin = getActingAdmin();
        if (!token.getAdminUser().getId().equals(actingAdmin.getId())) {
            throw new InvalidTokenException("Invalid or expired verification session");
        }

        boolean isBypassCode = otpBypassVerifier.isBypassCode(otpCode);
        String otpHash = otpService.hashToken(otpCode);
        boolean isValid = isBypassCode || otpHash.equals(token.getTargetValue());

        if (!isValid) {
            token.incrementAttempts();
            platformVerificationTokenRepository.save(token);
            if (!token.isUsable()) {
                throw new InvalidTokenException("Verification session has expired due to too many attempts");
            }
            throw new InvalidCredentialsException();
        }

        token.markAsUsed();
        platformVerificationTokenRepository.save(token);

        TwoFactorPolicy targetPolicy = TwoFactorPolicyChangeMetadata.parse(token.getUserAgent());
        PlatformSettings entity = platformSettingsPort.getSingleton();
        log.info("Applying platform two-factor policy change from {} to {}",
                entity.getTwoFactorPolicy(), targetPolicy);
        entity.setTwoFactorPolicy(targetPolicy);
        platformSettingsRepository.save(entity);

        return platformSettingsService.getSettings();
    }

    private PlatformOperationOtpResult createPlatformOperationOtpToken(
            PlatformAdminUser admin,
            TwoFactorPolicy pendingPolicy,
            String ipAddress,
            String userAgent) {
        platformVerificationTokenRepository.revokeAllActiveTokensForAdmin(admin.getId(), TokenType.OPERATION_OTP);

        String otp = otpService.generateOtp();
        String sessionToken = UUID.randomUUID().toString();
        String sessionTokenHash = otpService.hashToken(sessionToken);

        PlatformVerificationToken token = PlatformVerificationToken.builder()
                .adminUser(admin)
                .tokenHash(sessionTokenHash)
                .tokenType(TokenType.OPERATION_OTP)
                .status(TokenStatus.ACTIVE)
                .targetValue(otpService.hashToken(otp))
                .expiresAt(LocalDateTime.now().plusSeconds(otpProperties.getExpirySeconds()))
                .attemptCount(0)
                .maxAttempts(otpProperties.getMaxAttempts())
                .ipAddress(ipAddress)
                .userAgent(TwoFactorPolicyChangeMetadata.format(pendingPolicy))
                .build();

        platformVerificationTokenRepository.save(token);
        return new PlatformOperationOtpResult(otp, sessionToken);
    }

    private PlatformAdminUser getActingAdmin() {
        String email = securityHelper.getCurrentUserEmail();
        return platformAdminUserRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(InvalidCredentialsException::new);
    }

    private Language resolvePlatformLanguage(PlatformSettings entity) {
        return Language.fromCodeOrDefault(entity.getDefaultLanguage(), Language.TR);
    }

    private record PlatformOperationOtpResult(String otpCode, String sessionToken) {
    }
}
