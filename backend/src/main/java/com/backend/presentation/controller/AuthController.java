package com.backend.presentation.controller;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.dto.AuthResult;
import com.backend.application.dto.TokenValidationResult;
import com.backend.application.service.AuthenticationService;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.request.ForgotPasswordRequest;
import com.backend.presentation.dto.request.LoginRequest;
import com.backend.presentation.dto.request.ResetPasswordRequest;
import com.backend.presentation.dto.request.SetInitialPasswordRequest;
import com.backend.presentation.dto.request.VerifyOtpRequest;
import com.backend.presentation.dto.response.LoginResponse;
import com.backend.presentation.dto.response.TokenValidationResponse;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.RequestUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
            @RequestHeader(value = "X-Tenant-ID", required = false) Long tenantId,
            @RequestHeader(value = "X-Tenant-Subdomain", required = false) String subdomain,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode,
            HttpServletRequest httpRequest) {
        try {
            log.info("Login attempt for email: {}", loginRequest.email());

            if (loginRequest.email() == null || loginRequest.email().trim().isEmpty()) {
                throw new IllegalArgumentException("Email cannot be null or empty");
            }

            String effectiveSubdomain = subdomain != null ? subdomain : loginRequest.subdomain();

            String ipAddress = RequestUtils.getClientIpAddress(httpRequest);
            String userAgent = RequestUtils.getUserAgent(httpRequest);
            String deviceFingerprint = loginRequest.deviceFingerprint();

            AuthResult authResult = authenticationService.authenticate(
                    loginRequest.email(),
                    loginRequest.password(),
                    tenantId,
                    effectiveSubdomain,
                    deviceFingerprint,
                    ipAddress,
                    userAgent);

            LoginResponse loginResponse = toLoginResponse(authResult);
            String message = messageSource.getMessage("auth.login.successful", null,
                    Locale.forLanguageTag(languageCode));
            ApiResponse<LoginResponse> response = ApiResponse.success(message, loginResponse);

            log.info("Login successful for email: {}", loginRequest.email());
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Login failed for email {}: {}", loginRequest.email(), ex.getMessage());
            String message = messageSource.getMessage("auth.login.error", new Object[] { ex.getMessage() },
                    Locale.forLanguageTag(languageCode));
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

            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                throw new IllegalArgumentException("Refresh token cannot be null or empty");
            }
            String token = refreshToken.startsWith("Bearer ") ? refreshToken.substring(7) : refreshToken;
            if (token.trim().isEmpty()) {
                throw new IllegalArgumentException("Invalid token format");
            }
            AuthResult authResult = authenticationService.refreshToken(token);
            LoginResponse loginResponse = toLoginResponse(authResult);
            String message = messageSource.getMessage("auth.refresh.success", null,
                    Locale.forLanguageTag(languageCode));
            ApiResponse<LoginResponse> response = ApiResponse.success(message, loginResponse);

            log.info("Token refresh successful");
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Token refresh failed: {}", ex.getMessage());
            String message = messageSource.getMessage("auth.refresh.error", new Object[] { ex.getMessage() },
                    Locale.forLanguageTag(languageCode));
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
            if (token == null || token.trim().isEmpty()) {
                throw new IllegalArgumentException("Token cannot be null or empty");
            }
            String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;

            authenticationService.logout(cleanToken);

            String message = messageSource.getMessage("auth.logout.success", null, Locale.forLanguageTag(languageCode));
            log.info("Logout successful");
            return ResponseEntity.ok(ApiResponse.success(message, null));
        } catch (Exception ex) {
            log.error("Logout failed: {}", ex.getMessage());
            String message = messageSource.getMessage("auth.logout.error", new Object[] { ex.getMessage() },
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            @RequestHeader(value = "X-Tenant-ID", required = false) Long tenantId,
            @RequestHeader(value = "X-Tenant-Subdomain", required = false) String subdomain,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode,
            HttpServletRequest httpRequest) {
        try {
            log.info("Password reset requested for email: {}", request.email());

            Language language = RequestUtils.parseLanguage(languageCode);
            String ipAddress = RequestUtils.getClientIpAddress(httpRequest);
            String userAgent = RequestUtils.getUserAgent(httpRequest);

            authenticationService.requestPasswordReset(
                    request.email(), tenantId, subdomain, ipAddress, userAgent, language);

            String message = messageSource.getMessage("auth.password.reset.sent", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, null));
        } catch (Exception ex) {
            log.error("Password reset request failed: {}", ex.getMessage());
            String message = messageSource.getMessage("auth.password.reset.sent", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, null));
        }
    }

    @GetMapping("/verify-reset-token")
    public ResponseEntity<ApiResponse<TokenValidationResponse>> verifyResetToken(
            @RequestParam String token,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            TokenValidationResult result = authenticationService.validateResetToken(token);
            TokenValidationResponse validation = toTokenValidationResponse(result);

            if (!validation.valid()) {
                String message = messageSource.getMessage("auth.password.reset.invalid.token", null,
                        Locale.forLanguageTag(languageCode));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error(message));
            }

            return ResponseEntity.ok(ApiResponse.success(null, validation));
        } catch (Exception ex) {
            log.error("Token validation failed: {}", ex.getMessage());
            String message = messageSource.getMessage("auth.password.reset.invalid.token", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            log.info("Password reset attempt");

            authenticationService.resetPassword(request.token(), request.password());

            String message = messageSource.getMessage("auth.password.reset.success", null,
                    Locale.forLanguageTag(languageCode));
            log.info("Password reset successful");
            return ResponseEntity.ok(ApiResponse.success(message, null));
        } catch (Exception ex) {
            log.error("Password reset failed: {}", ex.getMessage());
            String message = messageSource.getMessage("auth.password.reset.invalid.token", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/verify-email-token")
    public ResponseEntity<ApiResponse<TokenValidationResponse>> verifyEmailToken(
            @RequestParam String token,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            TokenValidationResult result = authenticationService.validateEmailVerificationToken(token);
            TokenValidationResponse validation = toTokenValidationResponse(result);

            if (!validation.valid()) {
                String message = messageSource.getMessage("auth.email.verify.invalid.token", null,
                        Locale.forLanguageTag(languageCode));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error(message));
            }

            return ResponseEntity.ok(ApiResponse.success(null, validation));
        } catch (Exception ex) {
            log.error("Email token validation failed: {}", ex.getMessage());
            String message = messageSource.getMessage("auth.email.verify.invalid.token", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/set-initial-password")
    public ResponseEntity<ApiResponse<Void>> setInitialPassword(
            @Valid @RequestBody SetInitialPasswordRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            log.info("Setting initial password");

            authenticationService.setInitialPassword(request.token(), request.password());

            String message = messageSource.getMessage("auth.email.verify.success", null,
                    Locale.forLanguageTag(languageCode));
            log.info("Initial password set successfully");
            return ResponseEntity.ok(ApiResponse.success(message, null));
        } catch (Exception ex) {
            log.error("Set initial password failed: {}", ex.getMessage());
            String message = messageSource.getMessage("auth.email.verify.invalid.token", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<LoginResponse>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode,
            HttpServletRequest httpRequest) {
        try {
            log.info("OTP verification attempt");

            String ipAddress = RequestUtils.getClientIpAddress(httpRequest);
            String userAgent = RequestUtils.getUserAgent(httpRequest);

            AuthResult authResult = authenticationService.verifyOtp(
                    request.pendingToken(),
                    request.otpCode(),
                    request.trustDevice(),
                    request.deviceFingerprint(),
                    request.deviceName(),
                    ipAddress,
                    userAgent,
                    request.tenantId(),
                    request.subdomain());

            LoginResponse loginResponse = toLoginResponse(authResult);
            String message = messageSource.getMessage("auth.login.successful", null,
                    Locale.forLanguageTag(languageCode));
            log.info("OTP verification successful");
            return ResponseEntity.ok(ApiResponse.success(message, loginResponse));
        } catch (Exception ex) {
            log.error("OTP verification failed: {}", ex.getMessage());
            String message = messageSource.getMessage("auth.2fa.otp.invalid", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(message));
        }
    }

    private LoginResponse toLoginResponse(AuthResult authResult) {
        if (authResult.requires2FA()) {
            return LoginResponse.requiring2FA(
                    authResult.email(),
                    authResult.pendingToken(),
                    authResult.subdomain(),
                    authResult.tenantId());
        }
        return new LoginResponse(
                authResult.accessToken(),
                authResult.refreshToken(),
                authResult.tokenType(),
                authResult.expiresIn(),
                authResult.userId(),
                authResult.email(),
                authResult.role(),
                authResult.subdomain(),
                authResult.tenantId());
    }

    private TokenValidationResponse toTokenValidationResponse(TokenValidationResult result) {
        return new TokenValidationResponse(
                result.valid(),
                result.email(),
                result.tokenType(),
                result.expiryMinutes());
    }
}
