package com.backend.presentation.controller;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.service.TenantMailMarketingService;
import com.backend.shared.common.ApiResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/public/newsletter")
@RequiredArgsConstructor
public class TenantPublicNewsletterController {

    private final TenantMailMarketingService service;
    private final MessageSource messageSource;

    @PostMapping("/subscribe")
    public ResponseEntity<ApiResponse<Void>> subscribe(@Valid @RequestBody SubscribeRequest request) {
        try {
            service.subscribe(request.email(), request.source(), request.templateType());
            return ResponseEntity.ok(ApiResponse.success(message("mail.marketing.newsletter.subscribe.sent"), null));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(message(ex.getMessage())));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(message(ex.getMessage())));
        }
    }

    @GetMapping("/confirm")
    public ResponseEntity<ApiResponse<Void>> confirm(@RequestParam("token") String token) {
        try {
            service.confirm(token);
            return ResponseEntity.ok(ApiResponse.success(message("mail.marketing.newsletter.confirmed"), null));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(message(ex.getMessage())));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(message(ex.getMessage())));
        }
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<ApiResponse<Void>> unsubscribe(@Valid @RequestBody UnsubscribeRequest request) {
        try {
            service.unsubscribe(request.token());
            return ResponseEntity.ok(ApiResponse.success(message("mail.marketing.newsletter.unsubscribed"), null));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(message(ex.getMessage())));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(message(ex.getMessage())));
        }
    }

    private String message(String key) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, null, key, locale);
    }

    public record SubscribeRequest(
        @NotBlank @Email String email,
        String source,
        @NotBlank String templateType
    ) {
    }

    public record UnsubscribeRequest(
        @NotBlank String token
    ) {
    }
}
