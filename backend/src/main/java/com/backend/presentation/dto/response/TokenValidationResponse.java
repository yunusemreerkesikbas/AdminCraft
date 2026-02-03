package com.backend.presentation.dto.response;

public record TokenValidationResponse(
        boolean valid,
        String email,
        String tokenType,
        Long expiryMinutes
) {}
