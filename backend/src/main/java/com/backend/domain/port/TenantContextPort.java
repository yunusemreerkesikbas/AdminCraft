package com.backend.domain.port;

import com.backend.domain.enums.Currency;

public interface TenantContextPort {

  String getTenantId();

  String getTenantDbName();

  String getSubdomain();

  Currency getCurrency();
}
