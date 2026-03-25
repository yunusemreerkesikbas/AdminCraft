# Platform Admin (Dashboard, Tenant Detail, Settings)

## Purpose

Platform admin features provide SUPER_ADMIN users with:

- **Platform Dashboard** -- aggregated statistics across all tenants (counts, storage, recent activity, module distribution)
- **Tenant Detail** -- tab-based view of a single tenant (overview, modules, provisioning jobs)
- **Platform Settings** -- global configuration (platform name, default language/currency, email display settings, SUPER_ADMIN security policy)
- **Landing demo requests** -- inbox for marketing-site contact/demo submissions (`platform_demo_requests`; list UI `/:lang/demo-requests`)

These features read exclusively from the `platform_management` database. No cross-DB queries or tenant DB access.

## Database

### Platform Settings table

Migrations:
- [`backend/src/main/resources/db/platform/V38__platform_settings.sql`](../../backend/src/main/resources/db/platform/V38__platform_settings.sql)
- [`backend/src/main/resources/db/platform/V41__extend_platform_settings_security.sql`](../../backend/src/main/resources/db/platform/V41__extend_platform_settings_security.sql)

Singleton pattern -- the table always contains exactly one row (`id = 1`).

| Column               | Type         | Default                |
|----------------------|--------------|------------------------|
| `platform_name`      | VARCHAR(100) | `Craftive`           |
| `default_language`   | VARCHAR(2)   | `TR`                   |
| `default_currency`   | VARCHAR(3)   | `TRY`                  |
| `email_from_address` | VARCHAR(255) | `noreply@craftive.io` |
| `email_from_name`    | VARCHAR(100) | `Craftive`           |
| `two_factor_policy`  | ENUM         | `DISABLED`             |
| `recaptcha_enabled`  | BOOLEAN      | `false`                |
| `recaptcha_site_key` | VARCHAR(255) | `null`                 |
| `recaptcha_secret_key_encrypted` | TEXT | `null`           |
| `recaptcha_threshold`| DECIMAL(3,2) | `0.50`                |

Entity: [`backend/.../entity/PlatformSettings.java`](../../backend/src/main/java/com/backend/infrastructure/persistence/platform/entity/PlatformSettings.java)

Repository: [`backend/.../repository/PlatformSettingsRepository.java`](../../backend/src/main/java/com/backend/infrastructure/persistence/platform/repository/PlatformSettingsRepository.java) -- `getSingleton()` default method reads row 1.

### Platform verification tokens table

Migration: [`backend/src/main/resources/db/platform/V42__create_platform_verification_tokens.sql`](../../backend/src/main/resources/db/platform/V42__create_platform_verification_tokens.sql)

Used for SUPER_ADMIN login OTP sessions when platform 2FA policy is `REQUIRED`.

### Existing tables used

Dashboard queries read from existing platform tables (`tenants`, `provisioning_jobs`, `tenant_modules`, `modules_catalog`). No new columns were added to these tables.

Repository enhancements:

- `TenantPlatformRepository` -- `findTop5ByOrderByCreatedAtDesc()`, `sumTotalStorageMb()`
- `ProvisioningJobRepository` -- `findTop5ByOrderByCreatedAtDesc()`
- `TenantModuleRepository` -- `findModuleDistribution()` (JPQL join with `modules_catalog`)

## Admin API (SUPER_ADMIN)

Most endpoints in this document require `ROLE_SUPER_ADMIN` via `@PreAuthorize`. **Exception:** landing demo public `POST` is documented under [Landing demo requests (marketing site)](#landing-demo-requests-marketing-site) (`permitAll`).

### Platform Dashboard

Controller: [`backend/.../controller/PlatformDashboardController.java`](../../backend/src/main/java/com/backend/presentation/controller/PlatformDashboardController.java)

| Method | Path                    | Description                     |
|--------|-------------------------|---------------------------------|
| GET    | `/api/platform/dashboard` | Aggregated platform statistics |

Response DTO: [`PlatformDashboardResponse`](../../backend/src/main/java/com/backend/presentation/dto/response/PlatformDashboardResponse.java)

```json
{
  "summary": { "total", "active", "pending", "suspended", "totalStorageMb" },
  "recentTenants": [{ "id", "companyName", "subdomain", "status", "createdAt" }],
  "recentJobs": [{ "id", "tenantId", "tenantSubdomain", "type", "status", "createdAt", "error" }],
  "moduleDistribution": [{ "moduleCode", "moduleName", "enabledCount" }]
}
```

Service: [`PlatformDashboardService`](../../backend/src/main/java/com/backend/application/service/PlatformDashboardService.java) / [`PlatformDashboardServiceImpl`](../../backend/src/main/java/com/backend/application/service/PlatformDashboardServiceImpl.java)

### Tenant Provisioning Jobs

Added to existing TenantController: [`backend/.../controller/TenantController.java`](../../backend/src/main/java/com/backend/presentation/controller/TenantController.java)

| Method | Path                                   | Description                          |
|--------|----------------------------------------|--------------------------------------|
| GET    | `/api/tenants/{tenantId}/provisioning-jobs` | List all provisioning jobs for a tenant |

Response DTO: [`ProvisioningJobResponse`](../../backend/src/main/java/com/backend/presentation/dto/response/ProvisioningJobResponse.java)

### Platform Settings

Controller: [`backend/.../controller/PlatformSettingsController.java`](../../backend/src/main/java/com/backend/presentation/controller/PlatformSettingsController.java)

| Method | Path                     | Description                |
|--------|--------------------------|----------------------------|
| GET    | `/api/platform/settings` | Read platform settings     |
| PATCH  | `/api/platform/settings` | Update platform settings   |

PATCH uses null-skip semantics: only non-null fields in the request body are applied to the singleton row.
Validation rules in [`PatchPlatformSettingsRequest`](../../backend/src/main/java/com/backend/application/dto/request/PatchPlatformSettingsRequest.java):

- `platformName`: max 100 chars
- `defaultLanguage`: 2-10 chars, letters/hyphen pattern
- `defaultCurrency`: exactly 3 letters
- `emailFromAddress`: valid email, max 255 chars
- `emailFromName`: max 100 chars
- `twoFactorPolicy`: `DISABLED` or `REQUIRED`
- `recaptchaSiteKey`: 40 chars, `[A-Za-z0-9_-]` pattern
- `recaptchaSecretKey`: 40 chars, `[A-Za-z0-9_-]` pattern
- `recaptchaThreshold`: 0.0-1.0

Schema note: `platform_settings.default_language` is currently `VARCHAR(2)` in migration/entity, so persisted values are bounded by column length.

Response DTO: [`PlatformSettingsResponse`](../../backend/src/main/java/com/backend/presentation/dto/response/PlatformSettingsResponse.java)

Request DTO: [`PatchPlatformSettingsRequest`](../../backend/src/main/java/com/backend/application/dto/request/PatchPlatformSettingsRequest.java)

Service: [`PlatformSettingsService`](../../backend/src/main/java/com/backend/application/service/PlatformSettingsService.java) / [`PlatformSettingsServiceImpl`](../../backend/src/main/java/com/backend/application/service/PlatformSettingsServiceImpl.java)

## Landing demo requests (marketing site)

Database table: `platform_demo_requests` (platform migration `V1.0.2__platform_demo_requests.sql`). Stores name, email, message, locale, source (`landing`), optional `client_ip` / `user_agent`, audit timestamps.

**Public (unauthenticated)**

- `POST /api/platform/public/demo-requests` — Bean-validated JSON body: `fullName`, `email`, optional `phone`, `message`, `locale`, optional `recaptchaToken`. When platform reCAPTCHA is enabled, the backend verifies the token with action **`landing_demo_request`** (same family as `/api/platform/cms/config` for the site key). Success: `ApiResponse` with `message` = `platform.demo.request.submitted` and `data.followUpNote` = `platform.demo.request.submitted.note` (i18n via `Accept-Language`). Errors: `message` from validation or `recaptcha.verification.failed` (HTTP 400, `code` 400 on reCAPTCHA path).
- Permitted in [`SecurityConfig`](../../backend/src/main/java/com/backend/infrastructure/config/SecurityConfig.java); excluded from tenant requirement in [`TenantFilter`](../../backend/src/main/java/com/backend/infrastructure/tenant/TenantFilter.java) (same pattern as newsletter).

**Admin (`ROLE_SUPER_ADMIN`)**

- `GET /api/platform/demo-requests` — Server-side pagination, sort, and search (`page`, `size`, `sort`, `search`). Response shape matches other admin list APIs (`PageableResponse`). Each row includes `phone` (nullable), full `message` plus `messagePreview` (first 120 characters, ellipsis when truncated) for list UIs.
- Angular route: `/:lang/demo-requests` ([`DemoRequestsListComponent`](../../storefront/src/app/modules/admin/custom/demo-requests/demo-requests-list.component.ts)), `superAdminGuard`, nav id `platform.demoRequests`.

## Public delivery APIs

Platform admin list/settings/dashboard endpoints require `ROLE_SUPER_ADMIN`. The **landing demo** `POST /platform/public/demo-requests` endpoint is public (control-plane ingest only).

## Frontend integration

All routes are guarded by `superAdminGuard` and lazy-loaded.

### Platform Dashboard

| File | Path |
|------|------|
| Route | [`storefront/.../platform-dashboard/platform-dashboard.routes.ts`](../../storefront/src/app/modules/admin/custom/platform-dashboard/platform-dashboard.routes.ts) |
| Component | [`storefront/.../platform-dashboard/platform-dashboard.component.ts`](../../storefront/src/app/modules/admin/custom/platform-dashboard/platform-dashboard.component.ts) |
| Service | [`storefront/.../platform-dashboard/platform-dashboard.service.ts`](../../storefront/src/app/modules/admin/custom/platform-dashboard/platform-dashboard.service.ts) |
| Types | [`storefront/.../platform-dashboard/platform-dashboard.types.ts`](../../storefront/src/app/modules/admin/custom/platform-dashboard/platform-dashboard.types.ts) |
| Error Dialog | [`storefront/.../platform-dashboard/job-error-dialog.component.ts`](../../storefront/src/app/modules/admin/custom/platform-dashboard/job-error-dialog.component.ts) |

URL: `/:lang/platform-dashboard`

**Error display**: Failed provisioning jobs show a red error icon next to the status badge in the Recent Jobs table. Clicking the icon opens a Material Dialog displaying the full error message (up to 500 characters, truncated at storage time per OWASP security guidelines) along with job metadata (tenant subdomain, type, created date). Error text is displayed in monospace font within a red-tinted container for better readability of technical error messages.

### Tenant Detail

| File | Path |
|------|------|
| Component | [`storefront/.../tenants/detail/tenant-detail.component.ts`](../../storefront/src/app/modules/admin/custom/tenants/detail/tenant-detail.component.ts) |
| Overview tab | [`storefront/.../tenants/detail/tabs/tenant-overview.component.ts`](../../storefront/src/app/modules/admin/custom/tenants/detail/tabs/tenant-overview.component.ts) |
| Modules tab | [`storefront/.../tenants/detail/tabs/tenant-modules.component.ts`](../../storefront/src/app/modules/admin/custom/tenants/detail/tabs/tenant-modules.component.ts) |
| Jobs tab | [`storefront/.../tenants/detail/tabs/tenant-jobs.component.ts`](../../storefront/src/app/modules/admin/custom/tenants/detail/tabs/tenant-jobs.component.ts) |
| Types | [`storefront/.../tenants/detail/tenant-detail.types.ts`](../../storefront/src/app/modules/admin/custom/tenants/detail/tenant-detail.types.ts) |

URL: `/:lang/tenants/:id`

Entry point: clicking a tenant's company name in the tenants list navigates to the detail page.

### Platform Settings

| File | Path |
|------|------|
| Route | [`storefront/.../platform-settings/platform-settings.routes.ts`](../../storefront/src/app/modules/admin/custom/platform-settings/platform-settings.routes.ts) |
| Component | [`storefront/.../platform-settings/platform-settings.component.ts`](../../storefront/src/app/modules/admin/custom/platform-settings/platform-settings.component.ts) |
| Service | [`storefront/.../platform-settings/platform-settings.service.ts`](../../storefront/src/app/modules/admin/custom/platform-settings/platform-settings.service.ts) |
| Types | [`storefront/.../platform-settings/platform-settings.types.ts`](../../storefront/src/app/modules/admin/custom/platform-settings/platform-settings.types.ts) |

URL: `/:lang/platform-settings`

### Landing demo requests (admin UI)

| File | Path |
|------|------|
| List component | [`storefront/.../demo-requests/demo-requests-list.component.ts`](../../storefront/src/app/modules/admin/custom/demo-requests/demo-requests-list.component.ts) |
| List template | [`storefront/.../demo-requests/demo-requests-list.component.html`](../../storefront/src/app/modules/admin/custom/demo-requests/demo-requests-list.component.html) |
| HTTP service | [`storefront/.../demo-requests/platform-demo-request-admin.service.ts`](../../storefront/src/app/modules/admin/custom/demo-requests/platform-demo-request-admin.service.ts) |
| Types | [`storefront/.../demo-requests/demo-request.types.ts`](../../storefront/src/app/modules/admin/custom/demo-requests/demo-request.types.ts) |
| Public controller | [`PlatformPublicDemoRequestController`](../../backend/src/main/java/com/backend/presentation/controller/PlatformPublicDemoRequestController.java) |
| Admin controller | [`PlatformDemoRequestController`](../../backend/src/main/java/com/backend/presentation/controller/PlatformDemoRequestController.java) |
| Application service | [`PlatformDemoRequestServiceImpl`](../../backend/src/main/java/com/backend/application/service/impl/PlatformDemoRequestServiceImpl.java) |

URL: `/:lang/demo-requests`

### Landing site (Next.js modal)

| File | Path |
|------|------|
| Modal | [`landing/components/modals/DemoRequestModal.tsx`](../../landing/components/modals/DemoRequestModal.tsx) |
| API client | [`landing/lib/platform-api.ts`](../../landing/lib/platform-api.ts) |

### Navigation

Items in the `platform` group in [`navigation-data.constants.ts`](../../storefront/src/app/shared/navigation/navigation-data.constants.ts) include:

- **Platform Dashboard** (`platform.dashboard`) -- `heroicons_outline:chart-bar-square`
- **Platform Settings** (`platform.settings`) -- `heroicons_outline:cog-6-tooth`
- **Demo requests** (`platform.demoRequests`) — `heroicons_outline:chat-bubble-left-right`

All are filtered by `requiredRole: 'SUPER_ADMIN'` and are invisible to tenant admins.

### API endpoints

Keys in [`api-endpoints.ts`](../../storefront/src/app/modules/admin/api-endpoints.ts) include:

- `platformDashboard` -> `platform/dashboard`
- `platformSettings` -> `platform/settings`
- `platformDemoRequests` -> `platform/demo-requests`
- `tenantProvisioningJobs` -> `tenants/${tenantId}/provisioning-jobs`

### i18n keys

Keys include `admin.platform.dashboard.*`, `admin.platform.settings.*`, `admin.platform.demoRequests.*`, `admin.nav.platformDemoRequests`, and `admin.tenants.detail.*` in both [`langEN.ts`](../../storefront/src/app/modules/admin/i18n/langEN.ts) and [`langTR.ts`](../../storefront/src/app/modules/admin/i18n/langTR.ts).

## Security & tenant isolation

- Super-admin-only platform APIs use `@PreAuthorize("hasRole('SUPER_ADMIN')")`. The public landing demo `POST` is `permitAll` (see above).
- All data comes from `platform_management` database only -- no tenant DB access.
- `TenantFilter` classifies `/platform/**` paths as platform routes (no tenant context required), except documented public prefixes (`/platform/cms/config`, `/platform/public/newsletter`, `/platform/public/demo-requests`).
- Tenant admin email is read from the `tenants` table in platform DB (KVKK-compliant: no cross-DB PII access).
- Frontend routes are protected by `superAdminGuard`; navigation items are filtered by role.

## Implementation guide

### View platform dashboard

1. Super admin navigates to `/:lang/platform-dashboard`
2. Frontend calls `GET /api/platform/dashboard`
3. Backend aggregates counts, recent tenants/jobs, and module distribution from platform DB
4. Dashboard renders summary cards, recent activity tables, and module distribution bars
5. For failed jobs with errors, a red error icon appears next to the status badge; clicking it opens a dialog with the full error message and job context

### View tenant detail

1. From tenant list, click a tenant's company name
2. Router navigates to `/:lang/tenants/:id`
3. Frontend calls three endpoints in parallel via `forkJoin`:
   - `GET /api/tenants/{id}` (detail)
   - `GET /api/tenants/{id}/modules` (enabled modules)
   - `GET /api/tenants/{id}/provisioning-jobs` (job history)
4. Data populates three tabs: Overview, Modules, Jobs

### Update platform settings

1. Navigate to `/:lang/platform-settings`
2. Frontend calls `GET /api/platform/settings` to load current values
3. Edit form fields (only dirty fields are included in PATCH)
4. Submit calls `PATCH /api/platform/settings` with only changed fields
5. Backend applies non-null fields to the singleton row and returns updated state

### View landing demo requests

1. SUPER_ADMIN opens `/:lang/demo-requests`
2. Frontend calls `GET /api/platform/demo-requests` with `page`, `size`, `sort`, optional `search`
3. Grid shows rows from `platform_demo_requests` (name, email, `messagePreview` from API for list display, full `message` in payload, locale, submitted time)

### Submit from marketing landing (runtime)

1. Browser loads `GET /api/platform/cms/config` for reCAPTCHA site key / enabled flag
2. If enabled, client runs reCAPTCHA v3 with action `landing_demo_request`
3. Client `POST /api/platform/public/demo-requests` with JSON body; backend verifies token when reCAPTCHA is on and inserts into `platform_demo_requests`
