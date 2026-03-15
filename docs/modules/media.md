# Media Library (DAM)

## Purpose

The Media module is AdminCraft’s **Digital Asset Management** system. It supports:

- Uploading files (single and composite)
- Localized metadata (alt/title/description)
- Variants (generated formats: thumbnail/small/custom WxH)
- Focal point for smart cropping
- Public delivery for storefront usage (tenant-scoped, unauthenticated)

## Database

Tenant migrations:

- `backend/src/main/resources/db/tenant/media/`
  - `V20__media_baseline.sql`
  - `R__seed_media_formats.sql`

## Admin API (tenant-scoped, authenticated)

Controller: [`backend/src/main/java/com/backend/presentation/controller/MediaController.java`](../../backend/src/main/java/com/backend/presentation/controller/MediaController.java)

Base path: `/api/media`

- `GET /api/media` (paginated, search + sort)
- `POST /api/media` (multipart upload)
- `POST /api/media/composite` (multipart upload + translations JSON)
- `GET /api/media/{id}`
- `GET /api/media/{id}/detail` (includes container variants + all translations)
- `POST /api/media/{id}/bind` (bind uploaded media to a component or component entry)
- `PUT /api/media/{id}` (metadata patch/update)
- `DELETE /api/media/{id}`

Note:

- Upload is `POST /api/media` (there is no `/api/media/upload` endpoint on the backend).

i18n:

- `GET /api/media/{id}/i18n/{language}`
- `PUT /api/media/{id}/i18n/{language}` (upsert)
- `DELETE /api/media/{id}/i18n/{language}`

Formats and tools:

- `POST /api/media/{id}/generate-format`
- `POST /api/media/{id}/generate-formats`
- `DELETE /api/media/{mediaId}/variants/{variantId}`
- `PUT /api/media/{id}/focal-point`

Responsive sets (desktop/mobile pair):

Controller: `backend/src/main/java/com/backend/presentation/controller/ResponsiveMediaController.java`

- `POST /api/responsive-media`
- `GET /api/responsive-media/{id}`
- `GET /api/responsive-media/uid/{uid}`
- `PUT /api/responsive-media/{id}`
- `DELETE /api/responsive-media/{id}`
- `GET /api/responsive-media/media/{mediaId}/linked-components` (returns detailed component/entry usages)

Note:

- The backend uses `/uid/{uid}` (not `/code/{code}`).

File serving (public endpoint, still tenant-scoped by tenant resolution):

- `GET /api/media/files/{fileName}`

## Public CMS delivery (tenant-scoped, no auth)

Controller: [`backend/src/main/java/com/backend/presentation/controller/CmsMediaDeliveryController.java`](../../backend/src/main/java/com/backend/presentation/controller/CmsMediaDeliveryController.java)

Base path: `/api/cms/media`

- `GET /api/cms/media/{uid}` (optional `?format=thumbnail`)
- `GET /api/cms/media?uids=uid1&uids=uid2` (batch, max 50)

Rate limit: **100 req/min per tenant** (enforced per tenant id in `TenantContext`).

Notes:

- Media delivery uses `Accept-Language` (no `lang` query param on `CmsMediaDeliveryController`).
- Batch `uids` may be sent as repeated query params or comma-separated values (Spring binds both to `List<String>`).

## Frontend integration (Admin)

Location: `storefront/src/app/modules/admin/custom/media/`

Key files:

- `media.service.ts`, `media.store.ts`, `media.types.ts`
- Components:
  - `SpaMediaPickerComponent`: The main component for selecting media.
    - Supports **Single Selection**: `<spa-media-picker formControlName="media">`
    - Supports **Multiple Selection**: `<spa-media-picker formControlName="gallery" [multiple]="true">`
    - Supports **Responsive Mode** (Desktop/Mobile): `<spa-media-picker formControlName="responsiveMedia" [responsive]="true">`
- Dialogs:
  - `dialogs/media-upload-dialog/`
  - `dialogs/media-upload-result-dialog/`
  - `dialogs/media-detail-dialog/`
  - `dialogs/media-picker-dialog/` (Used by `SpaMediaPickerComponent` for selecting images)
  - `dialogs/media-bind-dialog/`

Recommended admin flow:

1. Bulk upload one or more assets from Media Library.
2. Use the upload result dialog or media detail dialog to bind each asset.
3. Bind to the component itself when the whole component shares the same image/video.
4. Bind to a component entry when each card/banner item needs its own asset.

Storefront delivery note:

- Public media files are still tenant-scoped in this project.
- Direct browser requests to `GET /api/media/files/{fileName}` can fail with `Tenant identifier required` if tenant headers are not present.
- `storefront-nextjs` solves this by rewriting CMS media URLs to its own tenant-aware proxy route: `GET /cms-media/{...path}`.
- The proxy forwards `X-Tenant-Subdomain` or `X-Tenant-ID` before streaming the file to Next/Image.

Linked usages:

- `linked-components` is driven by responsive media assignments.
- Component-level bindings and entry-level bindings are both returned by the backend.
- Legacy string fields such as `customFields.mediaUid` can still exist, but new CMS media linking should prefer responsive media binding.

### Media UID alignment for seeded content

**Why UIDs mismatch:** The upload endpoint (`POST /api/media`) auto-generates UIDs in the form `cmsitem_<random>` — there is no way to specify a custom UID at upload time. The update endpoint (`PUT /api/media/{id}`) only exposes `isPublic` and `tags`; it does not allow changing the UID. Component entry `custom_data` fields seeded by `seed_liko_components.sql` reference **semantic UIDs** (e.g. `homepage-hero-bg`, `homepage-project-1`). These do not match the auto-generated values, causing `GET /api/cms/media?uids=homepage-hero-bg&...` to return empty results and images to not appear on the storefront.

**Fix:** Run `backend/src/main/resources/impex/seed_liko_media_uids.sql` after uploading all assets. It updates `media.uid` by matching on `original_name` — the only supported way to assign semantic UIDs to uploaded media. See the [ImpEx execution order](./impex.md) for correct sequencing.

### Site logo media fields

`sites.logo_media_uid` and `sites.logo_dark_media_uid` store the media UIDs for the site logo. These are resolved to public URLs by `CmsDeliveryServiceImpl.getSiteForDelivery()` via `MediaService.resolvePublicUrl(uid)`.

`GET /api/site-settings` now also hydrates `global.logoMedia` and `global.logoDarkMedia` summaries for the admin Site Dashboard, so logo pickers no longer need a follow-up media lookup by UID.

**Clearing the logo:** Sending `logoMediaUid: ""` or `null` in a `PATCH /api/site-settings` request previously had no effect because `SiteSettingsServiceImpl.persistLogoUids` skipped null values. This is fixed — null values are now written, allowing the logo to be cleared by omitting or emptying the field in the admin Site Settings form.

## Security & tenant isolation

- Tenant resolution and request categorization is enforced by [`backend/src/main/java/com/backend/infrastructure/tenant/TenantFilter.java`](../../backend/src/main/java/com/backend/infrastructure/tenant/TenantFilter.java).
- Admin endpoints require `TENANT_ADMIN` (`@PreAuthorize("hasRole('TENANT_ADMIN')")` at controller level).
- Public delivery endpoints are unauthenticated but still tenant-scoped (tenant must be resolvable).

## Implementation guide

### Add a new media format preset

1. Add/adjust seed rows in:
   - `backend/src/main/resources/db/tenant/media/R__seed_media_formats.sql`
2. Apply migrations to existing tenants (if needed):
   - `POST /api/provisioning/tenants/{tenantId}/sync-migrations`
3. Ensure frontend UI treats the new format code as a selectable preset (if applicable).

### Add a new media processing capability

- Add backend capability in the application service layer (processing/service).
- Expose it via `MediaController` (admin) and/or `CmsMediaDeliveryController` (public) only if it is safe for public use.
- Update docs and types in `storefront/src/app/modules/admin/custom/media/media.types.ts`.
