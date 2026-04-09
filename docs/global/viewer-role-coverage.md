# Tenant Viewer Role Coverage

## Purpose

Define safe `VIEWER` authorization boundaries for tenant-side APIs and prevent accidental write access.

## Source of truth

- Role model: `backend/src/main/java/com/backend/domain/enums/UserRole.java`
- Controller layer: `backend/src/main/java/com/backend/presentation/controller`

## Endpoint matrix (priority controllers)

| Controller | Endpoint group | Current guard (before) | Proposed / applied guard | Risk note |
| --- | --- | --- | --- | --- |
| `PageController` | Read (`GET`) | `TENANT_ADMIN + VIEWER` | Keep | Low |
| `PageController` | Write (`POST/PUT/DELETE`) | `TENANT_ADMIN` | Keep | Low |
| `NavigationController` | Read (`GET`) | `TENANT_ADMIN + VIEWER` | Keep | Low |
| `NavigationController` | Write (`POST/PUT/DELETE`) | `TENANT_ADMIN` | Keep | Low |
| `ComponentController` | Read (`GET`) | `TENANT_ADMIN + VIEWER` | Keep | Medium (many endpoints) |
| `ComponentController` | Write (`POST/PUT/DELETE`) | `TENANT_ADMIN` | Keep | Medium |
| `MediaController` | Read (`GET`) | `TENANT_ADMIN + VIEWER` | Keep | Medium (file/link side effects must remain admin-only) |
| `MediaController` | Write (`POST/PUT/DELETE`) | `TENANT_ADMIN` | Keep | Medium |
| `ProductController` | Read (`GET`) | `TENANT_ADMIN + VIEWER` | Keep | Low |
| `ProductController` | Write (`POST/PUT/PATCH/DELETE`) | `TENANT_ADMIN` | Keep | Low |
| `TenantMailMarketingController` | Read (`GET`, export) | `TENANT_ADMIN` | `TENANT_ADMIN + VIEWER` | Medium (contains subscriber data) |
| `TenantMailMarketingController` | Write (`POST/PUT/DELETE`) | `TENANT_ADMIN` | Keep with explicit method guards | Medium |

## Applied normalization

- `TenantMailMarketingController` class guard broadened to read persona:
  - `@PreAuthorize("hasAnyRole('TENANT_ADMIN', 'VIEWER')")`
- Explicit admin-only method guards kept for mutating methods:
  - template translation update
  - subscriber create/update/delete
  - provider config upsert
  - campaign send

## Validation plan

1. Authenticate as `VIEWER` and verify read endpoints return `200`:
   - list/detail endpoints in pages, navigation, media, products
   - mail marketing read endpoints (`/mail/templates/types`, `/mail/campaigns`, `/mail/subscribers/admin`, export)
2. Verify `VIEWER` receives `403` on all mutation endpoints.
3. Authenticate as `TENANT_ADMIN` and verify mutation endpoints remain functional.
4. Run backend compile and targeted auth regression tests.

## Platform role decision

- Keep platform control-plane (`Platform*`, `TenantController`, `TenantLanguageController`) as `SUPER_ADMIN` only.
- Do not introduce platform `VIEWER` until a concrete read-only platform persona is required (for example, operations audit dashboard access).
