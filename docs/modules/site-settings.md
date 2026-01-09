# Site Settings

## Purpose

Site Settings stores tenant-specific configuration used by both the admin panel and storefront.
It supports a split model:

- **Global settings** (language-agnostic)
- **Per-language settings** (keyed by language)

## Admin API (tenant-scoped, authenticated)

Controller: [`backend/src/main/java/com/backend/presentation/controller/SiteSettingsController.java`](../../backend/src/main/java/com/backend/presentation/controller/SiteSettingsController.java)

Base path: `/api/site-settings`

- `GET /api/site-settings` (admin view)
- `PATCH /api/site-settings` (partial update)
  - body: `{ global: ..., languages: { "tr": ..., "en": ... } }` (see controller request record)

## Frontend integration (Admin)

Location: `storefront/src/app/modules/admin/custom/settings/`

Routes:

- `storefront/src/app/modules/admin/custom/settings/site-settings.routes.ts`

## Security & tenant isolation

The controller enforces tenant admin role per endpoint:

- `@PreAuthorize("hasRole('TENANT_ADMIN')")`

## Implementation guide

### Add a new global setting

1. Update DTOs:
   - `SiteSettingsGlobalDto`
   - Mapper: `SiteSettingsMapper`
2. Update application/service layer:
   - `SiteSettingsService` patch logic must treat the setting as tenant-scoped.
3. Update frontend form(s) under:
   - `storefront/src/app/modules/admin/custom/settings/`

### Add a new per-language setting

1. Update `SiteSettingsI18nDto` and mapping logic.
2. Ensure the frontend sends `languages` map entries only for supported tenant languages.

