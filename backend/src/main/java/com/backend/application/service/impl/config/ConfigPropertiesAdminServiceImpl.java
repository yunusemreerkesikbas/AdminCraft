package com.backend.application.service.impl.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.MDC;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.backend.application.dto.config.ConfigPrincipal;
import com.backend.application.dto.config.ConfigPropertyResult;
import com.backend.application.service.TenantDbExecutor;
import com.backend.application.service.config.ConfigPropertiesAdminService;
import com.backend.application.service.config.ConfigPropertyService;
import com.backend.domain.entity.ConfigProperty;
import com.backend.domain.entity.Tenant;
import com.backend.domain.repository.ConfigChangeAuditRepository;
import com.backend.domain.repository.TenantRepository;
import com.backend.domain.entity.ConfigChangeAudit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigPropertiesAdminServiceImpl implements ConfigPropertiesAdminService {

    private static final String AUDIT_SCOPE = "CONFIG_PROPERTIES";
    private static final String AUDIT_ACTION_UPSERT = "UPSERT";
    private static final String AUDIT_ACTION_DELETE = "DELETE";
    private static final String KEY_RECAPTCHA_ENABLED = "security.recaptcha.enabled";
    private static final String KEY_RECAPTCHA_SITE_KEY = "security.recaptcha.site_key";
    private static final String KEY_RECAPTCHA_SECRET_KEY = "security.recaptcha.secret_key";
    private static final List<String> MANAGED_KEYS = List.of(
            KEY_RECAPTCHA_ENABLED,
            KEY_RECAPTCHA_SITE_KEY,
            KEY_RECAPTCHA_SECRET_KEY);
    private static final Set<String> MANAGED_KEYS_SET = Set.copyOf(MANAGED_KEYS);

    private final TenantRepository tenantRepository;
    private final TenantDbExecutor tenantDbExecutor;
    private final ConfigPropertyService configPropertyService;
    private final ConfigChangeAuditRepository auditRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ConfigPropertyResult> listProperties(ConfigPrincipal principal) {
        Tenant tenant = resolveTargetTenant(principal);
        return tenantDbExecutor.withTenant(tenant.getId(), tenant.getDatabaseName(), () -> {
            Map<String, ConfigProperty> overridesByKey = configPropertyService.findAll(tenant.getId(), tenant.getDatabaseName())
                    .stream()
                    .collect(java.util.stream.Collectors.toMap(
                            prop -> prop.getConfigKey().trim().toLowerCase(),
                            prop -> prop,
                            (left, right) -> left,
                            LinkedHashMap::new));

            List<ConfigPropertyResult> results = new ArrayList<>();
            MANAGED_KEYS.forEach(key -> results.add(toManagedResult(key, overridesByKey.remove(key))));
            overridesByKey.values().stream()
                    .sorted(java.util.Comparator.comparing(ConfigProperty::getConfigKey, String.CASE_INSENSITIVE_ORDER))
                    .map(this::toResult)
                    .forEach(results::add);
            return results;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public ConfigPropertyResult getProperty(ConfigPrincipal principal, String key) {
        Tenant tenant = resolveTargetTenant(principal);
        String normalizedKey = normalizeKey(key);

        return tenantDbExecutor.withTenant(tenant.getId(), tenant.getDatabaseName(), () -> {
            ConfigProperty override = configPropertyService.find(tenant.getId(), tenant.getDatabaseName(), normalizedKey)
                    .orElse(null);

            if (MANAGED_KEYS_SET.contains(normalizedKey)) {
                return toManagedResult(normalizedKey, override);
            }

            if (override == null) {
                throw new IllegalArgumentException("Property not found: " + key);
            }

            return toResult(override);
        });
    }

    @Override
    @Transactional
    public ConfigPropertyResult upsertProperty(ConfigPrincipal principal, String key, String value, boolean secret, String reason) {
        Tenant tenant = resolveTargetTenant(principal);

        String beforeValue = configPropertyService.findRaw(tenant.getId(), tenant.getDatabaseName(), key).orElse(null);

        ConfigProperty saved = configPropertyService.upsert(tenant.getId(), tenant.getDatabaseName(), key, value, secret, principal.userId());

        writeAudit(principal, tenant.getId(), AUDIT_ACTION_UPSERT, reason,
                snapshotJson(key, beforeValue),
                snapshotJson(key, value));

        return toResult(saved);
    }

    @Override
    @Transactional
    public void deleteProperty(ConfigPrincipal principal, String key, String reason) {
        Tenant tenant = resolveTargetTenant(principal);

        String beforeValue = configPropertyService.findRaw(tenant.getId(), tenant.getDatabaseName(), key).orElse(null);

        configPropertyService.delete(tenant.getId(), tenant.getDatabaseName(), key);

        writeAudit(principal, tenant.getId(), AUDIT_ACTION_DELETE, reason,
                snapshotJson(key, beforeValue),
                snapshotJson(key, null));
    }

    private Tenant resolveTargetTenant(ConfigPrincipal principal) {
        if (principal.tenantId() == null) {
            throw new AccessDeniedException("Tenant admin token missing tenant scope");
        }
        return tenantRepository.findById(principal.tenantId())
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
    }

    private void writeAudit(ConfigPrincipal principal, Long tenantId, String action, String reason,
            String beforeJson, String afterJson) {
        ConfigChangeAudit audit = ConfigChangeAudit.builder()
                .actorUserId(principal.userId())
                .actorEmail(principal.email())
                .actorRole(principal.role())
                .targetTenantId(tenantId)
                .scope(AUDIT_SCOPE)
                .action(action)
                .beforeJson(beforeJson)
                .afterJson(afterJson)
                .reason(reason)
                .correlationId(MDC.get("correlationId"))
                .build();
        auditRepository.save(audit);
    }

    private String snapshotJson(String key, String value) {
        try {
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("key", key);
            map.put("value", value != null ? "[set]" : null);
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize audit snapshot", e);
        }
    }

    private ConfigPropertyResult toResult(ConfigProperty prop) {
        return new ConfigPropertyResult(
                prop.getConfigKey(),
                prop.getSecret() ? null : prop.getConfigValue(),
                Boolean.TRUE.equals(prop.getSecret()),
                prop.getUpdatedAt(),
                prop.getUpdatedBy());
    }

    private ConfigPropertyResult toManagedResult(String key, ConfigProperty override) {
        if (override != null) {
            return toResult(override);
        }

        return switch (key) {
            case KEY_RECAPTCHA_ENABLED -> new ConfigPropertyResult(
                    key,
                    "false",
                    false,
                    null,
                    null);
            case KEY_RECAPTCHA_SITE_KEY -> new ConfigPropertyResult(
                    key,
                    null,
                    false,
                    null,
                    null);
            case KEY_RECAPTCHA_SECRET_KEY -> new ConfigPropertyResult(
                    key,
                    null,
                    true,
                    null,
                    null);
            default -> throw new IllegalArgumentException("Unsupported managed property key: " + key);
        };
    }

    private String normalizeKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Property key is required");
        }
        return key.trim().toLowerCase();
    }
}
