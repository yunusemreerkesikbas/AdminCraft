package com.backend.application.dto.response;

/**
 * Response DTO for generated admin user credentials.
 * Sprint 20: Tenant Admin User Generator
 *
 * @param email              admin user email
 * @param temporaryPassword  randomly generated password (one-time view)
 * @param subdomain          tenant subdomain
 * @param loginUrl           full login URL for the tenant
 */
public record AdminUserResponse(
        String email,
        String temporaryPassword,
        String subdomain,
        String loginUrl
) {
}
