package com.backend.presentation.controller;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.service.config.ConfigAuthenticationService;
import com.backend.domain.exception.AccountLockedException;
import com.backend.domain.exception.InvalidCredentialsException;
import com.backend.domain.exception.InvalidTokenException;
import com.backend.presentation.dto.request.config.ConfigLoginRequest;
import com.backend.presentation.dto.request.config.ConfigRefreshTokenRequest;
import com.backend.presentation.dto.request.config.ConfigVerifyOtpRequest;
import com.backend.presentation.dto.response.config.ConfigAuthChallengeResponse;
import com.backend.presentation.dto.response.config.ConfigAuthResponse;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.RequestUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/config/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "Config Authentication", description = "Authentication endpoints for global config control panel")
public class ConfigAuthController {

    private final ConfigAuthenticationService configAuthenticationService;
    private final MessageSource messageSource;

    @PostMapping("/login")
    @Operation(summary = "Config login", description = "Authenticates user for config panel and starts OTP challenge")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OTP challenge created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials or account locked"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<ApiResponse<ConfigAuthChallengeResponse>> login(
            @Valid @RequestBody ConfigLoginRequest request,
            @RequestHeader(value = "X-Tenant-ID", required = false) Long tenantId,
            @RequestHeader(value = "X-Tenant-Subdomain", required = false) String subdomain,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode,
            HttpServletRequest httpRequest) {
        try {
            var result = configAuthenticationService.login(
                    request.email(),
                    request.password(),
                    tenantId != null ? tenantId : request.tenantId(),
                    subdomain != null ? subdomain : request.subdomain(),
                    RequestUtils.getClientIpAddress(httpRequest),
                    RequestUtils.getUserAgent(httpRequest));

            String message = messageSource.getMessage("auth.2fa.otp.sent", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, ConfigAuthChallengeResponse.from(result)));
        } catch (AccountLockedException ex) {
            String message = messageSource.getMessage("auth.account.locked",
                    new Object[] { ex.getRemainingMinutes() },
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(message));
        } catch (InvalidCredentialsException ex) {
            String message = messageSource.getMessage("auth.login.error", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(message));
        } catch (Exception ex) {
            log.error("Config auth login failed", ex);
            String message = messageSource.getMessage("auth.error.generic", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(message));
        }
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Config verify OTP", description = "Verifies OTP and issues config panel tokens")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OTP verified"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid OTP/token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<ApiResponse<ConfigAuthResponse>> verifyOtp(
            @Valid @RequestBody ConfigVerifyOtpRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode,
            HttpServletRequest httpRequest) {
        try {
            var result = configAuthenticationService.verifyOtp(
                    request.pendingToken(),
                    request.otpCode(),
                    request.tenantId(),
                    request.subdomain(),
                    RequestUtils.getClientIpAddress(httpRequest));

            String message = messageSource.getMessage("auth.login.successful", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, ConfigAuthResponse.from(result)));
        } catch (InvalidTokenException | InvalidCredentialsException ex) {
            String message = messageSource.getMessage("auth.2fa.otp.invalid", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(message));
        } catch (Exception ex) {
            log.error("Config auth OTP verification failed", ex);
            String message = messageSource.getMessage("auth.error.generic", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(message));
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Config refresh token", description = "Refreshes access token for config panel")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refreshed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid refresh token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<ApiResponse<ConfigAuthResponse>> refreshToken(
            @Valid @RequestBody ConfigRefreshTokenRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode,
            HttpServletRequest httpRequest) {
        try {
            var result = configAuthenticationService.refreshToken(
                    request.refreshToken(),
                    RequestUtils.getClientIpAddress(httpRequest));

            String message = messageSource.getMessage("auth.refresh.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, ConfigAuthResponse.from(result)));
        } catch (InvalidTokenException | InvalidCredentialsException | AccountLockedException ex) {
            String message = messageSource.getMessage("auth.refresh.error", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(message));
        } catch (Exception ex) {
            log.error("Config auth refresh failed", ex);
            String message = messageSource.getMessage("auth.error.generic", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(message));
        }
    }
}
