package com.backend.shared.constants;

import java.util.Set;

public final class ValidationConstants {

    private ValidationConstants() {
        // Prevent instantiation
    }

    public static final Set<String> RESERVED_SUBDOMAINS = Set.of(
            "admin",
            "www",
            "api",
            "app",
            "platform",
            "mail",
            "support",
            "cdn",
            "static",
            "assets",
            "auth",
            "login",
            "dashboard",
            "console",
            "portal");

    public static final String SUBDOMAIN_PATTERN = "^[a-z0-9](?:[a-z0-9-]{1,48}[a-z0-9])$";

    public static boolean isReservedSubdomain(String subdomain) {
        if (subdomain == null || subdomain.trim().isEmpty()) {
            return false;
        }
        return RESERVED_SUBDOMAINS.contains(subdomain.toLowerCase().trim());
    }
}
