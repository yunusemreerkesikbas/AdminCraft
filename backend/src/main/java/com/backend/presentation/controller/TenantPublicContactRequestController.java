package com.backend.presentation.controller;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.service.ContactRequestService;
import com.backend.presentation.dto.request.PublicContactRequestSubmitRequest;
import com.backend.shared.common.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/public/contact-requests")
@RequiredArgsConstructor
public class TenantPublicContactRequestController {

    private final ContactRequestService contactRequestService;
    private final MessageSource messageSource;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> submit(
            @Valid @RequestBody PublicContactRequestSubmitRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = resolveClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        contactRequestService.submit(request.toCommand(clientIp, userAgent));
        return ResponseEntity.ok(ApiResponse.success(messageCode("contact.request.submitted"), null));
    }

    private String resolveClientIp(HttpServletRequest request) {
        String cfIp = request.getHeader("CF-Connecting-IP");
        if (cfIp != null && !cfIp.isBlank()) {
            return cfIp.trim();
        }
        return request.getRemoteAddr();
    }

    private String messageCode(String key) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, null, key, locale);
    }
}
