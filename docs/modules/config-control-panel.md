# Config Control Panel (Independent Security Console)

## Purpose

Config Control Panel provides a storefront-independent recovery and runtime configuration surface at `/config`.

Current scope:

- password + OTP authentication for config operations
- tenant reCAPTCHA management (recovery from storefront lockout)
- super admin global runtime overrides (email + platform reCAPTCHA whitelisted keys)
- immutable backend audit trail for config changes

Out of scope:

- tenant 2FA policy management (`Site Dashboard -> Security` tab remains source of truth)
- audit UI for global runtime properties (audit is backend-only)

## Database

Platform migrations:

- [`backend/src/main/resources/db/platform/V45__create_config_change_audit.sql`](../../backend/src/main/resources/db/platform/V45__create_config_change_audit.sql)
- [`backend/src/main/resources/db/platform/V50__create_platform_config_properties.sql`](../../backend/src/main/resources/db/platform/V50__create_platform_config_properties.sql)

Platform tables:

- `config_change_audit` (actor, target, action, reason, before/after snapshots, correlation id, timestamp)
- `platform_config_properties` (global runtime key-value overrides)

Tenant config store (HAC-style key-value):

- Tenant migration: [`backend/src/main/resources/db/tenant/core/V39__create_config_properties.sql`](../../backend/src/main/resources/db/tenant/core/V39__create_config_properties.sql)
- Backfill migration: [`backend/src/main/resources/db/tenant/core/V40__backfill_recaptcha_config_properties.sql`](../../backend/src/main/resources/db/tenant/core/V40__backfill_recaptcha_config_properties.sql)

Tenant reCAPTCHA keys managed in panel:

- `security.recaptcha.enabled`
- `security.recaptcha.site_key`
- `security.recaptcha.secret_key` (encrypted)

Note: `security.recaptcha.threshold` may exist historically in tenant data but is not managed by Config Panel in current scope.

Global runtime whitelist (CONFIG_SUPER_ADMIN):

- `app.email.provider` (`console` | `smtp`)
- `app.email.from-address`
- `app.email.from-name`
- `app.frontend.base-url`
- `platform.security.recaptcha.enabled` (`true` | `false`)
- `platform.security.recaptcha.site_key`
- `platform.security.recaptcha.secret_key` (encrypted)

Note: `security.recaptcha.threshold` remains in platform settings and is not managed from Config Panel.
Namespace invariant: tenant `security.recaptcha.*` and platform `platform.security.recaptcha.*` are isolated and never cross-read.

## Admin API

Authentication APIs:

- `POST /api/config/auth/login`
- `POST /api/config/auth/verify-otp`
- `GET /api/platform/cms/config` (public pre-login flags for platform host, e.g. reCAPTCHA site key)

Tenant management APIs (`CONFIG_TENANT_ADMIN`):

- `GET /api/config/admin/security/recaptcha`
- `PATCH /api/config/admin/security/recaptcha`
- `GET /api/config/admin/security/recaptcha/audit?limit=20`
- `GET /api/config/admin/properties`
- `GET /api/config/admin/properties/{key}`
- `PUT /api/config/admin/properties/{key}`
- `DELETE /api/config/admin/properties/{key}?reason=...`

Global management APIs (`CONFIG_SUPER_ADMIN`):

- `GET /api/config/admin/global/properties`
- `GET /api/config/admin/global/properties/{key}`
- `PUT /api/config/admin/global/properties/{key}`
- `DELETE /api/config/admin/global/properties/{key}?reason=...`

Controllers:

- [`backend/src/main/java/com/backend/presentation/controller/ConfigAuthController.java`](../../backend/src/main/java/com/backend/presentation/controller/ConfigAuthController.java)
- [`backend/src/main/java/com/backend/presentation/controller/ConfigAdminRecaptchaController.java`](../../backend/src/main/java/com/backend/presentation/controller/ConfigAdminRecaptchaController.java)
- [`backend/src/main/java/com/backend/presentation/controller/ConfigAdminPropertiesController.java`](../../backend/src/main/java/com/backend/presentation/controller/ConfigAdminPropertiesController.java)
- [`backend/src/main/java/com/backend/presentation/controller/ConfigAdminGlobalPropertiesController.java`](../../backend/src/main/java/com/backend/presentation/controller/ConfigAdminGlobalPropertiesController.java)

## Frontend integration

Entry route:

- [`storefront/src/app/app.routes.ts`](../../storefront/src/app/app.routes.ts) (`path: 'config'`, empty layout)

Config panel module:

- [`storefront/src/app/modules/config/console/config-console.component.ts`](../../storefront/src/app/modules/config/console/config-console.component.ts)
- [`storefront/src/app/modules/config/auth/config-auth.component.ts`](../../storefront/src/app/modules/config/auth/config-auth.component.ts)
- [`storefront/src/app/modules/config/dashboard/config-dashboard.component.ts`](../../storefront/src/app/modules/config/dashboard/config-dashboard.component.ts)
- [`storefront/src/app/modules/config/properties/config-properties.component.ts`](../../storefront/src/app/modules/config/properties/config-properties.component.ts)
- [`storefront/src/app/modules/config/console/config-console.service.ts`](../../storefront/src/app/modules/config/console/config-console.service.ts)

Role-based behavior:

- `CONFIG_TENANT_ADMIN`: tenant properties + reCAPTCHA management
- `CONFIG_SUPER_ADMIN`: global runtime whitelist management

## Security & tenant isolation

Invariants:

- `/api/config/auth/**` is public from tenant-resolution perspective and used only for config login challenge flow
- `/api/config/admin/**` bypasses tenant-resolution in `TenantFilter`, but still requires JWT authentication
- tenant endpoints are hard-scoped to token tenant (no tenantId request override)
- global endpoints require `CONFIG_SUPER_ADMIN`

## Audit invariants

- every tenant/global property update and delete writes an audit record to `config_change_audit`
- global runtime audit scope: `GLOBAL_RUNTIME_CONFIG`
- global audit is backend-only (no dedicated frontend audit screen)

## Implementation guide

### Super admin: switch auth/recovery email runtime behavior

1. Open `/config` and complete login + OTP with super admin account.
2. Call `GET /api/config/admin/global/properties`.
3. Update allowed keys via `PUT /api/config/admin/global/properties/{key}` with `reason`.
4. Runtime behavior applies without redeploy/restart.

### Tenant admin: rotate reCAPTCHA keys safely

1. Open `/config` and complete login + OTP.
2. Call `GET /api/config/admin/security/recaptcha` to load current state.
3. Call `PATCH /api/config/admin/security/recaptcha` with `reason` and updated key values.
4. Verify with `GET /api/config/admin/security/recaptcha/audit`.
