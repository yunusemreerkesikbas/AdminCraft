package com.backend.infrastructure.tenant;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.backend.domain.enums.Currency;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.TenantStatus;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.TenantRepository;

@Component
public class TenantContext implements TenantContextPort {

  private static final ThreadLocal<String> currentTenantId = new ThreadLocal<>();
  private static final ThreadLocal<String> currentTenantDbName = new ThreadLocal<>();
  private static final ThreadLocal<String> currentSubdomain = new ThreadLocal<>();
  private static final ThreadLocal<Currency> currentCurrency = new ThreadLocal<>();
  private static final ThreadLocal<Language> currentDefaultLanguage = new ThreadLocal<>();

  private final TenantRepository tenantRepository;

  public TenantContext(@Lazy TenantRepository tenantRepository) {
    this.tenantRepository = tenantRepository;
  }

  @Override
  public void setTenantId(String tenantId) {
    currentTenantId.set(tenantId);
  }

  @Override
  public String getTenantId() {
    return currentTenantId.get();
  }

  @Override
  public void setTenantDbName(String dbName) {
    currentTenantDbName.set(dbName);
  }

  @Override
  public String getTenantDbName() {
    return currentTenantDbName.get();
  }

  @Override
  public void setSubdomain(String subdomain) {
    currentSubdomain.set(subdomain);
  }

  @Override
  public String getSubdomain() {
    return currentSubdomain.get();
  }

  @Override
  public void setCurrency(Currency currency) {
    currentCurrency.set(currency);
  }

  @Override
  public Currency getCurrency() {
    Currency currency = currentCurrency.get();
    return currency != null ? currency : Currency.getDefault();
  }

  @Override
  public Language getDefaultLanguage() {
    Language lang = currentDefaultLanguage.get();
    return lang != null ? lang : Language.getDefault();
  }

  @Override
  public void setDefaultLanguage(Language language) {
    currentDefaultLanguage.set(language);
  }

  @Override
  public void clear() {
    currentTenantId.remove();
    currentTenantDbName.remove();
    currentSubdomain.remove();
    currentCurrency.remove();
    currentDefaultLanguage.remove();
  }

  @Override
  public boolean isSet() {
    return currentTenantId.get() != null && currentTenantDbName.get() != null;
  }

  @Override
  public boolean isActive() {
    String tenantIdStr = getTenantId();
    if (tenantIdStr == null) {
      return false;
    }
    try {
      Long tenantId = Long.parseLong(tenantIdStr);
      return tenantRepository.findById(tenantId)
          .map(tenant -> tenant.getStatus() == TenantStatus.ACTIVE)
          .orElse(false);
    } catch (NumberFormatException e) {
      return false;
    }
  }

  public static void validateActive() {
    if (currentTenantId.get() == null || currentTenantDbName.get() == null) {
      throw new IllegalStateException(
        "Tenant context is not set. This operation requires an active tenant context.");
    }
  }
}
