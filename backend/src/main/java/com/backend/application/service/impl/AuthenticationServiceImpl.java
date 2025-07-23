package com.backend.application.service.impl;

import com.backend.application.service.AuthenticationService;
import com.backend.domain.entity.User;
import com.backend.domain.repository.UserRepository;
import com.backend.infrastructure.security.JwtTokenProvider;
import com.backend.presentation.dto.request.LoginRequest;
import com.backend.presentation.dto.response.LoginResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse authenticate(LoginRequest loginRequest) {
        log.info("Authenticating user with email: {}", loginRequest.email());
        
        // Find user by email
        User user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // Check if user is active
        if (!user.getIsActive()) {
            throw new RuntimeException("User account is disabled");
        }

        // Verify password
        log.debug("Password verification - Input password: {}", loginRequest.password());
        log.debug("Password verification - Stored hash: {}", user.getPasswordHash());
        log.debug("Password verification - Hash length: {}", user.getPasswordHash().length());
        
        boolean passwordMatches = passwordEncoder.matches(loginRequest.password(), user.getPasswordHash());
        log.debug("Password verification result: {}", passwordMatches);
        
        if (!passwordMatches) {
            throw new RuntimeException("Invalid email or password");
        }

        // Generate tokens
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getRole().name());
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
                user.getTenantId(),
                user.getPreferredLanguage().name()
        );
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        log.info("Refreshing token");
        
        // Validate refresh token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        // Extract email from token
        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        
        // Find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if user is still active
        if (!user.getIsActive()) {
            throw new RuntimeException("User account is disabled");
        }

        // Generate new tokens
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getRole().name());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

        log.info("Token refresh successful for user: {}", user.getEmail());
        
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
                user.getPreferredLanguage().name()
        );
    }
}