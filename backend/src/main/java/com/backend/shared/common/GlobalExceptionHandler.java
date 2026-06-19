package com.backend.shared.common;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.NoSuchMessageException;

import org.slf4j.MDC;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;

import com.backend.application.commerce.CommerceCartRateLimitExceededException;
import com.backend.application.commerce.CommerceCustomerRateLimitExceededException;
import com.backend.application.service.PublicContactRateLimitExceededException;
import com.backend.domain.exception.BusinessRuleViolationException;
import com.backend.domain.exception.ContentCannotBePublishedException;
import com.backend.domain.exception.DuplicateEntityException;
import com.backend.domain.exception.EntityNotFoundException;
import com.backend.domain.exception.InvalidCredentialsException;
import com.backend.domain.exception.InvalidTokenException;
import com.backend.domain.exception.MediaExceptions.ContainerNotFoundException;
import com.backend.domain.exception.MediaExceptions.InvalidFileException;
import com.backend.domain.exception.MediaExceptions.MediaNotFoundException;
import com.backend.domain.exception.MediaExceptions.MediaProcessingException;
import com.backend.domain.exception.MediaExceptions.UnsupportedFormatException;
import com.backend.domain.exception.SiteNotFoundException;
import com.backend.domain.exception.TenantCannotBeActivatedException;
import com.backend.domain.exception.TenantNotFoundException;
import com.backend.domain.exception.AccountLockedException;
import com.backend.domain.exception.OtpRateLimitExceededException;
import com.backend.domain.exception.RecaptchaVerificationException;
import com.backend.domain.exception.UserAccountDisabledException;
import com.backend.domain.exception.UserNotFoundException;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

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
        String message = resolveExceptionMessage(ex.getMessage(), "error.invalid.data");
        return new ResponseEntity<>(ApiResponse.error(400, message), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalStateException(IllegalStateException ex) {
        log.warn("Illegal state exception: {}", ex.getMessage());
        String message = resolveExceptionMessage(ex.getMessage(), "error.general");
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

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccountLocked(AccountLockedException ex) {
        log.warn("Account locked exception: {}", ex.getMessage());
        String message = messageSource.getMessage(
                "auth.account.locked",
                new Object[] { ex.getRemainingMinutes() },
                LocaleContextHolder.getLocale());
        Map<String, Object> data = new HashMap<>();
        data.put("remainingMinutes", ex.getRemainingMinutes());
        data.put("errorCode", "ACCOUNT_LOCKED");
        ApiResponse<?> response = new ApiResponse<>("ERROR", message, data);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(OtpRateLimitExceededException.class)
    public ResponseEntity<ApiResponse<?>> handleOtpRateLimitExceeded(OtpRateLimitExceededException ex) {
        log.warn("OTP rate limit exceeded: {}", ex.getMessage());
        String message = getMessage("auth.otp.rate.limit.exceeded");
        Map<String, Object> data = new HashMap<>();
        data.put("retryAfterSeconds", ex.getRetryAfterSeconds());
        data.put("errorCode", "OTP_RATE_LIMIT_EXCEEDED");
        ApiResponse<?> response = new ApiResponse<>("ERROR", message, data);
        return new ResponseEntity<>(response, HttpStatus.TOO_MANY_REQUESTS);
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
		String message = resolveExceptionMessage(ex.getMessage(), "auth.invalid.credentials");
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

    @ExceptionHandler(RecaptchaVerificationException.class)
    public ResponseEntity<ApiResponse<?>> handleRecaptchaVerification(RecaptchaVerificationException ex) {
        log.warn("reCAPTCHA verification failed: {}", ex.getMessage());
        String message = getMessage("recaptcha.verification.failed");
        ApiResponse<?> response = new ApiResponse<>("ERROR", message, null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Business Logic Exceptions
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleEntityNotFound(EntityNotFoundException ex) {
    String correlationId = MDC.get("correlationId");
        log.warn("[{}] Entity not found: {}", correlationId, ex.getMessage());
        String message = resolveExceptionMessage(ex.getMessage(), "error.not.found");
        ApiResponse<?> response = new ApiResponse<>("ERROR", message, null);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateEntityException.class)
    public ResponseEntity<ApiResponse<?>> handleDuplicateEntity(DuplicateEntityException ex) {
    String correlationId = MDC.get("correlationId");
        log.warn("[{}] Duplicate entity: {}", correlationId, ex.getMessage());
        String message = resolveExceptionMessage(ex.getMessage(), "error.data.duplicate");
        ApiResponse<?> response = new ApiResponse<>("ERROR", message, null);
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessRuleViolation(BusinessRuleViolationException ex) {
    String correlationId = MDC.get("correlationId");
        log.warn("[{}] Business rule violation: {}", correlationId, ex.getMessage());
        String message = resolveExceptionMessage(ex.getMessage(), "error.invalid.data");
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

    // Media Exceptions
    @ExceptionHandler(MediaNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleMediaNotFound(MediaNotFoundException ex) {
    String correlationId = MDC.get("correlationId");
        log.warn("[{}] Media not found: {}", correlationId, ex.getMessage());
        ApiResponse<?> response = new ApiResponse<>("ERROR",
                resolveExceptionMessage(ex.getMessage(), "error.not.found"),
                null);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidFile(InvalidFileException ex) {
    String correlationId = MDC.get("correlationId");
        log.warn("[{}] Invalid file: {}", correlationId, ex.getMessage());
        String message = getMessage("media.file.invalid");
        ApiResponse<?> response = new ApiResponse<>("ERROR", message, null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MediaProcessingException.class)
    public ResponseEntity<ApiResponse<?>> handleMediaProcessing(MediaProcessingException ex) {
    String correlationId = MDC.get("correlationId");
        log.error("[{}] Media processing error: ", correlationId, ex);
        String message = getMessage("media.processing.error");
        ApiResponse<?> response = new ApiResponse<>("ERROR", message, null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UnsupportedFormatException.class)
    public ResponseEntity<ApiResponse<?>> handleUnsupportedFormat(UnsupportedFormatException ex) {
    String correlationId = MDC.get("correlationId");
        log.warn("[{}] Unsupported format: {}", correlationId, ex.getMessage());
        String message = getMessage("media.format.unsupported");
        ApiResponse<?> response = new ApiResponse<>("ERROR", message, null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ContainerNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleContainerNotFound(ContainerNotFoundException ex) {
    String correlationId = MDC.get("correlationId");
        log.warn("[{}] Container not found: {}", correlationId, ex.getMessage());
        ApiResponse<?> response = new ApiResponse<>("ERROR",
                resolveExceptionMessage(ex.getMessage(), "error.not.found"),
                null);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // Validation Exceptions
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("Validation exception: {}", ex.getMessage());
        Map<String, String> validationErrors = new HashMap<>();
        String firstErrorMessage = null;
        for (var error : ex.getBindingResult().getAllErrors()) {
            String fieldName = error instanceof FieldError fe ? fe.getField() : error.getObjectName();
            String errorMessage = resolveBindingErrorMessage(error);
            validationErrors.put(fieldName, errorMessage);
        }
        if (!validationErrors.isEmpty()) {
            firstErrorMessage = validationErrors.values().iterator().next();
        }

        ApiResponse<?> response = new ApiResponse<>("ERROR",
                firstErrorMessage != null ? firstErrorMessage : "Validation failed", validationErrors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Database Constraint Exceptions
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
    String correlationId = MDC.get("correlationId");
        log.error("[{}] Data integrity violation: ", correlationId, ex); // Log full stack trace

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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Invalid request body: {}", ex.getMessage());
        String message = getMessage("error.invalid.data");
        return new ResponseEntity<>(ApiResponse.error(400, message), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ApiResponse<?>> handleRequestNotPermitted(RequestNotPermitted ex) {
    String correlationId = MDC.get("correlationId");
        log.warn("[{}] Rate limiter rejected request: {}", correlationId, ex.getMessage());
    String message = getMessage("rate.limit.exceeded");
    return new ResponseEntity<>(ApiResponse.error(message), HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(PublicContactRateLimitExceededException.class)
    public ResponseEntity<ApiResponse<?>> handlePublicContactRateLimit(PublicContactRateLimitExceededException ex) {
    String correlationId = MDC.get("correlationId");
        log.warn("[{}] Public contact rate limit exceeded", correlationId);
    String message = getMessage("rate.limit.exceeded");
    return new ResponseEntity<>(ApiResponse.error(message), HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(CommerceCartRateLimitExceededException.class)
    public ResponseEntity<ApiResponse<?>> handleCommerceCartRateLimit(CommerceCartRateLimitExceededException ex) {
    String correlationId = MDC.get("correlationId");
    log.warn("[{}] Commerce cart rate limit exceeded", correlationId);
    String message = resolveExceptionMessage(ex.getMessage(), "commerce.cart.rate.limit.exceeded");
    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
			.header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()))
			.body(ApiResponse.error(message));
    }

    @ExceptionHandler(CommerceCustomerRateLimitExceededException.class)
    public ResponseEntity<ApiResponse<?>> handleCommerceCustomerRateLimit(CommerceCustomerRateLimitExceededException ex) {
    String correlationId = MDC.get("correlationId");
    log.warn("[{}] Commerce customer rate limit exceeded", correlationId);
    String message = resolveExceptionMessage(ex.getMessage(), "commerce.customer.rate.limit.exceeded");
    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
			.header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()))
			.body(ApiResponse.error(message));
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
        String message = getMessage("product.field.max.limit");
        ApiResponse<?> response = new ApiResponse<>("ERROR", message, null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ApiResponse<?>> handleSQLException(SQLException ex) {
    String correlationId = MDC.get("correlationId");
        log.error("[{}] Database error: ", correlationId, ex);
        String message = getMessage("error.database");
        ApiResponse<?> response = new ApiResponse<>("ERROR", message, null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
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

    private String resolveExceptionMessage(String candidate, String fallbackKey) {
        if (candidate == null || candidate.isBlank()) {
            return getMessage(fallbackKey);
        }
        String translated = messageSource.getMessage(candidate, null, null, LocaleContextHolder.getLocale());
        if (translated != null && !translated.isBlank()) {
            return translated.length() > 500 ? translated.substring(0, 500) : translated;
        }
        return getMessage(fallbackKey);
    }

    private String resolveBindingErrorMessage(org.springframework.validation.ObjectError error) {
        String msg = error.getDefaultMessage();
        if (msg != null && msg.startsWith("{") && msg.endsWith("}")) {
            String key = msg.substring(1, msg.length() - 1);
            try {
                msg = messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
            } catch (NoSuchMessageException ignored) {
                msg = key;
            }
        }
        if (msg == null || msg.isBlank()) {
            msg = getMessage("validation.failed");
        }
        return msg.length() > 500 ? msg.substring(0, 500) : msg;
    }
}
