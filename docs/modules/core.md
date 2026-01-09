# Core

## Purpose

Core is the required tenant module that provides foundational tenant data-plane features such as:

- Users and roles
- System bootstrapping seeds (e.g., system user)
- Navigation storage (see the dedicated Navigation module doc)

## Database

Tenant migrations:

- `backend/src/main/resources/db/tenant/core/`
  - `V1__baseline.sql`
  - `R__seed_roles.sql`
  - `R__seed_system_user.sql`
  - `V17__navigation_nodes.sql`
  - `V18__navigation_i18n.sql`

## Admin UI

Admin features built on core data typically live under:

- `storefront/src/app/modules/admin/custom/users/`
- `storefront/src/app/modules/admin/custom/sites/`

Navigation management is documented separately:

- [`navigation.md`](navigation.md)

## Security & tenant isolation

- Core data lives in tenant databases (`ac_tenant_{id}`) and is always tenant-scoped.
- Tenant resolution is enforced by [`backend/src/main/java/com/backend/infrastructure/tenant/TenantFilter.java`](../../backend/src/main/java/com/backend/infrastructure/tenant/TenantFilter.java).

## Implementation guide

### Adding a new core-owned tenant feature

- Add tenant migrations under `backend/src/main/resources/db/tenant/core/` using normal Flyway versioning.
- Expose APIs in presentation layer controllers under `/api/*` (context path is configured in `application.yml`).
- Keep all business logic in the application layer; do not couple application services to servlet APIs.
