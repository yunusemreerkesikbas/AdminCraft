package com.backend.presentation.dto.response;

import com.backend.domain.entity.Tenant;

public record TenantAdminInfoResponse(
    String adminEmail,
    String adminName,
    String phone) {

  public static TenantAdminInfoResponse from(Tenant tenant) {
    return new TenantAdminInfoResponse(
        null,
        null,
        null);
  }
}
