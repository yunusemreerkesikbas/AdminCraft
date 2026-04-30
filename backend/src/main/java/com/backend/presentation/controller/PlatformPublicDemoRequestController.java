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

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/platform/public/demo-requests")
@RequiredArgsConstructor
@Tag(name = "Platform Public Demo Requests", description = "Public endpoint for landing demo request submissions")
public class PlatformPublicDemoRequestController {

    private final PlatformDemoRequestService platformDemoRequestService;
    private final MessageSource messageSource;

    @RateLimiter(name = "demoRequest")
    @PostMapping
    @Operation(summary = "Submit demo request", description = "Accepts public demo request submission from landing")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Submission accepted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed")
    })
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
