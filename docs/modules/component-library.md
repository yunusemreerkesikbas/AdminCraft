# Component Library

## Purpose

The Component Library is a tenant module for creating reusable CMS components and delivering them through public CMS delivery APIs.

It is:

- type-driven (`component_types`)
- schema-driven for entry forms (`entry_field_definitions`)
- translation-aware (component and entry i18n tables)

## Database

Tenant migration location:

- `backend/src/main/resources/db/tenant/component_library/`

Key migrations and seeds:

- `V17__add_component_navigation_bindings.sql` (component-level navigation binding fields)
- `V19__add_component_type_navigation_capabilities.sql` (legacy boolean capability columns)
- `V20__simplify_component_type_navigation_profile.sql` (single `navigation_profile` model; drops legacy columns)
- `R__seed_component_types.sql` (idempotent upsert with `navigation_profile`)
- `R__seed_entry_field_definitions.sql`

Navigation capability model source of truth:

- `../../backend/src/main/java/com/backend/domain/enums/ComponentNavigationProfile.java`
- `../../backend/src/main/java/com/backend/domain/entity/ComponentType.java`

## Admin API (tenant-scoped, authenticated)

Component type controller:

- `../../backend/src/main/java/com/backend/presentation/controller/ComponentTypeController.java`
- Base path: `/api/components/types`

Endpoints:

- `GET /api/components/types` (paginated + sort + search)
- `POST /api/components/types`
- `GET /api/components/types/{id}`
- `PUT /api/components/types/{id}`
- `DELETE /api/components/types/{id}`

Component type contract (relevant fields):

- `name`
- `category`
- `navigationProfile` (`NONE`, `NODE`, `NODE_REQUIRED`, `NODE_WITH_LINK`, `NODE_WITH_TYPE`, `CATEGORY`)

Request/response DTOs:

- `../../backend/src/main/java/com/backend/presentation/dto/request/ComponentTypeCreateRequest.java`
- `../../backend/src/main/java/com/backend/presentation/dto/response/ComponentTypeResponse.java`

Component controller:

- `../../backend/src/main/java/com/backend/presentation/controller/ComponentController.java`
- Base path: `/api/components`

Endpoints:

- `GET /api/components` (paginated + sort + search)
- `POST /api/components`
- `GET /api/components/{id}` (supports `?include=translations`)
- `PUT /api/components/{id}`
- `DELETE /api/components/{id}`
- `POST /api/components/composite`
- `GET /api/components/{id}/composite`
- `PUT /api/components/{id}/composite`
- `PATCH /api/components/{id}/responsive-media?responsiveMediaId={id}`

Entry field definition controller:

- `../../backend/src/main/java/com/backend/presentation/controller/EntryFieldController.java`
- Base path: `/api/components/types/{typeId}/entry-fields`
- `GET /api/components/types/{typeId}/entry-fields`
- `POST /api/components/types/{typeId}/entry-fields`

Component entries controller:

- `../../backend/src/main/java/com/backend/presentation/controller/ComponentEntryController.java`
- Base path: `/api/components`
- `POST /api/components/{componentId}/entries`
- `GET /api/components/{componentId}/entries`
- `GET /api/components/entries/{id}` (supports `?include=translations`)
- `PUT /api/components/entries/{id}`
- `DELETE /api/components/entries/{id}`
- `POST /api/components/entries/composite`
- `PUT /api/components/entries/{id}/composite`

Navigation-aware binding behavior is profile-driven in:

- `../../backend/src/main/java/com/backend/application/service/ComponentServiceImpl.java`
- `../../backend/src/main/java/com/backend/application/service/ComponentDeliveryServiceImpl.java`
- `../../backend/src/main/java/com/backend/application/service/PageDeliveryServiceImpl.java`

## Public delivery APIs

Component payloads are delivered through CMS delivery endpoints (not through admin controllers):

- `GET /api/cms/pages`
- `GET /api/cms/components/{uid}`

See:

- `cms-delivery.md`

When the selected component type profile supports navigation fields, delivery payload can include:

- `navigationType`
- `searchBox`
- `navigationNode`
- `navigationLinkNode`

## Frontend integration

Admin UI location:

- `../../storefront/src/app/modules/admin/custom/components/`

Key files:

- `models/component-library.types.ts` (includes `ComponentNavigationProfile`)
- `types/component-types-list.component.ts`
- `types/component-type-edit-dialog/component-type-edit-dialog.component.ts`
- `component-edit-dialog/component-edit-dialog.component.ts` (derives visibility/requirements from profile)
- `services/component-schema-builder.service.ts`

## Security & tenant isolation

- Admin APIs are tenant-scoped via `TenantFilter`:
  - `../../backend/src/main/java/com/backend/infrastructure/tenant/TenantFilter.java`
- Controller access is role-protected (`TENANT_ADMIN` for component endpoints).
- Entry field definitions allow `TENANT_ADMIN` or `SUPER_ADMIN`, still tenant-scoped.

## Implementation guide

### 1) Create a new type with navigation behavior

1. Create type with `navigationProfile` using `POST /api/components/types`.
2. Add entry field definitions via `POST /api/components/types/{typeId}/entry-fields`.
3. Use the type in component create/edit flows.

### 2) Create a navigation-aware component

1. Select a type whose `navigationProfile` is not `NONE`.
2. Save component via composite endpoint (`POST /api/components/composite`).
3. Provide only fields supported by that profile:
   - `NODE`: `navigationNodeId`
   - `NODE_REQUIRED`: `navigationNodeId` required
   - `NODE_WITH_LINK`: `navigationNodeId`, optional `navigationLinkNodeId`
   - `NODE_WITH_TYPE`: `navigationNodeId`, optional `navigationType`
   - `CATEGORY`: `navigationNodeId` required, optional `navigationType`, optional `searchBox`

### 3) Migrate existing tenants safely

1. Apply versioned migration `V20__simplify_component_type_navigation_profile.sql`.
2. Verify `flyway_component_library_history` includes version `20`.
3. Run sync migrations if needed:
   - `POST /api/provisioning/tenants/{tenantId}/sync-migrations`
