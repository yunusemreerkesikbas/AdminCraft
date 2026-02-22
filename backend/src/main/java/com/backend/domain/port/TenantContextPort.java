package com.backend.domain.port;

import com.backend.domain.enums.Currency;
import com.backend.domain.enums.Language;

public interface TenantContextPort {

  String getTenantId();

  void setTenantId(String tenantId);

  String getTenantDbName();

  void setTenantDbName(String dbName);

  String getSubdomain();

  void setSubdomain(String subdomain);

  Currency getCurrency();

  void setCurrency(Currency currency);

  Language getDefaultLanguage();

  void setDefaultLanguage(Language language);

  void clear();

  boolean isSet();

  boolean isActive();
}
