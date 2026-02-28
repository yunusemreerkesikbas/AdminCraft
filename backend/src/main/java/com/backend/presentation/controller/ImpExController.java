package com.backend.presentation.controller;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.dto.impex.ImpExRequest;
import com.backend.application.dto.impex.ImpExResult;
import com.backend.application.service.impex.ImpExInvalidScriptException;
import com.backend.application.service.impex.ImpExService;
import com.backend.shared.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/impex")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('TENANT_ADMIN', 'VIEWER')")
public class ImpExController {

    private final ImpExService impExService;
    private final MessageSource messageSource;

    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<ImpExResult>> execute(
            @Valid @RequestBody ImpExRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {

        Locale locale = Locale.forLanguageTag(languageCode);

        try {
            log.info("ImpEx execute request received");

            ImpExResult result = impExService.execute(request.sqlContent(), locale);

            if (result.failedStatements() > 0) {
                String message = messageSource.getMessage(
                    "impex.partial.success",
                    new Object[]{ result.executedStatements(), result.totalStatements() },
                    locale
                );
                log.info("ImpEx completed with partial failure — {}/{} succeeded", result.executedStatements(), result.totalStatements());
                return ResponseEntity.status(HttpStatus.MULTI_STATUS)
                        .body(ApiResponse.success(message, result));
            }

            String message = messageSource.getMessage(
                "impex.success",
                new Object[]{ result.totalStatements() },
                locale
            );
            log.info("ImpEx completed successfully — {} statement(s) executed", result.totalStatements());
            return ResponseEntity.ok(ApiResponse.success(message, result));

        } catch (ImpExInvalidScriptException ex) {
            log.warn("ImpEx rejected — invalid content: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ex.getMessage()));
        } catch (Exception ex) {
            log.error("ImpEx unexpected error: {}", ex.getMessage(), ex);
            String message = messageSource.getMessage("impex.error.execute", null, locale);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(message));
        }
    }
}
