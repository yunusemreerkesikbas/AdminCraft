# Page Builder

## Purpose

The Page Builder module manages CMS pages with multi-language content and supports template/slot based composition.

## Database

Tenant migrations:

- `backend/src/main/resources/db/tenant/pagebuilder/`
  - `V12__page_slots.sql`
  - `V15__page_templates.sql`
  - `V16__remove_page_categories.sql`
  - (and other evolutions under the same folder)

## Admin API (tenant-scoped, authenticated)

### Pages

Controller: [`backend/src/main/java/com/backend/presentation/controller/PageController.java`](../../backend/src/main/java/com/backend/presentation/controller/PageController.java)

Base path: `/api/pages`

- `GET /api/pages` (list)
- `POST /api/pages` (create)
- `GET /api/pages/{id}` (supports `?include=translations`)
- `PUT /api/pages/{id}`
- `DELETE /api/pages/{id}`

i18n:

- `GET /api/pages/{pageId}/i18n/{language}`
- `PUT /api/pages/{pageId}/i18n/{language}`
- `POST /api/pages/{pageId}/publish/{language}`

Composite operations:

- `POST /api/pages/composite`
- `GET /api/pages/{id}/composite`
- `PUT /api/pages/{id}/composite`

These endpoints are the **atomic-write** path for base + translations in one request.

### Page templates

Controller: [`backend/src/main/java/com/backend/presentation/controller/PageTemplateController.java`](../../backend/src/main/java/com/backend/presentation/controller/PageTemplateController.java)

Base path: `/api/page-templates`

- `GET /api/page-templates` (paginated + sort + search)
- `GET /api/page-templates/active`
- `GET /api/page-templates/{id}`
- `POST /api/page-templates`
- `PUT /api/page-templates/{id}`
- `DELETE /api/page-templates/{id}`
- `POST /api/page-templates/{id}/slots`
- `DELETE /api/page-templates/{id}/slots/{slotName}`
- `PUT /api/page-templates/{id}/slots/reorder`
- `POST /api/page-templates/{id}/assign/{pageId}`
- i18n:
  - `GET /api/page-templates/{id}/i18n/{language}`
  - `PUT /api/page-templates/{id}/i18n/{language}`

## Frontend integration (Admin)

Locations (confirmed):

- Pages UI: `storefront/src/app/modules/admin/custom/pages/`
- Templates UI: `storefront/src/app/modules/admin/custom/templates/`

## Security & tenant isolation

- Admin endpoints require `TENANT_ADMIN` (`@PreAuthorize("hasRole('TENANT_ADMIN')")` on `PageController` and `PageTemplateController`).
- Tenant resolution is enforced by [`backend/src/main/java/com/backend/infrastructure/tenant/TenantFilter.java`](../../backend/src/main/java/com/backend/infrastructure/tenant/TenantFilter.java).

## Implementation guide

### Add a new template slot rule

1. Update backend validation and DTOs in the application layer.
2. Expose it via `PageTemplateController` and ensure the sort/search contracts remain stable.
3. Update frontend types in `storefront/src/app/modules/admin/custom/templates/page-template.types.ts`.

