# SmartEdit (Admin WYSIWYG Editor)

## Purpose

SmartEdit is the admin-side WYSIWYG editor for CMS pages. It loads the headless Next.js storefront in an iframe, overlays the admin selection chrome on top, and reuses the existing page/component admin dialogs (`PageEditDialogComponent`, `ComponentEditDialogComponent`) for in-context editing.

The storefront iframe renders **DRAFT-only** content while a valid SmartEdit preview ticket is attached to its requests. Live storefront traffic continues to receive PUBLISHED content as before — preview mode and live mode never overlap.

Phase 1 scope:

| In scope | Out of scope |
| --- | --- |
| Component content edit (i18n strings, media, links) | Slot CRUD (managed in `/:lang/templates`) |
| Add/remove/reorder components in a slot | New page / new template creation |
| Page metadata (title, slug, meta, robots) | Workflow / approval / version history |
| Per-language Publish | Scheduled publish, personalization, multi-variant |

## Database

No new tables. SmartEdit consumes the existing CMS schema:

- `pages.status`, `page_i18n.status` — drives DRAFT vs PUBLISHED resolution.
- `components.status`, `component_entries.status` — same.

The HMAC preview secret is stored in configuration (`app.cms.preview.secret`), not in the database.

## Admin API

Base path: `/api/cms/preview` (tenant-scoped, authenticated).

Source of truth: [`backend/src/main/java/com/backend/presentation/controller/CmsPreviewController.java`](../../backend/src/main/java/com/backend/presentation/controller/CmsPreviewController.java).

### `POST /api/cms/preview/tickets`

Mints a short-lived HMAC-SHA256 signed ticket the admin SmartEdit shell embeds in the iframe URL.

- **Auth**: `@PreAuthorize("hasRole('TENANT_ADMIN')")`.
- **Tenant binding**: ticket payload includes the resolved `TenantContext` tenant id; verification fails if the request tenant does not match.
- **Request body** ([`IssuePreviewTicketRequest`](../../backend/src/main/java/com/backend/presentation/dto/request/IssuePreviewTicketRequest.java)):

  ```json
  { "pageId": 12 }
  ```

  `pageId` is optional; when absent the ticket is "any-page" (still tenant-scoped). When provided it is informational — verification still allows DRAFT for any page within the tenant.

- **Response** ([`PreviewTicketResponse`](../../backend/src/main/java/com/backend/presentation/dto/response/PreviewTicketResponse.java)):

  ```json
  {
    "result": "SUCCESS",
    "message": "Preview ticket issued",
    "data": {
      "ticket": "<base64url-payload>.<base64url-signature>",
      "expiresAt": "2026-05-06T11:30:00Z",
      "storefrontBaseUrl": "https://storefront.example.com"
    }
  }
  ```

- **TTL**: `app.cms.preview.ttl-seconds` (default 900 — 15 minutes).
- **Storefront base URL**: `app.cms.preview.storefront-base-url`. Currently a single platform-wide value; for subdomain-per-tenant deployments this must be derived from the tenant.

### Ticket format

Wire format: `<base64url(payload)>.<base64url(signature)>` where the payload is a pipe-delimited UTF-8 string `tenantId|userId|pageId|issuedAtEpoch|expiresAtEpoch`. A `pageId` of `0` means "no page binding". Tokens are intentionally opaque to clients (no JWT dependency). Implementation: [`CmsPreviewTicketService`](../../backend/src/main/java/com/backend/application/cms/preview/CmsPreviewTicketService.java).

## Public delivery APIs

SmartEdit reuses the existing CMS delivery endpoints documented in [`cms-delivery.md`](cms-delivery.md). The only contract change is an opt-in preview switch:

### Preview activation

Either of the following on any `/api/cms/**` request enables preview mode for that request:

- HTTP header **`X-Cms-Preview-Ticket: <token>`** (preferred for API callers).
- Query parameter **`?preview=<token>`** (used by the iframe URL because raw `<iframe src>` cannot set headers).

Filter: [`CmsPreviewFilter`](../../backend/src/main/java/com/backend/presentation/filter/CmsPreviewFilter.java). Scoped to `/cms/**` (excludes `/cms/preview/**` issuance endpoints).

If the header/param is present but the token is invalid (bad signature, expired, cross-tenant), the filter responds with **HTTP 401 — `Invalid CMS preview ticket`**. Requests without the token proceed as live PUBLISHED-only delivery.

### DRAFT-only contract under preview

When preview mode is active:

| Resource | Live (no ticket) | Preview (valid ticket) |
| --- | --- | --- |
| `Page.status` | `PUBLISHED` | `DRAFT` only |
| `PageI18n.status` (per language) | `PUBLISHED` | `DRAFT` only |
| `Component.status` | `PUBLISHED` | `DRAFT` only |
| `ComponentEntry.status` | `PUBLISHED` | `DRAFT` only |

There is **no fallback** to PUBLISHED in preview mode. If a tenant has no DRAFT for the requested language the delivery endpoint returns its standard not-found contract (HTTP 200 with `result: "ERROR"` for `/api/cms/pages`, `Optional.empty` mapping for individual components). This is intentional — content managers see exactly the content they are editing, not whatever was already live.

Status sets are centralised in [`CmsVisibility`](../../backend/src/main/java/com/backend/application/cms/preview/CmsVisibility.java). Per-request preview state lives in [`CmsRequestContext`](../../backend/src/main/java/com/backend/application/cms/preview/CmsRequestContext.java) (ThreadLocal, mirrors `TenantContext`).

### Caching

Preview responses must not be cached by storefront ISR or browser. The Next.js client switches to `cache: "no-store"` whenever a preview ticket is supplied — see `storefront-nextjs/lib/core/http/fetch-json.ts`.

## Frontend integration

### Admin (Angular, `storefront/`)

- Feature module: `storefront/src/app/modules/admin/custom/smartedit/`
  - Shell + iframe + inspector: `smartedit-shell.component.ts/.html/.scss`
  - postMessage gateway: `services/smartedit-gateway.service.ts`
  - Preview ticket client: `services/smartedit-preview.service.ts`
  - Cross-window message types: `smartedit.types.ts`
- Route: `/:lang/smartedit/:pageId` — registered in `storefront/src/app/app.routes.ts` behind `tenantAdminGuard` + `moduleGuard:core`.
- Entry point: "Open in SmartEdit" row action in `storefront/src/app/modules/admin/custom/pages/list/page-list.component.ts`.
- API endpoint constant: `cmsPreviewTickets` in `storefront/src/app/modules/admin/api-endpoints.ts`.
- i18n keys: `admin.smartedit.*` in `storefront/src/app/modules/admin/i18n/langTR.ts` and `langEN.ts`.

### Storefront (`storefront-nextjs/`)

- Selection markup attributes (always rendered, not preview-gated):
  - `CmsSlot.tsx` → `data-cms-slot-id`, `data-cms-slot-position`, `data-cms-slot-shared`
  - `CmsComponent.tsx` → `data-cms-component-id`, `data-cms-component-type`
- Preview ticket forwarding: `storefront-nextjs/lib/core/cms/{client,loaders}.ts` and `storefront-nextjs/lib/core/http/fetch-json.ts` add `X-Cms-Preview-Ticket` header and force `cache: "no-store"` when a ticket is supplied.
- Layout-level injection: `storefront-nextjs/app/[lang]/layout.tsx` mounts `<script src="/smartedit-injector.js" defer>` and sets `data-smartedit-mode="preview"` on `<body>` whenever `searchParams.preview` is present.
- Bridge script: `storefront-nextjs/public/smartedit-injector.js` — listens for clicks on `[data-cms-component-id]` / `[data-cms-slot-id]` and posts `smartedit:select` to the parent window; listens for `smartedit:reload` and reloads.

### Cross-window contract

postMessage envelope:

```ts
{ type: 'smartedit:ready' | 'smartedit:select' | 'smartedit:reload', payload: ... }
```

Direction:

- Iframe → Admin: `smartedit:ready`, `smartedit:select`
- Admin → Iframe: `smartedit:reload`

Origin allow-list is built from `previewBaseUrl` returned in the ticket response — both sides reject messages from other origins.

## Security & tenant isolation

### Filter chain order

`SecurityConfig` registers:

```
JwtAuthenticationFilter  → TenantFilter  → CmsPreviewFilter
```

This ordering matters: `CmsPreviewFilter` reads `TenantContext.getTenantId()` to verify the ticket payload's tenant id. If the order is changed the cross-check becomes meaningless. See [`backend/src/main/java/com/backend/infrastructure/config/SecurityConfig.java`](../../backend/src/main/java/com/backend/infrastructure/config/SecurityConfig.java).

### Tenant binding

`CmsPreviewTicket.matchesTenant(currentTenantId)` is checked inside `CmsPreviewFilter.activate(...)`. A ticket minted for tenant A presented to tenant B's hostname is rejected — the filter responds with 401 when a ticket is supplied but cannot be activated.

### Spring Security routing

```
/cms/preview/**  →  authenticated()  + @PreAuthorize("hasRole('TENANT_ADMIN')")
/cms/**          →  permitAll()      (preview activation handled by CmsPreviewFilter)
```

Both are registered explicitly so the preview-issuance endpoint never falls under the `/cms/**` permit-all rule.

### CORS

`X-Cms-Preview-Ticket` is added to `app.cors.allowed-headers` in `application.yml`. `app.cors.allowed-origins` must list both the admin origin (e.g. `http://localhost:4200`) and the storefront origin (e.g. `http://localhost:3000`) for preflight to pass.

### HMAC secret hardening

[`CmsPreviewProperties`](../../backend/src/main/java/com/backend/application/cms/preview/CmsPreviewProperties.java) has a `@PostConstruct` validator that:

- Refuses to start if `app.cms.preview.secret` is shorter than 32 bytes (HMAC-SHA256 minimum, RFC 2104).
- Refuses to start with the built-in `DEV_ONLY_*` placeholder secret unless an active profile is `dev` or `test` (default profile is also accepted as non-production for local convenience).

Operations must set `CMS_PREVIEW_SECRET` to a randomly-generated ≥32-byte value for stage and prod profiles.

### Multi-tenant notes (current limitations)

- The HMAC secret is platform-wide. A leaked secret allows ticket forgery for any tenant. Per-tenant key derivation is a Phase 2 item.
- The preview-ticket response uses `app.cms.preview.storefront-base-url` (platform config) as the iframe origin whenever it is set. When the platform config is blank the controller falls back to the tenant's `global.canonicalBaseUrl` site setting. Note: `canonicalBaseUrl` is primarily a SEO/sitemap value and may carry a placeholder in dev tenants — that is why the platform config wins by default. Subdomain-per-tenant production deployments either pin a single shared preview host in the platform config, or leave the platform config blank and rely on each tenant's canonical URL.

### Known limitations (Phase 2 candidates)

- **Component uid → numeric id resolution is frontend-cached.** When the inspector opens an `ComponentEditDialogComponent`, the SmartEdit shell maps the iframe-supplied component uid to a numeric `componentId` by walking its in-memory `pageSlotsSig`. The cache is refreshed after every save (`#reloadPageData`), so the staleness window is one user-tab — concurrent edits from another browser tab are not detected. A backend `GET /api/components/by-uid/{uid}` resolver would close this gap and is queued for Phase 2 along with concurrent-editor warnings.

## Implementation guide

### Flow 1: Open a page in SmartEdit

1. Admin user navigates to `/:lang/pages` and clicks the row action **"Open in SmartEdit"** (`page-list.component.ts`).
2. Router navigates to `/:lang/smartedit/:pageId`. `tenantAdminGuard` and `moduleGuard:core` enforce access.
3. `SmartEditShellComponent.ngOnInit`:
   - Reads `:pageId` and `:lang` from the route.
   - Calls `SmartEditPreviewService.issueTicket(pageId)` → backend `POST /api/cms/preview/tickets`.
   - Calls `PageBuilderService.getPageDetail`, `PageSlotService.getSlots`, `ComponentLibraryService.listComponentTypes` in parallel via `forkJoin`.
4. Iframe URL is built as `${storefrontBaseUrl}/${lang}${canonicalUrl}?preview=${encodedTicket}` and bound to a `[src]="iframeUrlSig() | safe"` attribute.
5. Storefront serves the page rendering DRAFT content (the preview filter activates because `?preview=` is valid).
6. The injector script posts `smartedit:ready`. The shell flips its **Connected** indicator on.

### Flow 2: Edit a component inline

1. Content manager clicks a component inside the iframe.
2. `smartedit-injector.js` walks up the DOM, finds the nearest `[data-cms-component-id]`, and posts `{ type: 'smartedit:select', payload: { kind: 'component', id, componentType, rect } }`.
3. `SmartEditGatewayService` receives the message, validates origin against the allow-list, pushes the selection into a Subject. The shell binds it to `selectionSig`.
4. The inspector shows component metadata and an **Edit** button. Clicking it:
   - Resolves the numeric id via `pageSlotsSig` (component uid → page slot binding).
   - Calls `ComponentLibraryService.getComponentDetail(id)`.
   - Opens `ComponentEditDialogComponent` with the existing admin dialog flow (no SmartEdit-specific edit UI).
5. On dialog save the shell calls `SmartEditGatewayService.requestReload()` which posts `smartedit:reload` to the iframe; `smartedit-injector.js` calls `location.reload()`. Because the URL still has `?preview=`, the new render fetches the updated DRAFT.

### Flow 3: Publish current language

1. Sidebar **Publish** button is enabled when the current language has a translation (`canPublishSig`).
2. The shell calls `PageBuilderService.publishPageI18n(pageId, lang)` — same endpoint used elsewhere in the admin.
3. On success the shell shows a localized notification (success message taken from the API response when present), reloads the iframe via `smartedit:reload`, and refreshes its in-memory page/slots state.
4. The newly published `PageI18n` is now visible to live storefront traffic. Preview mode continues to show DRAFT — if there is no DRAFT for that language any more, the iframe returns a not-found response (intentional; the manager publishes and is done).

## Verification

End-to-end check of preview activation (reproduces the SmartEdit data path manually):

```bash
# 1. Mint a ticket as a tenant admin (stand-in JWT setup omitted)
curl -sS -X POST http://localhost:8080/api/cms/preview/tickets \
  -H 'Authorization: Bearer <TENANT_ADMIN_JWT>' \
  -H 'X-Tenant-Subdomain: <subdomain>' \
  -H 'Content-Type: application/json' \
  -d '{}' | jq

# 2. Hit a delivery endpoint with the ticket — DRAFT content visible
curl -sS 'http://localhost:8080/api/cms/pages?lang=TR' \
  -H 'X-Tenant-Subdomain: <subdomain>' \
  -H 'X-Cms-Preview-Ticket: <ticket>'

# 3. Same call without the ticket — PUBLISHED content
curl -sS 'http://localhost:8080/api/cms/pages?lang=TR' \
  -H 'X-Tenant-Subdomain: <subdomain>'

# 4. Tampered ticket → HTTP 401
curl -sS -i 'http://localhost:8080/api/cms/pages?lang=TR&preview=invalid.token' \
  -H 'X-Tenant-Subdomain: <subdomain>' | head -1
```

Multi-tenant cross-check: mint a ticket on tenant A, present it on tenant B's hostname → expect HTTP 401.
