package com.backend.presentation.dto.response;

public record PasswordResetResponse(
    String newPassword,
    String message
) {}