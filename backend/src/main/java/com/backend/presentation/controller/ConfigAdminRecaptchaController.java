package com.backend.presentation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.dto.config.ConfigPrincipal;
import com.backend.application.dto.config.PatchConfigRecaptchaParams;
import com.backend.application.service.config.ConfigRecaptchaAdminService;
import com.backend.presentation.dto.request.config.PatchConfigRecaptchaRequest;
import com.backend.presentation.dto.response.config.ConfigAuditItemResponse;
import com.backend.presentation.dto.response.config.ConfigRecaptchaResponse;
import com.backend.shared.common.ApiResponse;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/config/admin")
@RequiredArgsConstructor
public class ConfigAdminRecaptchaController {

    private final ConfigRecaptchaAdminService recaptchaAdminService;
    private final ConfigPrincipalResolver principalResolver;

    @PreAuthorize("hasRole('CONFIG_TENANT_ADMIN')")
    @GetMapping("/security/recaptcha")
    public ResponseEntity<ApiResponse<ConfigRecaptchaResponse>> getRecaptcha(Authentication authentication) {
        ConfigPrincipal principal = principalResolver.resolve(authentication);
        var result = recaptchaAdminService.getRecaptcha(principal);
        return ResponseEntity.ok(ApiResponse.success(ConfigRecaptchaResponse.from(result)));
    }

    @RateLimiter(name = "configAdmin")
    @PreAuthorize("hasRole('CONFIG_TENANT_ADMIN')")
    @PatchMapping("/security/recaptcha")
    public ResponseEntity<ApiResponse<ConfigRecaptchaResponse>> patchRecaptcha(
            Authentication authentication,
            @Valid @RequestBody PatchConfigRecaptchaRequest request) {
        ConfigPrincipal principal = principalResolver.resolve(authentication);
        PatchConfigRecaptchaParams params = new PatchConfigRecaptchaParams(
                request.recaptchaEnabled(),
                request.recaptchaSiteKey(),
                request.recaptchaSecretKey(),
                request.reason());
        var result = recaptchaAdminService.patchRecaptcha(principal, params);
        return ResponseEntity.ok(ApiResponse.success("reCAPTCHA settings updated", ConfigRecaptchaResponse.from(result)));
    }

    @PreAuthorize("hasRole('CONFIG_TENANT_ADMIN')")
    @GetMapping("/security/recaptcha/audit")
    public ResponseEntity<ApiResponse<List<ConfigAuditItemResponse>>> getRecaptchaAudit(
            Authentication authentication,
            @RequestParam(value = "limit", defaultValue = "20") int limit) {
        ConfigPrincipal principal = principalResolver.resolve(authentication);
        var items = recaptchaAdminService.getAuditTrail(principal, limit)
                .stream()
                .map(ConfigAuditItemResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(items));
    }
}
