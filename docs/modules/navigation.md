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

The admin list view is a **tree view** (not a paginated grid): it uses Angular Material Tree (`MatTree`, `NestedTreeControl`) with lazy-loaded children. Root nodes are loaded via `GET /api/navigation/nodes` (with a single large page size); expanding a node loads its subtree via `GET /api/navigation/nodes/{id}`. The UI mirrors the Product categories tree (`storefront/.../products/categories/`): header with create button, empty state, expand/collapse, and row actions (add child, manage, delete).

## Security & tenant isolation

- Admin endpoints require `TENANT_ADMIN` (`@PreAuthorize("hasRole('TENANT_ADMIN')")` at controller level).
- Tenant resolution is enforced by [`backend/src/main/java/com/backend/infrastructure/tenant/TenantFilter.java`](../../backend/src/main/java/com/backend/infrastructure/tenant/TenantFilter.java).

## Public delivery

Navigation is exposed via CMS Delivery:

- `GET /api/cms/navigation/{uid}` (see [`cms-delivery.md`](cms-delivery.md))

## Storefront shared chrome contract

`storefront-nextjs` resolves header and footer navigation through shared CMS slots instead of special `HeaderComponent` / `FooterComponent` delivery types.

### Seeded node UIDs used by the storefront

| UID | Used by | Notes |
| --- | --- | --- |
| `LandingMainNavNode` | `StorefrontHeaderMainNavigation` | Main off-canvas header menu (`navigation_type = MAINMENU`) |
| `LandingFooterNavNode` | `StorefrontFooterSitemapNavigation` | Footer sitemap column (`navigation_type = STATICPAGE`) |

### Rendering notes

- `NavigationComponent` with `navigationType = MAINMENU` is rendered hierarchically in the header off-canvas.
- `NavigationComponent` with `navigationType = STATICPAGE` is flattened for the footer sitemap.
- Internal `URL` entries are locale-prefixed in the storefront renderer; external links remain untouched.
- Header social links, contact links, footer office links, newsletter copy, and footer social links are not modeled as navigation nodes. They come from standard CMS component entries in the shared `Header` / `Footer` slots.
