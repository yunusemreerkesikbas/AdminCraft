package com.backend.shared.common;

import com.backend.domain.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public static ResponseEntity<ApiResponse<?>> errorResponseEntity(String message, HttpStatus status) {
        ApiResponse<?> response = new ApiResponse<>("ERROR", message, null);
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument exception: {}", ex.getMessage());
        String message = getMessage(ex.getMessage());
        return new ResponseEntity<>(ApiResponse.error(400, message), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalStateException(IllegalStateException ex) {
        log.warn("Illegal state exception: {}", ex.getMessage());
        String message = getMessage(ex.getMessage());
        return new ResponseEntity<>(ApiResponse.error(400, message), HttpStatus.BAD_REQUEST);
    }

    // Authentication Exceptions
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidCredentials(InvalidCredentialsException ex) {
        log.warn("Invalid credentials exception: {}", ex.getMessage());
        String message = getMessage("auth.invalid.credentials");
        ApiResponse<?> response = new ApiResponse<>("ERROR", message, null);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UserAccountDisabledException.class)
    public ResponseEntity<ApiResponse<?>> handleUserAccountDisabled(UserAccountDisabledException ex) {
        log.warn("User account disabled exception: {}", ex.getMessage());
        String message = getMessage("auth.account.disabled");
        ApiResponse<?> response = new ApiResponse<>("ERROR", message, null);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidToken(InvalidTokenException ex) {
        log.warn("Invalid token exception: {}", ex.getMessage());
        String message = getMessage("auth.token.invalid");
        ApiResponse<?> response = new ApiResponse<>("ERROR", message, null);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleUserNotFound(UserNotFoundException ex) {
        log.warn("User not found exception: {}", ex.getMessage());
        String message = getMessage("auth.user.not.found");
        ApiResponse<?> response = new ApiResponse<>("ERROR", message, null);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<?>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Bad credentials exception: {}", ex.getMessage());
        String message = getMessage("auth.invalid.credentials");
        ApiResponse<?> response = new ApiResponse<>("ERROR", message, null);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<?>> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Authentication exception: {}", ex.getMessage());
        String message = getMessage("auth.authentication.failed");
        ApiResponse<?> response = new ApiResponse<>("ERROR", message, null);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    // Business Logic Exceptions
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleEntityNotFound(EntityNotFoundException ex) {
        String correlationId = MDC.get("correlationId");
        log.warn("[{}] Entity not found: {}", correlationId, ex.getMessage());
        String message = ex.getMessage();
        ApiResponse<?> response = new ApiResponse<>("ERROR", message, null);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateEntityException.class)
    public ResponseEntity<ApiResponse<?>> handleDuplicateEntity(DuplicateEntityException ex) {
        String correlationId = MDC.get("correlationId");
        log.warn("[{}] Duplicate entity: {}", correlationId, ex.getMessage());
        String message = ex.getMessage();
        ApiResponse<?> response = new ApiResponse<>("ERROR", message, null);
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(TenantNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleTenantNotFound(TenantNotFoundException ex) {
        log.warn("Tenant not found exception: {}", ex.getMessage());
        String message = getMessage("tenant.not.found");
        ApiResponse<?> response = new ApiResponse<>("ERROR", message, null);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SiteNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleSiteNotFound(SiteNotFoundException ex) {
        log.warn("Site not found exception: {}", ex.getMessage());
        String message = getMessage("site.not.found");
        ApiResponse<?> response = new ApiResponse<>("ERROR", message, null);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TenantCannotBeActivatedException.class)
    public ResponseEntity<ApiResponse<?>> handleTenantCannotBeActivated(TenantCannotBeActivatedException ex) {
        log.warn("Tenant cannot be activated exception: {}", ex.getMessage());
        String message = getMessage("tenant.cannot.be.activated");
        ApiResponse<?> response = new ApiResponse<>("ERROR", message, null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ContentCannotBePublishedException.class)
    public ResponseEntity<ApiResponse<?>> handleContentCannotBePublished(ContentCannotBePublishedException ex) {
        log.warn("Content cannot be published exception: {}", ex.getMessage());
        String message = getMessage("content.cannot.be.published");
        ApiResponse<?> response = new ApiResponse<>("ERROR", message, null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Security Exceptions
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied exception: {}", ex.getMessage());
        String message = getMessage("security.forbidden");
        ApiResponse<?> response = new ApiResponse<>("ERROR", message, null);
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    // File Upload Exceptions
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<?>> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        log.warn("Max upload size exceeded: {}", ex.getMessage());
        String message = getMessage("file.upload.size.exceeded");
        ApiResponse<?> response = new ApiResponse<>("ERROR", message, null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Validation Exceptions
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("Validation exception: {}", ex.getMessage());
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = getMessage(error.getDefaultMessage());
            validationErrors.put(fieldName, errorMessage);
        });
        ApiResponse<?> response = new ApiResponse<>("ERROR", "Validation failed", validationErrors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Database Constraint Exceptions
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("Data integrity violation: ", ex); // Log full stack trace

        String userMessage;
        String exceptionMessage = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";

        // Detect specific constraint violations and provide user-friendly messages
        if (exceptionMessage.contains("foreign key constraint")) {
            userMessage = getMessage("error.data.reference.invalid");
        } else if (exceptionMessage.contains("unique constraint") ||
                exceptionMessage.contains("duplicate")) {
            userMessage = getMessage("error.data.duplicate");
        } else {
            userMessage = getMessage("error.data.integrity");
        }

        return new ResponseEntity<>(
                ApiResponse.error(userMessage),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleConstraintViolation(ConstraintViolationException ex) {
        log.error("Constraint violation: ", ex); // Log full stack trace
        String message = getMessage("error.validation.constraint");
        return new ResponseEntity<>(
                ApiResponse.error(message),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<?>> handleRuntimeException(RuntimeException ex) {
        String correlationId = MDC.get("correlationId");
        log.error("[{}] Runtime exception: ", correlationId, ex);
        String message = getMessage("error.runtime");
        ApiResponse<?> response = new ApiResponse<>("ERROR", message, null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(com.backend.domain.exception.MaxFieldLimitException.class)
    public ResponseEntity<ApiResponse<?>> handleMaxFieldLimit(com.backend.domain.exception.MaxFieldLimitException ex) {
        String correlationId = MDC.get("correlationId");
        log.warn("[{}] Max field limit exceeded: {}", correlationId, ex.getMessage());
        String message = getMessage("error.max.field.limit");
        ApiResponse<?> response = new ApiResponse<>("ERROR", message, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGenericException(Exception ex) {
        String correlationId = MDC.get("correlationId");
        log.error("[{}] Unexpected exception: ", correlationId, ex);
        String message = getMessage("error.general");
        ApiResponse<?> response = new ApiResponse<>("ERROR", message, null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String getMessage(String key) {
        try {
            return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            log.warn("Message not found for key: {}, using key as fallback", key);
            return key;
        }
    }
}