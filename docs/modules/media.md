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
- `GET /api/media/uid/{uid}`
- `GET /api/media/{id}/detail` (includes container variants + all translations)
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
- `GET /api/responsive-media/media/{mediaId}/linked-components`

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
- dialogs:
  - `dialogs/media-upload-dialog/`
  - `dialogs/media-detail-dialog/`
  - `dialogs/media-picker-dialog/`

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

