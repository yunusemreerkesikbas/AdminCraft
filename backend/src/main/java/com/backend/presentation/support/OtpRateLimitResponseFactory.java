package com.backend.presentation.support;

import java.util.Locale;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.backend.domain.exception.OtpRateLimitExceededException;
import com.backend.shared.common.ApiResponse;

public final class OtpRateLimitResponseFactory {

    private OtpRateLimitResponseFactory() {
    }

    public static ResponseEntity<ApiResponse<?>> tooManyRequests(
            MessageSource messageSource,
            String languageCode,
            OtpRateLimitExceededException ex) {
        String message = messageSource.getMessage(
                "auth.otp.rate.limit.exceeded",
                null,
                Locale.forLanguageTag(languageCode));
        Map<String, Object> data = Map.of(
                "retryAfterSeconds", ex.getRetryAfterSeconds(),
                "errorCode", "OTP_RATE_LIMIT_EXCEEDED");
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ApiResponse<>("ERROR", message, data));
    }
}
