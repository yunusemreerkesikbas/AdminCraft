package com.backend.presentation.controller;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.dto.platform.PlatformPublicNewsletterSubscribeCommand;
import com.backend.application.service.PlatformMailMarketingService;
import com.backend.shared.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/platform/public/newsletter")
@RequiredArgsConstructor
@Tag(name = "Platform Public Newsletter", description = "Public newsletter subscribe/confirm/unsubscribe endpoints")
public class PlatformPublicNewsletterController {

    private final PlatformMailMarketingService service;
    private final MessageSource messageSource;

    @PostMapping("/subscribe")
    @Operation(summary = "Subscribe newsletter", description = "Creates newsletter subscription and sends confirmation mail")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Subscription accepted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Action forbidden")
    })
    public ResponseEntity<ApiResponse<Void>> subscribe(@Valid @RequestBody SubscribeRequest request) {
        try {
            String messageKey = service.subscribe(request.toCommand());
            return ResponseEntity.ok(ApiResponse.success(message(messageKey), null));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(message(ex.getMessage())));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(message(ex.getMessage())));
        }
    }

    @GetMapping("/confirm")
    @Operation(summary = "Confirm newsletter subscription", description = "Confirms newsletter subscription token")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Subscription confirmed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Action forbidden")
    })
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
    @Operation(summary = "Unsubscribe newsletter", description = "Cancels newsletter subscription by token")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Unsubscribed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Action forbidden")
    })
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
        @NotBlank String templateType,
        String locale,
        String honeypot,
        Long formStartedAt
    ) {
        PlatformPublicNewsletterSubscribeCommand toCommand() {
            return new PlatformPublicNewsletterSubscribeCommand(
                email,
                source,
                templateType,
                locale,
                honeypot,
                formStartedAt
            );
        }
    }

    public record UnsubscribeRequest(
        @NotBlank String token
    ) {
    }
}
