package com.backend.application.service.config;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.backend.domain.entity.ConfigProperty;

public interface ConfigPropertyService {

    Optional<ConfigProperty> find(Long tenantId, String tenantDbName, String key);

    Optional<String> findRaw(Long tenantId, String tenantDbName, String key);

    boolean getBoolean(Long tenantId, String tenantDbName, String key, boolean defaultValue);

    BigDecimal getDecimal(Long tenantId, String tenantDbName, String key, BigDecimal defaultValue);

    ConfigProperty upsert(Long tenantId, String tenantDbName, String key, String value, boolean secret, Long actorUserId);

    List<ConfigProperty> findAll(Long tenantId, String tenantDbName);

    void delete(Long tenantId, String tenantDbName, String key);
}

