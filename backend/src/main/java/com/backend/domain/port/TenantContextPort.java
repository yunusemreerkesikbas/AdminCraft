package com.backend.domain.port;

public interface TenantContextPort {

  String getTenantId();

  String getTenantDbName();

  String getSubdomain();
}
