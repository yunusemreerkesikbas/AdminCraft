package com.backend.application.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.SecuritySettingsResult;
import com.backend.application.dto.TwoFactorPolicyChangeRequestResult;
import com.backend.application.dto.UpdateSecuritySettingsCommand;
import com.backend.application.service.EmailService;
import com.backend.application.service.OtpBypassVerifier;
import com.backend.application.service.OtpRateLimitService;
import com.backend.application.service.OtpService;
import com.backend.application.service.SecuritySettingsService;
import com.backend.application.service.TwoFactorPolicyChangeMetadata;
import com.backend.domain.entity.Tenant;
import com.backend.domain.entity.User;
import com.backend.domain.entity.VerificationToken;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.TenantStatus;
import com.backend.domain.enums.TokenType;
import com.backend.domain.enums.TwoFactorPolicy;
import com.backend.domain.exception.InvalidCredentialsException;
import com.backend.domain.exception.InvalidTokenException;
import com.backend.domain.exception.TenantNotFoundException;
import com.backend.domain.exception.TwoFactorPolicyVerificationRequiredException;
import com.backend.domain.exception.UserNotFoundException;
import com.backend.domain.port.OtpConfig;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.TenantRepository;
import com.backend.domain.repository.UserRepository;
import com.backend.domain.repository.VerificationTokenRepository;
import com.backend.shared.common.LogSanitizer;
import com.backend.shared.common.SecurityHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecuritySettingsServiceImpl implements SecuritySettingsService {

    private final TenantRepository tenantRepository;
    private final TenantContextPort tenantContext;
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final SecurityHelper securityHelper;
    private final OtpService otpService;
    private final EmailService emailService;
    private final OtpRateLimitService otpRateLimitService;
    private final OtpBypassVerifier otpBypassVerifier;
    private final OtpConfig otpConfig;

    @Override
    public SecuritySettingsResult getSecuritySettings() {
        Tenant tenant = getCurrentTenant();
        TwoFactorPolicy policy = resolvePolicy(tenant);
        return SecuritySettingsResult.of(policy, getPolicyDescription(policy));
    }

    @Override
    @Transactional
    public SecuritySettingsResult updateTwoFactorPolicy(TwoFactorPolicy policy) {
        throw new TwoFactorPolicyVerificationRequiredException();
    }

    @Override
    @Transactional
    public SecuritySettingsResult updateSecuritySettings(UpdateSecuritySettingsCommand command) {
        if (command.twoFactorPolicy() != null) {
            throw new TwoFactorPolicyVerificationRequiredException();
        }
        return getSecuritySettings();
    }

    @Override
    @Transactional("tenantTransactionManager")
    public TwoFactorPolicyChangeRequestResult requestTwoFactorPolicyChange(
            TwoFactorPolicy targetPolicy,
            String ipAddress,
            String userAgent) {
        Tenant tenant = getCurrentTenant();
        ensureTenantActive(tenant);
        TwoFactorPolicy currentPolicy = resolvePolicy(tenant);
        if (currentPolicy == targetPolicy) {
            throw new IllegalArgumentException("Two-factor policy is already " + targetPolicy.name());
        }

        User actingUser = getActingUser();
        ensureTenantActive(tenant);
        otpRateLimitService.checkRateLimit(actingUser.getEmail());

        OtpService.OperationOtpResult otpResult = otpService.createOperationOtpToken(
                actingUser,
                targetPolicy,
                ipAddress,
                userAgent);

        Language language = tenant.getDefaultLanguage() != null ? tenant.getDefaultLanguage() : Language.TR;
        emailService.sendOperationOtpEmail(actingUser.getEmail(), otpResult.otpCode(), language);

        log.info("Two-factor policy change OTP requested for tenant {} by user {} -> {}",
                tenant.getId(), actingUser.getId(), targetPolicy);

        return new TwoFactorPolicyChangeRequestResult(
                otpResult.sessionToken(),
                LogSanitizer.maskEmail(actingUser.getEmail()),
                targetPolicy,
                otpConfig.getExpirySeconds());
    }

    @Override
    @Transactional("tenantTransactionManager")
    public SecuritySettingsResult confirmTwoFactorPolicyChange(String pendingChangeId, String otpCode) {
        String tokenHash = otpService.hashToken(pendingChangeId);
        VerificationToken token = verificationTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired verification session"));

        if (token.getTokenType() != TokenType.OPERATION_OTP) {
            throw new InvalidTokenException("Invalid or expired verification session");
        }

        if (!token.isUsable()) {
            throw new InvalidTokenException("Verification session has expired or is no longer valid");
        }

        User actingUser = getActingUser();
        if (!token.getUser().getId().equals(actingUser.getId())) {
            throw new InvalidTokenException("Invalid or expired verification session");
        }

        boolean isBypassCode = otpBypassVerifier.isBypassCode(otpCode);
        String otpHash = otpService.hashToken(otpCode);
        boolean isValid = isBypassCode || otpHash.equals(token.getTargetValue());

        if (!isValid) {
            token.incrementAttempts();
            verificationTokenRepository.save(token);
            if (!token.isUsable()) {
                throw new InvalidTokenException("Verification session has expired due to too many attempts");
            }
            throw new InvalidCredentialsException();
        }

        token.markAsUsed();
        verificationTokenRepository.save(token);

        TwoFactorPolicy targetPolicy = TwoFactorPolicyChangeMetadata.parse(token.getUserAgent());
        Tenant tenant = getCurrentTenant();
        ensureTenantActive(tenant);
        log.info("Applying two-factor policy change for tenant {} from {} to {}",
                tenant.getId(), tenant.getTwoFactorPolicy(), targetPolicy);
        tenant.setTwoFactorPolicy(targetPolicy);
        tenantRepository.save(tenant);

        return SecuritySettingsResult.of(targetPolicy, getPolicyDescription(targetPolicy));
    }

    private User getActingUser() {
        Long userId = securityHelper.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private Tenant getCurrentTenant() {
        String tenantIdStr = tenantContext.getTenantId();
        if (tenantIdStr == null) {
            throw new TenantNotFoundException("No tenant context available");
        }
        try {
            Long tenantId = Long.parseLong(tenantIdStr);
            return tenantRepository.findById(tenantId)
                    .orElseThrow(() -> new TenantNotFoundException(tenantId));
        } catch (NumberFormatException ex) {
            throw new TenantNotFoundException("Invalid tenant id in context: " + tenantIdStr);
        }
    }

    private TwoFactorPolicy resolvePolicy(Tenant tenant) {
        return tenant.getTwoFactorPolicy() != null ? tenant.getTwoFactorPolicy() : TwoFactorPolicy.DISABLED;
    }

    private void ensureTenantActive(Tenant tenant) {
        if (tenant.getStatus() != TenantStatus.ACTIVE) {
            throw new TenantNotFoundException("Tenant is not active");
        }
    }

    private String getPolicyDescription(TwoFactorPolicy policy) {
        return switch (policy) {
            case DISABLED -> "Two-factor authentication is disabled for all users";
            case REQUIRED -> "Two-factor authentication is required for all users";
        };
    }
}
