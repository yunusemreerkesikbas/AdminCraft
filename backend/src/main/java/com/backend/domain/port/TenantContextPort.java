package com.backend.domain.port;

import java.util.Set;

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

  Set<Language> getSupportedLanguages();

  void setSupportedLanguages(Set<Language> languages);

  void clear();

  boolean isSet();

  boolean isActive();
}
