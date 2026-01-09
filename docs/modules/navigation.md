# Navigation

## Purpose

Navigation provides a hierarchical menu structure (nodes + entries), designed for deep trees and efficient subtree operations.

Key characteristics:

- Max depth and tree behavior are enforced by the application layer.
- Server-side pagination/search/sort is supported for root inventories.
- i18n is modeled with Base + I18n tables.
- Composite endpoints exist for atomic create/update with translations.

## Database

Tenant migrations (core module):

- `backend/src/main/resources/db/tenant/core/V17__navigation_nodes.sql`
- `backend/src/main/resources/db/tenant/core/V18__navigation_i18n.sql`

## Admin API (tenant-scoped, authenticated)

Controller: [`backend/src/main/java/com/backend/presentation/controller/NavigationController.java`](../../backend/src/main/java/com/backend/presentation/controller/NavigationController.java)

Base path: `/api/navigation`

### Nodes

- `GET /api/navigation/nodes` (root nodes, paginated + sort + search)
- `GET /api/navigation/nodes/{id}`
- `POST /api/navigation/nodes` (create root)
- `POST /api/navigation/nodes/{id}/children` (create child)
- `PUT /api/navigation/nodes/{id}`
- `DELETE /api/navigation/nodes/{id}`
- `PUT /api/navigation/nodes/{id}/reorder`

i18n:

- `GET /api/navigation/nodes/{id}/i18n/{language}`
- `PUT /api/navigation/nodes/{id}/i18n/{language}`

Composite:

- `GET /api/navigation/nodes/{id}/composite`
- `POST /api/navigation/nodes/composite`
- `PUT /api/navigation/nodes/{id}/composite`

### Entries

- `POST /api/navigation/entries`
- `PUT /api/navigation/entries/{id}`
- `DELETE /api/navigation/entries/{id}`
- `PUT /api/navigation/nodes/{id}/entries/reorder`

i18n:

- `GET /api/navigation/entries/{id}/i18n/{language}`
- `PUT /api/navigation/entries/{id}/i18n/{language}`

Composite:

- `GET /api/navigation/entries/{id}/composite`
- `POST /api/navigation/entries/composite`
- `PUT /api/navigation/entries/{id}/composite`

## Frontend integration (Admin)

Location: `storefront/src/app/modules/admin/custom/navigation/`

## Security & tenant isolation

- Admin endpoints require `TENANT_ADMIN` (`@PreAuthorize("hasRole('TENANT_ADMIN')")` at controller level).
- Tenant resolution is enforced by [`backend/src/main/java/com/backend/infrastructure/tenant/TenantFilter.java`](../../backend/src/main/java/com/backend/infrastructure/tenant/TenantFilter.java).

## Public delivery

Navigation is exposed via CMS Delivery:

- `GET /api/cms/navigation/{uid}` (see [`cms-delivery.md`](cms-delivery.md))

