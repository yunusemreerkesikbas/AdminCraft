package com.backend.application.service.impl.config;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.backend.application.config.ConfigAuthProperties;
import com.backend.application.dto.config.ConfigAuthChallengeResult;
import com.backend.application.dto.config.ConfigAuthResult;
import com.backend.application.dto.config.ConfigLoginResult;
import com.backend.application.dto.config.ConfigPrincipal;
import com.backend.application.service.EmailService;
import com.backend.application.service.OtpService;
import com.backend.application.service.config.ConfigAuthenticationService;
import com.backend.domain.entity.ConfigChangeAudit;
import com.backend.domain.entity.Tenant;
import com.backend.domain.entity.User;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.TenantStatus;
import com.backend.domain.enums.TokenStatus;
import com.backend.domain.enums.TokenType;
import com.backend.domain.enums.UserRole;
import com.backend.domain.exception.AccountLockedException;
import com.backend.domain.exception.InvalidCredentialsException;
import com.backend.domain.exception.InvalidTokenException;
import com.backend.domain.exception.UserNotFoundException;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.PlatformVerificationTokenRepository;
import com.backend.domain.repository.TenantRepository;
import com.backend.domain.repository.UserRepository;
import com.backend.domain.repository.VerificationTokenRepository;
import com.backend.domain.entity.PlatformAdminUser;
import com.backend.domain.entity.PlatformVerificationToken;
import com.backend.domain.port.JwtProviderPort;
import com.backend.domain.port.OtpConfig;
import com.backend.domain.port.PlatformSettingsPort;
import com.backend.domain.repository.ConfigChangeAuditRepository;
import com.backend.domain.repository.PlatformAdminUserRepository;

import io.micrometer.core.instrument.MeterRegistry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigAuthenticationServiceImpl implements ConfigAuthenticationService {

    private static final String CONFIG_AUTH_AUDIT_SCOPE = "CONFIG_AUTH";
    private static final String CONFIG_AUTH_OTP_BYPASSED_ACTION = "OTP_BYPASSED_LOGIN";

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PlatformAdminUserRepository platformAdminUserRepository;
    private final PlatformSettingsPort platformSettingsPort;
    private final PlatformVerificationTokenRepository platformVerificationTokenRepository;
    private final TenantContextPort tenantContext;
    private final OtpService otpService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProviderPort jwtProviderPort;
    private final OtpConfig otpConfig;
    private final ConfigAuthProperties configAuthProperties;
    private final ConfigChangeAuditRepository configChangeAuditRepository;
    private final MeterRegistry meterRegistry;

    @Qualifier("tenantTransactionManager")
    private final PlatformTransactionManager tenantTransactionManager;

    @Qualifier("platformTransactionManager")
    private final PlatformTransactionManager platformTransactionManager;

    @Override
    public ConfigLoginResult login(String email, String password, Long tenantId, String subdomain,
            String ipAddress, String userAgent) {
        if (isPlatformRequest(tenantId, subdomain)) {
            return loginPlatformAdmin(email, password, ipAddress, userAgent);
        }
        return loginTenantAdmin(email, password, tenantId, subdomain, ipAddress, userAgent);
    }

    @Override
    public ConfigAuthResult verifyOtp(String pendingToken, String otpCode, Long tenantId, String subdomain,
            String ipAddress) {
        if (isPlatformRequest(tenantId, subdomain)) {
            return verifyPlatformOtp(pendingToken, otpCode, ipAddress);
        }
        return verifyTenantOtp(pendingToken, otpCode, tenantId, subdomain, ipAddress);
    }

    @Override
    public ConfigAuthResult refreshToken(String refreshToken, String ipAddress) {
        if (!jwtProviderPort.validateToken(refreshToken) || !jwtProviderPort.isRefreshToken(refreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        String role = jwtProviderPort.getRoleFromToken(refreshToken);
        if (ConfigPrincipal.ROLE_CONFIG_SUPER_ADMIN.equals(role)) {
            throw new InvalidTokenException("Config refresh is not available for super admin");
        }

        if (!ConfigPrincipal.ROLE_CONFIG_TENANT_ADMIN.equals(role)) {
            throw new InvalidTokenException("Unsupported config refresh role");
        }

        Long tenantId = jwtProviderPort.getTenantIdFromToken(refreshToken);
        if (tenantId == null) {
            throw new InvalidTokenException("Tenant ID required for config refresh");
        }

        Tenant tenant = resolveTenant(tenantId, null);

        if (tenant.getStatus() != TenantStatus.ACTIVE) {
            throw new InvalidTokenException("Tenant is not active");
        }

        try {
            setTenantContext(tenant);
            TransactionTemplate transactionTemplate = new TransactionTemplate(tenantTransactionManager);
            return transactionTemplate.execute(status -> {
                User user = userRepository.findByEmail(jwtProviderPort.getEmailFromToken(refreshToken))
                        .orElseThrow(() -> new UserNotFoundException(jwtProviderPort.getEmailFromToken(refreshToken)));

                if (user.getRole() != UserRole.TENANT_ADMIN || !Boolean.TRUE.equals(user.getIsActive())) {
                    throw new InvalidTokenException("Tenant admin account is not active");
                }

                if (user.isAccountLocked()) {
                    throw new AccountLockedException(user.getRemainingLockMinutes());
                }

                user.recordSuccessfulLogin(ipAddress);
                userRepository.save(user);

                long issuedAt = System.currentTimeMillis();
                String accessToken = jwtProviderPort.createAccessToken(
                        user.getEmail(),
                        ConfigPrincipal.ROLE_CONFIG_TENANT_ADMIN,
                        user.getId(),
                        tenant.getId());
                String rotatedRefreshToken = jwtProviderPort.createRefreshToken(
                        user.getEmail(),
                        ConfigPrincipal.ROLE_CONFIG_TENANT_ADMIN,
                        user.getId(),
                        tenant.getId());

                return new ConfigAuthResult(
                        accessToken,
                        rotatedRefreshToken,
                        "Bearer",
                        jwtProviderPort.getAccessTokenExpiration() / 1000,
                        issuedAt,
                        user.getId(),
                        user.getEmail(),
                        user.getFullName(),
                        ConfigPrincipal.ROLE_CONFIG_TENANT_ADMIN,
                        tenant.getId(),
                        tenant.getSubdomain());
            });
        } finally {
            clearTenantContext();
        }
    }

    private ConfigLoginResult loginTenantAdmin(String email, String password, Long tenantId, String subdomain,
            String ipAddress, String userAgent) {
        Tenant tenant = resolveTenant(tenantId, subdomain);

        if (tenant.getStatus() != TenantStatus.ACTIVE) {
            throw new InvalidCredentialsException();
        }

        try {
            setTenantContext(tenant);
            TransactionTemplate transactionTemplate = new TransactionTemplate(tenantTransactionManager);
            return transactionTemplate.execute(status -> {
                User user = userRepository.findByEmail(email)
                        .orElseThrow(InvalidCredentialsException::new);

                if (user.getRole() != UserRole.TENANT_ADMIN) {
                    throw new InvalidCredentialsException();
                }

                if (!Boolean.TRUE.equals(user.getIsActive())) {
                    throw new InvalidCredentialsException();
                }

                if (user.isAccountLocked()) {
                    throw new AccountLockedException(user.getRemainingLockMinutes());
                }

                if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                    user.recordFailedLogin();
                    userRepository.save(user);
                    if (user.isAccountLocked()) {
                        throw new AccountLockedException(user.getRemainingLockMinutes());
                    }
                    throw new InvalidCredentialsException();
                }

                if (configAuthProperties.otpEnabled()) {
                    OtpService.LoginOtpResult otpResult = otpService.createLoginOtpToken(user, ipAddress, userAgent);
                    Language language = tenant.getDefaultLanguage() != null ? tenant.getDefaultLanguage() : Language.TR;
                    emailService.sendOtpEmail(user.getEmail(), otpResult.otpCode(), language);

                    return ConfigLoginResult.challenge(new ConfigAuthChallengeResult(
                            otpResult.sessionToken(),
                            user.getEmail(),
                            tenant.getId(),
                            tenant.getSubdomain(),
                            ConfigPrincipal.ROLE_CONFIG_TENANT_ADMIN));
                }

                recordPasswordOnlyTenantConfigLogin(user, tenant, ipAddress);
                user.recordSuccessfulLogin(ipAddress);
                userRepository.save(user);
                return ConfigLoginResult.session(createTenantAuthResult(user, tenant));
            });
        } finally {
            clearTenantContext();
        }
    }

    private ConfigLoginResult loginPlatformAdmin(String email, String password, String ipAddress,
            String userAgent) {
        PlatformAdminUser admin = platformAdminUserRepository
                .findByEmailAndIsActiveTrue(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (admin.isAccountLocked()) {
            throw new AccountLockedException(admin.getRemainingLockMinutes());
        }

        if (!passwordEncoder.matches(password, admin.getPasswordHash())) {
            admin.recordFailedLogin();
            platformAdminUserRepository.save(admin);
            if (admin.isAccountLocked()) {
                throw new AccountLockedException(admin.getRemainingLockMinutes());
            }
            throw new InvalidCredentialsException();
        }

        if (!configAuthProperties.otpEnabled()) {
            admin.recordSuccessfulLogin(ipAddress);
            platformAdminUserRepository.save(admin);
            return ConfigLoginResult.session(createPlatformAuthResult(admin));
        }

        PlatformLoginOtpResult otpResult = createPlatformLoginOtpToken(admin, ipAddress, userAgent);
        emailService.sendOtpEmail(admin.getEmail(), otpResult.otpCode(), resolvePlatformLanguage());

        return ConfigLoginResult.challenge(new ConfigAuthChallengeResult(
                otpResult.sessionToken(),
                admin.getEmail(),
                null,
                "admin",
                ConfigPrincipal.ROLE_CONFIG_SUPER_ADMIN));
    }

    private ConfigAuthResult verifyTenantOtp(String pendingToken, String otpCode, Long tenantId, String subdomain,
            String ipAddress) {
        Tenant tenant = resolveTenant(tenantId, subdomain);

        try {
            setTenantContext(tenant);
            TransactionTemplate transactionTemplate = new TransactionTemplate(tenantTransactionManager);
            return transactionTemplate.execute(status -> {
                String tokenHash = otpService.hashToken(pendingToken);
                var token = verificationTokenRepository.findByTokenHash(tokenHash)
                        .orElseThrow(() -> new InvalidTokenException("Invalid or expired OTP session"));

                validateTenantOtpToken(token, otpCode);

                User user = token.getUser();
                if (user == null || user.getRole() != UserRole.TENANT_ADMIN || !Boolean.TRUE.equals(user.getIsActive())) {
                    throw new InvalidCredentialsException();
                }

                user.recordSuccessfulLogin(ipAddress);
                userRepository.save(user);

                long issuedAt = System.currentTimeMillis();
                String accessToken = jwtProviderPort.createAccessToken(
                        user.getEmail(),
                        ConfigPrincipal.ROLE_CONFIG_TENANT_ADMIN,
                        user.getId(),
                        tenant.getId());
                String refreshToken = jwtProviderPort.createRefreshToken(
                        user.getEmail(),
                        ConfigPrincipal.ROLE_CONFIG_TENANT_ADMIN,
                        user.getId(),
                        tenant.getId());

                return new ConfigAuthResult(
                        accessToken,
                        refreshToken,
                        "Bearer",
                        jwtProviderPort.getAccessTokenExpiration() / 1000,
                        issuedAt,
                        user.getId(),
                        user.getEmail(),
                        user.getFullName(),
                        ConfigPrincipal.ROLE_CONFIG_TENANT_ADMIN,
                        tenant.getId(),
                        tenant.getSubdomain());
            });
        } finally {
            clearTenantContext();
        }
    }

    private ConfigAuthResult verifyPlatformOtp(String pendingToken, String otpCode, String ipAddress) {
        String tokenHash = otpService.hashToken(pendingToken);
        PlatformVerificationToken token = platformVerificationTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired OTP session"));

        if (!token.isUsable()) {
            throw new InvalidTokenException("OTP session has expired or is no longer valid");
        }

        String otpHash = otpService.hashToken(otpCode);
        boolean isBypassCode = otpConfig.getBypassCode() != null && otpConfig.getBypassCode().equals(otpCode);
        boolean isValid = isBypassCode || otpHash.equals(token.getTargetValue());

        if (!isValid) {
            token.incrementAttempts();
            platformVerificationTokenRepository.save(token);
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

        long issuedAt = System.currentTimeMillis();
        String accessToken = jwtProviderPort.createAccessToken(
                admin.getEmail(),
                ConfigPrincipal.ROLE_CONFIG_SUPER_ADMIN,
                admin.getId(),
                null);

        return new ConfigAuthResult(
                accessToken,
                null,
                "Bearer",
                jwtProviderPort.getAccessTokenExpiration() / 1000,
                issuedAt,
                admin.getId(),
                admin.getEmail(),
                admin.getFullName(),
                ConfigPrincipal.ROLE_CONFIG_SUPER_ADMIN,
                null,
                "admin");
    }

    private ConfigAuthResult createTenantAuthResult(User user, Tenant tenant) {
        long issuedAt = System.currentTimeMillis();
        String accessToken = jwtProviderPort.createAccessToken(
                user.getEmail(),
                ConfigPrincipal.ROLE_CONFIG_TENANT_ADMIN,
                user.getId(),
                tenant.getId());
        String refreshToken = jwtProviderPort.createRefreshToken(
                user.getEmail(),
                ConfigPrincipal.ROLE_CONFIG_TENANT_ADMIN,
                user.getId(),
                tenant.getId());

        return new ConfigAuthResult(
                accessToken,
                refreshToken,
                "Bearer",
                jwtProviderPort.getAccessTokenExpiration() / 1000,
                issuedAt,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                ConfigPrincipal.ROLE_CONFIG_TENANT_ADMIN,
                tenant.getId(),
                tenant.getSubdomain());
    }

    private ConfigAuthResult createPlatformAuthResult(PlatformAdminUser admin) {
        long issuedAt = System.currentTimeMillis();
        String accessToken = jwtProviderPort.createAccessToken(
                admin.getEmail(),
                ConfigPrincipal.ROLE_CONFIG_SUPER_ADMIN,
                admin.getId(),
                null);

        return new ConfigAuthResult(
                accessToken,
                null,
                "Bearer",
                jwtProviderPort.getAccessTokenExpiration() / 1000,
                issuedAt,
                admin.getId(),
                admin.getEmail(),
                admin.getFullName(),
                ConfigPrincipal.ROLE_CONFIG_SUPER_ADMIN,
                null,
                "admin");
    }

    private void validateTenantOtpToken(com.backend.domain.entity.VerificationToken token, String otpCode) {
        if (!token.isUsable()) {
            throw new InvalidTokenException("OTP session has expired or is no longer valid");
        }

        String otpHash = otpService.hashToken(otpCode);
        boolean isBypassCode = otpConfig.getBypassCode() != null && otpConfig.getBypassCode().equals(otpCode);
        boolean isValid = isBypassCode || otpHash.equals(token.getTargetValue());

        if (!isValid) {
            token.incrementAttempts();
            verificationTokenRepository.save(token);
            if (!token.isUsable()) {
                throw new InvalidTokenException("OTP session has expired due to too many attempts");
            }
            throw new InvalidCredentialsException();
        }

        token.markAsUsed();
        verificationTokenRepository.save(token);
    }

    private PlatformLoginOtpResult createPlatformLoginOtpToken(PlatformAdminUser admin, String ipAddress,
            String userAgent) {
        platformVerificationTokenRepository.revokeAllActiveTokensForAdmin(admin.getId(), TokenType.LOGIN_OTP);

        String otpCode = otpService.generateOtp();
        String sessionToken = UUID.randomUUID().toString();

        PlatformVerificationToken token = PlatformVerificationToken.builder()
                .adminUser(admin)
                .tokenHash(otpService.hashToken(sessionToken))
                .tokenType(TokenType.LOGIN_OTP)
                .status(TokenStatus.ACTIVE)
                .targetValue(otpService.hashToken(otpCode))
                .expiresAt(LocalDateTime.now().plusSeconds(otpConfig.getExpirySeconds()))
                .attemptCount(0)
                .maxAttempts(otpConfig.getMaxAttempts())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        platformVerificationTokenRepository.save(token);
        return new PlatformLoginOtpResult(otpCode, sessionToken);
    }

    private Tenant resolveTenant(Long tenantId, String subdomain) {
        if (tenantId != null) {
            return tenantRepository.findById(tenantId)
                    .orElseThrow(InvalidCredentialsException::new);
        }

        if (subdomain != null && !subdomain.isBlank()) {
            return tenantRepository.findBySubdomain(subdomain.trim().toLowerCase())
                    .orElseThrow(InvalidCredentialsException::new);
        }

        throw new InvalidCredentialsException();
    }

    private boolean isPlatformRequest(Long tenantId, String subdomain) {
        if (tenantId != null) {
            return false;
        }

        if (subdomain == null || subdomain.isBlank()) {
            return true;
        }

        return "admin".equalsIgnoreCase(subdomain.trim());
    }

    private Language resolvePlatformLanguage() {
        String languageCode = platformSettingsPort.getSingleton().getDefaultLanguage();
        return Language.fromCodeOrDefault(languageCode, Language.TR);
    }

    private void setTenantContext(Tenant tenant) {
        tenantContext.setTenantId(String.valueOf(tenant.getId()));
        tenantContext.setTenantDbName(tenant.getDatabaseName());
        MDC.put("tenantId", String.valueOf(tenant.getId()));
        MDC.put("tenantDb", tenant.getDatabaseName());
        MDC.put("correlationId", UUID.randomUUID().toString());
    }

    private void clearTenantContext() {
        tenantContext.clear();
        MDC.remove("tenantId");
        MDC.remove("tenantDb");
        MDC.remove("correlationId");
    }

    private void recordPasswordOnlyTenantConfigLogin(User user, Tenant tenant, String ipAddress) {
        log.warn(
                "Config tenant login without OTP — actorEmailHash={}, tenantId={}, correlationId={}",
                sha256Hex(user.getEmail()),
                tenant.getId(),
                MDC.get("correlationId"));
        meterRegistry.counter("config.auth.otp_bypassed_login", "role", ConfigPrincipal.ROLE_CONFIG_TENANT_ADMIN)
                .increment();
        String reason = "OTP disabled for config login; ip=" + ipAddress
                + " tenantId=" + tenant.getId() + " subdomain=" + tenant.getSubdomain();
        TransactionTemplate platformTx = new TransactionTemplate(platformTransactionManager);
        platformTx.executeWithoutResult(status -> {
            ConfigChangeAudit audit = ConfigChangeAudit.builder()
                    .actorUserId(user.getId())
                    .actorEmail(user.getEmail())
                    .actorRole(ConfigPrincipal.ROLE_CONFIG_TENANT_ADMIN)
                    .targetTenantId(tenant.getId())
                    .scope(CONFIG_AUTH_AUDIT_SCOPE)
                    .action(CONFIG_AUTH_OTP_BYPASSED_ACTION)
                    .beforeJson(null)
                    .afterJson(null)
                    .reason(reason)
                    .correlationId(MDC.get("correlationId"))
                    .build();
            configChangeAuditRepository.save(audit);
        });
    }

    private static String sha256Hex(String value) {
        if (value == null) {
            return "";
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            return "unavailable";
        }
    }

    private record PlatformLoginOtpResult(String otpCode, String sessionToken) {
    }
}
