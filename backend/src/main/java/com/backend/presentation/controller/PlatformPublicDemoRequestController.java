package com.backend.presentation.controller;

import java.util.Locale;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.service.PlatformDemoRequestService;
import com.backend.presentation.dto.request.PlatformPublicDemoRequestSubmitRequest;
import com.backend.shared.common.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/platform/public/demo-requests")
@RequiredArgsConstructor
public class PlatformPublicDemoRequestController {

    private final PlatformDemoRequestService platformDemoRequestService;
    private final MessageSource messageSource;

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> submit(
            @Valid @RequestBody PlatformPublicDemoRequestSubmitRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = resolveClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        platformDemoRequestService.submit(request.toCommand(clientIp, userAgent));
        Locale locale = LocaleContextHolder.getLocale();
        String followUpNote = messageSource.getMessage("platform.demo.request.submitted.note", null, "", locale);
        return ResponseEntity.ok(ApiResponse.success(messageCode("platform.demo.request.submitted"),
                Map.of("followUpNote", followUpNote)));
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
