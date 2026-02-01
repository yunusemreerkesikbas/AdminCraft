package com.backend.application.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.backend.application.service.AuthenticationService;
import com.backend.domain.entity.Tenant;
import com.backend.domain.entity.User;
import com.backend.domain.enums.TenantStatus;
import com.backend.domain.exception.AccountLockedException;
import com.backend.domain.exception.InvalidCredentialsException;
import com.backend.domain.exception.InvalidTokenException;
import com.backend.domain.exception.UserAccountDisabledException;
import com.backend.domain.exception.UserNotFoundException;
import com.backend.domain.repository.TenantRepository;
import com.backend.domain.repository.UserRepository;
import com.backend.infrastructure.persistence.platform.entity.PlatformAdminUser;
import com.backend.infrastructure.persistence.platform.repository.PlatformAdminUserRepository;
import com.backend.infrastructure.security.JwtTokenProvider;
import com.backend.presentation.dto.response.LoginResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final TenantRepository tenantRepository;
    private final PlatformAdminUserRepository platformAdminUserRepository;
    private final com.backend.infrastructure.tenant.TenantContext tenantContext;

    public AuthenticationServiceImpl(
            UserRepository userRepository,
            JwtTokenProvider jwtTokenProvider,
            PasswordEncoder passwordEncoder,
            TenantRepository tenantRepository,
            PlatformAdminUserRepository platformAdminUserRepository,
            com.backend.infrastructure.tenant.TenantContext tenantContext) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.tenantRepository = tenantRepository;
        this.platformAdminUserRepository = platformAdminUserRepository;
        this.tenantContext = tenantContext;
    }

    @Override
    public LoginResponse authenticate(String email, String password, Long tenantId, String subdomain) {
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

    private LoginResponse authenticateTenantUserById(String email, String password, Long tenantId) {
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
            log.debug("TenantContext set: tenantId={}, dbName={}", tenant.getId(), tenant.getDatabaseName());
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("User not found for provided credentials");
                        return new InvalidCredentialsException();
                    });

            return authenticateUser(user, password, tenant.getId(), tenant.getSubdomain());
        } finally {
            tenantContext.clear();
            log.debug("TenantContext cleared");
        }
    }

    private LoginResponse authenticateTenantUserBySubdomain(String email, String password, String subdomain) {
        try {
            String cleanSubdomain = subdomain.trim().toLowerCase();
            if ("admin".equals(cleanSubdomain)) {
                log.debug("Subdomain 'admin' detected, redirecting to platform admin authentication");
                return authenticatePlatformAdmin(email, password);
            }
            Tenant tenant = tenantRepository.findBySubdomain(cleanSubdomain)
                    .orElseThrow(() -> {
                        log.warn("Tenant not found for subdomain: {}", cleanSubdomain);
                        return new InvalidCredentialsException(); // Generic error for security
                    });
            if (tenant.getStatus() != TenantStatus.ACTIVE) {
                log.warn("Tenant is not active: subdomain={}, status={}", cleanSubdomain, tenant.getStatus());
                throw new InvalidCredentialsException();
            }
            tenantContext.setTenantId(String.valueOf(tenant.getId()));
            tenantContext.setTenantDbName(tenant.getDatabaseName());
            log.debug("TenantContext set: subdomain={}, tenantId={}, dbName={}", cleanSubdomain, tenant.getId(),
                    tenant.getDatabaseName());
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("User not found in subdomain: {}", cleanSubdomain);
                        return new InvalidCredentialsException();
                    });

            return authenticateUser(user, password, tenant.getId(), tenant.getSubdomain());
        } finally {
            tenantContext.clear();
            log.debug("TenantContext cleared");
        }
    }

    private LoginResponse authenticateUser(User user, String password, Long tenantId, String subdomain) {

        if (!user.canLogin()) {
            log.warn("User cannot login - userId: {}, isActive: {}, emailVerified: {}, isAccountLocked: {}",
                    user.getId(), user.getIsActive(), user.getEmailVerified(), user.isAccountLocked());
            if (!user.getIsActive()) {
                throw new UserAccountDisabledException();
            } else if (!user.getEmailVerified()) {
                throw new InvalidCredentialsException(); // For security, don't reveal email not verified
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

        // Reset failed login attempts on successful login while preserving existing
        // last login IP
        user.recordSuccessfulLogin(user.getLastLoginIp());
        userRepository.save(user);
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getEmail(),
                user.getRole().name(),
                user.getId(),
                tenantId);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

        log.info("Authentication successful for userId: {}", user.getId());

        return new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtTokenProvider.getAccessTokenExpiration(),
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                subdomain,
                tenantId);
    }

    private LoginResponse authenticatePlatformAdmin(String email, String password) {
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
        admin.recordSuccessfulLogin(null); // IP can be added later from request context
        platformAdminUserRepository.save(admin);

        String accessToken = jwtTokenProvider.createAccessToken(
                admin.getEmail(),
                "SUPER_ADMIN",
                admin.getId(),
                null);
        String refreshToken = jwtTokenProvider.createRefreshToken(admin.getEmail());

        log.info("Authentication successful for platform admin userId: {}", admin.getId());

        return new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtTokenProvider.getAccessTokenExpiration(),
                admin.getId(),
                admin.getEmail(),
                admin.getFullName(),
                "SUPER_ADMIN",
                null,
                null);
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
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

            return new LoginResponse(
                    newAccessToken,
                    newRefreshToken,
                    "Bearer",
                    jwtTokenProvider.getAccessTokenExpiration(),
                    admin.getId(),
                    admin.getEmail(),
                    admin.getFullName(),
                    "SUPER_ADMIN",
                    null,
                    null);
        } else {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException(email));
            if (!user.canLogin()) {
                log.warn("User cannot refresh token - email: {}, isActive: {}, emailVerified: {}, isAccountLocked: {}",
                        email, user.getIsActive(), user.getEmailVerified(), user.isAccountLocked());
                throw new UserAccountDisabledException();
            }
            String newAccessToken = jwtTokenProvider.createAccessToken(
                    user.getEmail(),
                    user.getRole().name(),
                    user.getId(),
                    tenantId);
            String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

            log.info("Token refresh successful for user: {}, tenantId: {}", user.getEmail(), tenantId);

            return new LoginResponse(
                    newAccessToken,
                    newRefreshToken,
                    "Bearer",
                    jwtTokenProvider.getAccessTokenExpiration(),
                    user.getId(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getRole().name(),
                    resolveTenantSubdomain(tenantId),
                    tenantId);
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
}