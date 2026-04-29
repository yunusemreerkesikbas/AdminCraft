# Security & Multi-Tenancy

## Tenant resolution and context

Every tenant-scoped request must resolve a tenant and set the context before accessing tenant repositories.

Source of truth: [`backend/src/main/java/com/backend/infrastructure/tenant/TenantFilter.java`](../../backend/src/main/java/com/backend/infrastructure/tenant/TenantFilter.java).

### Resolution order (simplified)

- Resolve tenant from headers:
  - `X-Tenant-ID`
  - `X-Tenant-Subdomain`
- Fallback to hostname-based resolution:
  - `X-Forwarded-Host` only when `app.tenant.trust-forwarded-host=true`
  - `request.getServerName()`

`Origin` and `Referer` are not used for tenant resolution because they are client-controlled. `X-Forwarded-Host` is also client-spoofable unless a trusted reverse proxy strips and rewrites it, so the default is `app.tenant.trust-forwarded-host=false`.

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

- `/api/actuator/health` (health check only — all other actuator endpoints require `ROLE_SUPER_ADMIN`, see SEC-005)
- `/api/health/**`
- `/api/config/auth/**`
- `/api/platform/public/newsletter/**`
- `/api/swagger-ui/**`
- `/api/v3/api-docs/**`

Auth note:

- `/api/auth/login`, `/api/auth/refresh`, `/api/auth/verify-otp`, and `/api/auth/forgot-password` are also allowed without tenant context, but through a dedicated branch in `TenantFilter` (not via `isPublicNoTenantRequired()`).
- `/api/auth/forgot-password` still sends reset mail only when the application service resolves an active tenant from headers or existing context; invalid/missing tenant identifiers return the generic password-reset response for anti-enumeration.
- `/api/auth/reset-password`, `/api/auth/verify-reset-token`, and `/api/auth/set-initial-password` are unauthenticated but still tenant-scoped. They require a resolved tenant via `X-Tenant-ID`, `X-Tenant-Subdomain`, or trusted hostname.

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

**Unauthenticated but tenant-scoped:** `POST /api/public/contact-requests` has no JWT but still flows through normal tenant resolution (headers / hostname). It is **not** listed under “Public, no tenant required”.

This includes ImpEx:

- `POST /api/impex/execute` executes against the **active tenant database only**. Every caller, including `SUPER_ADMIN`, must have a resolved tenant context (`X-Tenant-ID` or `X-Tenant-Subdomain`, or trusted hostname resolution). There is **no** ImpEx execution path against `platform_management` at runtime; platform seed data is applied via Flyway migrations or DBA tooling.
- `TENANT_ADMIN` and `SUPER_ADMIN` both require the same tenant resolution invariants before ImpEx runs.

## Rate limiting

Resilience4j rate limiter instances are configured in `application.yml` under `resilience4j.ratelimiter.instances`. All instances use `timeout-duration: 0s` (fail-fast, no queue). Exceeding a limit returns HTTP 429.

| Instance name        | Endpoint(s)                                    | Limit        | Purpose |
|----------------------|------------------------------------------------|--------------|---------|
| `entryFieldDefinition` | `POST /api/entry-fields/definitions`         | 5 / 60 s     | Prevent bulk definition abuse |
| `impexExecute`       | `POST /api/impex/execute`                      | 5 / 60 s     | Slow down mass DB manipulation |
| `configAdmin`        | `PATCH /api/config/admin/security/recaptcha`   | 5 / 60 s     | SEC-105: prevent single-request reCAPTCHA disable |
| `demoRequest`        | `POST /api/platform/public/demo-requests`      | 10 / 60 s    | SEC-107: mail-bomb protection on public ingest |

Demo request deduplication (SEC-107): in addition to the global rate limit, the service suppresses duplicate submissions from the same email + IP within a 5-minute window. Suppressed requests return HTTP 200 with the standard success body (constant-time response — no enumeration signal).

## HTTP security response headers (SEC-014/SEC-117)

`SecurityConfig` applies the following headers to every API response:

| Header | Value |
|--------|-------|
| `X-Frame-Options` | `DENY` |
| `X-Content-Type-Options` | `nosniff` |
| `Referrer-Policy` | `strict-origin-when-cross-origin` |
| `Content-Security-Policy` | `default-src 'self'; frame-ancestors 'none'; object-src 'none'; img-src 'self' data: https:` |
| `Strict-Transport-Security` | `max-age=31536000; includeSubDomains` (disabled on `dev` profile) |

CSP note: The backend is a pure REST API — it does not serve HTML. The Angular admin panel and Next.js storefront receive their CSP at the Traefik / CDN layer. The backend CSP is a defence-in-depth measure for API responses.

HSTS note: HSTS is active on `stage` and `prod` profiles only. On the `dev` profile (`localhost` HTTP) it is suppressed to avoid browser HSTS preload conflicts.

## Correlation IDs

- Incoming `X-Correlation-ID` is used when present.
- Otherwise a new UUID is generated in `TenantFilter`.
- This should be included in logs for troubleshooting.
