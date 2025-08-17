package com.backend.presentation.controller;

import com.backend.application.service.AuthenticationService;
import com.backend.presentation.dto.request.LoginRequest;
import com.backend.presentation.dto.response.LoginResponse;
import com.backend.shared.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthenticationService authenticationService;
    private final MessageSource messageSource;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            log.info("Login attempt for email: {}", loginRequest.email());
            
            // Input sanitization
            if (loginRequest.email() == null || loginRequest.email().trim().isEmpty()) {
                throw new IllegalArgumentException("Email cannot be null or empty");
            }
            
            LoginResponse loginResponse = authenticationService.authenticate(loginRequest);
            
            String message = messageSource.getMessage("auth.login.success", null, Locale.forLanguageTag(languageCode));
            ApiResponse<LoginResponse> response = ApiResponse.success(message, loginResponse);
            
            log.info("Login successful for email: {}", loginRequest.email());
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Login failed for email {}: {}", loginRequest.email(), ex.getMessage());
            String message = messageSource.getMessage("auth.login.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
            @RequestHeader("Authorization") @Valid @NotBlank String refreshToken,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            log.info("Token refresh attempt");
            
            // Input validation and sanitization
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                throw new IllegalArgumentException("Refresh token cannot be null or empty");
            }
            
            // Remove "Bearer " prefix if present
            String token = refreshToken.startsWith("Bearer ") ? 
                refreshToken.substring(7) : refreshToken;
                
            // Additional token format validation
            if (token.trim().isEmpty()) {
                throw new IllegalArgumentException("Invalid token format");
            }
            
            LoginResponse loginResponse = authenticationService.refreshToken(token);
            
            String message = messageSource.getMessage("auth.refresh.success", null, Locale.forLanguageTag(languageCode));
            ApiResponse<LoginResponse> response = ApiResponse.success(message, loginResponse);
            
            log.info("Token refresh successful");
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Token refresh failed: {}", ex.getMessage());
            String message = messageSource.getMessage("auth.refresh.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") @Valid @NotBlank String token,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            log.info("Logout attempt");
            
            // Input validation
            if (token == null || token.trim().isEmpty()) {
                throw new IllegalArgumentException("Token cannot be null or empty");
            }
            
            // Remove "Bearer " prefix if present
            String cleanToken = token.startsWith("Bearer ") ? 
                token.substring(7) : token;
                
            authenticationService.logout(cleanToken);
            
            String message = messageSource.getMessage("auth.logout.success", null, Locale.forLanguageTag(languageCode));
            log.info("Logout successful");
            return ResponseEntity.ok(ApiResponse.success(message, null));
        } catch (Exception ex) {
            log.error("Logout failed: {}", ex.getMessage());
            String message = messageSource.getMessage("auth.logout.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }
}