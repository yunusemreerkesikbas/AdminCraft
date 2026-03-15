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
- `V20__simplify_component_type_navigation_profile.sql` (consolidated legacy boolean columns → `navigation_profile` enum)
- `V21__replace_navigation_profile_with_is_navigation_aware.sql` (replaces `navigation_profile` enum with single `is_navigation_aware` boolean)
- `R__seed_component_types.sql` (idempotent upsert with `is_navigation_aware`; includes `NavigationComponent` system type)
- `R__seed_entry_field_definitions.sql`

Navigation capability model source of truth:

- `../../backend/src/main/java/com/backend/domain/entity/ComponentType.java` (`navigationAware` boolean field)

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
- `navigationAware` (boolean — replaces the old 6-variant `navigationProfile` enum)

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

Composite note:
- Composite create/update **do not accept `status`**. Status defaults to `DRAFT` and is managed via publish flows (i18n publish endpoints).

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

Composite entry note:
- Entry composite create/update **do not accept `status`**. Status defaults to `DRAFT` and is managed via publish flows.
- `GET /api/components/entries/{id}?include=translations` can hydrate legacy `translations[*].customFields.mediaUid` values into `translations[*].customFields.media`, so the admin UI can preview/edit legacy entry media without a follow-up media metadata request.

Navigation-aware binding behavior is driven by the `isNavigationAware()` flag in:

- `../../backend/src/main/java/com/backend/application/service/ComponentServiceImpl.java`
- `../../backend/src/main/java/com/backend/application/service/ComponentDeliveryServiceImpl.java`
- `../../backend/src/main/java/com/backend/application/service/PageDeliveryServiceImpl.java`

## Public delivery APIs

Component payloads are delivered through CMS delivery endpoints (not through admin controllers):

- `GET /api/cms/pages`
- `GET /api/cms/components/{uid}`

See:

- `cms-delivery.md`

When the component type has `navigationAware = true`, the delivery payload can include:

- `navigationType` (`MAINMENU` or `STATICPAGE`) — drives storefront rendering
- `searchBox` (boolean)
- `navigationNode` (resolved navigation tree)

> `navigationLinkNodeId` is preserved in the DB schema but hidden from the UI and not populated.
> It is reserved for future use.

## Frontend integration

Admin UI location:

- `../../storefront/src/app/modules/admin/custom/components/`

Key files:

- `models/component-library.types.ts` (defines `ComponentTypeDto` with `navigationAware: boolean`)
- `models/component-form.types.ts` (defines `ComponentTypeFormData` with `navigationAware`)
- `types/component-types-list.component.ts`
- `types/component-type-edit-dialog/component-type-edit-dialog.component.ts` (checkbox for `navigationAware`)
- `component-edit-dialog/component-edit-dialog.component.ts` (`isNavigationAware` getter drives field visibility)
- `services/component-schema-builder.service.ts`

## Security & tenant isolation

- Admin APIs are tenant-scoped via `TenantFilter`:
  - `../../backend/src/main/java/com/backend/infrastructure/tenant/TenantFilter.java`
- Controller access is role-protected (`TENANT_ADMIN` for component endpoints).
- Entry field definitions allow `TENANT_ADMIN` or `SUPER_ADMIN`, still tenant-scoped.

## Implementation guide

### 1) Create a new navigation-aware type

1. Create type via `POST /api/components/types` with `navigationAware: true`.
2. Add entry field definitions via `POST /api/components/types/{typeId}/entry-fields`.
3. Use the type in component create/edit flows.

### 2) Create a navigation-aware component

1. Select a type whose `navigationAware` is `true`.
2. Save component via composite endpoint (`POST /api/components/composite`).
3. Provide navigation fields (all optional):
   - `navigationNodeId` — links to a `NavigationNode`
   - `navigationType` — `MAINMENU` or `STATICPAGE` (defaults to `MAINMENU`)
   - `searchBox` — boolean (defaults to `false`)

### 3) Migrate existing tenants safely

1. Apply versioned migration `V21__replace_navigation_profile_with_is_navigation_aware.sql`.
2. Verify `flyway_component_library_history` includes version `21`.
3. Run sync migrations if needed:
   - `POST /api/provisioning/tenants/{tenantId}/sync-migrations`
