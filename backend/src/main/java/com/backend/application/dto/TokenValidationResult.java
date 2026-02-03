package com.backend.application.dto;

public record TokenValidationResult(
        boolean valid,
        String email,
        String tokenType,
        Long expiryMinutes) {

    public static TokenValidationResult invalid() {
        return new TokenValidationResult(false, null, null, 0L);
    }

    public static TokenValidationResult valid(String email, String tokenType, Long expiryMinutes) {
        return new TokenValidationResult(true, email, tokenType, expiryMinutes);
    }
}
