package com.backend.application.support;

import java.util.Locale;
import java.util.Optional;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;

import com.backend.application.dto.response.BulkDeleteResultResponse;
import com.backend.domain.exception.BusinessRuleViolationException;
import com.backend.domain.exception.EntityNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class BulkDeleteExceptionMapper {

    private BulkDeleteExceptionMapper() {
    }

    public static BulkDeleteResultResponse.BulkDeleteError toError(Long id, Exception ex,
            MessageSource messageSource) {
        Locale locale = Optional.ofNullable(LocaleContextHolder.getLocale()).orElse(Locale.ENGLISH);
        String code;
        String messageKey;
        if (ex instanceof EntityNotFoundException) {
            code = "NOT_FOUND";
            messageKey = "bulk.delete.item.not_found";
        } else if (ex instanceof BusinessRuleViolationException) {
            code = "BUSINESS_RULE";
            messageKey = "bulk.delete.item.business";
        } else if (ex instanceof DataIntegrityViolationException) {
            code = "CONSTRAINT";
            messageKey = "bulk.delete.item.constraint";
        } else if (ex instanceof IllegalArgumentException) {
            code = "BUSINESS_RULE";
            messageKey = "bulk.delete.item.business";
        } else {
            code = "GENERIC";
            messageKey = "bulk.delete.item.generic";
        }
        log.warn("Bulk delete skipped id {} [{}]: {}", id, code, ex.getMessage());
        String message = messageSource.getMessage(messageKey, null, messageKey, locale);
        return new BulkDeleteResultResponse.BulkDeleteError(id, code, message);
    }
}
