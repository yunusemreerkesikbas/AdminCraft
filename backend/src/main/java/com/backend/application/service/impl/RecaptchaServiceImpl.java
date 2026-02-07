package com.backend.application.service.impl;

import com.backend.application.service.RecaptchaService;
import com.backend.domain.entity.Site;
import com.backend.domain.exception.RecaptchaVerificationException;
import com.backend.domain.repository.SiteRepository;
import com.backend.infrastructure.security.EncryptionService;
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
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecaptchaServiceImpl implements RecaptchaService {

    private final SiteRepository siteRepository;
    private final EncryptionService encryptionService;
    private final RestTemplate restTemplate;

    @Value("${app.recaptcha.verify-url:https://www.google.com/recaptcha/api/siteverify}")
    private String verifyUrl;

    @Override
    public boolean verifyToken(String token, String action) {
        Site site = getFirstSite();

        if (!Boolean.TRUE.equals(site.getRecaptchaEnabled())) {
            log.debug("reCAPTCHA not enabled for site {}", site.getId());
            return true;
        }

        if (token == null || token.isBlank()) {
            log.warn("reCAPTCHA token is null or empty");
            throw new RecaptchaVerificationException("reCAPTCHA token is required");
        }

        String encryptedSecretKey = site.getRecaptchaSecretKeyEncrypted();

        if (encryptedSecretKey == null || encryptedSecretKey.isBlank()) {
            log.error("No secret key configured for reCAPTCHA verification (siteId: {})", site.getId());
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
                    new ParameterizedTypeReference<>() {});
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
            log.warn("reCAPTCHA verification failed for siteId: {}, errors: {}",
                    site.getId(), responseBody.get("error-codes"));
            throw new RecaptchaVerificationException("reCAPTCHA verification failed");
        }

        Object scoreObj = responseBody.get("score");
        if (scoreObj == null) {
            log.warn("No score returned from reCAPTCHA API for siteId: {}", site.getId());
            throw new RecaptchaVerificationException("Invalid reCAPTCHA response");
        }

        if (!(scoreObj instanceof Number scoreNum)) {
            log.warn("Invalid reCAPTCHA response: missing or invalid score for siteId: {}", site.getId());
            throw new RecaptchaVerificationException("Invalid reCAPTCHA response: missing score");
        }
        double score = scoreNum.doubleValue();
        BigDecimal threshold = site.getRecaptchaThreshold();

        if (threshold == null) {
            threshold = new BigDecimal("0.5");
        }

        if (action != null) {
            Object actionObj = responseBody.get("action");
            if (!(actionObj instanceof String returnedAction) || !action.equals(returnedAction)) {
                log.warn("reCAPTCHA action mismatch. Expected: {}, Got: {} (siteId: {})",
                        action, actionObj, site.getId());
                throw new RecaptchaVerificationException("reCAPTCHA action mismatch");
            }
        }

        boolean passed = score >= threshold.doubleValue();

        log.info("reCAPTCHA verification for siteId: {}, action: {}, score: {}, threshold: {}, passed: {}",
                site.getId(), action, score, threshold, passed);

        if (!passed) {
            throw new RecaptchaVerificationException("reCAPTCHA score below threshold");
        }

        return true;
    }

    private Site getFirstSite() {
        return siteRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new RecaptchaVerificationException("No site configured for tenant"));
    }
}
