# Architecture

## High-level concepts

- **Clean Architecture** boundaries:
  - `presentation` → controllers, request/response DTOs
  - `application` → services/use-cases (business logic orchestration)
  - `domain` → entities, enums, repository interfaces
  - `infrastructure` → JPA repositories, configs, tenant plumbing, external adapters

- **Multi-tenancy**: database-per-tenant
  - Platform DB: `platform_management` (control-plane)
  - Tenant DBs: `ac_subdomain_{id}` (data-plane, physically isolated)

## Backend request path

Backend uses the global context path:

- `server.servlet.context-path: /api` in [`backend/src/main/resources/application.yml`](../../backend/src/main/resources/application.yml)

So a controller with `@RequestMapping("/media")` is reachable at `GET /api/media`.

## Tenant isolation rules

- Tenant tables must **not** contain `tenant_id` columns (physical isolation).
- Tenant DB selection must happen **before any tenant-scoped repository call**.
- Tenant context must be cleared in a `finally` block for every request.

See the operational details in [`security-multi-tenancy.md`](security-multi-tenancy.md).
