package com.backend.application.service.impl;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.backend.application.dto.AuthResult;
import com.backend.application.dto.TokenValidationResult;
import com.backend.application.service.AuthenticationService;
import com.backend.application.service.EmailService;
import com.backend.application.service.OtpService;
import com.backend.application.service.TrustedDeviceService;
import com.backend.domain.entity.Tenant;
import com.backend.domain.entity.User;
import com.backend.domain.entity.VerificationToken;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.TenantStatus;
import com.backend.domain.enums.TokenType;
import com.backend.domain.enums.TwoFactorPolicy;
import com.backend.domain.exception.AccountLockedException;
import com.backend.domain.exception.InvalidCredentialsException;
import com.backend.domain.exception.InvalidTokenException;
import com.backend.domain.exception.OtpRateLimitExceededException;
import com.backend.domain.exception.UserAccountDisabledException;
import com.backend.domain.exception.UserNotFoundException;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.TenantRepository;
import com.backend.domain.repository.UserRepository;
import com.backend.domain.repository.VerificationTokenRepository;
import com.backend.infrastructure.email.OtpProperties;
import com.backend.infrastructure.persistence.platform.entity.PlatformAdminUser;
import com.backend.infrastructure.persistence.platform.repository.PlatformAdminUserRepository;
import com.backend.infrastructure.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final int OTP_RATE_LIMIT_WINDOW_SECONDS = 300; // 5 minutes
    private static final int OTP_MAX_REQUESTS_PER_WINDOW = 3;

    private final ConcurrentHashMap<String, OtpRateLimitEntry> otpRateLimiters = new ConcurrentHashMap<>();

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final TenantRepository tenantRepository;
    private final PlatformAdminUserRepository platformAdminUserRepository;
    private final TenantContextPort tenantContext;
    private final OtpService otpService;
    private final EmailService emailService;
    private final TrustedDeviceService trustedDeviceService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final OtpProperties otpProperties;

    @Qualifier("tenantTransactionManager")
    private final PlatformTransactionManager tenantTransactionManager;

    @Override
    public AuthResult authenticate(String email, String password, Long tenantId, String subdomain) {
        log.info("Processing authentication request");

        if (tenantId != null) {
            log.debug("Using X-Tenant-ID based authentication: tenantId={}", tenantId);
            return authenticateTenantUserById(email, password, tenantId);
        } else if (subdomain != null && !subdomain.trim().isEmpty()) {
            log.debug("Using subdomain-based authentication: subdomain={}", subdomain);
            return authenticateTenantUserBySubdomain(email, password, subdomain);
        } else {
            log.debug("Using platform admin authentication");
            return authenticatePlatformAdmin(email, password);
        }
    }

    private AuthResult authenticateTenantUserById(String email, String password, Long tenantId) {
        return authenticateTenantUserById(email, password, tenantId, null, null, null);
    }

    private AuthResult authenticateTenantUserById(String email, String password, Long tenantId,
            String deviceFingerprint, String ipAddress, String userAgent) {
        try {
            Tenant tenant = tenantRepository.findById(tenantId)
                    .orElseThrow(() -> {
                        log.warn("Tenant not found for id: {}", tenantId);
                        return new InvalidCredentialsException();
                    });

            if (tenant.getStatus() != TenantStatus.ACTIVE) {
                log.warn("Tenant is not active: tenantId={}, status={}", tenantId, tenant.getStatus());
                throw new InvalidCredentialsException();
            }
            tenantContext.setTenantId(String.valueOf(tenant.getId()));
            tenantContext.setTenantDbName(tenant.getDatabaseName());

            // Populate MDC for logging context
            MDC.put("tenantId", String.valueOf(tenant.getId()));
            MDC.put("tenantDb", tenant.getDatabaseName());
            MDC.put("correlationId", UUID.randomUUID().toString());

            log.debug("TenantContext set: tenantId={}, dbName={}", tenant.getId(), tenant.getDatabaseName());

            TransactionTemplate transactionTemplate = new TransactionTemplate(tenantTransactionManager);
            return transactionTemplate.execute(status -> {
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> {
                            log.warn("User not found for provided credentials");
                            return new InvalidCredentialsException();
                        });

                return authenticateUser(user, password, tenant, deviceFingerprint, ipAddress, userAgent);
            });
        } finally {
            tenantContext.clear();
            MDC.remove("tenantId");
            MDC.remove("tenantDb");
            MDC.remove("correlationId");
            log.debug("TenantContext cleared");
        }
    }

    private AuthResult authenticateTenantUserBySubdomain(String email, String password, String subdomain) {
        return authenticateTenantUserBySubdomain(email, password, subdomain, null, null, null);
    }

    private AuthResult authenticateTenantUserBySubdomain(String email, String password, String subdomain,
            String deviceFingerprint, String ipAddress, String userAgent) {
        try {
            String cleanSubdomain = subdomain.trim().toLowerCase();
            if ("admin".equals(cleanSubdomain)) {
                log.debug("Subdomain 'admin' detected, redirecting to platform admin authentication");
                return authenticatePlatformAdmin(email, password);
            }
            Tenant tenant = tenantRepository.findBySubdomain(cleanSubdomain)
                    .orElseThrow(() -> {
                        log.warn("Tenant not found for subdomain: {}", cleanSubdomain);
                        return new InvalidCredentialsException();
                    });
            if (tenant.getStatus() != TenantStatus.ACTIVE) {
                log.warn("Tenant is not active: subdomain={}, status={}", cleanSubdomain, tenant.getStatus());
                throw new InvalidCredentialsException();
            }
            tenantContext.setTenantId(String.valueOf(tenant.getId()));
            tenantContext.setTenantDbName(tenant.getDatabaseName());
            log.debug("TenantContext set: subdomain={}, tenantId={}, dbName={}", cleanSubdomain, tenant.getId(),
                    tenant.getDatabaseName());

            TransactionTemplate transactionTemplate = new TransactionTemplate(tenantTransactionManager);
            return transactionTemplate.execute(status -> {
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> {
                            log.warn("User not found in subdomain: {}", cleanSubdomain);
                            return new InvalidCredentialsException();
                        });

                return authenticateUser(user, password, tenant, deviceFingerprint, ipAddress, userAgent);
            });
        } finally {
            tenantContext.clear();
            MDC.remove("tenantId");
            MDC.remove("tenantDb");
            MDC.remove("correlationId");
            log.debug("TenantContext cleared");
        }
    }

    private AuthResult authenticateUser(User user, String password, Tenant tenant,
            String deviceFingerprint, String ipAddress, String userAgent) {

        Long tenantId = tenant.getId();
        String subdomain = tenant.getSubdomain();

        if (!user.canLogin()) {
            log.warn("User cannot login - userId: {}, isActive: {}, emailVerified: {}, isAccountLocked: {}",
                    user.getId(), user.getIsActive(), user.getEmailVerified(), user.isAccountLocked());
            if (!user.getIsActive()) {
                throw new UserAccountDisabledException();
            } else if (!user.getEmailVerified()) {
                throw new InvalidCredentialsException();
            } else if (user.isAccountLocked()) {
                throw new AccountLockedException(user.getRemainingLockMinutes());
            } else {
                throw new InvalidCredentialsException();
            }
        }
        log.debug("Attempting password verification");
        boolean passwordMatches = passwordEncoder.matches(password, user.getPasswordHash());

        if (!passwordMatches) {
            user.recordFailedLogin();
            userRepository.save(user);
            log.warn("Password verification failed for userId: {}, failed attempts: {}",
                    user.getId(), user.getFailedLoginAttempts());
            if (user.isAccountLocked()) {
                throw new AccountLockedException(user.getRemainingLockMinutes());
            }
            throw new InvalidCredentialsException();
        }

        // Check if 2FA is required
        TwoFactorPolicy twoFactorPolicy = tenant.getTwoFactorPolicy();
        boolean requires2FA = twoFactorPolicy == TwoFactorPolicy.REQUIRED;

        if (requires2FA) {
            // Check if device is trusted
            boolean deviceTrusted = deviceFingerprint != null && !deviceFingerprint.isBlank() &&
                    trustedDeviceService.isDeviceTrusted(user.getId(), deviceFingerprint);

            if (!deviceTrusted) {
                log.info("2FA required for user: {}, generating OTP", maskEmail(user.getEmail()));

                // Check rate limit before generating OTP
                checkOtpRateLimit(user.getEmail());

                // Generate OTP and session token, send OTP via email
                OtpService.LoginOtpResult otpResult = otpService.createLoginOtpToken(user, ipAddress, userAgent);
                Language userLanguage = tenant.getDefaultLanguage() != null ? tenant.getDefaultLanguage() : Language.TR;
                emailService.sendOtpEmail(user.getEmail(), otpResult.otpCode(), userLanguage);

                // Return response with session token (not the OTP)
                return AuthResult.requiring2FA(user.getEmail(), otpResult.sessionToken(), subdomain, tenantId);
            } else {
                log.info("Device trusted for user: {}, skipping 2FA", user.getId());
                // Update last used time for trusted device
                trustedDeviceService.updateLastUsed(user.getId(), deviceFingerprint);
            }
        }

        // Reset failed login attempts on successful login
        user.recordSuccessfulLogin(ipAddress);
        userRepository.save(user);
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getEmail(),
                user.getRole().name(),
                user.getId(),
                tenantId);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

        log.info("Authentication successful for userId: {}", user.getId());

        return AuthResult.success(
                accessToken,
                refreshToken,
                "Bearer",
                jwtTokenProvider.getAccessTokenExpiration(),
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                subdomain,
                tenantId);
    }

    private AuthResult authenticatePlatformAdmin(String email, String password) {
        PlatformAdminUser admin = platformAdminUserRepository
                .findByEmailAndIsActiveTrue(email)
                .orElseThrow(InvalidCredentialsException::new);

        // Check if account is locked
        if (admin.isAccountLocked()) {
            log.warn("Platform admin account is locked: userId={}", admin.getId());
            throw new AccountLockedException(admin.getRemainingLockMinutes());
        }

        boolean passwordMatches = passwordEncoder.matches(password, admin.getPasswordHash());
        if (!passwordMatches) {
            admin.recordFailedLogin();
            platformAdminUserRepository.save(admin);
            log.warn("Password verification failed for platform admin userId: {}, failed attempts: {}",
                    admin.getId(), admin.getFailedLoginAttempts());
            if (admin.isAccountLocked()) {
                throw new AccountLockedException(admin.getRemainingLockMinutes());
            }
            throw new InvalidCredentialsException();
        }

        // Record successful login
        admin.recordSuccessfulLogin(null);
        platformAdminUserRepository.save(admin);

        String accessToken = jwtTokenProvider.createAccessToken(
                admin.getEmail(),
                "SUPER_ADMIN",
                admin.getId(),
                null);
        String refreshToken = jwtTokenProvider.createRefreshToken(admin.getEmail());

        log.info("Authentication successful for platform admin userId: {}", admin.getId());

        return AuthResult.success(
                accessToken,
                refreshToken,
                "Bearer",
                jwtTokenProvider.getAccessTokenExpiration(),
                admin.getId(),
                admin.getEmail(),
                "SUPER_ADMIN",
                null,
                null);
    }

    @Override
    public AuthResult refreshToken(String refreshToken) {
        log.info("Refreshing token");
        if (!jwtTokenProvider.validateToken(refreshToken) ||
                !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }
        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        String role = jwtTokenProvider.getRoleFromToken(refreshToken);
        Long tenantId = jwtTokenProvider.getTenantIdFromToken(refreshToken);

        if ("SUPER_ADMIN".equals(role) && tenantId == null) {
            PlatformAdminUser admin = platformAdminUserRepository
                    .findByEmailAndIsActiveTrue(email)
                    .orElseThrow(() -> new UserNotFoundException(email));

            String newAccessToken = jwtTokenProvider.createAccessToken(
                    admin.getEmail(),
                    "SUPER_ADMIN",
                    admin.getId(),
                    null);
            String newRefreshToken = jwtTokenProvider.createRefreshToken(admin.getEmail());

            log.info("Token refresh successful for platform admin: {}", admin.getEmail());

            return AuthResult.success(
                    newAccessToken,
                    newRefreshToken,
                    "Bearer",
                    jwtTokenProvider.getAccessTokenExpiration(),
                    admin.getId(),
                    admin.getEmail(),
                    "SUPER_ADMIN",
                    null,
                    null);
        } else {
            if (tenantId == null) {
                throw new InvalidTokenException("Tenant ID required for user refresh");
            }
            Tenant tenant = tenantRepository.findById(tenantId)
                    .orElseThrow(() -> new InvalidTokenException("Invalid tenant"));

            if (tenant.getStatus() != TenantStatus.ACTIVE) {
                log.warn("Tenant is not active: tenantId={}, status={}", tenantId, tenant.getStatus());
                throw new InvalidTokenException("Tenant is not active");
            }

            try {
                tenantContext.setTenantId(String.valueOf(tenant.getId()));
                tenantContext.setTenantDbName(tenant.getDatabaseName());
                tenantContext.setSubdomain(tenant.getSubdomain());

                // Populate MDC for logging context
                MDC.put("tenantId", String.valueOf(tenant.getId()));
                MDC.put("tenantDb", tenant.getDatabaseName());
                MDC.put("correlationId", UUID.randomUUID().toString());

                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new UserNotFoundException(email));
                if (!user.canLogin()) {
                    log.warn(
                            "User cannot refresh token - userId: {}, isActive: {}, emailVerified: {}, isAccountLocked: {}",
                            user.getId(), user.getIsActive(), user.getEmailVerified(), user.isAccountLocked());
                    throw new UserAccountDisabledException();
                }
                String newAccessToken = jwtTokenProvider.createAccessToken(
                        user.getEmail(),
                        user.getRole().name(),
                        user.getId(),
                        tenantId);
                String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

                log.info("Token refresh successful for userId: {}, tenantId: {}", user.getId(), tenantId);

                return AuthResult.success(
                        newAccessToken,
                        newRefreshToken,
                        "Bearer",
                        jwtTokenProvider.getAccessTokenExpiration(),
                        user.getId(),
                        user.getEmail(),
                        user.getRole().name(),
                        tenant.getSubdomain(),
                        tenantId);
            } finally {
                tenantContext.clear();
                MDC.remove("tenantId");
                MDC.remove("tenantDb");
                MDC.remove("correlationId");
            }
        }
    }

    @Override
    public void logout(String token) {
        log.info("Logging out user");

        try {
            if (!jwtTokenProvider.validateToken(token)) {
                throw new InvalidTokenException("Invalid token");
            }
            String email = jwtTokenProvider.getEmailFromToken(token);
            String role = jwtTokenProvider.getRoleFromToken(token);
            Long tenantId = jwtTokenProvider.getTenantIdFromToken(token);

            if ("SUPER_ADMIN".equals(role) && tenantId == null) {
                log.info("Logout successful for platform admin: {}", email);
            } else {
                log.info("Logout successful for user: {}", email);
            }
        } catch (Exception ex) {
            log.error("Error during logout: {}", ex.getMessage());
            throw new InvalidTokenException("Logout failed");
        }
    }

    private String resolveTenantSubdomain(Long tenantId) {
        try {
            if (tenantId == null) {
                return null;
            }
            return tenantRepository
                    .findById(tenantId)
                    .map(Tenant::getSubdomain)
                    .orElse(null);
        } catch (Exception e) {
            log.warn("Could not resolve tenant subdomain for id {}: {}", tenantId, e.getMessage());
            return null;
        }
    }

    @Override
    public AuthResult authenticate(String email, String password, Long tenantId, String subdomain,
            String deviceFingerprint, String ipAddress, String userAgent) {
        log.info("Processing authentication request with device info");

        if (tenantId != null) {
            log.debug("Using X-Tenant-ID based authentication: tenantId={}", tenantId);
            return authenticateTenantUserById(email, password, tenantId, deviceFingerprint, ipAddress, userAgent);
        } else if (subdomain != null && !subdomain.trim().isEmpty()) {
            log.debug("Using subdomain-based authentication: subdomain={}", subdomain);
            return authenticateTenantUserBySubdomain(email, password, subdomain, deviceFingerprint, ipAddress,
                    userAgent);
        } else {
            log.debug("Using platform admin authentication");
            return authenticatePlatformAdmin(email, password);
        }
    }

    @Override
    public AuthResult verifyOtp(String pendingToken, String otpCode, boolean trustDevice,
            String deviceFingerprint, String deviceName, String ipAddress, String userAgent,
            Long tenantId, String subdomain) {
        log.info("Verifying OTP");

        Tenant tenant = resolveTenant(tenantId, subdomain);
        if (tenant == null) {
            throw new InvalidTokenException("Invalid tenant");
        }

        try {
            tenantContext.setTenantId(String.valueOf(tenant.getId()));
            tenantContext.setTenantDbName(tenant.getDatabaseName());

            // Populate MDC for logging context
            MDC.put("tenantId", String.valueOf(tenant.getId()));
            MDC.put("tenantDb", tenant.getDatabaseName());
            MDC.put("correlationId", UUID.randomUUID().toString());

            TransactionTemplate transactionTemplate = new TransactionTemplate(tenantTransactionManager);
            return transactionTemplate.execute(status -> {
                String tokenHash = otpService.hashToken(pendingToken);
                VerificationToken token = verificationTokenRepository.findByTokenHash(tokenHash)
                        .orElseThrow(() -> new InvalidTokenException("Invalid or expired OTP session"));

                if (!token.isUsable()) {
                    throw new InvalidTokenException("OTP session has expired or is no longer valid");
                }

                boolean isBypassCode = otpProperties.getBypassCode() != null &&
                        otpProperties.getBypassCode().equals(otpCode);
                String otpHash = otpService.hashToken(otpCode);
                boolean isValid = isBypassCode || token.getTargetValue().equals(otpHash);

                if (isBypassCode) {
                    log.info("OTP bypass code used for user: {}", token.getUser().getEmail());
                }

                if (!isValid) {
                    token.incrementAttempts();
                    verificationTokenRepository.save(token);
                    log.warn("Invalid OTP attempt for userId: {}, remaining attempts: {}",
                            token.getUser().getId(), token.getRemainingAttempts());

                    if (!token.isUsable()) {
                        throw new InvalidTokenException("OTP session has expired due to too many attempts");
                    }
                    throw new InvalidCredentialsException();
                }

                token.markAsUsed();
                verificationTokenRepository.save(token);

                User user = token.getUser();

                if (trustDevice && deviceFingerprint != null && !deviceFingerprint.isBlank()) {
                    TrustedDeviceService.DeviceInfo deviceInfo = new TrustedDeviceService.DeviceInfo(
                            deviceName, null, null, ipAddress);
                    trustedDeviceService.addTrustedDevice(user, deviceFingerprint, deviceInfo);
                    log.info("Device trusted for userId: {}", user.getId());
                }

                user.recordSuccessfulLogin(ipAddress);
                userRepository.save(user);

                String accessToken = jwtTokenProvider.createAccessToken(
                        user.getEmail(),
                        user.getRole().name(),
                        user.getId(),
                        tenant.getId());
                String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

                log.info("OTP verification successful for userId: {}", user.getId());

                return AuthResult.success(
                        accessToken,
                        refreshToken,
                        "Bearer",
                        jwtTokenProvider.getAccessTokenExpiration(),
                        user.getId(),
                        user.getEmail(),
                        user.getRole().name(),
                        tenant.getSubdomain(),
                        tenant.getId());
            });
        } finally {
            tenantContext.clear();
            MDC.remove("tenantId");
            MDC.remove("tenantDb");
            MDC.remove("correlationId");
        }
    }

    @Override
    public void requestPasswordReset(String email, Long tenantId, String subdomain,
            String ipAddress, String userAgent, Language language) {
        log.info("Password reset requested");

        Tenant tenant = resolveTenant(tenantId, subdomain);
        if (tenant == null) {
            log.warn("Tenant not found for password reset request");
            return;
        }

        try {
            tenantContext.setTenantId(String.valueOf(tenant.getId()));
            tenantContext.setTenantDbName(tenant.getDatabaseName());

            // Populate MDC for logging context
            MDC.put("tenantId", String.valueOf(tenant.getId()));
            MDC.put("tenantDb", tenant.getDatabaseName());
            MDC.put("correlationId", UUID.randomUUID().toString());

            TransactionTemplate transactionTemplate = new TransactionTemplate(tenantTransactionManager);
            transactionTemplate.executeWithoutResult(status -> {
                User user = userRepository.findByEmail(email).orElse(null);
                if (user == null) {
                    log.warn("User not found for password reset");
                    return;
                }

                var tokenResult = otpService.createPasswordResetToken(user, ipAddress, userAgent);
                emailService.sendPasswordResetEmail(email, tokenResult.plainToken(), tenant.getSubdomain(), language);

                log.info("Password reset email sent for userId: {}", user.getId());
            });
        } finally {
            tenantContext.clear();
            MDC.remove("tenantId");
            MDC.remove("tenantDb");
            MDC.remove("correlationId");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TokenValidationResult validateResetToken(String token) {
        if (!tenantContext.isSet()) {
            throw new IllegalStateException("Tenant context is required for token validation");
        }

        if (!tenantContext.isActive()) {
            throw new IllegalStateException("Tenant is not active");
        }

        String tokenHash = otpService.hashToken(token);
        VerificationToken verificationToken = verificationTokenRepository.findByTokenHash(tokenHash)
                .orElse(null);

        if (verificationToken == null || !verificationToken.isUsable()) {
            return TokenValidationResult.invalid();
        }

        String maskedEmail = maskEmail(verificationToken.getUser().getEmail());
        return TokenValidationResult.valid(
                maskedEmail,
                verificationToken.getTokenType().name(),
                verificationToken.getExpiryMinutes());
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        log.info("Resetting password with token");

        TransactionTemplate transactionTemplate = new TransactionTemplate(tenantTransactionManager);
        transactionTemplate.executeWithoutResult(status -> {
            String tokenHash = otpService.hashToken(token);
            VerificationToken verificationToken = verificationTokenRepository.findByTokenHash(tokenHash)
                    .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));

            if (!verificationToken.isUsable() || verificationToken.getTokenType() != TokenType.PASSWORD_RESET) {
                throw new InvalidTokenException("Reset token is no longer valid");
            }

            User user = verificationToken.getUser();
            String passwordHash = passwordEncoder.encode(newPassword);
            user.changePassword(passwordHash);
            userRepository.save(user);

            verificationToken.markAsUsed();
            verificationTokenRepository.save(verificationToken);

            otpService.revokeAllUserTokens(user.getId(), TokenType.PASSWORD_RESET);

            log.info("Password reset successful for userId: {}", user.getId());
        });
    }

    @Override
    public void requestEmailVerification(Long userId, String ipAddress, String userAgent, Language language) {
        log.info("Email verification requested for userId: {}", userId);

        TransactionTemplate transactionTemplate = new TransactionTemplate(tenantTransactionManager);
        transactionTemplate.executeWithoutResult(status -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException(userId));

            String subdomain = tenantContext.getSubdomain();
            if (subdomain == null) {
                String tenantId = tenantContext.getTenantId();
                if (tenantId != null) {
                    try {
                        long tenantIdLong = Long.parseLong(tenantId);
                        Tenant tenant = tenantRepository.findById(tenantIdLong).orElse(null);
                        if (tenant != null) {
                            subdomain = tenant.getSubdomain();
                        }
                    } catch (NumberFormatException ex) {
                        log.warn("Invalid tenantId format in tenant context: '{}'", tenantId);
                    }
                }
            }

            var tokenResult = otpService.createEmailVerificationToken(user, ipAddress, userAgent);
            emailService.sendEmailVerificationEmail(user.getEmail(), tokenResult.plainToken(), subdomain, language);

            log.info("Email verification sent for userId: {}", userId);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public TokenValidationResult validateEmailVerificationToken(String token) {
        if (!tenantContext.isSet()) {
            throw new IllegalStateException("Tenant context is required for token validation");
        }

        if (!tenantContext.isActive()) {
            throw new IllegalStateException("Tenant is not active");
        }

        String tokenHash = otpService.hashToken(token);
        VerificationToken verificationToken = verificationTokenRepository.findByTokenHash(tokenHash)
                .orElse(null);

        if (verificationToken == null || !verificationToken.isUsable()) {
            return TokenValidationResult.invalid();
        }

        String maskedEmail = maskEmail(verificationToken.getUser().getEmail());
        return TokenValidationResult.valid(
                maskedEmail,
                verificationToken.getTokenType().name(),
                verificationToken.getExpiryMinutes());
    }

    @Override
    public AuthResult setInitialPassword(
            String token,
            String password,
            String deviceFingerprint,
            boolean trustDevice,
            String deviceName,
            String ipAddress,
            String userAgent) {

        log.info("Setting initial password with verification token");

        return new TransactionTemplate(tenantTransactionManager).execute(status -> {
            // 1. Validate token
            String tokenHash = otpService.hashToken(token);
            VerificationToken verificationToken = verificationTokenRepository.findByTokenHash(tokenHash)
                    .orElseThrow(() -> new InvalidTokenException("Invalid or expired verification token"));

            if (!verificationToken.isUsable() || verificationToken.getTokenType() != TokenType.EMAIL_VERIFY) {
                throw new InvalidTokenException("Verification token is no longer valid");
            }

            // 2. Set password + verify email
            User user = verificationToken.getUser();
            String passwordHash = passwordEncoder.encode(password);
            user.changePassword(passwordHash);
            user.setEmailVerified(true);
            user.recordSuccessfulLogin(ipAddress);
            userRepository.save(user);

            // 3. Mark token as used + revoke all email verification tokens
            verificationToken.markAsUsed();
            verificationTokenRepository.save(verificationToken);
            otpService.revokeAllUserTokens(user.getId(), TokenType.EMAIL_VERIFY);

            log.info("Initial password set and email verified for userId: {}", user.getId());

            // 4. Register trusted device if requested
            if (trustDevice && deviceFingerprint != null && !deviceFingerprint.isBlank()) {
                TrustedDeviceService.DeviceInfo deviceInfo = new TrustedDeviceService.DeviceInfo(
                        deviceName, null, null, ipAddress);
                trustedDeviceService.addTrustedDevice(user, deviceFingerprint, deviceInfo);
                log.info("Device trusted for userId: {}", user.getId());
            }

            // 5. Get tenant info
            String tenantIdStr = tenantContext.getTenantId();
            if (tenantIdStr == null) {
                throw new IllegalStateException("Tenant context is not set");
            }
            Long tenantId = Long.parseLong(tenantIdStr);
            Tenant tenant = tenantRepository.findById(tenantId)
                    .orElseThrow(() -> new IllegalStateException("Tenant not found"));

            // 6. Generate JWT tokens
            String accessToken = jwtTokenProvider.createAccessToken(
                    user.getEmail(),
                    user.getRole().name(),
                    user.getId(),
                    tenantId);
            String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

            // 7. Return AuthResult with tokens
            return AuthResult.success(
                    accessToken,
                    refreshToken,
                    "Bearer",
                    jwtTokenProvider.getAccessTokenExpiration(),
                    user.getId(),
                    user.getEmail(),
                    user.getRole().name(),
                    tenant.getSubdomain(),
                    tenant.getId());
        });
    }

    private Tenant resolveTenant(Long tenantId, String subdomain) {
        if (tenantId != null) {
            return tenantRepository.findById(tenantId)
                    .filter(t -> t.getStatus() == TenantStatus.ACTIVE)
                    .orElse(null);
        }
        if (subdomain != null && !subdomain.isBlank()) {
            return tenantRepository.findBySubdomain(subdomain.trim().toLowerCase())
                    .filter(t -> t.getStatus() == TenantStatus.ACTIVE)
                    .orElse(null);
        }
        return null;
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) {
            return email.charAt(0) + "***" + email.substring(atIndex);
        }
        return email.charAt(0) + "***" + email.substring(atIndex - 1);
    }

    private void checkOtpRateLimit(String email) {
        // Include tenant context in rate limit key to prevent cross-tenant throttling
        String tenantKey = tenantContext.getTenantId();
        if (tenantKey == null) {
            tenantKey = "platform";
        }
        String key = tenantKey + ":" + email.toLowerCase();
        long currentTime = System.currentTimeMillis();

        otpRateLimiters.compute(key, (k, entry) -> {
            if (entry == null || currentTime - entry.windowStart > OTP_RATE_LIMIT_WINDOW_SECONDS * 1000L) {
                return new OtpRateLimitEntry(currentTime, 1);
            }
            entry.requestCount++;
            return entry;
        });

        OtpRateLimitEntry entry = otpRateLimiters.get(key);
        if (entry != null && entry.requestCount > OTP_MAX_REQUESTS_PER_WINDOW) {
            long remainingSeconds = OTP_RATE_LIMIT_WINDOW_SECONDS -
                    (currentTime - entry.windowStart) / 1000;
            log.warn("OTP rate limit exceeded for email: {}", email);
            throw new OtpRateLimitExceededException(
                    "Too many OTP requests. Please try again later.",
                    (int) Math.max(remainingSeconds, 60));
        }
    }

    /**
     * Scheduled cleanup of expired rate limit entries to prevent memory leaks.
     * Runs every 5 minutes.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void cleanupExpiredRateLimiters() {
        long cutoff = System.currentTimeMillis() - OTP_RATE_LIMIT_WINDOW_SECONDS * 1000L;
        int removedCount = 0;

        var iterator = otpRateLimiters.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (entry.getValue().windowStart < cutoff) {
                iterator.remove();
                removedCount++;
            }
        }

        if (removedCount > 0) {
            log.debug("Cleaned up {} expired OTP rate limit entries", removedCount);
        }
    }

    private static class OtpRateLimitEntry {
        long windowStart;
        int requestCount;

        OtpRateLimitEntry(long windowStart, int requestCount) {
            this.windowStart = windowStart;
            this.requestCount = requestCount;
        }
    }
}
