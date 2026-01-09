# Component Library

## Purpose

The Component Library is a tenant module for creating reusable UI components and delivering them through CMS Delivery.
It follows a **type-driven** model with **dynamic entry fields** (SAP Commerce/Hybris-inspired).

## Database

Tenant migrations:

- `backend/src/main/resources/db/tenant/component_library/`
  - `V1__component_library_baseline.sql`
  - `R__seed_component_types.sql`
  - `R__seed_entry_field_definitions.sql`

## Admin API (tenant-scoped, authenticated)

### Component Types

Controller: [`backend/src/main/java/com/backend/presentation/controller/ComponentTypeController.java`](../../backend/src/main/java/com/backend/presentation/controller/ComponentTypeController.java)

Base path: `/api/components/types`

- `GET /api/components/types`
- `POST /api/components/types`
- `GET /api/components/types/{id}`
- `PUT /api/components/types/{id}`
- `DELETE /api/components/types/{id}`

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

- `GET /api/components`
- `POST /api/components`
- `GET /api/components/{id}` (supports `?include=translations`)
- `PUT /api/components/{id}`
- `DELETE /api/components/{id}`

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
  - `list/`
  - `component-edit-dialog/`
- Type manager UI: `types/`

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

