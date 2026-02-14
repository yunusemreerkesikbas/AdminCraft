# Component Library

## Purpose

The Component Library is a tenant module for creating reusable UI components and delivering them through CMS Delivery.
It follows a **type-driven** model with **dynamic entry fields** (SAP Commerce/Hybris-inspired).

## Database

Tenant migrations:

- `backend/src/main/resources/db/tenant/component_library/`
  - `V1__component_library_baseline.sql`
  - `V17__add_component_navigation_bindings.sql` (CategoryNavigationComponent navigation bindings)
  - `R__seed_component_types.sql` (idempotent upsert; does not truncate component tables)
  - `R__seed_entry_field_definitions.sql`

## Admin API (tenant-scoped, authenticated)

### Component Types

Controller: [`backend/src/main/java/com/backend/presentation/controller/ComponentTypeController.java`](../../backend/src/main/java/com/backend/presentation/controller/ComponentTypeController.java)

Base path: `/api/components/types`

- `GET /api/components/types` (paginated + sort + search)
- `POST /api/components/types`
- `GET /api/components/types/{id}`
- `PUT /api/components/types/{id}`
- `DELETE /api/components/types/{id}`

List contract:

- Query params:
  - `page` (0-based, default: `0`)
  - `size` (default: `20`, max: `100`)
  - `sort` (e.g. `createdAt,desc`, `name,asc`, `category,asc`)
  - `search` (optional, min 2 chars, searches by name/uid/category)
- Response: `PageableResponse<ComponentTypeResponse>` (content + `totalPages` + `totalElements` + `sortConfig`)

Example:

- `GET /api/components/types?page=0&size=20&sort=createdAt,desc&search=header`

### Entry field definitions (dynamic form schema)

Controller: [`backend/src/main/java/com/backend/presentation/controller/EntryFieldController.java`](../../backend/src/main/java/com/backend/presentation/controller/EntryFieldController.java)

Base path: `/api/components/types`

- `GET /api/components/types/{typeId}/entry-fields`
- `POST /api/components/types/{typeId}/entry-fields`

Note:

- Frontend endpoint config contains templates for import/export/validate, but the backend currently exposes only **list + create** for entry fields.
- Authorization is `TENANT_ADMIN` or `SUPER_ADMIN`, but requests are still tenant-scoped (a tenant must be resolved by `TenantFilter`).

### Components

Controller: [`backend/src/main/java/com/backend/presentation/controller/ComponentController.java`](../../backend/src/main/java/com/backend/presentation/controller/ComponentController.java)

Base path: `/api/components`

- `GET /api/components` (paginated + sort + search)
- `POST /api/components`
- `GET /api/components/{id}` (supports `?include=translations`)
- `PUT /api/components/{id}`
- `DELETE /api/components/{id}`

List contract:

- Query params:
  - `page` (0-based, default: `0`)
  - `size` (default: `20`)
  - `sort` (e.g. `createdAt,desc`)
  - `search` (optional, min 2 chars)
- Response: `PageableResponse<ComponentListItemResponse>` (content + `totalPages` + `totalElements` + `sortConfig`)

Example:

- `GET /api/components?page=0&size=20&sort=createdAt,desc&search=test`

i18n:

- `GET /api/components/{id}/i18n/{language}`
- `PUT /api/components/{id}/i18n/{language}`
- `POST /api/components/{id}/publish/{language}`

Composite:

- `POST /api/components/composite`
- `GET /api/components/{id}/composite`
- `PUT /api/components/{id}/composite`

Responsive media assignment:

- `PATCH /api/components/{id}/responsive-media?responsiveMediaId={id}`

### CategoryNavigationComponent bindings

When component type is `CategoryNavigationComponent`, component-level navigation binding fields are enabled:

- `navigationNodeId` (required)
- `navigationLinkNodeId` (optional)
- `navigationType` (optional, defaults to `MAINMENU`)
- `searchBox` (optional, defaults to `false`)
- `wrapAfter` (optional, defaults to `0`)

Validation and mapping source-of-truth:

- `backend/src/main/java/com/backend/application/service/ComponentServiceImpl.java`
- `backend/src/main/java/com/backend/presentation/controller/ComponentController.java`
- `backend/src/main/java/com/backend/application/command/ComponentCommands.java`

Delivery behavior:

- CMS delivery includes `navigationType`, `searchBox`, `wrapAfter`, `navigationNode`, `navigationLinkNode`.
- `navigationNode` and `navigationLinkNode` are delivered as full navigation tree objects when IDs are set.
- Source-of-truth:
  - `backend/src/main/java/com/backend/application/service/ComponentDeliveryServiceImpl.java`
  - `backend/src/main/java/com/backend/application/service/PageDeliveryServiceImpl.java`
  - `backend/src/main/java/com/backend/application/service/NavigationServiceImpl.java`

### Component entries

Controller: [`backend/src/main/java/com/backend/presentation/controller/ComponentEntryController.java`](../../backend/src/main/java/com/backend/presentation/controller/ComponentEntryController.java)

Base path: `/api/components`

Entries:

- `POST /api/components/{componentId}/entries`
- `GET /api/components/{componentId}/entries`
- `GET /api/components/entries/{id}` (supports `?include=translations`)
- `PUT /api/components/entries/{id}`
- `DELETE /api/components/entries/{id}`

Composite:

- `POST /api/components/entries/composite`
- `PUT /api/components/entries/{id}/composite`

i18n:

- `GET /api/components/entries/{id}/i18n/{language}`
- `PUT /api/components/entries/{id}/i18n/{language}` (upsert)
- `POST /api/components/entries/{id}/publish/{language}`

## Frontend integration (Admin)

Location: `storefront/src/app/modules/admin/custom/components/`

Key parts:

- Types and schemas: `models/`, `services/component-schema-builder.service.ts`
- CRUD list and edit dialogs:
  - `list/` (paginated list with `BasePaginatedListComponent`)
  - `component-edit-dialog/` (uses `SpaMediaPicker` for responsive media)
- Component Types UI: `types/`
  - `component-types-list.component.ts` (paginated list with sorting/search)
  - `component-type-edit-dialog/`
- Services:
  - `component-library.service.ts` (main CRUD service for components)
  - `component-type.service.ts` (dedicated CRUD service for component types)
  - `component.store.ts` (state store for components)
  - `component-type.store.ts` (state store for component types)

## Security & tenant isolation

- Admin endpoints are tenant-scoped (tenant must be resolved by [`backend/src/main/java/com/backend/infrastructure/tenant/TenantFilter.java`](../../backend/src/main/java/com/backend/infrastructure/tenant/TenantFilter.java)).
- Most endpoints require `TENANT_ADMIN` at controller level.
- Entry field definitions are allowed for `TENANT_ADMIN` or `SUPER_ADMIN` but are still tenant-scoped.

## Implementation guide

### Minimal working flow

1. Create a component type:
   - `POST /api/components/types`
2. Define entry fields for the type:
   - `POST /api/components/types/{typeId}/entry-fields`
3. Create a component with translations (atomic):
   - `POST /api/components/composite`
4. Create an entry with translations (atomic):
   - `POST /api/components/entries/composite`
5. Publish i18n (optional, per language):
   - Component: `POST /api/components/{id}/publish/{language}`
   - Entry: `POST /api/components/entries/{id}/publish/{language}`

### Add a new component type

1. Ensure the type exists in tenant seeds (for default availability) or create it via API:
   - Seed: `backend/src/main/resources/db/tenant/component_library/R__seed_component_types.sql`
   - API: `POST /api/components/types`
2. Define entry fields (dynamic schema):
   - Seed: `backend/src/main/resources/db/tenant/component_library/R__seed_entry_field_definitions.sql`
   - API: `POST /api/components/types/{typeId}/entry-fields`
3. Add i18n labels for dynamic fields (frontend label strategy depends on the schema builder).

### Add a new dynamic entry field

1. Backend:
   - Add a seed row or provide via API (`EntryFieldController`).
   - Validation uses the shared validation framework (see `../global/validation.md`) and component config:
     - `backend/src/main/java/com/backend/application/service/ComponentFieldValidatorConfig.java`
2. Frontend:
   - Ensure schema builder can map the field type to a form control:
     - `storefront/src/app/modules/admin/custom/components/services/component-schema-builder.service.ts`
