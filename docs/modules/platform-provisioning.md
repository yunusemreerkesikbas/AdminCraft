# Platform Provisioning (Tenants, Modules, Jobs)

## Purpose

Platform provisioning is the control-plane used by SUPER_ADMIN users to:

- Create and manage tenants
- Provision tenant databases and run tenant module migrations
- Enable and query tenant modules
- Run migration sync for existing tenants

## Database

Provisioning uses both platform and tenant migration sources:

- Platform migrations: [`backend/src/main/resources/db/platform`](../../backend/src/main/resources/db/platform)
- Tenant module migrations: [`backend/src/main/resources/db/tenant`](../../backend/src/main/resources/db/tenant)

Core platform tables used by provisioning flows:

- `tenants`
- `provisioning_jobs`
- `tenant_modules`
- `modules_catalog`

## Admin API (SUPER_ADMIN)

### Tenants

Controller: [`backend/src/main/java/com/backend/presentation/controller/TenantController.java`](../../backend/src/main/java/com/backend/presentation/controller/TenantController.java)

Base path: `/api/tenants`

- `GET /api/tenants` (list)
- `GET /api/tenants/{id}` (detail)
- `POST /api/tenants` (create)
- `PUT /api/tenants/{id}` (update)
- `DELETE /api/tenants/{id}` (delete)
- `GET /api/tenants/{tenantId}/modules` (module enablement list)
- `GET /api/tenants/{tenantId}/provisioning-jobs` (provisioning job history)
- `POST /api/tenants/{id}/generate-admin` (generate tenant admin credentials)

`GET /api/tenants` supports list pagination/sort/search:

- Query params: `page`, `size`, `sort`, `search`, `status`
- Response envelope: `PageableResponse<TenantListResponse>`
- Invalid `sort` returns `400` with i18n key `tenant.sort.invalid`

Tenant-scoped access for tenant admins (guarded by tenant header matching):

- `GET /api/tenants/current/modules`
- `GET /api/tenants/current/detail`

Module visibility semantics:

- `GET /api/tenants/{tenantId}/modules` and `GET /api/tenants/current/modules` return only user-facing enabled modules.
- Current user-facing module set is `core`, `product`, `mail_marketing`.
- Core execution dependencies (`media`, `component_library`, `pagebuilder`) are runtime migration modules, not tenant-facing module flags.
- Legacy internal `tenant_modules` rows for `media`, `component_library`, and `pagebuilder` are cleaned by platform repair migration `V1.0.1__repair_internal_tenant_modules.sql`.

### Provisioning jobs

Controller: [`backend/src/main/java/com/backend/presentation/ProvisioningController.java`](../../backend/src/main/java/com/backend/presentation/ProvisioningController.java)

Base path: `/api/provisioning`

- `GET /api/provisioning/modules/catalog` (available modules)
- `POST /api/provisioning/tenants/{tenantId}/provision` (start provisioning)
- `GET /api/provisioning/jobs/{jobId}` (poll status)
- `POST /api/provisioning/tenants/{tenantId}/sync-migrations` (sync missing migrations)

Module catalog behavior:

- `GET /api/provisioning/modules/catalog` returns provisioning-selectable modules: `core`, `product`, `mail_marketing`.
- `modules_catalog` still stores execution modules (`media`, `component_library`, `pagebuilder`) for migration ordering and tenant module history joins.

Authorization note:

- Tenant management endpoints in `TenantController` are guarded with `@PreAuthorize("hasRole('SUPER_ADMIN')")`.
- `ProvisioningController` endpoints are guarded with `@PreAuthorize("hasRole('SUPER_ADMIN')")`.

Rate limit: **5 requests/min per tenant** on provisioning endpoints.

## Public delivery APIs

Not applicable. Provisioning endpoints are authenticated control-plane APIs.

## Frontend integration

Provisioning-related admin UI is integrated through tenant management views:

- Tenants list route: [`storefront/src/app/app.routes.ts`](../../storefront/src/app/app.routes.ts) (under `tenants` path)
- Tenants service: [`storefront/src/app/modules/admin/custom/tenants/tenants.service.ts`](../../storefront/src/app/modules/admin/custom/tenants/tenants.service.ts)
- Tenant detail jobs tab: [`storefront/src/app/modules/admin/custom/tenants/detail/tabs/tenant-jobs.component.ts`](../../storefront/src/app/modules/admin/custom/tenants/detail/tabs/tenant-jobs.component.ts)

### Messaging convention (tenant/provisioning flows)

- Backend owns user-facing operation messages and returns them in `ApiResponse.message` (localized by `Accept-Language`).
- Frontend should only display backend `message` for success/error states (`response.message` and `error.error.message`), not author module-specific business messages.
- Frontend may use only generic fallback text when backend message is missing (unexpected failure scenarios).

## Security & tenant isolation

Platform routes are treated as non-tenant-scoped control-plane paths by:

- [`backend/src/main/java/com/backend/infrastructure/tenant/TenantFilter.java`](../../backend/src/main/java/com/backend/infrastructure/tenant/TenantFilter.java)
- Tenant management endpoints are `ROLE_SUPER_ADMIN` protected in `TenantController`.
- Provisioning endpoints are `ROLE_SUPER_ADMIN` protected in `ProvisioningController`.
- List/pagination/sort behavior should follow [`../global/list-pagination-search.md`](../global/list-pagination-search.md)

## Implementation guide

### Provision a tenant (end-to-end)

1. Create tenant:
   - `POST /api/tenants`
2. Start provisioning job:
   - `POST /api/provisioning/tenants/{tenantId}/provision`
3. Poll job status until completion:
   - `GET /api/provisioning/jobs/{jobId}`

Job identifier:

- Provisioning endpoints return `ProvisioningJobResponse` which includes `jobId` (use this value for polling).

Request/response shape (high level):

- Provision request body:
  - `{ "modules": ["core"] }`
  - `{ "modules": ["core", "product"] }`
  - `{ "modules": ["core", "mail_marketing"] }`
  - `{ "modules": ["core", "product", "mail_marketing"] }`
- Job status response contains:
  - `jobId`, `tenantId`, `type`, `status`, `progress`, `error`, `createdAt`, `startedAt`, `completedAt`

Provisioning module canonicalization:

- `core` is required for full provision.
- When request includes `core`, backend uses `ModuleCode.resolveExecutionCodes(List<String>)` to expand the execution set to: `core`, `media`, `component_library`, `pagebuilder`
- `product` and `mail_marketing` remain optional and are appended only when requested.
- Only provisioning-selectable module codes are accepted in full provision requests: `core`, `product`, `mail_marketing`
- Core execution modules cannot be sent directly in `POST /api/provisioning/tenants/{tenantId}/provision`; they are derived by backend normalization.
- `ModuleCode.resolveExecutionCodes()` is the single source of truth for core expansion — used by both `ProvisioningServiceImpl` (full provision) and `TenantStartupMigrator` (sync/startup migration) to guarantee identical behavior.

DTO references (source of truth):

- `backend/src/main/java/com/backend/application/dto/provisioning/ProvisionRequest.java`
- `backend/src/main/java/com/backend/application/dto/provisioning/ProvisioningJobResponse.java`

### Add a new tenant module

Update these files in order. See also [Module Execution Order](../global/migrations.md#module-execution-order) and `TenantMigrationService.MODULE_ORDER` when adding a new module to the migration sequence.

#### Backend

1. **Database seed** — `backend/src/main/resources/db/platform/R__seed_modules.sql`  
   Use `ON DUPLICATE KEY UPDATE` (see existing rows) to avoid cascade-deleting `tenant_modules`:

   ```sql
   INSERT INTO modules_catalog (code, name, type, version, deps, enabled_by_default, description)
   VALUES ('new_module', 'New Module', 'core', '1.0.0', '["core"]', FALSE, 'Module description here.')
   AS new_vals
   ON DUPLICATE KEY UPDATE
       name = new_vals.name,
       type = new_vals.type,
       version = new_vals.version,
       description = new_vals.description;
   ```

2. **Enum** — `backend/src/main/java/com/backend/domain/enums/ModuleCode.java`

   ```java
   public enum ModuleCode {
       // ... existing
       NEW_MODULE("new_module", "New Module");
   }
   ```

3. **Migration order** — If the module participates in tenant migrations, add it to `MODULE_ORDER` in [TenantMigrationService](../../backend/src/main/java/com/backend/application/service/TenantMigrationService.java) and document the order in [docs/global/migrations.md](../global/migrations.md).

4. **Tenant migration** — `backend/src/main/resources/db/tenant/new_module/V1__baseline.sql`

   ```sql
   CREATE TABLE new_module_data (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,
       -- fields here
   ) ENGINE=InnoDB CHARSET=utf8mb4;
   ```

#### Frontend

1. **Navigation constant** — `storefront/src/app/core/navigation/navigation-modules.constants.ts`  
   Only add **provisioning-selectable** modules here (catalog exposes `core`, `product`, `mail_marketing`; core-expanded modules like `media`, `component_library`, `pagebuilder` are covered by `CORE` and do not need an entry):

   ```typescript
   export const NAVIGATION_MODULES = {
     // ... existing
     NEW_MODULE: "new_module",
   } as const;
   ```

2. **Module guard** — `storefront/src/app/core/auth/guards/module.guard.ts`  
   The guard reads `requiredModule` from route data; typically no changes needed. Add `requiredModule` to route/navigation data when protecting new routes.

3. **Navigation items** (if needed) — `storefront/src/app/shared/navigation/navigation-data.constants.ts`  
   Use i18n keys for `title` (e.g. `admin.nav.newModule`):

   ```typescript
   {
       id: 'apps.custom.newModule',
       title: 'admin.nav.newModule',
       type: 'basic',
       icon: 'heroicons_outline:icon-name',
       link: 'new-module',
       requiredModule: NAVIGATION_MODULES.NEW_MODULE,
       excludedRoles: ['SUPER_ADMIN'],
   }
   ```

4. **API endpoints** (when admin UI needs new routes) — `storefront/src/app/modules/admin/api-endpoints.ts`

#### Verification

- [ ] Backend compiles: `mvn clean compile`
- [ ] Module in catalog: `curl http://localhost:8080/api/provisioning/modules/catalog`
- [ ] Provision dialog shows new module
- [ ] Navigation appears when module is enabled
- [ ] Migration runs successfully

#### Notes

- Module types: core-expanded modules (`core`, `media`, `component_library`, `pagebuilder`) use `type: 'core'`; optional catalog modules (e.g. `product`, `mail_marketing`) use `type: 'b2c'`
- Core module deps: `NULL`; others: `'["core"]'`
- Module codes: lowercase with underscores

### Apply new migrations to existing tenants

1. Trigger a sync job:
   - `POST /api/provisioning/tenants/{tenantId}/sync-migrations`
2. Poll until the job completes:
   - `GET /api/provisioning/jobs/{jobId}`

Sync behavior note:

- `sync-migrations` applies the same module normalization logic as full provisioning.
- If tenant has `core`, requests for core-covered modules (`media`, `component_library`, `pagebuilder`) are treated as covered.
- Startup auto-sync uses the same runtime expansion. An active tenant with only the user-facing `core` flag still runs `core`, `media`, `component_library`, and `pagebuilder` tenant migrations on boot.
