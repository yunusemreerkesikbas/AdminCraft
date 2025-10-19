package com.backend.application.service.impl;

import com.backend.application.command.auth.AuthenticateCommand;
import com.backend.application.service.AuthenticationService;
import com.backend.domain.entity.User;
import com.backend.domain.entity.Tenant;
import com.backend.domain.enums.TenantStatus;
import com.backend.domain.exception.InvalidCredentialsException;
import com.backend.domain.exception.InvalidTokenException;
import com.backend.domain.exception.UserAccountDisabledException;
import com.backend.domain.exception.UserNotFoundException;
import com.backend.domain.repository.UserRepository;
import com.backend.domain.repository.TenantRepository;
import com.backend.infrastructure.security.JwtTokenProvider;
import com.backend.infrastructure.persistence.platform.repository.PlatformAdminUserRepository;
import com.backend.infrastructure.persistence.platform.entity.PlatformAdminUser;
import com.backend.presentation.dto.request.LoginRequest;
import com.backend.presentation.dto.response.LoginResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    public LoginResponse authenticate(AuthenticateCommand command) {
        log.info("Authenticating user with email: {}", command.email());

        if (command.tenantId() != null) {
            log.debug("Using X-Tenant-ID based authentication: tenantId={}", command.tenantId());
            return authenticateTenantUserById(command);
        } else if (command.subdomain() != null && !command.subdomain().trim().isEmpty()) {
            log.debug("Using subdomain-based authentication: subdomain={}", command.subdomain());
            return authenticateTenantUserBySubdomain(command);
        } else {
            log.debug("Using platform admin authentication");
            return authenticatePlatformAdmin(command);
        }
    }

    private LoginResponse authenticateTenantUserById(AuthenticateCommand command) {
        try {
            Tenant tenant = tenantRepository.findById(command.tenantId())
                    .orElseThrow(() -> {
                        log.warn("Tenant not found for id: {}", command.tenantId());
                        return new InvalidCredentialsException();
                    });

            if (tenant.getStatus() != TenantStatus.ACTIVE) {
                log.warn("Tenant is not active: tenantId={}, status={}", command.tenantId(), tenant.getStatus());
                throw new InvalidCredentialsException();
            }
            tenantContext.setTenantId(String.valueOf(tenant.getId()));
            tenantContext.setTenantDbName(tenant.getDatabaseName());
            log.debug("TenantContext set: tenantId={}, dbName={}", tenant.getId(), tenant.getDatabaseName());
            User user = userRepository.findByEmail(command.email())
                    .orElseThrow(() -> {
                        log.warn("User not found for email: {}", command.email());
                        return new InvalidCredentialsException();
                    });

            return authenticateUser(user, command, tenant.getId(), tenant.getSubdomain());
        } finally {
            // CRITICAL: Always clear context after auth
            tenantContext.clear();
            log.debug("TenantContext cleared");
        }
    }

    private LoginResponse authenticateTenantUserBySubdomain(AuthenticateCommand command) {
        try {
            String subdomain = command.subdomain().trim().toLowerCase();
            if ("admin".equals(subdomain)) {
                log.debug("Subdomain 'admin' detected, redirecting to platform admin authentication");
                return authenticatePlatformAdmin(command);
            }
            Tenant tenant = tenantRepository.findBySubdomain(subdomain)
                    .orElseThrow(() -> {
                        log.warn("Tenant not found for subdomain: {}", subdomain);
                        return new InvalidCredentialsException(); // Generic error for security
                    });
            if (tenant.getStatus() != TenantStatus.ACTIVE) {
                log.warn("Tenant is not active: subdomain={}, status={}", subdomain, tenant.getStatus());
                throw new InvalidCredentialsException();
            }
            tenantContext.setTenantId(String.valueOf(tenant.getId()));
            tenantContext.setTenantDbName(tenant.getDatabaseName());
            log.debug("TenantContext set: subdomain={}, tenantId={}, dbName={}", subdomain, tenant.getId(),
                    tenant.getDatabaseName());
            User user = userRepository.findByEmail(command.email())
                    .orElseThrow(() -> {
                        log.warn("User not found for email: {} in subdomain: {}", command.email(), subdomain);
                        return new InvalidCredentialsException();
                    });

            return authenticateUser(user, command, tenant.getId(), tenant.getSubdomain());
        } finally {
            tenantContext.clear();
            log.debug("TenantContext cleared");
        }
    }

    private LoginResponse authenticateUser(User user, AuthenticateCommand command, Long tenantId, String subdomain) {

        if (!user.canLogin()) {
            log.warn("User cannot login - email: {}, isActive: {}, emailVerified: {}, isAccountLocked: {}",
                    command.email(), user.getIsActive(), user.getEmailVerified(), user.isAccountLocked());
            if (!user.getIsActive()) {
                throw new UserAccountDisabledException();
            } else if (!user.getEmailVerified()) {
                throw new InvalidCredentialsException(); // For security, don't reveal email not verified
            } else if (user.isAccountLocked()) {
                throw new InvalidCredentialsException(); // For security, don't reveal account locked
            } else {
                throw new InvalidCredentialsException();
            }
        }
        log.debug("Attempting password verification");
        boolean passwordMatches = passwordEncoder.matches(command.password(), user.getPasswordHash());

        if (!passwordMatches) {
            log.warn("Password verification failed");
            throw new InvalidCredentialsException();
        }
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getRole().name(), tenantId);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

        log.info("Authentication successful for user: {}", user.getEmail());

        return new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtTokenProvider.getAccessTokenExpiration(),
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                user.getPreferredLanguage().name(),
                subdomain);
    }

    private LoginResponse authenticatePlatformAdmin(AuthenticateCommand command) {
        PlatformAdminUser admin = platformAdminUserRepository
                .findByEmailAndIsActiveTrue(command.email())
                .orElseThrow(InvalidCredentialsException::new);
        boolean passwordMatches = passwordEncoder.matches(command.password(), admin.getPasswordHash());
        if (!passwordMatches) {
            throw new InvalidCredentialsException();
        }
        String accessToken = jwtTokenProvider.createAccessToken(admin.getEmail(), "SUPER_ADMIN", null);
        String refreshToken = jwtTokenProvider.createRefreshToken(admin.getEmail());

        return new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtTokenProvider.getAccessTokenExpiration(),
                admin.getId(),
                admin.getEmail(),
                admin.getFullName(),
                "SUPER_ADMIN",
                command.preferredLanguageCode(),
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

            String newAccessToken = jwtTokenProvider.createAccessToken(admin.getEmail(), "SUPER_ADMIN", null);
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
            String newAccessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getRole().name(),
                    user.getTenantId());
            String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

            log.info("Token refresh successful for user: {}, tenantId: {}", user.getEmail(), user.getTenantId());

            return new LoginResponse(
                    newAccessToken,
                    newRefreshToken,
                    "Bearer",
                    jwtTokenProvider.getAccessTokenExpiration(),
                    user.getId(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getRole().name(),
                    user.getPreferredLanguage().name(),
                    resolveTenantSubdomain(user.getTenantId()));
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