package com.backend.presentation.dto.response;

public record PasswordResetResponse(
    String message,
    String instructions
) {}