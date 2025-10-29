package com.backend.application.service;

import com.backend.infrastructure.tenant.TenantContext;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
public class TenantDbExecutor {

  private final TenantContext tenantContext;

  public TenantDbExecutor(TenantContext tenantContext) {
    this.tenantContext = tenantContext;
  }

  public <T> T withTenant(Long tenantId, String tenantDbName, Supplier<T> action) {
    tenantContext.setTenantId(String.valueOf(tenantId));
    tenantContext.setTenantDbName(tenantDbName);
    try {
      return action.get();
    } finally {
      tenantContext.clear();
    }
  }
}
