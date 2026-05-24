# Site Settings

> **Status**: ✅ Current (integrated into Site Dashboard, not a provisioning module)
>
> Site Settings functionality is provided through the [Site Dashboard](site-dashboard.md).
> This capability is part of `core` and is not listed as a separate provisioning module.

## Purpose

Site Settings stores tenant-specific configuration used by both the admin panel and storefront.
It supports a split model:

- **Global settings** (language-agnostic)
- **Per-language settings** (keyed by language)

Security scope note:

- Tenant `twoFactorPolicy` is managed from Site Dashboard Security tab via a **two-step email verification** flow (`POST /api/sites/security/two-factor/request-change` then `confirm-change`). Direct `PATCH /api/sites/security` with `twoFactorPolicy` is rejected.
- This remains separate from Config Control Panel global runtime email overrides.

## Admin API (tenant-scoped, authenticated)

Controller: [`backend/src/main/java/com/backend/presentation/controller/SiteSettingsController.java`](../../backend/src/main/java/com/backend/presentation/controller/SiteSettingsController.java)

Base path: `/api/site-settings`

- `GET /api/site-settings` (admin view)
  - response includes `global.logoMedia` / `global.logoDarkMedia` summaries for preloading logo pickers in admin UI
- `PATCH /api/site-settings` (partial update)
  - body: `{ global: ..., languages: { "tr": ..., "en": ... } }` (see controller request record)

## Frontend integration (Admin)

Site Settings forms are integrated into Site Dashboard tabs. Save button is placed at top-right for consistent UX.

Location: `storefront/src/app/modules/admin/custom/settings/`

Routes:

- `storefront/src/app/modules/admin/custom/settings/site-settings.routes.ts`

Frontend notifications for site settings should prefer the backend `ApiResponse.message` value (success/error) when available.

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
