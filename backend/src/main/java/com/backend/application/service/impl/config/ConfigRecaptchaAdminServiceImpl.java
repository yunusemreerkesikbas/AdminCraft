package com.backend.application.service.impl.config;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.MDC;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.config.ConfigAuditItemResult;
import com.backend.application.dto.config.ConfigPrincipal;
import com.backend.application.dto.config.ConfigRecaptchaResult;
import com.backend.application.dto.config.PatchConfigRecaptchaParams;
import com.backend.application.service.TenantDbExecutor;
import com.backend.application.service.config.ConfigPropertyService;
import com.backend.application.service.config.ConfigRecaptchaAdminService;
import com.backend.domain.entity.ConfigProperty;
import com.backend.domain.entity.Tenant;
import com.backend.domain.repository.ConfigChangeAuditRepository;
import com.backend.domain.repository.TenantRepository;
import com.backend.domain.entity.ConfigChangeAudit;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigRecaptchaAdminServiceImpl implements ConfigRecaptchaAdminService {

    private static final String AUDIT_SCOPE = "SECURITY_RECAPTCHA";
    private static final String AUDIT_ACTION_PATCH = "PATCH";

    private static final String KEY_RECAPTCHA_ENABLED = "security.recaptcha.enabled";
    private static final String KEY_RECAPTCHA_SITE_KEY = "security.recaptcha.site_key";
    private static final String KEY_RECAPTCHA_SECRET_KEY = "security.recaptcha.secret_key";

    private final TenantRepository tenantRepository;
    private final TenantDbExecutor tenantDbExecutor;
    private final ConfigPropertyService configPropertyService;
    private final ConfigChangeAuditRepository auditRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public ConfigRecaptchaResult getRecaptcha(ConfigPrincipal principal) {
        Tenant tenant = resolveTargetTenant(principal);

        return tenantDbExecutor.withTenant(
                tenant.getId(),
                tenant.getDatabaseName(),
                () -> toResult(resolveRecaptchaState(tenant.getId(), tenant.getDatabaseName())));
    }

    @Override
    @Transactional
    public ConfigRecaptchaResult patchRecaptcha(ConfigPrincipal principal, PatchConfigRecaptchaParams request) {
        Tenant tenant = resolveTargetTenant(principal);

        return tenantDbExecutor.withTenant(tenant.getId(), tenant.getDatabaseName(), () -> {
            ResolvedRecaptchaState beforeState = resolveRecaptchaState(tenant.getId(), tenant.getDatabaseName());
            RecaptchaAuditSnapshot before = RecaptchaAuditSnapshot.from(beforeState);

            boolean nextEnabled = request.recaptchaEnabled() != null ? request.recaptchaEnabled() : beforeState.enabled();
            String nextSiteKey = request.recaptchaSiteKey() != null ? request.recaptchaSiteKey() : beforeState.siteKey();
            boolean nextSecretConfigured = beforeState.secretConfigured()
                    || (request.recaptchaSecretKey() != null && !request.recaptchaSecretKey().isBlank());

            validateRecaptchaSettings(nextEnabled, nextSiteKey, nextSecretConfigured);

            if (request.recaptchaEnabled() != null) {
                configPropertyService.upsert(tenant.getId(), tenant.getDatabaseName(), KEY_RECAPTCHA_ENABLED,
                        String.valueOf(request.recaptchaEnabled()), false, principal.userId());
            }
            if (request.recaptchaSiteKey() != null) {
                configPropertyService.upsert(tenant.getId(), tenant.getDatabaseName(), KEY_RECAPTCHA_SITE_KEY,
                        request.recaptchaSiteKey(), false, principal.userId());
            }
            if (request.recaptchaSecretKey() != null && !request.recaptchaSecretKey().isBlank()) {
                configPropertyService.upsert(tenant.getId(), tenant.getDatabaseName(), KEY_RECAPTCHA_SECRET_KEY,
                        request.recaptchaSecretKey(), true, principal.userId());
            }

            ResolvedRecaptchaState afterState = resolveRecaptchaState(tenant.getId(), tenant.getDatabaseName());

            RecaptchaAuditSnapshot after = RecaptchaAuditSnapshot.from(afterState);
            writeAudit(principal, tenant.getId(), AUDIT_ACTION_PATCH, request.reason(), before, after);

            return toResult(afterState);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConfigAuditItemResult> getAuditTrail(ConfigPrincipal principal, int limit) {
        Tenant tenant = resolveTargetTenant(principal);
        return auditRepository.findByTargetTenantIdOrderByCreatedAtDesc(tenant.getId(), limit)
                .stream()
                .map(this::toAuditItem)
                .toList();
    }

    private Tenant resolveTargetTenant(ConfigPrincipal principal) {
        if (principal.tenantId() == null) {
            throw new AccessDeniedException("Tenant admin token missing tenant scope");
        }
        return tenantRepository.findById(principal.tenantId())
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
    }

    private void validateRecaptchaSettings(boolean enabled, String siteKey, boolean secretConfigured) {
        if (!enabled) {
            return;
        }

        if (siteKey == null || siteKey.isBlank()) {
            throw new IllegalArgumentException("reCAPTCHA site key is required when enabled");
        }

        if (!secretConfigured) {
            throw new IllegalArgumentException("reCAPTCHA secret key is required when enabled");
        }
    }

    private ConfigRecaptchaResult toResult(ResolvedRecaptchaState state) {
        return new ConfigRecaptchaResult(
                state.enabled(),
                maskSiteKey(state.siteKey()),
                state.secretConfigured(),
                state.updatedAt());
    }

    private void writeAudit(ConfigPrincipal principal, Long tenantId, String action, String reason,
            RecaptchaAuditSnapshot before, RecaptchaAuditSnapshot after) {
        String beforeJson = toJson(before);
        String afterJson = toJson(after);

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

    private ConfigAuditItemResult toAuditItem(ConfigChangeAudit item) {
        return new ConfigAuditItemResult(
                item.getId(),
                item.getActorUserId(),
                item.getActorEmail(),
                item.getActorRole(),
                item.getTargetTenantId(),
                item.getAction(),
                item.getReason(),
                item.getBeforeJson(),
                item.getAfterJson(),
                item.getCorrelationId(),
                item.getCreatedAt());
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            log.warn("Failed to serialize audit snapshot", ex);
            return "{}";
        }
    }

    private String maskSiteKey(String siteKey) {
        if (siteKey == null || siteKey.isBlank()) {
            return null;
        }
        if (siteKey.length() <= 8) {
            return "****";
        }
        return siteKey.substring(0, 4) + "..." + siteKey.substring(siteKey.length() - 4);
    }

    private ResolvedRecaptchaState resolveRecaptchaState(Long tenantId, String tenantDbName) {
        Optional<String> siteKeyRaw = configPropertyService.findRaw(tenantId, tenantDbName, KEY_RECAPTCHA_SITE_KEY);
        Optional<String> secretRaw = configPropertyService.findRaw(tenantId, tenantDbName, KEY_RECAPTCHA_SECRET_KEY);
        boolean enabled = configPropertyService.getBoolean(
                tenantId,
                tenantDbName,
                KEY_RECAPTCHA_ENABLED,
                false);

        String siteKey = siteKeyRaw.orElse(null);

        boolean secretConfigured = secretRaw
                .map(v -> v != null && !v.isBlank())
                .orElse(false);

        LocalDateTime updatedAt = resolveUpdatedAt(tenantId, tenantDbName);

        return new ResolvedRecaptchaState(enabled, siteKey, secretConfigured, updatedAt);
    }

    private LocalDateTime resolveUpdatedAt(Long tenantId, String tenantDbName) {
        LocalDateTime enabledAt = configPropertyService.find(tenantId, tenantDbName, KEY_RECAPTCHA_ENABLED)
                .map(ConfigProperty::getUpdatedAt)
                .orElse(null);
        LocalDateTime siteKeyAt = configPropertyService.find(tenantId, tenantDbName, KEY_RECAPTCHA_SITE_KEY)
                .map(ConfigProperty::getUpdatedAt)
                .orElse(null);
        LocalDateTime secretAt = configPropertyService.find(tenantId, tenantDbName, KEY_RECAPTCHA_SECRET_KEY)
                .map(ConfigProperty::getUpdatedAt)
                .orElse(null);

        LocalDateTime max = later(null, enabledAt);
        max = later(max, siteKeyAt);
        max = later(max, secretAt);

        return max;
    }

    private LocalDateTime later(LocalDateTime a, LocalDateTime b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.isAfter(b) ? a : b;
    }

    private record RecaptchaAuditSnapshot(
            boolean enabled,
            String siteKeyMasked,
            boolean secretConfigured) {

        static RecaptchaAuditSnapshot from(ResolvedRecaptchaState state) {
            String key = state.siteKey();
            String masked = null;
            if (key != null && !key.isBlank()) {
                masked = key.length() <= 8 ? "****" : key.substring(0, 4) + "..." + key.substring(key.length() - 4);
            }

            return new RecaptchaAuditSnapshot(
                    state.enabled(),
                    masked,
                    state.secretConfigured());
        }
    }

    private record ResolvedRecaptchaState(
            boolean enabled,
            String siteKey,
            boolean secretConfigured,
            LocalDateTime updatedAt) {
    }
}
