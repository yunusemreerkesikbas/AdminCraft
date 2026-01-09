# Backend Patterns

## Flyway migrations

Tenant migrations live under:

- `backend/src/main/resources/db/tenant/{module}/`

Examples confirmed in the repo:

- `backend/src/main/resources/db/tenant/media/V20__media_baseline.sql`
- `backend/src/main/resources/db/tenant/pagebuilder/V15__page_templates.sql`
- `backend/src/main/resources/db/tenant/core/V17__navigation_nodes.sql`

Platform migrations/seeds live under:

- `backend/src/main/resources/db/platform/`

## Modules catalog sync (manual)

When adding a new tenant module, follow:

- [`backend/docs/MODULE_SYNC_CHECKLIST.md`](../../backend/docs/MODULE_SYNC_CHECKLIST.md)

## API base path

The backend runs under `/api` context path:

- [`backend/src/main/resources/application.yml`](../../backend/src/main/resources/application.yml)

So controller mappings are addressed as `/api/{controllerMapping}`.

## Response wrapper

Controllers return a wrapped response type:

- `ApiResponse<T>` (see usage in controllers, e.g. `CmsDeliveryController` and `MediaController`)

## Async jobs (platform operations)

Provisioning and migration sync are implemented as asynchronous jobs:

- `backend/src/main/java/com/backend/presentation/ProvisioningController.java`
- `backend/src/main/java/com/backend/application/service/ProvisioningServiceImpl.java`

