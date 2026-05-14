# CMS Delivery (Public APIs)

## Purpose

CMS Delivery provides **storefront-friendly** endpoints that return render-ready data without requiring authentication.
Requests are still **tenant-scoped** via tenant resolution (subdomain/header/hostname).

## Controllers

- CMS content delivery: [`backend/src/main/java/com/backend/presentation/controller/CmsDeliveryController.java`](../../backend/src/main/java/com/backend/presentation/controller/CmsDeliveryController.java)
- CMS media delivery: [`backend/src/main/java/com/backend/presentation/controller/CmsMediaDeliveryController.java`](../../backend/src/main/java/com/backend/presentation/controller/CmsMediaDeliveryController.java)
- CMS product + category delivery: [`backend/src/main/java/com/backend/presentation/controller/ProductCmsDeliveryController.java`](../../backend/src/main/java/com/backend/presentation/controller/ProductCmsDeliveryController.java)
- Public tenant contact: [`backend/src/main/java/com/backend/presentation/controller/TenantPublicContactRequestController.java`](../../backend/src/main/java/com/backend/presentation/controller/TenantPublicContactRequestController.java)

## Endpoints

Base path: `/api/cms`

### Components

- `GET /api/cms/components/{uid}?lang=TR`
- `GET /api/cms/components?uids=uid1&uids=uid2&lang=TR` (max 50)
  - Query format is **repeated params** because the controller uses `@RequestParam List<String> uids`.
  - Example: `/api/cms/components?uids=header&uids=footer&lang=EN`

### Pages

- `GET /api/cms/pages?lang=TR` (homepage)
- `GET /api/cms/pages?pageType=ContentPage&pageLabelOrId=/about&lang=TR`
- `GET /api/cms/pages?pageType=ProductPage&code=123&lang=TR`
- `GET /api/cms/pages?pageType=CategoryPage&code=electronics&lang=TR`
- `GET /api/cms/pages?pageType=SearchResultPage&lang=TR`
- `GET /api/cms/pages?pageType=LandingPage&pageLabelOrId=/campaign&lang=TR`

Public delivery resolution rules:

- Only `PUBLISHED` pages and translations are returned.
- `pageType=ProductPage` and `pageType=CategoryPage` require `code` parameter.
- `pageType=ContentPage` and `pageType=LandingPage` require `pageLabelOrId` parameter.
- `PRODUCT`, `CATEGORY`, `SEARCH` resolve to one published template per tenant (if misconfigured with multiple published templates, the lowest `id` is used).

SmartEdit preview exception:

- A valid `X-Cms-Preview-Ticket` header or `preview` query parameter activates preview mode for `/api/cms/**`.
- Live requests remain `PUBLISHED` only.
- Preview requests resolve `DRAFT` or `PUBLISHED` fallback through `CmsVisibility`, and overlay matching rows from `cms_draft_overrides` for component/component_i18n draft edits.
- Page-bound preview uses `previewPageId`; when the ticket contains a page id, the requested `previewPageId` must match.

### Site

- `GET /api/cms/site`

### Navigation

- `GET /api/cms/navigation/{uid}?lang=TR`
  - Returns the navigation tree rooted at `{uid}`.
  - `entries[].resolvedHref` — locale-prefixed href pre-computed by the backend; `null` for `COMPONENT` type entries.
  - `flatLinks[]` — deduplicated flat list of all links in the subtree, pre-built for `STATICPAGE` renderers. `null` when empty.

### Sitemap pages

- `GET /api/cms/pages/sitemap?lang=TR`
  - Returns all `PUBLISHED` pages eligible for sitemap inclusion as a flat list.
  - Filtered by: `indexingEnabled` flag on `SiteTechnicalSettings`; pages with `RobotTag.NOINDEX_FOLLOW` or `NOINDEX_NOFOLLOW` are excluded.
  - When site record does not exist, returns an empty list.
  - Response shape: `ApiResponse<List<SitemapPageEntry>>` where each entry has `uid`, `typeCode`, `canonicalUrl`, `updatedAt`.
  - No auth required — public delivery endpoint.

### Robots.txt

- `GET /api/cms/robots.txt`
  - Returns `text/plain` content type.
  - Reads the effective robots.txt from `SiteTechnicalService.getRobotsTxt()`.
  - When `indexingEnabled = false` → `Disallow: /`; otherwise → the admin-configured robots.txt (or a default `Allow: /` with `Sitemap: /sitemap.xml`).
  - No auth required — public delivery endpoint.
  - No application-level rate limit; if needed, configure per-IP rate limiting via Traefik middleware.

### Media

Base path: `/api/cms/media`

- `GET /api/cms/media/{uid}` (optional `?format=thumbnail`)
- `GET /api/cms/media?uids=uid1&uids=uid2` (max 50)

### Products and categories

Base path: `/api/cms/products`

- `GET /api/cms/products/{uid}?lang=TR`
- `GET /api/cms/products?uids=uid1&uids=uid2&lang=TR` (max 50)
- `GET /api/cms/products/category/{categoryUid}?page=0&size=20&lang=TR`
- `GET /api/cms/products/search?q=query&page=0&size=20&lang=TR`
- `GET /api/cms/products/categories/{uid}?lang=TR`
- `GET /api/cms/products/categories?lang=TR`

## Language resolution

From `CmsDeliveryController`:

- `lang` query parameter wins when provided (uses `Language` enum values like `TR`, `EN`)
- otherwise `Accept-Language` is mapped to `Language` (ISO codes like `tr`, `en`)
- otherwise a default language is used

Note:

- CMS media delivery (`CmsMediaDeliveryController`) does not accept `lang`; it uses `Accept-Language` only.

## Rate limiting

`CmsDeliveryController` and `CmsMediaDeliveryController` have **no application-level rate limiter**. These are high-traffic public endpoints — a global counter would affect all legitimate users equally. If rate limiting is needed, apply it as a **per-IP Traefik middleware** at the infrastructure level.

The tenant **public contact** endpoint (below) is different: it uses **Resilience4j-backed limits** in the application (`app.security.public-contact-per-ip-per-minute` and `app.security.public-contact-per-tenant-per-minute`).

## Public contact requests

Base path: `/api/public/contact-requests` (still under servlet context `/api`).

- `POST /api/public/contact-requests` — accepts [`PublicContactRequestSubmitRequest`](../../backend/src/main/java/com/backend/presentation/dto/request/PublicContactRequestSubmitRequest.java); persists a `contact_requests` row; optional Google reCAPTCHA when tenant + platform policy require it (`recaptchaToken` may be absent when reCAPTCHA is disabled).
- **Client IP:** Prefer `X-Forwarded-For` / framework forward-header strategy for resolved client IP. Header `CF-Connecting-IP` is used **only** when `app.security.trust-cf-connecting-ip=true` and optional `app.security.trusted-proxy-cidrs` match `getRemoteAddr()` — default is do-not-trust to avoid off-Cloudflare spoofing.
- **Abuse:** Exceeding per-IP or per-tenant limits returns **HTTP 429** with a localized `ApiResponse.message`.
- **Retention:** Old rows can be purged by a scheduled job when `app.security.contact-request-retention-job-enabled=true`; see tenant Flyway under `db/tenant/core` for index/retention migrations.

Admin listing for the same entity lives under authenticated tenant APIs (Site Dashboard / contact module), not in this public delivery doc.

## Response contract (high level)

- Delivery endpoints return `ApiResponse<T>` where `T` is a delivery DTO.
- Batch endpoints still return `ApiResponse<T>`, but `T` depends on the endpoint.
- `GET /api/cms/media?uids=...` currently returns `ApiResponse<List<MediaResponse>>`.
- The batch contract is not a generic `{ uid -> dto }` map wrapper.
- Max batch size is enforced server-side (**50**).

### Page not found behavior

`GET /api/cms/pages` does **not** return HTTP 404 when no matching page exists.
It returns **HTTP 200** with `result: "ERROR"` and a localized not-found message.
The `data` field is **omitted** when null (global `JsonInclude.Include.NON_NULL`):

```json
{ "result": "ERROR", "message": "Page not found" }
```

Frontend clients must check `payload.result === "ERROR"` (not response status) to detect a missing page and handle it as `null` — not as a thrown error. Use `payload.data ?? null` when reading the payload.

### ContentSlot component list field name

`ContentSlotDeliveryResponse.ComponentsWrapper` serializes the component array as **`component`** (not `componentList`), matching the SAP Commerce / Hybris OCC contract:

```json
{
  "contentSlots": {
    "contentSlot": [
      {
        "slotId": "HeroSlot",
        "components": {
          "component": [ ... ]
        }
      }
    ]
  }
}
```

Source: `backend/src/main/java/com/backend/application/dto/delivery/ContentSlotDeliveryResponse.java`

Template-slot resolution note:

- For pages with `templateId`, delivery slot list is resolved from `template_slots` (slot order/position contract).
- **Slot precedence is intentionally shared-first for template-based pages**: when both a shared slot and a page-specific slot exist for the same `slot_name`, the shared slot's component bindings win. This protects chrome slots (`Header`, `Footer`) from being accidentally shadowed by page-level overrides. Implemented in `PageDeliveryServiceImpl.resolveEffectiveSlotsForDelivery()`.
- For template-less pages (`mergeSlotsWithoutTemplate()`), precedence is reversed: page slot > shared slot (fallback mode).
- `contentSlots.contentSlot[].slotId` = `slotName + "Slot"` (e.g. `"Section1Slot"`).
- `contentSlots.contentSlot[].position` = position enum value (e.g. `"TOP"`, `"CENTER"`, `"BOTTOM"`), **not** slot name.
- The Next.js storefront's `buildSlotMap` keys by `slotId.replace(/Slot$/, "")` (→ `"Section1"`) so template components can reference slots by their logical name rather than the position enum.
- `component.type` is populated from `componentType.uid` (stable registry key); falls back to `componentType.name` if uid is missing.

DTO references (source of truth):

- `backend/src/main/java/com/backend/application/dto/delivery/ComponentDeliveryResponse.java`
- `backend/src/main/java/com/backend/application/dto/delivery/BatchDeliveryResponse.java`
- `backend/src/main/java/com/backend/application/dto/delivery/PageDeliveryResponse.java`
- `backend/src/main/java/com/backend/application/dto/delivery/SiteDeliveryResponse.java`

## Frontend integration

### Admin Angular storefront

- `storefront/src/app/cms/` (delivery service + types)

### Next.js headless storefront (`storefront-nextjs/`)

- API client: `storefront-nextjs/lib/cms-client.ts`
- Type definitions: `storefront-nextjs/lib/types.ts`
- CMS rendering components: `storefront-nextjs/components/cms/`

Key patterns used in the Next.js client:

- `resolvePage` and `fetchSiteConfig` are wrapped with React `cache()` to deduplicate identical calls within a single SSR render cycle (e.g. `generateMetadata` + page component both calling the same endpoint).
- `resolvePage` uses flat primitive arguments (`lang, pageType, pageLabelOrId, code`) instead of an object — required for `cache()` identity comparison to work correctly.
- HTTP 200 + `result: "ERROR"` is treated as `null` (not an error throw), matching the backend's page-not-found contract.
- `page.template` drives layout selection via `templateRegistry` — equivalent to Spartacus's `cx-storefront` / `cx-page-slot` pattern. Each template places `<CmsSlot slotName="..." />` where it needs; unknown templates render `null` + `console.warn`. See `storefront-nextjs/components/cms/templates/`.

## Security & tenant isolation

- Delivery endpoints do not require auth, but they are still tenant-scoped.
- Tenant resolution is enforced by [`backend/src/main/java/com/backend/infrastructure/tenant/TenantFilter.java`](../../backend/src/main/java/com/backend/infrastructure/tenant/TenantFilter.java).
