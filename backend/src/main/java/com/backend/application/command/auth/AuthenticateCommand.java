package com.backend.application.command.auth;

public record AuthenticateCommand(
    String email,
    String password,
    Long tenantId,
    String subdomain,
    String preferredLanguageCode) {
}
