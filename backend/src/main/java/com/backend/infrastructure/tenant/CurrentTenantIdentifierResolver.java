package com.backend.infrastructure.tenant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CurrentTenantIdentifierResolver
    implements org.hibernate.context.spi.CurrentTenantIdentifierResolver<String> {

  private static final String DEFAULT_TENANT = "platform_management";

  private final TenantContext tenantContext;

  public CurrentTenantIdentifierResolver(TenantContext tenantContext) {
    this.tenantContext = tenantContext;
  }

  @Override
  public String resolveCurrentTenantIdentifier() {
    String tenantDb = tenantContext.getTenantDbName();
    if (tenantDb == null) {
      log.debug("No tenant context; using default: {}", DEFAULT_TENANT);
      return DEFAULT_TENANT;
    }
    log.debug("Resolved tenant DB: {}", tenantDb);
    return tenantDb;
  }

  @Override
  public boolean validateExistingCurrentSessions() {
    return true;
  }
}









