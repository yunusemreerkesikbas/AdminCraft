package com.backend.application.service.impl;

import com.backend.application.service.AuthenticationService;
import com.backend.domain.entity.User;
import com.backend.domain.entity.Tenant;
import com.backend.domain.exception.InvalidCredentialsException;
import com.backend.domain.exception.InvalidTokenException;
import com.backend.domain.exception.UserAccountDisabledException;
import com.backend.domain.exception.UserNotFoundException;
import com.backend.domain.repository.UserRepository;
import com.backend.domain.repository.TenantRepository;
import com.backend.infrastructure.security.JwtTokenProvider;
import com.backend.infrastructure.persistence.platform.repository.PlatformAdminUserRepository;
import com.backend.infrastructure.persistence.platform.entity.PlatformAdminUser;
import jakarta.servlet.http.HttpServletRequest;
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
    private final HttpServletRequest request;

    public AuthenticationServiceImpl(
            UserRepository userRepository,
            JwtTokenProvider jwtTokenProvider,
            PasswordEncoder passwordEncoder,
            TenantRepository tenantRepository,
            PlatformAdminUserRepository platformAdminUserRepository,
            HttpServletRequest request) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.tenantRepository = tenantRepository;
        this.platformAdminUserRepository = platformAdminUserRepository;
        this.request = request;
    }

    @Override
    public LoginResponse authenticate(LoginRequest loginRequest) {
        log.info("Authenticating user with email: {}", loginRequest.email());

        String tenantIdHeader = request.getHeader("X-Tenant-ID");
        if (tenantIdHeader == null || tenantIdHeader.isBlank()) {
            return authenticatePlatformAdmin(loginRequest);
        }
        User user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> {
                    log.warn("User not found for email: {}", loginRequest.email());
                    return new InvalidCredentialsException();
                });

        log.debug("Found user: id={}, email={}, isActive={}, emailVerified={}, tenantId={}",
                user.getId(), user.getEmail(), user.getIsActive(), user.getEmailVerified(), user.getTenantId());
        log.debug("Password hash from DB: {}", user.getPasswordHash());
        log.debug("Password hash length: {}",
                user.getPasswordHash() != null ? user.getPasswordHash().length() : "null");

        if (!user.canLogin()) {
            log.warn("User cannot login - email: {}, isActive: {}, emailVerified: {}, isAccountLocked: {}",
                    loginRequest.email(), user.getIsActive(), user.getEmailVerified(), user.isAccountLocked());
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

        // Verify password
        log.debug("Attempting password verification for user: {}", loginRequest.email());
        // SECURITY: Never log actual passwords
        boolean passwordMatches = passwordEncoder.matches(loginRequest.password(), user.getPasswordHash());

        if (!passwordMatches) {
            log.warn("Password verification failed for email: {}", loginRequest.email());
            throw new InvalidCredentialsException();
        }

        // Generate tokens with tenantId included
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getRole().name(),
                user.getTenantId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

        log.info("Authentication successful for user: {}, tenantId: {}", user.getEmail(), user.getTenantId());

        return new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtTokenProvider.getAccessTokenExpiration(),
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                user.getTenantId(),
                user.getPreferredLanguage().name(),
                // subdomain resolution (optional if user has tenant)
                resolveTenantSubdomain(user.getTenantId()));
    }

    private LoginResponse authenticatePlatformAdmin(LoginRequest loginRequest) {
        // Lookup platform admin by email
        PlatformAdminUser admin = platformAdminUserRepository
                .findByEmailAndIsActiveTrue(loginRequest.email())
                .orElseThrow(InvalidCredentialsException::new);

        // Verify password
        boolean passwordMatches = passwordEncoder.matches(loginRequest.password(), admin.getPasswordHash());
        if (!passwordMatches) {
            throw new InvalidCredentialsException();
        }

        // Issue token with role SUPER_ADMIN and no tenantId
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
                null,
                "TR", // default language for platform admin (could be extended later)
                null);
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        log.info("Refreshing token");

        // Validate refresh token
        if (!jwtTokenProvider.validateToken(refreshToken) ||
                !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }
        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
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
                user.getTenantId(),
                user.getPreferredLanguage().name(),
                resolveTenantSubdomain(user.getTenantId()));
    }

    @Override
    public void logout(String token) {
        log.info("Logging out user");

        try {
            if (!jwtTokenProvider.validateToken(token)) {
                throw new InvalidTokenException("Invalid token");
            }
            String email = jwtTokenProvider.getEmailFromToken(token);

            // TODO: Add token to blacklist/invalidate token
            // This could be implemented by:
            // 1. Adding token to a blacklist in Redis/database
            // 2. Reducing token expiration time
            // 3. Using a token versioning system

            log.info("Logout successful for user: {}", email);
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