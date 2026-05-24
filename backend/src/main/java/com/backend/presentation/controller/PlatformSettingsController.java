package com.backend.presentation.controller;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.dto.request.PatchPlatformSettingsRequest;
import com.backend.application.dto.response.PlatformSettingsData;
import com.backend.application.service.PlatformSecuritySettingsService;
import com.backend.application.service.PlatformSettingsService;
import com.backend.domain.exception.InvalidCredentialsException;
import com.backend.domain.exception.InvalidTokenException;
import com.backend.domain.exception.OtpRateLimitExceededException;
import com.backend.domain.exception.TwoFactorPolicyVerificationRequiredException;
import com.backend.presentation.dto.request.ConfirmTwoFactorPolicyChangeRequest;
import com.backend.presentation.dto.request.RequestTwoFactorPolicyChangeRequest;
import com.backend.presentation.dto.response.PlatformSettingsResponse;
import com.backend.presentation.dto.response.TwoFactorPolicyChangeRequestResponse;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.RequestUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/platform/settings")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequiredArgsConstructor
public class PlatformSettingsController {

    private final PlatformSettingsService service;
    private final PlatformSecuritySettingsService platformSecuritySettingsService;
    private final MessageSource messageSource;

    @GetMapping
    public ResponseEntity<ApiResponse<PlatformSettingsResponse>> getSettings() {
        return ResponseEntity.ok(ApiResponse.success(
                PlatformSettingsResponse.from(service.getSettings())));
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<PlatformSettingsResponse>> patchSettings(
            @Valid @RequestBody PatchPlatformSettingsRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    PlatformSettingsResponse.from(service.patchSettings(request))));
        } catch (TwoFactorPolicyVerificationRequiredException ex) {
            String message = messageSource.getMessage("platform.settings.security.verification.required", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/two-factor/request-change")
    public ResponseEntity<ApiResponse<TwoFactorPolicyChangeRequestResponse>> requestTwoFactorPolicyChange(
            @Valid @RequestBody RequestTwoFactorPolicyChangeRequest request,
            HttpServletRequest httpRequest,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            var result = platformSecuritySettingsService.requestTwoFactorPolicyChange(
                    request.twoFactorPolicy(),
                    RequestUtils.getClientIpAddress(httpRequest),
                    RequestUtils.getUserAgent(httpRequest));
            String message = messageSource.getMessage("platform.settings.security.twoFactor.otp.sent", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, TwoFactorPolicyChangeRequestResponse.from(result)));
        } catch (OtpRateLimitExceededException ex) {
            String message = messageSource.getMessage("auth.otp.rateLimit", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error(message));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ex.getMessage()));
        } catch (Exception ex) {
            log.error("Error requesting platform two-factor policy change", ex);
            String message = messageSource.getMessage("platform.settings.security.twoFactor.otp.request.error", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/two-factor/confirm-change")
    public ResponseEntity<ApiResponse<PlatformSettingsResponse>> confirmTwoFactorPolicyChange(
            @Valid @RequestBody ConfirmTwoFactorPolicyChangeRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            PlatformSettingsData data = platformSecuritySettingsService.confirmTwoFactorPolicyChange(
                    request.pendingChangeId(),
                    request.otpCode());
            String message = messageSource.getMessage("platform.settings.messages.saveSuccess", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, PlatformSettingsResponse.from(data)));
        } catch (InvalidTokenException ex) {
            String message = messageSource.getMessage("platform.settings.security.twoFactor.otp.invalid", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(message));
        } catch (InvalidCredentialsException ex) {
            String message = messageSource.getMessage("auth.otp.invalid", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(message));
        } catch (Exception ex) {
            log.error("Error confirming platform two-factor policy change", ex);
            String message = messageSource.getMessage("platform.settings.messages.saveFailed", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(message));
        }
    }
}
