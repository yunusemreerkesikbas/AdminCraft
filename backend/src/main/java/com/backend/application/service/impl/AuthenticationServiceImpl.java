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
    
    public AuthenticationServiceImpl(
            UserRepository userRepository,
            JwtTokenProvider jwtTokenProvider,
            PasswordEncoder passwordEncoder,
            TenantRepository tenantRepository) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.tenantRepository = tenantRepository;
    }

    @Override
    public LoginResponse authenticate(LoginRequest loginRequest) {
        log.info("Authenticating user with email: {}", loginRequest.email());
        
        // Find user by email
        User user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> {
                    log.warn("User not found for email: {}", loginRequest.email());
                    return new InvalidCredentialsException();
                });

        log.debug("Found user: id={}, email={}, isActive={}, emailVerified={}, tenantId={}", 
                 user.getId(), user.getEmail(), user.getIsActive(), user.getEmailVerified(), user.getTenantId());
        log.debug("Password hash from DB: {}", user.getPasswordHash());
        log.debug("Password hash length: {}", user.getPasswordHash() != null ? user.getPasswordHash().length() : "null");

        // Check if user can login (includes active, email verified, not locked checks)
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
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getRole().name(), user.getTenantId());
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
                resolveTenantSubdomain(user.getTenantId())
        );
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        log.info("Refreshing token");
        
        // Validate refresh token
        if (!jwtTokenProvider.validateToken(refreshToken) || 
            !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        // Extract email from token
        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        
        // Find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        // Check if user can still login
        if (!user.canLogin()) {
            log.warn("User cannot refresh token - email: {}, isActive: {}, emailVerified: {}, isAccountLocked: {}", 
                    email, user.getIsActive(), user.getEmailVerified(), user.isAccountLocked());
            throw new UserAccountDisabledException();
        }

        // Generate new tokens with tenantId included
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getRole().name(), user.getTenantId());
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
                resolveTenantSubdomain(user.getTenantId())
        );
    }

    @Override
    public void logout(String token) {
        log.info("Logging out user");
        
        try {
            // Validate token
            if (!jwtTokenProvider.validateToken(token)) {
                throw new InvalidTokenException("Invalid token");
            }

            // Extract email from token for logging
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