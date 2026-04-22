package com.backend.presentation.controller;

import java.time.Duration;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
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
import com.backend.application.service.RecaptchaService;
import com.backend.domain.enums.Language;
import com.backend.domain.exception.AccountLockedException;
import com.backend.domain.exception.InvalidTokenException;
import com.backend.domain.exception.OtpRateLimitExceededException;
import com.backend.domain.exception.RecaptchaVerificationException;
import com.backend.infrastructure.security.JwtProperties;
import com.backend.presentation.dto.request.ForgotPasswordRequest;
import com.backend.presentation.dto.request.LoginRequest;
import com.backend.presentation.dto.request.ResetPasswordRequest;
import com.backend.presentation.dto.request.SetInitialPasswordRequest;
import com.backend.presentation.dto.request.VerifyOtpRequest;
import com.backend.presentation.dto.response.LoginResponse;
import com.backend.presentation.dto.response.TokenValidationResponse;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.RequestUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "Authentication", description = "Authentication and credential recovery endpoints")
public class AuthController {

        private final AuthenticationService authenticationService;
        private final MessageSource messageSource;
        private final RecaptchaService recaptchaService;
        private final JwtProperties jwtProperties;

        @PostMapping("/login")
        @Operation(summary = "Login", description = "Authenticates user credentials and returns access/refresh tokens or 2FA challenge")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation or reCAPTCHA failed"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials or account locked"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
        })
        public ResponseEntity<ApiResponse<?>> login(
                        @Valid @RequestBody LoginRequest loginRequest,
                        @RequestHeader(value = "X-Tenant-ID", required = false) Long tenantId,
                        @RequestHeader(value = "X-Tenant-Subdomain", required = false) String subdomain,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode,
                        HttpServletRequest httpRequest,
                        HttpServletResponse httpResponse) throws OtpRateLimitExceededException {
                try {
                        log.info("Login attempt received");

                        if (loginRequest.email() == null || loginRequest.email().trim().isEmpty()) {
                                throw new IllegalArgumentException("Email cannot be null or empty");
                        }

                        String effectiveSubdomain = subdomain != null ? subdomain : loginRequest.subdomain();

                        validateRecaptchaIfEnabled(loginRequest.recaptchaToken(), "login");

                        String ipAddress = RequestUtils.getClientIpAddress(httpRequest);
                        String userAgent = RequestUtils.getUserAgent(httpRequest);
                        String deviceFingerprint = loginRequest.deviceFingerprint();

                        boolean rememberMe = Boolean.TRUE.equals(loginRequest.rememberMe());
                        AuthResult authResult = authenticationService.authenticate(
                                        loginRequest.email(),
                                        loginRequest.password(),
                                        tenantId,
                                        effectiveSubdomain,
                                        deviceFingerprint,
                                        ipAddress,
                                        userAgent,
                                        rememberMe);

                        LoginResponse loginResponse = toLoginResponse(authResult);
                        if (!authResult.requires2FA() && authResult.refreshToken() != null) {
                                long maxAgeSecs = rememberMe
                                                ? jwtProperties.getRememberMeExpiration() / 1000
                                                : jwtProperties.getRefreshExpiration() / 1000;
                                setRefreshTokenCookie(httpResponse, authResult.refreshToken(), maxAgeSecs);
                        }

                        String message = messageSource.getMessage("auth.login.successful", null,
                                        Locale.forLanguageTag(languageCode));
                        ApiResponse<LoginResponse> response = ApiResponse.success(message, loginResponse);

                        log.info("Login successful");
                        return ResponseEntity.ok(response);
                } catch (AccountLockedException ex) {
                        log.warn("Account locked: {}", ex.getMessage());
                        String message = messageSource.getMessage("auth.account.locked",
                                        new Object[] { ex.getRemainingMinutes() },
                                        Locale.forLanguageTag(languageCode));
                        ApiResponse<?> response = new ApiResponse<>("ERROR", message,
                                        java.util.Map.of(
                                                        "remainingMinutes", ex.getRemainingMinutes(),
                                                        "errorCode", "ACCOUNT_LOCKED"));
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                } catch (RecaptchaVerificationException ex) {
                        log.warn("reCAPTCHA verification failed: {}", ex.getMessage());
                        String message = messageSource.getMessage("recaptcha.verification.failed", null,
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(message));
                } catch (Exception ex) {
                        log.error("Login failed: {}", ex.getMessage());
                        String message = messageSource.getMessage("auth.login.error", null,
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(ApiResponse.error(message));
                }
        }

        @PostMapping("/refresh")
        @Operation(summary = "Refresh token", description = "Exchanges a valid refresh token for a new access token pair")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refreshed"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or expired refresh token"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
        })
        public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
                        @CookieValue(name = "craftive_rt", required = false) String refreshToken,
                        @RequestHeader(value = "X-Device-Fingerprint", required = false) String deviceFingerprint,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode,
                        HttpServletRequest httpRequest,
                        HttpServletResponse httpResponse) {
                try {
                        log.info("Token refresh attempt");

                        if (refreshToken == null || refreshToken.isBlank()) {
                                String message = messageSource.getMessage("auth.refresh.error", null,
                                                Locale.forLanguageTag(languageCode));
                                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(message));
                        }

                        String ipAddress = RequestUtils.getClientIpAddress(httpRequest);
                        String userAgent = RequestUtils.getUserAgent(httpRequest);

                        AuthResult authResult = authenticationService.refreshToken(refreshToken, deviceFingerprint,
                                        ipAddress, userAgent);
                        LoginResponse loginResponse = toLoginResponse(authResult);

                        if (!authResult.requires2FA() && authResult.refreshToken() != null) {
                                long maxAgeSecs = jwtProperties.getRefreshExpiration() / 1000;
                                setRefreshTokenCookie(httpResponse, authResult.refreshToken(), maxAgeSecs);
                        }

                        String message = messageSource.getMessage("auth.refresh.success", null,
                                        Locale.forLanguageTag(languageCode));

                        log.info("Token refresh successful");
                        return ResponseEntity.ok(ApiResponse.success(message, loginResponse));
                } catch (Exception ex) {
                        log.error("Token refresh failed: {}", ex.getMessage());
                        clearRefreshTokenCookie(httpResponse);
                        String message = messageSource.getMessage("auth.refresh.error", null,
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(ApiResponse.error(message));
                }
        }

        @PostMapping("/logout")
        @Operation(summary = "Logout", description = "Invalidates current session token and logs user out")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logout successful"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid token"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
        })
        public ResponseEntity<ApiResponse<Void>> logout(
                        @RequestHeader(value = "Authorization", required = false) String token,
                        @CookieValue(name = "craftive_rt", required = false) String refreshToken,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode,
                        HttpServletResponse httpResponse) {
                try {
                        log.info("Logout attempt");
                        String cleanAccessToken = (token != null && token.startsWith("Bearer "))
                                        ? token.substring(7)
                                        : token;
                        authenticationService.logout(cleanAccessToken, refreshToken);
                        clearRefreshTokenCookie(httpResponse);
                        String message = messageSource.getMessage("auth.logout.success", null,
                                        Locale.forLanguageTag(languageCode));
                        log.info("Logout successful");
                        return ResponseEntity.ok(ApiResponse.success(message, null));
                } catch (Exception ex) {
                        log.error("Logout failed: {}", ex.getMessage());
                        clearRefreshTokenCookie(httpResponse);
                        String message = messageSource.getMessage("auth.logout.error", null,
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(message));
                }
        }

        @PostMapping("/forgot-password")
        @Operation(summary = "Request password reset", description = "Creates password reset request and sends reset email if account exists")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reset flow started"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation or reCAPTCHA failed"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
        })
        public ResponseEntity<ApiResponse<Void>> forgotPassword(
                        @Valid @RequestBody ForgotPasswordRequest request,
                        @RequestHeader(value = "X-Tenant-ID", required = false) Long tenantId,
                        @RequestHeader(value = "X-Tenant-Subdomain", required = false) String subdomain,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode,
                        HttpServletRequest httpRequest) {
                try {
                        log.info("Password reset requested");

                        validateRecaptchaIfEnabled(request.recaptchaToken(), "forgot_password");

                        Language language = RequestUtils.parseLanguage(languageCode);
                        String ipAddress = RequestUtils.getClientIpAddress(httpRequest);
                        String userAgent = RequestUtils.getUserAgent(httpRequest);

                        authenticationService.requestPasswordReset(
                                        request.email(), tenantId, subdomain, ipAddress, userAgent, language);

                        String message = messageSource.getMessage("auth.password.reset.sent", null,
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.ok(ApiResponse.success(message, null));
                } catch (RecaptchaVerificationException ex) {
                        log.warn("reCAPTCHA verification failed for forgot password: {}", ex.getMessage());
                        String message = messageSource.getMessage("recaptcha.verification.failed", null,
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(message));
                } catch (Exception ex) {
                        log.error("Password reset request failed: {}", ex.getMessage());
                        String message = messageSource.getMessage("auth.password.reset.sent", null,
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.ok(ApiResponse.success(message, null));
                }
        }

        @GetMapping("/verify-reset-token")
        @Operation(summary = "Verify reset token", description = "Validates password reset token state")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token validation returned"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Token invalid or expired"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
        })
        public ResponseEntity<ApiResponse<TokenValidationResponse>> verifyResetToken(
                        @RequestParam @NotBlank String token,
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
        @Operation(summary = "Reset password", description = "Sets new password with a valid password-reset token")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password reset successful"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid token, invalid payload or reCAPTCHA failed"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
        })
        public ResponseEntity<ApiResponse<Void>> resetPassword(
                        @Valid @RequestBody ResetPasswordRequest request,
                        @RequestHeader(value = "X-Tenant-ID", required = false) Long tenantId,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                try {
                        log.info("Password reset attempt");

                        validateRecaptchaIfEnabled(request.recaptchaToken(), "reset_password");

                        authenticationService.resetPassword(request.token(), request.password());

                        String message = messageSource.getMessage("auth.password.reset.success", null,
                                        Locale.forLanguageTag(languageCode));
                        log.info("Password reset successful");
                        return ResponseEntity.ok(ApiResponse.success(message, null));
                } catch (RecaptchaVerificationException ex) {
                        log.warn("reCAPTCHA verification failed for reset password: {}", ex.getMessage());
                        String message = messageSource.getMessage("recaptcha.verification.failed", null,
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(message));
                } catch (Exception ex) {
                        log.error("Password reset failed: {}", ex.getMessage());
                        String message = messageSource.getMessage("auth.password.reset.invalid.token", null,
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(message));
                }
        }

        @GetMapping("/verify-email-token")
        @Operation(summary = "Verify email token", description = "Validates email verification token state")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token validation returned"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Token invalid or expired"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
        })
        public ResponseEntity<ApiResponse<TokenValidationResponse>> verifyEmailToken(
                        @RequestParam @NotBlank String token,
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
        @Operation(summary = "Set initial password", description = "Sets initial password after email verification")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Initial password set"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid token, invalid payload or reCAPTCHA failed"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
        })
        public ResponseEntity<ApiResponse<Void>> setInitialPassword(
                        @Valid @RequestBody SetInitialPasswordRequest request,
                        @RequestHeader(value = "X-Tenant-ID", required = false) Long tenantId,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode,
                        HttpServletRequest httpRequest) {
                try {
                        log.info("Setting initial password");

                        validateRecaptchaIfEnabled(request.recaptchaToken(), "set_password");

                        authenticationService.setInitialPassword(request.token(), request.password());

                        String message = messageSource.getMessage("auth.email.verify.success", null,
                                        Locale.forLanguageTag(languageCode));

                        log.info("Initial password set successfully");
                        return ResponseEntity.ok(ApiResponse.success(message, null));

                } catch (InvalidTokenException ex) {
                        log.error("Invalid token for initial password: {}", ex.getMessage());
                        String message = messageSource.getMessage("auth.email.verify.invalid.token", null,
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(message));
                } catch (RecaptchaVerificationException ex) {
                        log.warn("reCAPTCHA verification failed for set initial password: {}", ex.getMessage());
                        String message = messageSource.getMessage("recaptcha.verification.failed", null,
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(message));
                } catch (Exception ex) {
                        log.error("Failed to set initial password: {}", ex.getMessage(), ex);
                        String message = messageSource.getMessage("auth.error.generic", null,
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.error(message));
                }
        }

        @PostMapping("/verify-otp")
        @Operation(summary = "Verify OTP", description = "Verifies one-time password for two-factor authentication flow")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OTP verified"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid OTP or pending token"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
        })
        public ResponseEntity<ApiResponse<LoginResponse>> verifyOtp(
                        @Valid @RequestBody VerifyOtpRequest request,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode,
                        HttpServletRequest httpRequest,
                        HttpServletResponse httpResponse) throws OtpRateLimitExceededException {
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
                        if (!authResult.requires2FA() && authResult.refreshToken() != null) {
                                long maxAgeSecs = jwtProperties.getRefreshExpiration() / 1000;
                                setRefreshTokenCookie(httpResponse, authResult.refreshToken(), maxAgeSecs);
                        }

                        String message = messageSource.getMessage("auth.login.successful", null,
                                        Locale.forLanguageTag(languageCode));
                        log.info("OTP verification successful");
                        return ResponseEntity.ok(ApiResponse.success(message, loginResponse));
                } catch (InvalidTokenException ex) {
                        log.error("Invalid token during OTP verification: {}", ex.getMessage());
                        String message = messageSource.getMessage("auth.2fa.otp.invalid", null,
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(ApiResponse.error(message));
                } catch (Exception ex) {
                        log.error("OTP verification failed: {}", ex.getMessage());
                        String message = messageSource.getMessage("auth.2fa.otp.invalid", null,
                                        Locale.forLanguageTag(languageCode));
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(ApiResponse.error(message));
                }
        }

        private void setRefreshTokenCookie(HttpServletResponse response, String token, long maxAgeSeconds) {
                JwtProperties.Cookie cfg = jwtProperties.getCookie();
                ResponseCookie cookie = ResponseCookie.from(cfg.getName(), token)
                                .httpOnly(true)
                                .secure(cfg.isSecure())
                                .path(cfg.getPath())
                                .maxAge(Duration.ofSeconds(maxAgeSeconds))
                                .sameSite(cfg.getSameSite())
                                .build();
                response.addHeader(HttpHeaders.SET_COOKIE, cookie.toHeaderValue());
        }

        private void clearRefreshTokenCookie(HttpServletResponse response) {
                JwtProperties.Cookie cfg = jwtProperties.getCookie();
                ResponseCookie cookie = ResponseCookie.from(cfg.getName(), "")
                                .httpOnly(true)
                                .secure(cfg.isSecure())
                                .path(cfg.getPath())
                                .maxAge(Duration.ZERO)
                                .sameSite(cfg.getSameSite())
                                .build();
                response.addHeader(HttpHeaders.SET_COOKIE, cookie.toHeaderValue());
        }

        private void validateRecaptchaIfEnabled(String token, String action) {
                recaptchaService.verifyToken(token, action);
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
                                authResult.fullName(),
                                authResult.role(),
                                authResult.subdomain(),
                                authResult.tenantId(),
                                false,
                                null);
        }

        private TokenValidationResponse toTokenValidationResponse(TokenValidationResult result) {
                return new TokenValidationResponse(
                                result.valid(),
                                result.email(),
                                result.tokenType(),
                                result.expiryMinutes());
        }
}
