# Security & Multi-Tenancy

## Tenant resolution and context

Every tenant-scoped request must resolve a tenant and set the context before accessing tenant repositories.

Source of truth: [`backend/src/main/java/com/backend/infrastructure/tenant/TenantFilter.java`](../../backend/src/main/java/com/backend/infrastructure/tenant/TenantFilter.java).

### Resolution order (simplified)

- Resolve tenant from headers:
  - `X-Tenant-ID`
  - `X-Tenant-Subdomain`
- Fallback to hostname-based resolution:
  - `X-Forwarded-Host` → `Origin` → `Referer` → `request.getServerName()`

If the tenant cannot be resolved for a tenant-scoped endpoint, the request is rejected.

### Tenant status validation

Tenant access is rejected if the tenant status is not `ACTIVE` (see `TenantFilter`).

### Context lifecycle

- Context is set:
  - `tenantId`, `tenantDbName`, `subdomain`
  - MDC: `correlationId`, `tenantId`, `tenantDb`
- Context is cleared in `finally`:
  - `tenantContext.clear()`
  - `MDC.clear()`

## Endpoint categories

### 1) Public, no tenant required

These endpoints bypass tenant resolution entirely (see `isPublicNoTenantRequired()` in `TenantFilter`):

- `/api/actuator/**`
- `/api/health/**`
- `/api/config/auth/**`
- `/api/platform/public/newsletter/**`
- `/api/swagger-ui/**`
- `/api/v3/api-docs/**`

Auth note:

- `/api/auth/login`, `/api/auth/refresh`, and `/api/auth/verify-otp` are also allowed without tenant context, but through a dedicated branch in `TenantFilter` (not via `isPublicNoTenantRequired()`).

**Note:** `/api/cms/config` is **not** in this list. It requires tenant resolution (via `X-Tenant-Subdomain` header) but no authentication. It is used by `ConfigFlagsService` at app startup to load tenant config properties (`config_properties` table).

Authentication details: [`authentication.md`](authentication.md)

### 2) Platform endpoints (SUPER_ADMIN only)

Platform endpoints require `ROLE_SUPER_ADMIN` (see `isPlatformEndpoint()` in `TenantFilter`), e.g.:

- `/api/platform/**`
- `/api/provisioning/**`
- `/api/tenants/**` (except `/api/tenants/current`)

Public exception under `/api/platform/**`:

- `/api/platform/public/newsletter/**` is intentionally non-tenant public.

Module catalog is exposed under provisioning:

- `/api/provisioning/modules/catalog`

Note:

- `TenantFilter` also classifies `/api/modules/catalog/**` as a platform endpoint, but the implemented controller mapping is under `/api/provisioning/modules/catalog`.

### 3) Config admin endpoints (independent control panel)

`/api/config/admin/**` bypasses tenant resolution in `TenantFilter` and relies on JWT + role checks.

Role checks are enforced by:

- [`backend/src/main/java/com/backend/presentation/controller/ConfigAdminRecaptchaController.java`](../../backend/src/main/java/com/backend/presentation/controller/ConfigAdminRecaptchaController.java) (`@PreAuthorize`)
- [`backend/src/main/java/com/backend/application/service/impl/config/ConfigRecaptchaAdminServiceImpl.java`](../../backend/src/main/java/com/backend/application/service/impl/config/ConfigRecaptchaAdminServiceImpl.java) (tenant scope rules)

### 4) Tenant endpoints (default)

All other endpoints require a resolved tenant, and run with tenant DB context.

## Correlation IDs

- Incoming `X-Correlation-ID` is used when present.
- Otherwise a new UUID is generated in `TenantFilter`.
- This should be included in logs for troubleshooting.
