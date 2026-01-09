# Platform Provisioning (Tenants, Modules, Jobs)

## Purpose

Platform provisioning is the control-plane used by SUPER_ADMIN users to:

- Create and manage tenants
- Provision tenant databases and run tenant module migrations
- Enable and query tenant modules
- Run migration sync for existing tenants

## Platform APIs (SUPER_ADMIN)

### Tenants

Controller: [`backend/src/main/java/com/backend/presentation/controller/TenantController.java`](../../backend/src/main/java/com/backend/presentation/controller/TenantController.java)

Base path: `/api/tenants`

- `GET /api/tenants` (list)
- `GET /api/tenants/{id}` (detail)
- `POST /api/tenants` (create)
- `PUT /api/tenants/{id}` (update)
- `DELETE /api/tenants/{id}` (delete)
- `GET /api/tenants/{tenantId}/modules` (module enablement list)
- `POST /api/tenants/{id}/generate-admin` (generate tenant admin credentials)

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

Rate limit: **5 requests/min per tenant** on provisioning endpoints.

## Tenant module catalog synchronization

When adding a new tenant module, keep backend + frontend + migrations consistent:

- [`backend/docs/MODULE_SYNC_CHECKLIST.md`](../../backend/docs/MODULE_SYNC_CHECKLIST.md)

## Security & tenant isolation

Platform endpoints require `ROLE_SUPER_ADMIN` and are treated as platform routes by:

- [`backend/src/main/java/com/backend/infrastructure/tenant/TenantFilter.java`](../../backend/src/main/java/com/backend/infrastructure/tenant/TenantFilter.java)

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

