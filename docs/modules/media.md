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

Storage migration (available only when `admincraft.storage.provider=s3`):

- `POST /api/media/migration/start` — Triggers async LOCAL → S3 migration for all media in the current tenant. Returns `202 Accepted`. If a migration is already RUNNING, the second call is silently ignored.
- `GET /api/media/migration/status` — Returns current migration progress: `{ tenantSubdomain, total, migrated, failed, failedFileNames, state }` where `state` is one of `IDLE | RUNNING | COMPLETED | PARTIAL_FAILURE`.

Both migration endpoints require `TENANT_ADMIN`.

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
- Legacy string fields such as `customFields.mediaUid` can still exist, but new CMS media linking should prefer responsive media binding.

### Media UID alignment for seeded content

**Why UIDs mismatch:** The upload endpoint (`POST /api/media`) auto-generates UIDs in the form `cmsitem_<random>` — there is no way to specify a custom UID at upload time. The update endpoint (`PUT /api/media/{id}`) only exposes `isPublic` and `tags`; it does not allow changing the UID. Component entry `custom_data` fields seeded by `seed_liko_components.sql` reference **semantic UIDs** (e.g. `homepage-hero-bg`, `homepage-project-1`). These do not match the auto-generated values, causing `GET /api/cms/media?uids=homepage-hero-bg&...` to return empty results and images to not appear on the storefront.

**Fix:** Run `backend/src/main/resources/impex/seed_liko_media_uids.sql` after uploading all assets. It updates `media.uid` by matching on `original_name` — the only supported way to assign semantic UIDs to uploaded media. See the [ImpEx execution order](./impex.md) for correct sequencing.

### Site logo media fields

`sites.logo_media_uid` and `sites.logo_dark_media_uid` store the media UIDs for the site logo. These are resolved to public URLs by `CmsDeliveryServiceImpl.getSiteForDelivery()` via `MediaService.resolvePublicUrl(uid)`.

`GET /api/site-settings` now also hydrates `global.logoMedia` and `global.logoDarkMedia` summaries for the admin Site Dashboard, so logo pickers no longer need a follow-up media lookup by UID.

**Clearing the logo:** Sending `logoMediaUid: ""` or `null` in a `PATCH /api/site-settings` request previously had no effect because `SiteSettingsServiceImpl.persistLogoUids` skipped null values. This is fixed — null values are now written, allowing the logo to be cleared by omitting or emptying the field in the admin Site Settings form.

## Storage providers & CDN

AdminCraft supports pluggable storage backends via `StorageAdapter`. The active provider is set by `admincraft.storage.provider`.

| Provider | Bean | Active when | File path stored |
|---|---|---|---|
| `local` | `LocalStorageAdapter` | Default (dev, always registered) | Absolute filesystem path: `uploads/{subdomain}/media/{fileName}` |
| `s3` | `S3StorageAdapter` (`@Primary`) | `provider=s3` (stage/prod) | S3 object key: `{subdomain}/media/{fileName}` |

### CDN architecture (stage/prod)

```
Upload  →  Backend  →  DigitalOcean Spaces (FRA1, origin)
                              ↓
                       Cloudflare CDN (orange-cloud proxy)
                              ↓
               media.craftive.io  /  s1.media.craftive.io
```

- DO Spaces CDN is **disabled** — Cloudflare CDN covers this at 330 PoP.
- UUID-based file names are immutable → `Cache-Control: public, max-age=31536000, immutable` → near-100% Cloudflare cache hit rate.
- Object key isolation: `{tenantSubdomain}/media/{uuid}.{ext}` (cross-tenant collision impossible).

### Buckets and CDN domains

| Env | Bucket | CDN domain |
|---|---|---|
| Stage | `craftive-media-stage` | `s1.media.craftive.io` |
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

### Migration: LOCAL → S3

One-time per-tenant migration. Idempotent: files already at S3 are skipped.

```
POST /api/media/migration/start    → 202 Accepted (async, runs in background)
GET  /api/media/migration/status   → { state: IDLE|RUNNING|COMPLETED|PARTIAL_FAILURE, total, migrated, failed, failedFileNames }
```

Local files are preserved after migration (`delete-local-after-migration: false` by default). Set to `true` only after validating CDN delivery end-to-end.

### Infrastructure setup (one-time)

**DigitalOcean Spaces:**
1. Create two buckets in FRA1: `craftive-media-stage`, `craftive-media-prod`
2. Add CORS policy to each: `GET` allowed from `https://*.craftive.io`
3. Create separate Spaces key pairs (Spaces Keys page, **not** Personal Access Tokens):
   - `craftive-stage` → Limited Access → `craftive-media-stage` only (Read & Write)
   - `craftive-prod`  → Limited Access → `craftive-media-prod` only (Read & Write)

**Cloudflare DNS (craftive.io zone):**
4. `CNAME media    → craftive-media-prod.fra1.digitaloceanspaces.com`  — Proxy ON (orange cloud)
5. `CNAME s1.media → craftive-media-stage.fra1.digitaloceanspaces.com` — Proxy ON (orange cloud)

**Cloudflare Cache Rule:**
6. Caching → Cache Rules → Create:
   - Match: `Hostname equals media.craftive.io OR s1.media.craftive.io`
   - Cache eligibility: Eligible for cache
   - Edge TTL: Use cache-control header if present (backend sends `max-age=31536000, immutable`)
   - Browser TTL: Respect origin TTL

**GitHub Secrets:**
7. Add to `.env.stage` → re-encode → update `ENV_STAGE` secret: `SPACES_ACCESS_KEY`, `SPACES_SECRET_KEY`
8. Add to `.env.prod`  → re-encode → update `ENV_PROD`  secret: `SPACES_ACCESS_KEY`, `SPACES_SECRET_KEY`

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

□ 3. Migration    — POST /api/media/migration/start  (Bearer token, TENANT_ADMIN)
      → 202 Accepted
      → Poll: GET /api/media/migration/status
              { "state": "COMPLETED", "total": N, "migrated": N, "failed": 0 }

□ 4. Legacy files — open a page that used a previously local image
      → After migration: image now loads from CDN, not /cms-media/ proxy

□ 5. DELETE test  — delete a media item from Admin Panel
      → DO Spaces dashboard: file no longer exists in bucket
```

**Quick curl for migration (replace token and host):**
```bash
# Start migration
curl -X POST https://s1.api.craftive.io/api/media/migration/start \
  -H "Authorization: Bearer <token>"

# Poll status
curl https://s1.api.craftive.io/api/media/migration/status \
  -H "Authorization: Bearer <token>"
```

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
