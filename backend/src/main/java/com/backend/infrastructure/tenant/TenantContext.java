package com.backend.infrastructure.tenant;

import org.springframework.stereotype.Component;

import com.backend.domain.port.TenantContextPort;

@Component
public class TenantContext implements TenantContextPort {

  private static final ThreadLocal<String> currentTenantId = new ThreadLocal<>();
  private static final ThreadLocal<String> currentTenantDbName = new ThreadLocal<>();
  private static final ThreadLocal<String> currentSubdomain = new ThreadLocal<>();

  public void setTenantId(String tenantId) {
    currentTenantId.set(tenantId);
  }

  @Override
  public String getTenantId() {
    return currentTenantId.get();
  }

  public void setTenantDbName(String dbName) {
    currentTenantDbName.set(dbName);
  }

  @Override
  public String getTenantDbName() {
    return currentTenantDbName.get();
  }

  public void setSubdomain(String subdomain) {
    currentSubdomain.set(subdomain);
  }

  @Override
  public String getSubdomain() {
    return currentSubdomain.get();
  }

  public void clear() {
    currentTenantId.remove();
    currentTenantDbName.remove();
    currentSubdomain.remove();
  }

  public boolean isSet() {
    return currentTenantId.get() != null && currentTenantDbName.get() != null;
  }

  /**
   * Validates that tenant context is properly set.
   * Should be called at the entry point of all tenant-scoped service methods.
   * 
   * @throws IllegalStateException if tenant context is not set
   */
  public static void validateActive() {
    if (currentTenantId.get() == null || currentTenantDbName.get() == null) {
      throw new IllegalStateException(
        "Tenant context is not set. This operation requires an active tenant context.");
    }
  }
}
