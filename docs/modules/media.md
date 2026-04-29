# Media Library (DAM)

## Purpose

The Media module is Craftive’s **Digital Asset Management** system. It supports:

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
- `POST /api/media/bulk-delete` (body `{ "ids": [...] }`, max **100** IDs; **200** + `ApiResponse.SUCCESS` with localized summary in `message` and `data` `{ requestedCount, deletedIds, failedIds, errors[] }` for partial or full success; **422** + `ApiResponse.ERROR` + `message` when **every** ID fails; `TENANT_ADMIN` only, same per-item error mapping as other bulk-delete modules)

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

File serving (tenant-scoped; authentication required for private files):

- `GET /api/media/files/{fileName}` — serves the file bytes. **Public media** (`isPublic = true`) is accessible without authentication. **Private media** (`isPublic = false`) requires a valid JWT; anonymous requests receive `403 Forbidden` (SEC-009). S3-stored files already have `externalUrl` and are never served through this endpoint.

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

The main library view [`list/media-list.component.ts`](../../storefront/src/app/modules/admin/custom/media/list/media-list.component.ts) (non–selection-mode) supports page-scoped multi-select, **Delete selected** via `POST /api/media/bulk-delete`, and shows **`ApiResponse.message`** from the server for bulk success or error responses. `VIEWER` users do not see checkboxes, upload, row actions, or bulk delete. When embedded with **selection mode** (e.g. media picker), bulk/admin selection UI is hidden and only the picker “select” action applies.

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

Media URL resolution depends on `storageProvider` stored on the `Media` entity:

| `storageProvider` | `externalUrl` | `Media.getPublicUrl()` | Storefront delivery |
|---|---|---|---|
| `LOCAL` | `null` | `/api/media/files/{fileName}` | `/cms-media/` proxy in `storefront-nextjs` adds tenant headers before streaming |
| `S3` | `https://media.craftive.io/…` | CDN URL directly | Absolute URL — no proxy, served from Cloudflare CDN |

- Direct browser requests to `GET /api/media/files/{fileName}` can fail with `Tenant identifier required` if tenant headers are not present.
- `storefront-nextjs` solves this for LOCAL media by rewriting CMS media URLs to its own tenant-aware proxy route: `GET /cms-media/{...path}`.
- S3 media bypasses the proxy entirely — `externalUrl` is the Cloudflare CDN URL and is returned as-is.

Linked usages:

- `linked-components` is driven by responsive media assignments.
- Component-level bindings and entry-level bindings are both returned by the backend.
- Same component/entry responsive usages are grouped into one item with `componentLabel`, optional `entryLabel`, and `linkTypes` objects (`code`, `label`) so admin clients do not duplicate grouping or display-label rules.
- Legacy string fields such as `customFields.mediaUid` can still exist, but new CMS media linking should prefer responsive media binding.

### Media UID alignment for seeded content

**Why UIDs mismatch:** The upload endpoint (`POST /api/media`) auto-generates UIDs in the form `cmsitem_<random>` — there is no way to specify a custom UID at upload time. The update endpoint (`PUT /api/media/{id}`) only exposes `isPublic` and `tags`; it does not allow changing the UID. Component entry `custom_data` fields seeded by the theme page scripts reference **semantic UIDs** (e.g. `homepage-hero-bg`, `homepage-project-1`). These do not match the auto-generated values, causing `GET /api/cms/media?uids=homepage-hero-bg&...` to return empty results and images to not appear on the storefront.

**Fix:** Run the relevant theme page script after uploading all assets. For the default Liko theme this is `backend/src/main/resources/impex/theme/liko/homepage.sql` for homepage assets, `backend/src/main/resources/impex/theme/liko/about_page.sql` for About assets, and `backend/src/main/resources/impex/theme/liko/service_page.sql` for Service assets. These scripts update `media.uid` by matching on `original_name` — the only supported way to assign semantic UIDs to uploaded media. See the [ImpEx execution order](./impex.md) for correct sequencing.

### Site logo media fields

`sites.logo_media_uid` and `sites.logo_dark_media_uid` store the media UIDs for the site logo. These are resolved to public URLs by `CmsDeliveryServiceImpl.getSiteForDelivery()` via `MediaService.resolvePublicUrl(uid)`.

`GET /api/site-settings` now also hydrates `global.logoMedia` and `global.logoDarkMedia` summaries for the admin Site Dashboard, so logo pickers no longer need a follow-up media lookup by UID.

**Clearing the logo:** Sending `logoMediaUid: ""` or `null` in a `PATCH /api/site-settings` request previously had no effect because `SiteSettingsServiceImpl.persistLogoUids` skipped null values. This is fixed — null values are now written, allowing the logo to be cleared by omitting or emptying the field in the admin Site Settings form.

## Storage providers & CDN

Craftive supports pluggable storage backends via `StorageAdapter`. The active provider is set by `craftive.storage.provider`.

| Provider | Bean | Active when | File path stored |
|---|---|---|---|
| `local` | `LocalStorageAdapter` | Default (dev, always registered) | Absolute filesystem path: `uploads/{subdomain}/media/{fileName}` |
| `s3` | `S3StorageAdapter` (`@Primary`) | `provider=s3` (stage/prod) | S3 object key: `{subdomain}/media/{fileName}` |

### CDN architecture (stage/prod)

```
Upload  →  Backend  →  DO Spaces FRA1 (origin, CDN enabled)
                              ↓
                    Cloudflare Worker (reverse proxy)
                    - s1-cdn.craftive.io/* → craftive-media-stage.fra1.cdn.digitaloceanspaces.com
                    - media.craftive.io/*  → craftive-media-prod.fra1.cdn.digitaloceanspaces.com
                    + Cloudflare edge cache (Cache-Control: public, max-age=31536000, immutable)
                    + WAF + DDoS protection
                              ↓
               media.craftive.io  /  s1-cdn.craftive.io
```

- DO Spaces CDN is **enabled** (without custom domain) — Worker proxies to the DO CDN endpoint so origin-level caching is also active.
- **Cloudflare Origin Rule (Host Header Override) is Enterprise-only** and cannot be used on free/pro plans. Cloudflare Worker is the alternative: it rewrites the hostname before forwarding the request to DO Spaces CDN, bypassing the Host header restriction.
- DNS records for CDN domains use `AAAA 100::` (dummy IPv6) with Proxy ON — the Worker intercepts all requests before they reach the origin, so no real IP is needed.
- UUID-based file names are immutable → `Cache-Control: public, max-age=31536000, immutable` → near-100% Cloudflare cache hit rate.
- Object key isolation: `{tenantSubdomain}/media/{uuid}.{ext}` (cross-tenant collision impossible).

### Buckets and CDN domains

| Env | Bucket | CDN domain |
|---|---|---|
| Stage | `craftive-media-stage` | `s1-cdn.craftive.io` |
| Prod | `craftive-media-prod` | `media.craftive.io` |

### Local development with MinIO

MinIO is a Docker-based S3-compatible storage server for testing S3 behavior without real DO Spaces credentials.

```powershell
# Start MinIO alongside MySQL
docker compose -f docker-compose.yml -f docker-compose.dev.yml --env-file .env.dev up -d

# MinIO Console → http://localhost:9001  (minioadmin / minioadmin)
# MinIO S3 API  → http://localhost:9000
```

1. Open `http://localhost:9001`, create a bucket named `craftive-media-dev`.
2. Set an access key in MinIO Console (Access Keys → Create).
3. Start the backend with S3 env vars:

```powershell
$env:STORAGE_PROVIDER = "s3"
$env:SPACES_ACCESS_KEY = "your-minio-key"
$env:SPACES_SECRET_KEY = "your-minio-secret"
$env:SPACES_ENDPOINT   = "http://localhost:9000"
$env:SPACES_BUCKET     = "craftive-media-dev"
$env:SPACES_CDN_URL    = "http://localhost:9000/craftive-media-dev"

mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

4. Upload a file via Admin Panel → Media Library.
5. Verify in MinIO Console that the file appears under `craftive-media-dev/{subdomain}/media/`.
6. Check `media.external_url` in DB — should be `http://localhost:9000/craftive-media-dev/…`.

### Infrastructure setup (one-time)

**DigitalOcean Spaces:**
1. Create two buckets in FRA1: `craftive-media-stage`, `craftive-media-prod`
2. Add CORS policy to each: `GET` allowed from `https://*.craftive.io`
3. Create separate Spaces key pairs (Spaces Keys page, **not** Personal Access Tokens):
   - `craftive-stage` → Limited Access → `craftive-media-stage` only (Read & Write)
   - `craftive-prod`  → Limited Access → `craftive-media-prod` only (Read & Write)

**DigitalOcean Spaces CDN:**
4. Enable CDN on each bucket (Settings → CDN → Enable CDN, **no custom subdomain**):
   - `craftive-media-stage` CDN endpoint: `craftive-media-stage.fra1.cdn.digitaloceanspaces.com`
   - `craftive-media-prod`  CDN endpoint: `craftive-media-prod.fra1.cdn.digitaloceanspaces.com`

**Cloudflare DNS (craftive.io zone):**
5. `AAAA s1-cdn → 100::` — Proxy ON (orange cloud)
6. `AAAA media  → 100::` — Proxy ON (orange cloud)

> `100::` is a dummy IPv6 address. Cloudflare Worker intercepts all requests before they reach the origin, so no real IP is needed. Do NOT use CNAME pointing to Spaces endpoint — that caused Host header issues.

**Cloudflare Workers:**
7. Workers & Pages → Create Worker → `craftive-media-stage`:
```javascript
export default {
  async fetch(request) {
    const url = new URL(request.url);
    url.hostname = 'craftive-media-stage.fra1.cdn.digitaloceanspaces.com';
    const response = await fetch(url.toString(), {
      method: request.method,
      headers: request.headers,
    });
    const newHeaders = new Headers(response.headers);
    newHeaders.set('Access-Control-Allow-Origin', '*');
    return new Response(response.body, { status: response.status, headers: newHeaders });
  }
}
```
   Worker → Settings → Triggers → Routes → Add Route: `s1-cdn.craftive.io/*` (zone: `craftive.io`)

8. Create Worker → `craftive-media-prod` (same code, hostname → `craftive-media-prod.fra1.cdn.digitaloceanspaces.com`):
   Worker route: `media.craftive.io/*` (zone: `craftive.io`)

> Worker free tier: 100k req/day. Sufficient for stage. For prod with high traffic, upgrade to Workers Paid ($5/month for 10M req).

**GitHub Secrets:**
9. Add to `.env.stage` → re-encode → update `ENV_STAGE` secret: `SPACES_ACCESS_KEY`, `SPACES_SECRET_KEY`
10. Add to `.env.prod` → re-encode → update `ENV_PROD`  secret: `SPACES_ACCESS_KEY`, `SPACES_SECRET_KEY`

### Verification checklist (post-deploy)

Run these checks after each stage/prod deploy:

```
□ 1. Upload test  — Admin Panel → Media Library → upload an image
      → DO Spaces dashboard: file appears under craftive-media-{env}/{subdomain}/media/
      → DB: media.storage_provider = 'S3'
             media.external_url    = 'https://[s1.]media.craftive.io/...'

□ 2. CDN delivery — open storefront page that uses the uploaded image
      → Browser Network tab: image request goes to media.craftive.io (not /cms-media/)
      → Response header: CF-Cache-Status: HIT (after first request)

□ 3. DELETE test  — delete a media item from Admin Panel
      → DO Spaces dashboard: file no longer exists in bucket
```

## Security & tenant isolation

- Tenant resolution and request categorization is enforced by [`backend/src/main/java/com/backend/infrastructure/tenant/TenantFilter.java`](../../backend/src/main/java/com/backend/infrastructure/tenant/TenantFilter.java).
- Admin endpoints require `TENANT_ADMIN` (`@PreAuthorize("hasRole('TENANT_ADMIN')")` at controller level).
- Public delivery endpoints are unauthenticated but still tenant-scoped (tenant must be resolvable).
- **Private media gating (SEC-009):** `GET /api/media/files/{fileName}` checks the `isPublic` flag on the `Media` entity. Unauthenticated requests to private files return `403 Forbidden`. Private files are intended for authenticated admin use; storefront themes should only reference public media.
- **Upload content-type validation (SEC-111):** `POST /api/media` and `POST /api/media/composite` validate the uploaded file against its declared MIME type using magic-byte detection (`MediaStorageService`). A file whose actual byte signature does not match the declared content-type (e.g. a text file submitted as `image/jpeg`) is rejected with `400 Bad Request`. Allowed types: `image/png`, `image/jpeg`, `image/gif`, `image/webp`, `application/pdf`. SVG is not permitted through this path. `video/mp4` and `audio/mpeg` bypass magic-byte checks (offset-0 signature ambiguous) and are validated by MIME type alone.

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
