package com.backend.application.service.impl.config;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.MDC;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.backend.application.dto.config.ConfigPrincipal;
import com.backend.application.dto.config.ConfigPropertyResult;
import com.backend.application.service.config.ConfigGlobalPropertiesAdminService;
import com.backend.application.service.config.GlobalRuntimeConfigService;
import com.backend.domain.entity.ConfigChangeAudit;
import com.backend.domain.entity.PlatformConfigProperty;
import com.backend.domain.port.EncryptionServicePort;
import com.backend.domain.port.PlatformSettingsPort;
import com.backend.domain.repository.ConfigChangeAuditRepository;
import com.backend.domain.repository.PlatformConfigPropertyRepository;
import com.backend.shared.constants.ValidationConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigGlobalPropertiesAdminServiceImpl implements ConfigGlobalPropertiesAdminService, GlobalRuntimeConfigService {

    private static final Long GLOBAL_TARGET_TENANT_ID = 0L;

    private static final String AUDIT_SCOPE = "GLOBAL_RUNTIME_CONFIG";
    private static final String AUDIT_ACTION_UPSERT = "UPSERT";
    private static final String AUDIT_ACTION_DELETE = "DELETE";

    private static final String KEY_EMAIL_PROVIDER = "app.email.provider";
    private static final String KEY_EMAIL_FROM_ADDRESS = "app.email.from-address";
    private static final String KEY_EMAIL_FROM_NAME = "app.email.from-name";
    private static final String KEY_FRONTEND_BASE_URL = "app.frontend.base-url";
    private static final String KEY_RECAPTCHA_ENABLED = "platform.security.recaptcha.enabled";
    private static final String KEY_RECAPTCHA_SITE_KEY = "platform.security.recaptcha.site_key";
    private static final String KEY_RECAPTCHA_SECRET_KEY = "platform.security.recaptcha.secret_key";

    private static final Set<String> ALLOWED_KEYS = Set.of(
            KEY_EMAIL_PROVIDER,
            KEY_EMAIL_FROM_ADDRESS,
            KEY_EMAIL_FROM_NAME,
            KEY_FRONTEND_BASE_URL,
            KEY_RECAPTCHA_ENABLED,
            KEY_RECAPTCHA_SITE_KEY,
            KEY_RECAPTCHA_SECRET_KEY);

    private static final List<String> ORDERED_KEYS = List.of(
            KEY_EMAIL_PROVIDER,
            KEY_EMAIL_FROM_ADDRESS,
            KEY_EMAIL_FROM_NAME,
            KEY_FRONTEND_BASE_URL,
            KEY_RECAPTCHA_ENABLED,
            KEY_RECAPTCHA_SITE_KEY,
            KEY_RECAPTCHA_SECRET_KEY);

    private static final Set<String> ALLOWED_PROVIDER_VALUES = Set.of("console", "smtp");

    private static final String DEFAULT_EMAIL_PROVIDER = "console";
    private static final String DEFAULT_EMAIL_FROM_ADDRESS = "noreply@craftive.io";
    private static final String DEFAULT_EMAIL_FROM_NAME = "Craftive";
    private static final String DEFAULT_FRONTEND_BASE_URL = "http://%s.localhost:4200";
    private static final String DEFAULT_RECAPTCHA_ENABLED = "false";

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern RECAPTCHA_KEY_PATTERN = Pattern.compile(ValidationConstants.RECAPTCHA_KEY_PATTERN);

    private final PlatformConfigPropertyRepository propertyRepository;
    private final ConfigChangeAuditRepository auditRepository;
    private final ObjectMapper objectMapper;
    private final Environment environment;
    private final EncryptionServicePort encryptionService;
    private final PlatformSettingsPort platformSettingsPort;

    @Override
    @Transactional(readOnly = true)
    public List<ConfigPropertyResult> listProperties(ConfigPrincipal principal) {
        assertSuperAdmin(principal);
        Map<String, PlatformConfigProperty> overridesByKey = propertyRepository.findAll().stream()
                .filter(prop -> ALLOWED_KEYS.contains(prop.getConfigKey()))
                .collect(java.util.stream.Collectors.toMap(
                        PlatformConfigProperty::getConfigKey,
                        prop -> prop,
                        (left, right) -> left,
                        LinkedHashMap::new));

        return ORDERED_KEYS.stream()
                .map(key -> toResult(key, overridesByKey.get(key)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ConfigPropertyResult getProperty(ConfigPrincipal principal, String key) {
        assertSuperAdmin(principal);
        String normalizedKey = normalizeAllowedKey(key);
        PlatformConfigProperty override = propertyRepository.findByConfigKey(normalizedKey).orElse(null);
        return toResult(normalizedKey, override);
    }

    @Override
    @Transactional
    public ConfigPropertyResult upsertProperty(ConfigPrincipal principal, String key, String value, boolean secret, String reason) {
        assertSuperAdmin(principal);

        String normalizedKey = normalizeAllowedKey(key);
        validateSecretFlag(normalizedKey, secret);

        String normalizedValue = validateAndNormalizeValue(normalizedKey, value);
        String valueToStore = shouldEncryptValue(normalizedKey) ? encryptionService.encrypt(normalizedValue) : normalizedValue;

        java.util.Optional<PlatformConfigProperty> existing = propertyRepository.findByConfigKey(normalizedKey);
        String beforeValue = existing.map(PlatformConfigProperty::getConfigValue).orElse(null);

        PlatformConfigProperty saved = existing
                .map(prop -> {
                    prop.setConfigValue(valueToStore);
                    prop.setSecret(secret);
                    prop.setUpdatedBy(principal.userId());
                    return propertyRepository.save(prop);
                })
                .orElseGet(() -> {
                    PlatformConfigProperty created = new PlatformConfigProperty();
                    created.setConfigKey(normalizedKey);
                    created.setConfigValue(valueToStore);
                    created.setSecret(secret);
                    created.setUpdatedBy(principal.userId());
                    return propertyRepository.save(created);
                });

        writeAudit(principal, AUDIT_ACTION_UPSERT, reason,
                snapshotJson(normalizedKey, beforeValue),
                snapshotJson(normalizedKey, normalizedValue));

        return toResult(normalizedKey, saved);
    }

    @Override
    @Transactional
    public void deleteProperty(ConfigPrincipal principal, String key, String reason) {
        assertSuperAdmin(principal);

        String normalizedKey = normalizeAllowedKey(key);
        String beforeValue = propertyRepository.findByConfigKey(normalizedKey)
                .map(PlatformConfigProperty::getConfigValue)
                .orElse(null);

        propertyRepository.deleteByConfigKey(normalizedKey);

        writeAudit(principal, AUDIT_ACTION_DELETE, reason,
                snapshotJson(normalizedKey, beforeValue),
                snapshotJson(normalizedKey, null));
    }

    @Override
    @Transactional(readOnly = true)
    public String getEmailProvider() {
        String configured = resolveConfiguredValue(KEY_EMAIL_PROVIDER);
        if (!StringUtils.hasText(configured)) {
            return resolveFallbackValue(KEY_EMAIL_PROVIDER);
        }
        String normalized = configured.trim().toLowerCase();
        return ALLOWED_PROVIDER_VALUES.contains(normalized)
                ? normalized
                : resolveFallbackValue(KEY_EMAIL_PROVIDER);
    }

    @Override
    @Transactional(readOnly = true)
    public String getEmailFromAddress() {
        String configured = resolveConfiguredValue(KEY_EMAIL_FROM_ADDRESS);
        if (!StringUtils.hasText(configured)) {
            return resolveFallbackValue(KEY_EMAIL_FROM_ADDRESS);
        }
        return configured.trim();
    }

    @Override
    @Transactional(readOnly = true)
    public String getEmailFromName() {
        String configured = resolveConfiguredValue(KEY_EMAIL_FROM_NAME);
        if (!StringUtils.hasText(configured)) {
            return resolveFallbackValue(KEY_EMAIL_FROM_NAME);
        }
        return configured.trim();
    }

    @Override
    @Transactional(readOnly = true)
    public String getFrontendBaseUrl() {
        String configured = resolveConfiguredValue(KEY_FRONTEND_BASE_URL);
        if (!StringUtils.hasText(configured)) {
            return resolveFallbackValue(KEY_FRONTEND_BASE_URL);
        }
        return configured.trim();
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean getRecaptchaEnabled() {
        String configured = resolveConfiguredValue(KEY_RECAPTCHA_ENABLED);
        if (!StringUtils.hasText(configured)) {
            configured = resolveFallbackValue(KEY_RECAPTCHA_ENABLED);
        }
        if (!StringUtils.hasText(configured)) {
            return false;
        }
        return Boolean.parseBoolean(configured.trim().toLowerCase());
    }

    @Override
    @Transactional(readOnly = true)
    public String getRecaptchaSiteKey() {
        String configured = resolveConfiguredValue(KEY_RECAPTCHA_SITE_KEY);
        if (!StringUtils.hasText(configured)) {
            return resolveFallbackValue(KEY_RECAPTCHA_SITE_KEY);
        }
        return configured.trim();
    }

    @Override
    @Transactional(readOnly = true)
    public String getRecaptchaSecretKeyEncrypted() {
        String configured = resolveConfiguredValue(KEY_RECAPTCHA_SECRET_KEY);
        if (!StringUtils.hasText(configured)) {
            return resolveFallbackValue(KEY_RECAPTCHA_SECRET_KEY);
        }
        return configured;
    }

    private ConfigPropertyResult toResult(String key, PlatformConfigProperty override) {
        String value = override != null && StringUtils.hasText(override.getConfigValue())
                ? override.getConfigValue()
                : resolveFallbackValue(key);

        LocalDateTime updatedAt = override != null ? override.getUpdatedAt() : null;
        Long updatedBy = override != null ? override.getUpdatedBy() : null;
        boolean secret = isSecretKey(key) || (override != null && Boolean.TRUE.equals(override.getSecret()));

        return new ConfigPropertyResult(key, value, secret, updatedAt, updatedBy);
    }

    private String normalizeAllowedKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Config key is required");
        }
        String normalized = key.trim().toLowerCase();
        if (!ALLOWED_KEYS.contains(normalized)) {
            throw new IllegalArgumentException("Config key is not allowed: " + key);
        }
        return normalized;
    }

    private String validateAndNormalizeValue(String key, String value) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("Config value is required");
        }

        String normalized = value.trim();
        switch (key) {
            case KEY_EMAIL_PROVIDER -> {
                String provider = normalized.toLowerCase();
                if (!ALLOWED_PROVIDER_VALUES.contains(provider)) {
                    throw new IllegalArgumentException("Invalid email provider. Allowed values: console, smtp");
                }
                return provider;
            }
            case KEY_EMAIL_FROM_ADDRESS -> {
                if (normalized.length() > 255 || !EMAIL_PATTERN.matcher(normalized).matches()) {
                    throw new IllegalArgumentException("Invalid email from-address");
                }
                return normalized;
            }
            case KEY_EMAIL_FROM_NAME -> {
                if (normalized.length() > 100) {
                    throw new IllegalArgumentException("Invalid email from-name");
                }
                return normalized;
            }
            case KEY_FRONTEND_BASE_URL -> {
                if (normalized.length() > 255) {
                    throw new IllegalArgumentException("Invalid frontend base-url");
                }
                validateFrontendBaseUrl(normalized);
                return normalized;
            }
            case KEY_RECAPTCHA_ENABLED -> {
                String bool = normalized.toLowerCase();
                if (!"true".equals(bool) && !"false".equals(bool)) {
                    throw new IllegalArgumentException("Invalid reCAPTCHA enabled value. Allowed values: true, false");
                }
                return bool;
            }
            case KEY_RECAPTCHA_SITE_KEY, KEY_RECAPTCHA_SECRET_KEY -> {
                if (!RECAPTCHA_KEY_PATTERN.matcher(normalized).matches()) {
                    throw new IllegalArgumentException("Invalid reCAPTCHA key format");
                }
                return normalized;
            }
            default -> throw new IllegalArgumentException("Config key is not allowed: " + key);
        }
    }

    private void validateFrontendBaseUrl(String value) {
        String candidate = value.contains("%s") ? value.replace("%s", "demo") : value;
        try {
            URI uri = URI.create(candidate);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (!StringUtils.hasText(scheme) || !StringUtils.hasText(host)) {
                throw new IllegalArgumentException("Frontend base-url must include scheme and host");
            }
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                throw new IllegalArgumentException("Frontend base-url scheme must be http or https");
            }
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid frontend base-url format", ex);
        }
    }

    private String resolveConfiguredValue(String key) {
        return propertyRepository.findByConfigKey(key)
                .map(PlatformConfigProperty::getConfigValue)
                .orElse(null);
    }

    private String resolveFallbackValue(String key) {
        return switch (key) {
            case KEY_EMAIL_PROVIDER -> {
                String val = environment.getProperty(KEY_EMAIL_PROVIDER, DEFAULT_EMAIL_PROVIDER).trim().toLowerCase();
                yield ALLOWED_PROVIDER_VALUES.contains(val) ? val : DEFAULT_EMAIL_PROVIDER;
            }
            case KEY_EMAIL_FROM_ADDRESS -> {
                String val = environment.getProperty(KEY_EMAIL_FROM_ADDRESS, DEFAULT_EMAIL_FROM_ADDRESS);
                yield (val != null && val.length() <= 255 && EMAIL_PATTERN.matcher(val.trim()).matches())
                        ? val.trim() : DEFAULT_EMAIL_FROM_ADDRESS;
            }
            case KEY_EMAIL_FROM_NAME -> {
                String val = environment.getProperty(KEY_EMAIL_FROM_NAME, DEFAULT_EMAIL_FROM_NAME);
                yield (val != null && val.trim().length() <= 100) ? val.trim() : DEFAULT_EMAIL_FROM_NAME;
            }
            case KEY_FRONTEND_BASE_URL -> {
                String val = environment.getProperty(KEY_FRONTEND_BASE_URL, DEFAULT_FRONTEND_BASE_URL);
                try {
                    if (val != null) validateFrontendBaseUrl(val.trim());
                    yield val != null ? val.trim() : DEFAULT_FRONTEND_BASE_URL;
                } catch (IllegalArgumentException e) {
                    yield DEFAULT_FRONTEND_BASE_URL;
                }
            }
            case KEY_RECAPTCHA_ENABLED -> resolvePlatformRecaptchaEnabledFallback();
            case KEY_RECAPTCHA_SITE_KEY -> resolvePlatformRecaptchaSiteKeyFallback();
            case KEY_RECAPTCHA_SECRET_KEY -> resolvePlatformRecaptchaSecretFallback();
            default -> environment.getProperty(key);
        };
    }

    private String resolvePlatformRecaptchaEnabledFallback() {
        var settings = platformSettingsPort.getSingleton();
        if (settings.getRecaptchaEnabled() != null) {
            return String.valueOf(settings.getRecaptchaEnabled());
        }
        return DEFAULT_RECAPTCHA_ENABLED;
    }

    private String resolvePlatformRecaptchaSiteKeyFallback() {
        return platformSettingsPort.getSingleton().getRecaptchaSiteKey();
    }

    private String resolvePlatformRecaptchaSecretFallback() {
        return platformSettingsPort.getSingleton().getRecaptchaSecretKeyEncrypted();
    }

    private boolean isSecretKey(String key) {
        return KEY_RECAPTCHA_SECRET_KEY.equals(key);
    }

    private void validateSecretFlag(String key, boolean secret) {
        if (isSecretKey(key) && !secret) {
            throw new IllegalArgumentException("Secret flag must be true for reCAPTCHA secret key");
        }
        if (!isSecretKey(key) && secret) {
            throw new IllegalArgumentException("Secret flag is not supported for this key");
        }
    }

    private boolean shouldEncryptValue(String key) {
        return KEY_RECAPTCHA_SECRET_KEY.equals(key);
    }

    private void assertSuperAdmin(ConfigPrincipal principal) {
        if (principal == null || !principal.isConfigSuperAdmin()) {
            throw new AccessDeniedException("Config super admin role is required");
        }
    }

    private void writeAudit(
            ConfigPrincipal principal,
            String action,
            String reason,
            String beforeJson,
            String afterJson) {
        ConfigChangeAudit audit = ConfigChangeAudit.builder()
                .actorUserId(principal.userId())
                .actorEmail(principal.email())
                .actorRole(principal.role())
                .targetTenantId(GLOBAL_TARGET_TENANT_ID)
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
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("key", key);
            map.put("value", value != null ? "[set]" : null);
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize audit snapshot", e);
        }
    }
}
