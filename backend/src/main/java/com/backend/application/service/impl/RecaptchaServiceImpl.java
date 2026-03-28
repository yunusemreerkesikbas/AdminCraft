package com.backend.application.service.impl;

import com.backend.application.service.RecaptchaService;
import com.backend.application.service.config.ConfigPropertyService;
import com.backend.application.service.config.GlobalRuntimeConfigService;
import com.backend.domain.port.EncryptionServicePort;
import com.backend.domain.port.PlatformSettingsPort;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.exception.RecaptchaVerificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecaptchaServiceImpl implements RecaptchaService {

    private static final String KEY_RECAPTCHA_ENABLED = "security.recaptcha.enabled";
    private static final String KEY_RECAPTCHA_SITE_KEY = "security.recaptcha.site_key";
    private static final String KEY_RECAPTCHA_SECRET_KEY = "security.recaptcha.secret_key";

    private final PlatformSettingsPort platformSettings;
    private final GlobalRuntimeConfigService globalRuntimeConfigService;
    private final ConfigPropertyService configPropertyService;
    private final TenantContextPort tenantContext;
    private final EncryptionServicePort encryptionService;
    private final RestTemplate restTemplate;

    @Value("${app.recaptcha.verify-url:https://www.google.com/recaptcha/api/siteverify}")
    private String verifyUrl;

    @Override
    public boolean verifyToken(String token, String action) {
        RecaptchaContext context = resolveRecaptchaContext();

        if (!context.enabled()) {
            log.debug("reCAPTCHA is not enabled for {}", context.scope());
            return true;
        }

        if (token == null || token.isBlank()) {
            log.warn("reCAPTCHA token is null or empty");
            throw new RecaptchaVerificationException("reCAPTCHA token is required");
        }

        String encryptedSecretKey = context.encryptedSecretKey();

        if (encryptedSecretKey == null || encryptedSecretKey.isBlank()) {
            log.error("No secret key configured for reCAPTCHA verification ({})", context.scope());
            throw new RecaptchaVerificationException("reCAPTCHA configuration error");
        }

        String secretKey = encryptionService.decrypt(encryptedSecretKey);

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("secret", secretKey);
        requestBody.add("response", token);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map<String, Object>> response;
        try {
            response = restTemplate.exchange(
                    verifyUrl, HttpMethod.POST, request,
                    new ParameterizedTypeReference<>() {
                    });
        } catch (RestClientException e) {
            log.error("Failed to call Google reCAPTCHA API", e);
            throw new RecaptchaVerificationException("reCAPTCHA service unavailable", e);
        }

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            log.error("Invalid response from Google reCAPTCHA API: {}", response.getStatusCode());
            throw new RecaptchaVerificationException("Failed to verify reCAPTCHA");
        }

        Map<String, Object> responseBody = response.getBody();
        Object successObj = responseBody.get("success");

        if (!(successObj instanceof Boolean success) || !success) {
            log.warn("reCAPTCHA verification failed for {}, errors: {}",
                    context.scope(), responseBody.get("error-codes"));
            throw new RecaptchaVerificationException("reCAPTCHA verification failed");
        }

        Object scoreObj = responseBody.get("score");
        if (scoreObj == null) {
            log.warn("No score returned from reCAPTCHA API for {}", context.scope());
            throw new RecaptchaVerificationException("Invalid reCAPTCHA response");
        }

        if (!(scoreObj instanceof Number scoreNum)) {
            log.warn("Invalid reCAPTCHA response: missing or invalid score for {}", context.scope());
            throw new RecaptchaVerificationException("Invalid reCAPTCHA response: missing score");
        }
        double score = scoreNum.doubleValue();
        BigDecimal threshold = context.threshold();

        if (threshold == null) {
            threshold = new BigDecimal("0.5");
        }

        if (action != null) {
            Object actionObj = responseBody.get("action");
            if (!(actionObj instanceof String returnedAction) || !action.equals(returnedAction)) {
                log.warn("reCAPTCHA action mismatch. Expected: {}, Got: {} ({})",
                        action, actionObj, context.scope());
                throw new RecaptchaVerificationException("reCAPTCHA action mismatch");
            }
        }

        boolean passed = score >= threshold.doubleValue();

        log.info("reCAPTCHA verification for {}, action: {}, score: {}, threshold: {}, passed: {}",
                context.scope(), action, score, threshold, passed);

        if (!passed) {
            throw new RecaptchaVerificationException("reCAPTCHA score below threshold");
        }

        return true;
    }

    @Override
    public Map<String, String> getPublicClientConfig() {
        RecaptchaPublicConfig config = resolvePublicConfig();
        Map<String, String> response = new LinkedHashMap<>();
        response.put("security.recaptcha.enabled", String.valueOf(config.enabled()));
        response.put("security.recaptcha.site_key", config.siteKey() != null ? config.siteKey() : "");
        return response;
    }

    private RecaptchaContext resolveRecaptchaContext() {
        if (tenantContext.isSet()) {
            String tenantIdRaw = tenantContext.getTenantId();
            String tenantDbName = tenantContext.getTenantDbName();
            if (tenantIdRaw == null || tenantDbName == null || tenantDbName.isBlank()) {
                log.debug("Tenant context is missing identifiers, reCAPTCHA treated as disabled");
                return new RecaptchaContext("tenant (missing context)", false, null, new BigDecimal("0.5"));
            }

            Long tenantId = Long.parseLong(tenantIdRaw);
            boolean enabled = configPropertyService.getBoolean(
                    tenantId,
                    tenantDbName,
                    KEY_RECAPTCHA_ENABLED,
                    false);
            String encryptedSecret = configPropertyService.findRaw(tenantId, tenantDbName, KEY_RECAPTCHA_SECRET_KEY)
                    .filter(value -> value != null && !value.isBlank())
                    .orElse(null);
            return new RecaptchaContext(
                    "tenant tenantId=" + tenantId,
                    enabled,
                    encryptedSecret,
                    new BigDecimal("0.5"));
        }

        return resolvePlatformRecaptchaContext();
    }

    private RecaptchaPublicConfig resolvePublicConfig() {
        if (tenantContext.isSet()) {
            String tenantIdRaw = tenantContext.getTenantId();
            String tenantDbName = tenantContext.getTenantDbName();
            if (tenantIdRaw == null || tenantDbName == null || tenantDbName.isBlank()) {
                return new RecaptchaPublicConfig(false, "");
            }

            Long tenantId = Long.parseLong(tenantIdRaw);
            boolean enabled = configPropertyService.getBoolean(
                    tenantId,
                    tenantDbName,
                    KEY_RECAPTCHA_ENABLED,
                    false);
            String siteKey = configPropertyService.findRaw(tenantId, tenantDbName, KEY_RECAPTCHA_SITE_KEY)
                    .filter(value -> value != null && !value.isBlank())
                    .orElse(null);
            return new RecaptchaPublicConfig(
                    enabled,
                    siteKey);
        }

        var settings = platformSettings.getSingleton();
        Boolean enabledOverride = globalRuntimeConfigService.getRecaptchaEnabled();
        String siteKeyOverride = globalRuntimeConfigService.getRecaptchaSiteKey();
        boolean enabled = enabledOverride != null ? enabledOverride : Boolean.TRUE.equals(settings.getRecaptchaEnabled());
        String siteKey = (siteKeyOverride != null && !siteKeyOverride.isBlank())
                ? siteKeyOverride
                : settings.getRecaptchaSiteKey();
        return new RecaptchaPublicConfig(enabled, siteKey);
    }

    private RecaptchaContext resolvePlatformRecaptchaContext() {
        var settings = platformSettings.getSingleton();
        Boolean globalEnabled = globalRuntimeConfigService.getRecaptchaEnabled();
        String globalSecretEncrypted = globalRuntimeConfigService.getRecaptchaSecretKeyEncrypted();
        boolean enabled = globalEnabled != null ? globalEnabled : Boolean.TRUE.equals(settings.getRecaptchaEnabled());
        String encryptedSecret = (globalSecretEncrypted != null && !globalSecretEncrypted.isBlank())
                ? globalSecretEncrypted
                : settings.getRecaptchaSecretKeyEncrypted();
        return new RecaptchaContext(
                "platform runtime config",
                enabled,
                encryptedSecret,
                settings.getRecaptchaThreshold());
    }

    private record RecaptchaContext(
            String scope,
            boolean enabled,
            String encryptedSecretKey,
            BigDecimal threshold) {
    }

    private record RecaptchaPublicConfig(
            boolean enabled,
            String siteKey) {
    }
}
