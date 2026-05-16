# Page Builder

## Purpose

The Page Builder module manages CMS pages with multi-language content and supports template/slot based composition.

## Database

Tenant migrations:

- `backend/src/main/resources/db/tenant/pagebuilder/`
  - `V12__page_slots.sql`
  - `V15__page_templates.sql`
  - `V16__remove_page_categories.sql`
  - `V33__backfill_page_slots_from_templates.sql` (template/page slot alignment backfill)
  - `R__zz_seed_page_slots.sql` (active repeatable slot seed; runs after template/page seeds)
  - `R__seed_page_slots.sql` is kept as deprecated no-op to avoid ordering issues in fresh tenants
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
- `GET /api/pages/{id}/slots`
- `POST /api/pages/{id}/slots`
- `PUT /api/pages/{id}/slots/{slotName}`
- `DELETE /api/pages/{id}/slots/{slotName}`
- `POST /api/pages/{id}/slots/{slotName}/components`
- `DELETE /api/pages/{id}/slots/{slotName}/components/{componentId}`
- `PUT /api/pages/{id}/slots/{slotName}/reorder` (`ReorderRequest.items`)
- `GET /api/pages/shared/slots`
- `POST /api/pages/shared/slots`

i18n:

- `GET /api/pages/{pageId}/i18n/{language}`
- `PUT /api/pages/{pageId}/i18n/{language}`
- `POST /api/pages/{pageId}/publish/{language}`

Publish request note:

- `PagePublishRequest` body is optional and typically contains only `scheduledAt` for scheduled publish. Tenant scope is resolved from `TenantContext` by `TenantFilter`, not from the request body.
- SmartEdit publish applies component draft overrides from `cms_draft_overrides` **scoped to the published page** (page-specific slots, visible slot components) to published component/component_i18n rows before clearing those overrides. Shared header/footer slots are not in that graph today (see SmartEdit docs).

Composite operations:

- `POST /api/pages/composite`
- `GET /api/pages/{id}/composite`
- `PUT /api/pages/{id}/composite`

These endpoints are the **atomic-write** path for base + translations in one request.

Published edit note:

- Normal page/component admin update endpoints can still update their live records directly.
- SmartEdit component editing uses a separate draft override endpoint so published storefront content does not change until the page publish action runs.

Slots contract (`GET /api/pages/{id}/slots`):

- `GET /api/pages/{id}` returns page metadata/translation info; slot tree is read from the dedicated `/slots` endpoint.
- If the page has a template, returned slots are **template-driven effective slots**.
- Slot list/order/position are sourced from `template_slots`.
- Component source priority for each template slot:
  - page-specific slot (`page_slots.page_id = {id}`)
  - shared slot fallback (`page_slots.page_id IS NULL AND is_shared = true`)
- This matches Hybris-style behavior where `PageTemplate` defines the content-slot skeleton.
- When component operations target a template/shared fallback slot that is not yet materialized in `page_slots`,
  backend auto-materializes a page-specific slot first, then applies the component mutation.
- Template-managed slot structure cannot be changed from page slot endpoints (create/update/delete); use template slot APIs.

### Page templates

Controller: [`backend/src/main/java/com/backend/presentation/controller/PageTemplateController.java`](../../backend/src/main/java/com/backend/presentation/controller/PageTemplateController.java)

Base path: `/api/page-templates`

- `GET /api/page-templates` (paginated + sort + search)
- `GET /api/page-templates/active`
- `GET /api/page-templates/{id}`
- `POST /api/page-templates`
- `PUT /api/page-templates/{id}`
- `DELETE /api/page-templates/{id}`
- `POST /api/page-templates/bulk-delete` (same semantics as component bulk delete: max **100** IDs, **200** + partial/full report and localized `ApiResponse.message`, **422** when all IDs fail)
- `POST /api/page-templates/{id}/slots`
- `DELETE /api/page-templates/{id}/slots/{slotName}`
- `PUT /api/page-templates/{id}/slots/reorder`
- `POST /api/page-templates/{id}/assign/{pageId}`
- i18n:
  - `GET /api/page-templates/{id}/i18n/{language}`
  - `PUT /api/page-templates/{id}/i18n/{language}`

Template-to-page propagation:

- Adding a template slot creates the missing page slots for all pages assigned to that template.
- Removing a template slot removes the corresponding page slots from assigned pages.
- Reordering template slots propagates sort order to existing page slots of assigned pages.
- Assigning/changing a page template synchronizes page slot structure with template slots.

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
