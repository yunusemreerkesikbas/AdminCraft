package com.backend.application.service.impl.config;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.service.TenantDbExecutor;
import com.backend.application.service.config.ConfigPropertyService;
import com.backend.domain.entity.ConfigProperty;
import com.backend.domain.port.EncryptionServicePort;
import com.backend.domain.repository.ConfigPropertyRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigPropertyServiceImpl implements ConfigPropertyService {

    private final TenantDbExecutor tenantDbExecutor;
    private final ConfigPropertyRepository configPropertyRepository;
    private final EncryptionServicePort encryptionService;

    @Override
    @Transactional(readOnly = true)
    public Optional<ConfigProperty> find(Long tenantId, String tenantDbName, String key) {
        return tenantDbExecutor.withTenant(tenantId, tenantDbName,
                () -> configPropertyRepository.findByConfigKey(key));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> findRaw(Long tenantId, String tenantDbName, String key) {
        return find(tenantId, tenantDbName, key).map(ConfigProperty::getConfigValue);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean getBoolean(Long tenantId, String tenantDbName, String key, boolean defaultValue) {
        Optional<String> raw = findRaw(tenantId, tenantDbName, key);
        if (raw.isEmpty() || raw.get() == null) {
            return defaultValue;
        }
        return parseBooleanOrDefault(raw.get(), defaultValue, key);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getDecimal(Long tenantId, String tenantDbName, String key, BigDecimal defaultValue) {
        Optional<String> raw = findRaw(tenantId, tenantDbName, key);
        if (raw.isEmpty() || raw.get() == null || raw.get().isBlank()) {
            return defaultValue;
        }
        return parseDecimalOrDefault(raw.get(), defaultValue, key);
    }

    @Override
    @Transactional
    public ConfigProperty upsert(Long tenantId, String tenantDbName, String key, String value, boolean secret,
            Long actorUserId) {
        return tenantDbExecutor.withTenant(tenantId, tenantDbName, () -> {
            ConfigProperty entity = configPropertyRepository.findByConfigKey(key).orElseGet(ConfigProperty::new);
            entity.setConfigKey(key);
            entity.setSecret(secret);
            entity.setUpdatedBy(actorUserId);

            if (secret) {
                entity.setConfigValue(value != null && !value.isBlank() ? encryptionService.encrypt(value) : null);
            } else {
                entity.setConfigValue(value);
            }

            return configPropertyRepository.save(entity);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConfigProperty> findAll(Long tenantId, String tenantDbName) {
        return tenantDbExecutor.withTenant(tenantId, tenantDbName,
                () -> configPropertyRepository.findAll());
    }

    @Override
    public void delete(Long tenantId, String tenantDbName, String key) {
        tenantDbExecutor.withTenant(tenantId, tenantDbName, () -> {
            configPropertyRepository.deleteByConfigKey(key);
            return null;
        });
    }

    private boolean parseBooleanOrDefault(String raw, boolean defaultValue, String key) {
        String v = raw.trim().toLowerCase(Locale.ROOT);
        if ("true".equals(v) || "1".equals(v) || "yes".equals(v) || "y".equals(v) || "on".equals(v)) {
            return true;
        }
        if ("false".equals(v) || "0".equals(v) || "no".equals(v) || "n".equals(v) || "off".equals(v)) {
            return false;
        }
        log.warn("Invalid boolean config value for key [{}]: [{}]", key, raw);
        return defaultValue;
    }

    private BigDecimal parseDecimalOrDefault(String raw, BigDecimal defaultValue, String key) {
        try {
            return new BigDecimal(raw.trim());
        } catch (Exception ex) {
            log.warn("Invalid decimal config value for key [{}]: [{}]", key, raw);
            return defaultValue;
        }
    }
}

