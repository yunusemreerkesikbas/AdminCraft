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

### Provisioning jobs

Controller: [`backend/src/main/java/com/backend/presentation/ProvisioningController.java`](../../backend/src/main/java/com/backend/presentation/ProvisioningController.java)

Base path: `/api/provisioning`

- `GET /api/provisioning/modules/catalog` (available modules)
- `POST /api/provisioning/tenants/{tenantId}/provision` (start provisioning)
- `GET /api/provisioning/jobs/{jobId}` (poll status)
- `POST /api/provisioning/tenants/{tenantId}/sync-migrations` (sync missing migrations)

Authorization note:

- Tenant management endpoints in `TenantController` are guarded with `@PreAuthorize("hasRole('SUPER_ADMIN')")`.
- `ProvisioningController` endpoints are guarded with `@PreAuthorize("hasRole('SUPER_ADMIN')")`.

Rate limit: **5 requests/min per tenant** on provisioning endpoints.

## Public delivery APIs

Not applicable. Provisioning endpoints are authenticated control-plane APIs.

## Frontend integration

Provisioning-related admin UI is integrated through tenant management views:

- Tenants list route: [`storefront/src/app/modules/admin/custom/tenants/tenants.routes.ts`](../../storefront/src/app/modules/admin/custom/tenants/tenants.routes.ts)
- Tenants service: [`storefront/src/app/modules/admin/custom/tenants/tenants.service.ts`](../../storefront/src/app/modules/admin/custom/tenants/tenants.service.ts)
- Tenant detail jobs tab: [`storefront/src/app/modules/admin/custom/tenants/detail/tabs/tenant-jobs.component.ts`](../../storefront/src/app/modules/admin/custom/tenants/detail/tabs/tenant-jobs.component.ts)

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
  - `{ "modules": ["core", "media"] }`
- Job status response contains:
  - `jobId`, `tenantId`, `type`, `status`, `progress`, `error`, `createdAt`, `startedAt`, `completedAt`

DTO references (source of truth):

- `backend/src/main/java/com/backend/application/dto/provisioning/ProvisionRequest.java`
- `backend/src/main/java/com/backend/application/dto/provisioning/ProvisioningJobResponse.java`

### Add a new tenant module

Follow the checklist exactly:

- [`backend/docs/MODULE_SYNC_CHECKLIST.md`](../../backend/docs/MODULE_SYNC_CHECKLIST.md)

Quick reference (files you will typically touch):

- Backend
  - `backend/src/main/resources/db/platform/R__seed_modules.sql`
  - `backend/src/main/java/com/backend/domain/enums/ModuleCode.java`
  - `backend/src/main/resources/db/tenant/{module}/V1__baseline.sql`
- Frontend
  - `storefront/src/app/core/navigation/navigation-modules.constants.ts`
  - `storefront/src/app/shared/navigation/navigation-data.constants.ts` (only if module adds new navigation items)
  - `storefront/src/app/core/auth/guards/module.guard.ts` (only if custom module messaging is needed)
  - `storefront/src/app/modules/admin/api-endpoints.ts` (add endpoint templates when the admin UI needs new routes)

### Apply new migrations to existing tenants

1. Trigger a sync job:
   - `POST /api/provisioning/tenants/{tenantId}/sync-migrations`
2. Poll until the job completes:
   - `GET /api/provisioning/jobs/{jobId}`

