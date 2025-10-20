package com.backend.presentation.dto.response;

public record TenantAdminInfoResponse(
        String adminEmail,
        String adminName,
        String phone) {

    public static TenantAdminInfoResponse from(com.backend.domain.entity.Tenant tenant) {
        return new TenantAdminInfoResponse(
                tenant.getAdminEmail(),
                tenant.getAdminName(),
                tenant.getPhone());
    }
}
