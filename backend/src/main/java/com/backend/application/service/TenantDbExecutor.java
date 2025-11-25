package com.backend.application.service;

import com.backend.infrastructure.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.function.Supplier;

@Service
public class TenantDbExecutor {

  private final TenantContext tenantContext;
  private final PlatformTransactionManager tenantTransactionManager;

  public TenantDbExecutor(TenantContext tenantContext,
      @Qualifier("tenantTransactionManager") PlatformTransactionManager tenantTransactionManager) {
    this.tenantContext = tenantContext;
    this.tenantTransactionManager = tenantTransactionManager;
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

  public <T> T withTenantTransaction(Long tenantId, String tenantDbName, Supplier<T> action) {
    // Step 1: Set tenant context FIRST
    tenantContext.setTenantId(String.valueOf(tenantId));
    tenantContext.setTenantDbName(tenantDbName);

    // Step 2: THEN start transaction (now it will use the correct tenant DB)
    TransactionStatus status = tenantTransactionManager.getTransaction(new DefaultTransactionDefinition());

    try {
      T result = action.get();
      tenantTransactionManager.commit(status);
      return result;
    } catch (Exception e) {
      tenantTransactionManager.rollback(status);
      throw e;
    } finally {
      tenantContext.clear();
    }
  }
}
