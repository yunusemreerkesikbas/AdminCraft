package com.backend.presentation.controller;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.service.ContactRequestService;
import com.backend.application.service.PublicContactRateLimitService;
import com.backend.domain.port.TenantContextPort;
import com.backend.infrastructure.config.AppSecurityProperties;
import com.backend.presentation.dto.request.PublicContactRequestSubmitRequest;
import com.backend.shared.common.ApiResponse;

import com.google.common.net.InetAddresses;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/public/contact-requests")
@RequiredArgsConstructor
@Slf4j
public class TenantPublicContactRequestController {

    private final ContactRequestService contactRequestService;
    private final MessageSource messageSource;
    private final AppSecurityProperties appSecurityProperties;
    private final TenantContextPort tenantContext;
    private final PublicContactRateLimitService publicContactRateLimitService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> submit(
            @Valid @RequestBody PublicContactRequestSubmitRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = resolveClientIp(httpRequest);
        publicContactRateLimitService.checkOrThrow(clientIp, tenantContext.getTenantId());
        String userAgent = httpRequest.getHeader("User-Agent");
        contactRequestService.submit(request.toCommand(clientIp, userAgent));
        return ResponseEntity.ok(ApiResponse.success(resolveLocalizedMessage("contact.request.submitted"), null));
    }

    private String resolveClientIp(HttpServletRequest request) {
        String remoteAddr = Optional.ofNullable(request.getRemoteAddr()).orElse("").trim();
        if (appSecurityProperties.isTrustCfConnectingIp() && remoteAddrMatchesTrustedProxy(remoteAddr)) {
            String cf = firstForwardedIp(request.getHeader("CF-Connecting-IP"));
            if (cf != null) {
                return cf;
            }
        }
        return remoteAddr;
    }

    private boolean remoteAddrMatchesTrustedProxy(String remoteAddr) {
        List<String> cidrs = appSecurityProperties.getTrustedProxyCidrs();
        if (cidrs == null || cidrs.isEmpty()) {
            return true;
        }
        for (String cidr : cidrs) {
            if (cidr == null || cidr.isBlank()) {
                continue;
            }
            try {
                if (new IpAddressMatcher(cidr.trim()).matches(remoteAddr)) {
                    return true;
                }
            } catch (IllegalArgumentException ex) {
                log.warn("Invalid trusted proxy CIDR skipped: {}", cidr);
            }
        }
        return false;
    }

    private static String firstForwardedIp(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return null;
        }
        String first = headerValue.split(",")[0].trim();
        if (first.isEmpty()) {
            return null;
        }
        try {
            InetAddresses.forString(first);
            return first;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String resolveLocalizedMessage(String key) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, null, key, locale);
    }
}
