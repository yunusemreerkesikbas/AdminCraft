package com.backend.application.dto.config;

public record ConfigPrincipal(
        Long userId,
        String email,
        String role,
        Long tenantId
) {

    public static final String ROLE_CONFIG_SUPER_ADMIN = "CONFIG_SUPER_ADMIN";
    public static final String ROLE_CONFIG_TENANT_ADMIN = "CONFIG_TENANT_ADMIN";

    public boolean isConfigSuperAdmin() {
        return ROLE_CONFIG_SUPER_ADMIN.equals(role);
    }

    public boolean isConfigTenantAdmin() {
        return ROLE_CONFIG_TENANT_ADMIN.equals(role);
    }
}
