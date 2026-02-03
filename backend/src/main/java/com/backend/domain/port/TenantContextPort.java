package com.backend.domain.port;

import com.backend.domain.enums.Currency;

public interface TenantContextPort {

  String getTenantId();

  void setTenantId(String tenantId);

  String getTenantDbName();

  void setTenantDbName(String dbName);

  String getSubdomain();

  void setSubdomain(String subdomain);

  Currency getCurrency();

  void setCurrency(Currency currency);

  void clear();

  boolean isSet();
}
