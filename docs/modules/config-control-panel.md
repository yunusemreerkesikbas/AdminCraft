# Config Control Panel (Independent Security Console)

## Purpose

Config Control Panel provides a storefront-independent recovery and configuration surface at `/config`.

Current scope:

- password + OTP authentication for config operations
- tenant reCAPTCHA management
- immutable audit trail for security changes

This flow is designed to recover from storefront login lockout scenarios caused by invalid reCAPTCHA settings.

## Database

Platform migration:

- [`backend/src/main/resources/db/platform/V45__create_config_change_audit.sql`](../../backend/src/main/resources/db/platform/V45__create_config_change_audit.sql)

New platform table:

- `config_change_audit` (actor, tenant target, action, reason, before/after snapshots, correlation id, timestamp)

Tenant config store (HAC-style key-value):

- Tenant migration: [`backend/src/main/resources/db/tenant/core/V39__create_config_properties.sql`](../../backend/src/main/resources/db/tenant/core/V39__create_config_properties.sql)
- Backfill migration (from legacy `sites.recaptcha_*` columns): [`backend/src/main/resources/db/tenant/core/V40__backfill_recaptcha_config_properties.sql`](../../backend/src/main/resources/db/tenant/core/V40__backfill_recaptcha_config_properties.sql)

Canonical keys (Phase 1: reCAPTCHA):

- `security.recaptcha.enabled`
- `security.recaptcha.site_key`
- `security.recaptcha.secret_key` (encrypted)
- `security.recaptcha.threshold`

Legacy tenant columns (kept for backward compatibility during migration):

- `sites.recaptcha_enabled`
- `sites.recaptcha_site_key`
- `sites.recaptcha_secret_key_encrypted`
- `sites.recaptcha_threshold`

Source of truth:

- **Config Panel (reCAPTCHA)**: [`ConfigRecaptchaAdminServiceImpl`](../../backend/src/main/java/com/backend/application/service/impl/config/ConfigRecaptchaAdminServiceImpl.java) reads/writes via [`ConfigPropertyServiceImpl`](../../backend/src/main/java/com/backend/application/service/impl/config/ConfigPropertyServiceImpl.java) and [`ConfigProperty`](../../backend/src/main/java/com/backend/domain/entity/ConfigProperty.java); it also syncs to `Site` for backward compatibility.
- **Audit**: [`ConfigChangeAudit`](../../backend/src/main/java/com/backend/infrastructure/persistence/platform/entity/ConfigChangeAudit.java) (platform table).
- **Legacy read path**: [`Site`](../../backend/src/main/java/com/backend/domain/entity/Site.java) columns are still used by public config and login verification; Config Panel keeps them in sync.

## Admin API

Authentication APIs:

- `POST /api/config/auth/login`
- `POST /api/config/auth/verify-otp`

Controller:

- [`backend/src/main/java/com/backend/presentation/controller/ConfigAuthController.java`](../../backend/src/main/java/com/backend/presentation/controller/ConfigAuthController.java)

Management APIs:

- `GET /api/config/admin/security/recaptcha`
- `PATCH /api/config/admin/security/recaptcha`
- `GET /api/config/admin/security/recaptcha/audit?limit=20`

Controller:

- [`backend/src/main/java/com/backend/presentation/controller/ConfigAdminRecaptchaController.java`](../../backend/src/main/java/com/backend/presentation/controller/ConfigAdminRecaptchaController.java)

Validation contracts:

- login request: [`ConfigLoginRequest`](../../backend/src/main/java/com/backend/presentation/dto/request/config/ConfigLoginRequest.java)
- OTP verification request: [`ConfigVerifyOtpRequest`](../../backend/src/main/java/com/backend/presentation/dto/request/config/ConfigVerifyOtpRequest.java)
- reCAPTCHA patch request: [`PatchConfigRecaptchaRequest`](../../backend/src/main/java/com/backend/presentation/dto/request/config/PatchConfigRecaptchaRequest.java)

Role rules:

- only `CONFIG_TENANT_ADMIN` can access management APIs
- `tenantId` is always resolved from the JWT principal — no request parameter needed
- `CONFIG_SUPER_ADMIN` login path is preserved in `ConfigAuthenticationServiceImpl` for future platform admin panel use, but has no active management endpoints

## Public delivery APIs

Not applicable. Config panel APIs are control-plane APIs.

## Frontend integration

Entry route:

- [`storefront/src/app/app.routes.ts`](../../storefront/src/app/app.routes.ts) (`path: 'config'`, empty layout)

Config panel module:

- [`storefront/src/app/modules/config/console/config-console.component.ts`](../../storefront/src/app/modules/config/console/config-console.component.ts) — thin shell (stage + session management)
- [`storefront/src/app/modules/config/auth/config-auth.component.ts`](../../storefront/src/app/modules/config/auth/config-auth.component.ts) — login + OTP UI
- [`storefront/src/app/modules/config/dashboard/config-dashboard.component.ts`](../../storefront/src/app/modules/config/dashboard/config-dashboard.component.ts) — section card dashboard
- [`storefront/src/app/modules/config/recaptcha/config-recaptcha.component.ts`](../../storefront/src/app/modules/config/recaptcha/config-recaptcha.component.ts) — reCAPTCHA form + audit trail
- [`storefront/src/app/modules/config/console/config-console.service.ts`](../../storefront/src/app/modules/config/console/config-console.service.ts)
- [`storefront/src/app/modules/config/console/config-console.types.ts`](../../storefront/src/app/modules/config/console/config-console.types.ts)

Interceptor behavior (critical):

- [`storefront/src/app/core/tenant/tenant.interceptor.ts`](../../storefront/src/app/core/tenant/tenant.interceptor.ts) skips tenant header injection for `config/auth` and `config/admin`
- [`storefront/src/app/core/auth/auth.interceptor.ts`](../../storefront/src/app/core/auth/auth.interceptor.ts) skips CMS token injection entirely for config endpoints — config components manage their own `Authorization` header

## Security & tenant isolation

Source of truth:

- [`backend/src/main/java/com/backend/infrastructure/config/SecurityConfig.java`](../../backend/src/main/java/com/backend/infrastructure/config/SecurityConfig.java)
- [`backend/src/main/java/com/backend/infrastructure/security/JwtAuthenticationFilter.java`](../../backend/src/main/java/com/backend/infrastructure/security/JwtAuthenticationFilter.java)
- [`backend/src/main/java/com/backend/infrastructure/tenant/TenantFilter.java`](../../backend/src/main/java/com/backend/infrastructure/tenant/TenantFilter.java)
- [`backend/src/main/java/com/backend/presentation/controller/ConfigPrincipalResolver.java`](../../backend/src/main/java/com/backend/presentation/controller/ConfigPrincipalResolver.java)

Invariants:

- `/api/config/auth/**` is public from tenant-resolution perspective and used only for config login challenge flow
- `/api/config/admin/**` bypasses tenant-resolution in `TenantFilter`, but still requires JWT authentication
- authorization is enforced by controller `@PreAuthorize('hasRole(CONFIG_TENANT_ADMIN)')` only
- tenant admins are hard-scoped to their token tenant; `tenantId` is never accepted as a request parameter
- session restore checks token expiry: `Date.now() > issuedAt + expiresIn * 1000`

Audit invariants:

- each update writes a platform audit record with actor, role, tenant, reason, before/after JSON, and correlation id

## Implementation guide

### Tenant admin: rotate reCAPTCHA keys safely

1. Open `/config` and complete login + OTP.
2. Call `GET /api/config/admin/security/recaptcha` to load current state.
3. Call `PATCH /api/config/admin/security/recaptcha` with `reason` and updated key values.
4. Verify with `GET /api/config/admin/security/recaptcha/audit`.

### Audit review and incident trace

1. Query `GET /api/config/admin/security/recaptcha/audit?limit=20`.
2. Review `action`, `reason`, `actorEmail`, and `correlationId`.
3. Use `correlationId` to join with backend logs during incident analysis.
