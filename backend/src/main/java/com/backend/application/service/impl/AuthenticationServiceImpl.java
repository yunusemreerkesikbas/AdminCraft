package com.backend.application.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HexFormat;
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
import com.backend.domain.entity.PlatformRefreshToken;
import com.backend.domain.entity.RefreshToken;
import com.backend.domain.entity.Tenant;
import com.backend.domain.entity.User;
import com.backend.domain.entity.VerificationToken;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.TenantStatus;
import com.backend.domain.enums.TokenStatus;
import com.backend.domain.enums.TokenType;
import com.backend.domain.enums.TwoFactorPolicy;
import com.backend.domain.enums.UserRole;
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
import com.backend.domain.entity.PlatformAdminUser;
import com.backend.domain.entity.PlatformVerificationToken;
import com.backend.domain.port.JwtProviderPort;
import com.backend.domain.port.OtpConfig;
import com.backend.domain.port.PlatformSettingsPort;
import com.backend.domain.repository.PlatformAdminUserRepository;
import com.backend.domain.repository.PlatformRefreshTokenRepository;
import com.backend.domain.repository.PlatformVerificationTokenRepository;
import com.backend.domain.repository.RefreshTokenRepository;

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
    private final JwtProviderPort jwtProviderPort;
    private final PasswordEncoder passwordEncoder;
    private final TenantRepository tenantRepository;
    private final PlatformAdminUserRepository platformAdminUserRepository;
    private final PlatformSettingsPort platformSettingsPort;
    private final PlatformVerificationTokenRepository platformVerificationTokenRepository;
    private final TenantContextPort tenantContext;
    private final OtpService otpService;
    private final EmailService emailService;
    private final TrustedDeviceService trustedDeviceService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final OtpConfig otpConfig;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PlatformRefreshTokenRepository platformRefreshTokenRepository;

    @Qualifier("tenantTransactionManager")
    private final PlatformTransactionManager tenantTransactionManager;

    @Qualifier("platformTransactionManager")
    private final PlatformTransactionManager platformTransactionManager;

    @Override
    public AuthResult authenticate(String email, String password, Long tenantId, String subdomain) {
        return authenticate(email, password, tenantId, subdomain, null, null, null, false);
    }

    @Override
    public AuthResult authenticate(String email, String password, Long tenantId, String subdomain,
            String deviceFingerprint, String ipAddress, String userAgent) {
        return authenticate(email, password, tenantId, subdomain, deviceFingerprint, ipAddress, userAgent, false);
    }

    @Override
    public AuthResult authenticate(String email, String password, Long tenantId, String subdomain,
            String deviceFingerprint, String ipAddress, String userAgent, boolean rememberMe) {
        log.info("Processing authentication request");

        if (tenantId != null) {
            log.debug("Using X-Tenant-ID based authentication: tenantId={}", tenantId);
            return authenticateTenantUserById(email, password, tenantId, deviceFingerprint, ipAddress, userAgent, rememberMe);
        } else if (subdomain != null && !subdomain.trim().isEmpty()) {
            log.debug("Using subdomain-based authentication: subdomain={}", subdomain);
            return authenticateTenantUserBySubdomain(email, password, subdomain, deviceFingerprint, ipAddress, userAgent, rememberMe);
        } else {
            log.debug("Using platform admin authentication");
            return authenticatePlatformAdmin(email, password, ipAddress, userAgent, rememberMe);
        }
    }

    private AuthResult authenticateTenantUserById(String email, String password, Long tenantId,
            String deviceFingerprint, String ipAddress, String userAgent, boolean rememberMe) {
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

                return authenticateUser(user, password, tenant, deviceFingerprint, ipAddress, userAgent, rememberMe);
            });
        } finally {
            tenantContext.clear();
            MDC.remove("tenantId");
            MDC.remove("tenantDb");
            MDC.remove("correlationId");
            log.debug("TenantContext cleared");
        }
    }

    private AuthResult authenticateTenantUserBySubdomain(String email, String password, String subdomain,
            String deviceFingerprint, String ipAddress, String userAgent, boolean rememberMe) {
        try {
            String cleanSubdomain = subdomain.trim().toLowerCase();
            if ("admin".equals(cleanSubdomain)) {
                log.debug("Subdomain 'admin' detected, redirecting to platform admin authentication");
                return authenticatePlatformAdmin(email, password, ipAddress, userAgent, rememberMe);
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

                return authenticateUser(user, password, tenant, deviceFingerprint, ipAddress, userAgent, rememberMe);
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
            String deviceFingerprint, String ipAddress, String userAgent, boolean rememberMe) {

        Long tenantId = tenant.getId();
        String subdomain = tenant.getSubdomain();

        boolean loginAllowed = user.getRole() == UserRole.TENANT_ADMIN
                ? (Boolean.TRUE.equals(user.getIsActive()) && !user.isAccountLocked())
                : user.canLogin();
        if (!loginAllowed) {
            log.warn("User cannot login - userId: {}, isActive: {}, emailVerified: {}, isAccountLocked: {}",
                    user.getId(), user.getIsActive(), user.getEmailVerified(), user.isAccountLocked());
            if (!Boolean.TRUE.equals(user.getIsActive())) {
                throw new UserAccountDisabledException();
            } else if (user.getRole() != UserRole.TENANT_ADMIN && !Boolean.TRUE.equals(user.getEmailVerified())) {
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

        TwoFactorPolicy twoFactorPolicy = tenant.getTwoFactorPolicy();
        boolean requires2FA = twoFactorPolicy == TwoFactorPolicy.REQUIRED;

        if (requires2FA) {
            boolean deviceTrusted = deviceFingerprint != null && !deviceFingerprint.isBlank() &&
                    trustedDeviceService.isDeviceTrusted(user.getId(), deviceFingerprint);

            if (!deviceTrusted) {
                log.info("2FA required for user: {}, generating OTP", maskEmail(user.getEmail()));

                checkOtpRateLimit(user.getEmail());

                OtpService.LoginOtpResult otpResult = otpService.createLoginOtpToken(user, ipAddress, userAgent);
                Language userLanguage = tenant.getDefaultLanguage() != null ? tenant.getDefaultLanguage() : Language.TR;
                emailService.sendOtpEmail(user.getEmail(), otpResult.otpCode(), userLanguage);

                return AuthResult.requiring2FA(user.getEmail(), otpResult.sessionToken(), subdomain, tenantId);
            } else {
                log.info("Device trusted for user: {}, skipping 2FA", user.getId());
                trustedDeviceService.updateLastUsed(user.getId(), deviceFingerprint);
            }
        }

        user.recordSuccessfulLogin(ipAddress);
        userRepository.save(user);
        String accessToken = jwtProviderPort.createAccessToken(
                user.getEmail(),
                user.getRole().name(),
                user.getId(),
                tenantId);
        String refreshToken = jwtProviderPort.createRefreshToken(
                user.getEmail(),
                user.getRole().name(),
                user.getId(),
                tenantId,
                rememberMe);

        saveTenantRefreshToken(user, refreshToken, rememberMe);

        log.info("Authentication successful for userId: {}", user.getId());

        return AuthResult.success(
                accessToken,
                refreshToken,
                "Bearer",
                jwtProviderPort.getAccessTokenExpiration() / 1000,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                subdomain,
                tenantId,
                rememberMe);
    }

    private AuthResult authenticatePlatformAdmin(String email, String password, String ipAddress, String userAgent, boolean rememberMe) {
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

        if (isPlatformTwoFactorRequired()) {
            checkOtpRateLimit(admin.getEmail());
            PlatformLoginOtpResult otpResult = createPlatformLoginOtpToken(admin, ipAddress, userAgent);
            Language language = resolvePlatformLanguage();
            emailService.sendOtpEmail(admin.getEmail(), otpResult.otpCode(), language);
            return AuthResult.requiring2FA(admin.getEmail(), otpResult.sessionToken(), null, null);
        }

        // Record successful login
        admin.recordSuccessfulLogin(ipAddress);
        platformAdminUserRepository.save(admin);

        String accessToken = jwtProviderPort.createAccessToken(
                admin.getEmail(),
                "SUPER_ADMIN",
                admin.getId(),
                null);
        String refreshToken = jwtProviderPort.createRefreshToken(
                admin.getEmail(),
                "SUPER_ADMIN",
                admin.getId(),
                null,
                rememberMe);

        savePlatformRefreshToken(admin.getId(), refreshToken, rememberMe);

        log.info("Authentication successful for platform admin userId: {}", admin.getId());

        return AuthResult.success(
                accessToken,
                refreshToken,
                "Bearer",
                jwtProviderPort.getAccessTokenExpiration() / 1000,
                admin.getId(),
                admin.getEmail(),
                admin.getFullName(),
                "SUPER_ADMIN",
                null,
                null,
                rememberMe);
    }

    @Override
    public AuthResult refreshToken(String refreshToken, String deviceFingerprint, String ipAddress, String userAgent) {
        log.info("Refreshing token");
        if (!jwtProviderPort.validateToken(refreshToken) ||
                !jwtProviderPort.isRefreshToken(refreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        String tokenHash = hashRefreshToken(refreshToken);
        String role = jwtProviderPort.getRoleFromToken(refreshToken);
        String email = jwtProviderPort.getEmailFromToken(refreshToken);
        Long tenantId = jwtProviderPort.getTenantIdFromToken(refreshToken);

        if ("SUPER_ADMIN".equals(role) && tenantId == null) {
            boolean wasRememberMe = jwtProviderPort.isRememberMeToken(refreshToken);
            PlatformAdminUser admin = platformAdminUserRepository
                    .findByEmailAndIsActiveTrue(email)
                    .orElseThrow(() -> new UserNotFoundException(email));

            if (isPlatformTwoFactorRequired()) {
                checkOtpRateLimit(admin.getEmail());
                PlatformLoginOtpResult otpResult = createPlatformLoginOtpToken(admin, ipAddress, userAgent);
                Language language = resolvePlatformLanguage();
                emailService.sendOtpEmail(admin.getEmail(), otpResult.otpCode(), language);
                return AuthResult.requiring2FA(admin.getEmail(), otpResult.sessionToken(), null, null);
            }

            String newAccessToken = jwtProviderPort.createAccessToken(
                    admin.getEmail(),
                    "SUPER_ADMIN",
                    admin.getId(),
                    null);
            String newRefreshToken = jwtProviderPort.createRefreshToken(
                    admin.getEmail(),
                    "SUPER_ADMIN",
                    admin.getId(),
                    null,
                    wasRememberMe);

            TransactionTemplate platformTxTemplate = new TransactionTemplate(platformTransactionManager);
            platformTxTemplate.executeWithoutResult(status -> {
                int revoked = platformRefreshTokenRepository.revokeByTokenHash(tokenHash);
                if (revoked == 0) {
                    throw new InvalidTokenException("Refresh token has been revoked or expired");
                }
                savePlatformRefreshToken(admin.getId(), newRefreshToken, wasRememberMe);
            });

            log.info("Token refresh successful for platform admin: {}", admin.getEmail());

            return AuthResult.success(
                    newAccessToken,
                    newRefreshToken,
                    "Bearer",
                    jwtProviderPort.getAccessTokenExpiration() / 1000,
                    admin.getId(),
                    admin.getEmail(),
                    admin.getFullName(),
                    "SUPER_ADMIN",
                    null,
                    null,
                    wasRememberMe);
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

                MDC.put("tenantId", String.valueOf(tenant.getId()));
                MDC.put("tenantDb", tenant.getDatabaseName());
                MDC.put("correlationId", UUID.randomUUID().toString());

                TransactionTemplate transactionTemplate = new TransactionTemplate(tenantTransactionManager);
                return transactionTemplate.execute(status -> {
                    int revoked = refreshTokenRepository.revokeByTokenHash(tokenHash);
                    if (revoked == 0) {
                        throw new InvalidTokenException("Refresh token has been revoked or expired");
                    }

                    User user = userRepository.findByEmail(email)
                            .orElseThrow(() -> new UserNotFoundException(email));
                    boolean refreshAllowed = user.getRole() == UserRole.TENANT_ADMIN
                            ? (Boolean.TRUE.equals(user.getIsActive()) && !user.isAccountLocked())
                            : user.canLogin();
                    if (!refreshAllowed) {
                        log.warn(
                                "User cannot refresh token - userId: {}, isActive: {}, emailVerified: {}, isAccountLocked: {}",
                                user.getId(), user.getIsActive(), user.getEmailVerified(), user.isAccountLocked());
                        throw new UserAccountDisabledException();
                    }

                    if (tenant.getTwoFactorPolicy() == TwoFactorPolicy.REQUIRED) {
                        boolean deviceTrusted = deviceFingerprint != null && !deviceFingerprint.isBlank() &&
                                trustedDeviceService.isDeviceTrusted(user.getId(), deviceFingerprint);
                        if (!deviceTrusted) {
                            log.info("2FA required on refresh for userId: {}", user.getId());
                            checkOtpRateLimit(user.getEmail());
                            OtpService.LoginOtpResult otpResult = otpService.createLoginOtpToken(user, ipAddress, userAgent);
                            Language userLanguage = tenant.getDefaultLanguage() != null ? tenant.getDefaultLanguage() : Language.TR;
                            emailService.sendOtpEmail(user.getEmail(), otpResult.otpCode(), userLanguage);
                            return AuthResult.requiring2FA(user.getEmail(), otpResult.sessionToken(), tenant.getSubdomain(), tenantId);
                        }
                        trustedDeviceService.updateLastUsed(user.getId(), deviceFingerprint);
                    }

                    boolean wasRememberMe = jwtProviderPort.isRememberMeToken(refreshToken);

                    String newAccessToken = jwtProviderPort.createAccessToken(
                            user.getEmail(),
                            user.getRole().name(),
                            user.getId(),
                            tenantId);
                    String newRefreshToken = jwtProviderPort.createRefreshToken(
                            user.getEmail(),
                            user.getRole().name(),
                            user.getId(),
                            tenantId,
                            wasRememberMe);

                    saveTenantRefreshToken(user, newRefreshToken, wasRememberMe);

                    log.info("Token refresh successful for userId: {}, tenantId: {}", user.getId(), tenantId);

                    return AuthResult.success(
                            newAccessToken,
                            newRefreshToken,
                            "Bearer",
                            jwtProviderPort.getAccessTokenExpiration() / 1000,
                            user.getId(),
                            user.getEmail(),
                            user.getFullName(),
                            user.getRole().name(),
                            tenant.getSubdomain(),
                            tenantId,
                            wasRememberMe);
                });
            } finally {
                tenantContext.clear();
                MDC.remove("tenantId");
                MDC.remove("tenantDb");
                MDC.remove("correlationId");
            }
        }
    }

    @Override
    public void logout(String accessToken, String refreshToken) {
        log.info("Logging out user");

        try {
            if (accessToken != null && jwtProviderPort.validateToken(accessToken)) {
                String role = jwtProviderPort.getRoleFromToken(accessToken);
                Long userId = jwtProviderPort.getUserIdFromToken(accessToken);
                if ("SUPER_ADMIN".equals(role)) {
                    log.info("Logout for platform admin: userId={}", userId);
                } else {
                    log.info("Logout for user: userId={}", userId);
                }
            }
        } catch (Exception ex) {
            log.warn("Could not validate access token during logout: {}",
                    ex.getMessage() != null ? ex.getMessage().substring(0, Math.min(500, ex.getMessage().length())) : "null");
        }

        if (refreshToken != null && !refreshToken.isBlank()) {
            try {
                String tokenHash = hashRefreshToken(refreshToken);
                String role = jwtProviderPort.getRoleFromToken(refreshToken);
                if ("SUPER_ADMIN".equals(role)) {
                    platformRefreshTokenRepository.revokeByTokenHash(tokenHash);
                    log.info("Platform refresh token revoked on logout");
                } else {
                    Long tenantId = jwtProviderPort.getTenantIdFromToken(refreshToken);
                    if (tenantId != null) {
                        Tenant tenant = tenantRepository.findById(tenantId).orElse(null);
                        if (tenant != null) {
                            try {
                                tenantContext.setTenantId(String.valueOf(tenant.getId()));
                                tenantContext.setTenantDbName(tenant.getDatabaseName());
                                refreshTokenRepository.revokeByTokenHash(tokenHash);
                                log.info("Refresh token revoked on logout");
                            } finally {
                                tenantContext.clear();
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                log.warn("Could not revoke refresh token during logout: {}", ex.getMessage());
            }
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
    public AuthResult verifyOtp(String pendingToken, String otpCode, boolean trustDevice,
            String deviceFingerprint, String deviceName, String ipAddress, String userAgent,
            Long tenantId, String subdomain, boolean rememberMe) {
        log.info("Verifying OTP");

        if (isPlatformOtpRequest(tenantId, subdomain)) {
            return verifyPlatformOtp(pendingToken, otpCode, ipAddress, rememberMe);
        }

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

                boolean isBypassCode = otpConfig.getBypassCode() != null &&
                        otpConfig.getBypassCode().equals(otpCode);
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

                String accessToken = jwtProviderPort.createAccessToken(
                        user.getEmail(),
                        user.getRole().name(),
                        user.getId(),
                        tenant.getId());
                String refreshToken = jwtProviderPort.createRefreshToken(
                        user.getEmail(),
                        user.getRole().name(),
                        user.getId(),
                        tenant.getId(),
                        rememberMe);

                saveTenantRefreshToken(user, refreshToken, rememberMe);

                log.info("OTP verification successful for userId: {}", user.getId());

                return AuthResult.success(
                        accessToken,
                        refreshToken,
                        "Bearer",
                        jwtProviderPort.getAccessTokenExpiration() / 1000,
                        user.getId(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getRole().name(),
                        tenant.getSubdomain(),
                        tenant.getId(),
                        rememberMe);
            });
        } finally {
            tenantContext.clear();
            MDC.remove("tenantId");
            MDC.remove("tenantDb");
            MDC.remove("correlationId");
        }
    }

    @Transactional("platformTransactionManager")
    public AuthResult verifyPlatformOtp(String pendingToken, String otpCode, String ipAddress, boolean rememberMe) {
        String tokenHash = otpService.hashToken(pendingToken);
        PlatformVerificationToken token = platformVerificationTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired OTP session"));

        if (!token.isUsable()) {
            throw new InvalidTokenException("OTP session has expired or is no longer valid");
        }

        boolean isBypassCode = otpConfig.getBypassCode() != null &&
                otpConfig.getBypassCode().equals(otpCode);
        String otpHash = otpService.hashToken(otpCode);
        boolean isValid = isBypassCode || otpHash.equals(token.getTargetValue());

        if (!isValid) {
            token.incrementAttempts();
            platformVerificationTokenRepository.save(token);
            log.warn("Invalid OTP attempt for platform admin userId: {}, remaining attempts: {}",
                    token.getAdminUser().getId(), token.getRemainingAttempts());

            if (!token.isUsable()) {
                throw new InvalidTokenException("OTP session has expired due to too many attempts");
            }
            throw new InvalidCredentialsException();
        }

        token.markAsUsed();
        platformVerificationTokenRepository.save(token);

        PlatformAdminUser admin = token.getAdminUser();
        if (admin == null || !Boolean.TRUE.equals(admin.getIsActive())) {
            throw new InvalidTokenException("Platform admin account is not active");
        }

        admin.recordSuccessfulLogin(ipAddress);
        platformAdminUserRepository.save(admin);

        String accessToken = jwtProviderPort.createAccessToken(
                admin.getEmail(),
                "SUPER_ADMIN",
                admin.getId(),
                null);
        String refreshToken = jwtProviderPort.createRefreshToken(
                admin.getEmail(),
                "SUPER_ADMIN",
                admin.getId(),
                null,
                rememberMe);

        savePlatformRefreshToken(admin.getId(), refreshToken, rememberMe);

        log.info("OTP verification successful for platform admin userId: {}", admin.getId());

        return AuthResult.success(
                accessToken,
                refreshToken,
                "Bearer",
                jwtProviderPort.getAccessTokenExpiration() / 1000,
                admin.getId(),
                admin.getEmail(),
                admin.getFullName(),
                "SUPER_ADMIN",
                null,
                null,
                rememberMe);
    }

    @Override
    public void requestPasswordReset(String email, Long tenantId, String subdomain,
            String ipAddress, String userAgent, Language language) {
        log.info("Password reset requested");

        boolean hasTenantIdentifier = tenantId != null || (subdomain != null && !subdomain.isBlank());
        Tenant tenant = resolveTenant(tenantId, subdomain);
        if (tenant == null && !hasTenantIdentifier) {
            tenant = resolveTenantFromContext();
        }
        if (tenant == null) {
            log.warn("Tenant not found for password reset request");
            return;
        }

        Tenant resolvedTenant = tenant;
        try {
            tenantContext.setTenantId(String.valueOf(resolvedTenant.getId()));
            tenantContext.setTenantDbName(resolvedTenant.getDatabaseName());
            tenantContext.setSubdomain(resolvedTenant.getSubdomain());

            MDC.put("tenantId", String.valueOf(resolvedTenant.getId()));
            MDC.put("tenantDb", resolvedTenant.getDatabaseName());
            MDC.put("correlationId", UUID.randomUUID().toString());

            TransactionTemplate transactionTemplate = new TransactionTemplate(tenantTransactionManager);
            transactionTemplate.executeWithoutResult(status -> {
                User user = userRepository.findByEmail(email).orElse(null);
                if (user == null) {
                    log.warn("User not found for password reset");
                    return;
                }

                var tokenResult = otpService.createPasswordResetToken(user, ipAddress, userAgent);
                emailService.sendPasswordResetEmail(email, tokenResult.plainToken(), resolvedTenant.getSubdomain(), language);

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
            user.setEmailVerified(true);
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
    public void setInitialPassword(String token, String password) {
        log.info("Setting initial password with verification token");

        new TransactionTemplate(tenantTransactionManager).executeWithoutResult(status -> {
            String tokenHash = otpService.hashToken(token);
            VerificationToken verificationToken = verificationTokenRepository.findByTokenHash(tokenHash)
                    .orElseThrow(() -> new InvalidTokenException("Invalid or expired verification token"));

            if (!verificationToken.isUsable() || verificationToken.getTokenType() != TokenType.EMAIL_VERIFY) {
                throw new InvalidTokenException("Verification token is no longer valid");
            }

            User user = verificationToken.getUser();
            String passwordHash = passwordEncoder.encode(password);
            user.changePassword(passwordHash);
            user.setEmailVerified(true);
            userRepository.save(user);

            verificationToken.markAsUsed();
            verificationTokenRepository.save(verificationToken);
            otpService.revokeAllUserTokens(user.getId(), TokenType.EMAIL_VERIFY);

            log.info("Initial password set and email verified for userId: {}", user.getId());
        });
    }

    private boolean isPlatformTwoFactorRequired() {
        TwoFactorPolicy policy = platformSettingsPort.getSingleton().getTwoFactorPolicy();
        return policy == TwoFactorPolicy.REQUIRED;
    }

    private Language resolvePlatformLanguage() {
        String languageCode = platformSettingsPort.getSingleton().getDefaultLanguage();
        return Language.fromCodeOrDefault(languageCode, Language.TR);
    }

    @Transactional("platformTransactionManager")
    public PlatformLoginOtpResult createPlatformLoginOtpToken(
            PlatformAdminUser admin,
            String ipAddress,
            String userAgent) {
        platformVerificationTokenRepository.revokeAllActiveTokensForAdmin(admin.getId(), TokenType.LOGIN_OTP);

        String otp = otpService.generateOtp();
        String sessionToken = UUID.randomUUID().toString();
        String sessionTokenHash = otpService.hashToken(sessionToken);

        PlatformVerificationToken token = PlatformVerificationToken.builder()
                .adminUser(admin)
                .tokenHash(sessionTokenHash)
                .tokenType(TokenType.LOGIN_OTP)
                .status(TokenStatus.ACTIVE)
                .targetValue(otpService.hashToken(otp))
                .expiresAt(LocalDateTime.now().plusSeconds(otpConfig.getExpirySeconds()))
                .attemptCount(0)
                .maxAttempts(otpConfig.getMaxAttempts())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        platformVerificationTokenRepository.save(token);
        return new PlatformLoginOtpResult(otp, sessionToken);
    }

    /**
     * Determines if the OTP verification request is for platform admin.
     * 
     * <p><b>Platform Routing Rules:</b></p>
     * <ul>
     *   <li><b>Platform Admin:</b> {@code tenantId == null} AND 
     *       ({@code subdomain == null} OR {@code subdomain == "admin"})</li>
     *   <li><b>Tenant User:</b> {@code tenantId != null}</li>
     * </ul>
     * 
     * <p><b>Client Contract:</b></p>
     * Clients must provide consistent {@code tenantId} and {@code subdomain} values 
     * across authentication flow:
     * <ul>
     *   <li>Platform: Send {@code tenantId=null} and {@code subdomain="admin"}</li>
     *   <li>Tenant: Send valid {@code tenantId} and {@code subdomain}</li>
     * </ul>
     * 
     * @param tenantId tenant ID from request (null for platform admin)
     * @param subdomain subdomain from request (null/blank/"admin" for platform admin)
     * @return true if platform OTP request, false if tenant OTP request
     * 
     * @see #authenticatePlatformAdmin
     * @see #authenticateTenantUser
     */
    private boolean isPlatformOtpRequest(Long tenantId, String subdomain) {
        if (tenantId != null) {
            return false;
        }

        if (subdomain == null || subdomain.isBlank()) {
            return true;
        }

        return "admin".equalsIgnoreCase(subdomain.trim());
    }

    private Tenant resolveTenant(Long tenantId, String subdomain) {
        if (tenantId != null) {
            Tenant tenant = tenantRepository.findById(tenantId)
                    .filter(t -> t.getStatus() == TenantStatus.ACTIVE)
                    .orElse(null);
            if (tenant == null || subdomain == null || subdomain.isBlank()) {
                return tenant;
            }
            if (!tenant.getSubdomain().equalsIgnoreCase(subdomain.trim())) {
                log.warn("Tenant ID and subdomain mismatch for auth request");
                return null;
            }
            return tenant;
        }
        if (subdomain != null && !subdomain.isBlank()) {
            return tenantRepository.findBySubdomain(subdomain.trim().toLowerCase())
                    .filter(t -> t.getStatus() == TenantStatus.ACTIVE)
                    .orElse(null);
        }
        return null;
    }

    private Tenant resolveTenantFromContext() {
        if (!tenantContext.isSet()) {
            return null;
        }

        String tenantId = tenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            return null;
        }

        try {
            return tenantRepository.findById(Long.parseLong(tenantId))
                    .filter(t -> t.getStatus() == TenantStatus.ACTIVE)
                    .orElse(null);
        } catch (NumberFormatException ex) {
            log.warn("Invalid tenantId format in tenant context: '{}'", tenantId);
            return null;
        }
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

    private record PlatformLoginOtpResult(String otpCode, String sessionToken) {
    }

    private String hashRefreshToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private void saveTenantRefreshToken(User user, String rawToken, boolean rememberMe) {
        long expirationMs = jwtProviderPort.getRefreshTokenExpiration(rememberMe);
        LocalDateTime expiresAt = Instant.now()
                .plusMillis(expirationMs)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        RefreshToken record = RefreshToken.builder()
                .userId(user.getId())
                .tokenHash(hashRefreshToken(rawToken))
                .expiresAt(expiresAt)
                .build();
        refreshTokenRepository.save(record);
    }

    private void savePlatformRefreshToken(Long adminId, String rawToken, boolean rememberMe) {
        long expirationMs = jwtProviderPort.getRefreshTokenExpiration(rememberMe);
        LocalDateTime expiresAt = Instant.now()
                .plusMillis(expirationMs)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        PlatformRefreshToken record = PlatformRefreshToken.builder()
                .userId(adminId)
                .tokenHash(hashRefreshToken(rawToken))
                .expiresAt(expiresAt)
                .build();
        platformRefreshTokenRepository.save(record);
    }
}
