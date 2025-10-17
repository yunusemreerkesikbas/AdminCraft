package com.backend.infrastructure.tenant;

import org.springframework.stereotype.Component;

@Component
public class TenantContext {

  private static final ThreadLocal<String> currentTenantId = new ThreadLocal<>();
  private static final ThreadLocal<String> currentTenantDbName = new ThreadLocal<>();

  public void setTenantId(String tenantId) {
    currentTenantId.set(tenantId);
  }

  public String getTenantId() {
    return currentTenantId.get();
  }

  public void setTenantDbName(String dbName) {
    currentTenantDbName.set(dbName);
  }

  public String getTenantDbName() {
    return currentTenantDbName.get();
  }

  public void clear() {
    currentTenantId.remove();
    currentTenantDbName.remove();
  }

  public boolean isSet() {
    return currentTenantId.get() != null && currentTenantDbName.get() != null;
  }
}

