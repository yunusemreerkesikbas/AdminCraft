# Storefront Next.js

Headless storefront for AdminCraft CMS delivery APIs using the Next.js App Router.

## Purpose

- Resolve CMS pages and render slots/components from the delivery API.
- Keep routes locale-prefixed (`/{lang}/...`) with SEO metadata derived from CMS page + site config.
- Drive locale support dynamically from the tenant's `SiteDeliveryResponse` (`defaultLanguage`, `enabledLanguages`).
- Support SSR by default and static export when `NEXT_OUTPUT=export` is set.

## Source of truth

- App routes: [`../../storefront-nextjs/app`](../../storefront-nextjs/app)
- Middleware (format check): [`../../storefront-nextjs/middleware.ts`](../../storefront-nextjs/middleware.ts)
- i18n utilities: [`../../storefront-nextjs/lib/i18n.ts`](../../storefront-nextjs/lib/i18n.ts)
- next-intl request config: [`../../storefront-nextjs/i18n/request.ts`](../../storefront-nextjs/i18n/request.ts)
- UI translation messages: [`../../storefront-nextjs/messages/`](../../storefront-nextjs/messages/)
- CMS client + types: [`../../storefront-nextjs/lib/cms-client.ts`](../../storefront-nextjs/lib/cms-client.ts), [`../../storefront-nextjs/lib/types.ts`](../../storefront-nextjs/lib/types.ts)
- CMS render pipeline: [`../../storefront-nextjs/components/cms`](../../storefront-nextjs/components/cms)
- Layout shell: [`../../storefront-nextjs/app/[lang]/layout.tsx`](../../storefront-nextjs/app/%5Blang%5D/layout.tsx), [`../../storefront-nextjs/components/layout`](../../storefront-nextjs/components/layout)
- SEO metadata: [`../../storefront-nextjs/lib/seo.ts`](../../storefront-nextjs/lib/seo.ts)
- Runtime config: [`../../storefront-nextjs/next.config.ts`](../../storefront-nextjs/next.config.ts)
- Environment example: [`../../storefront-nextjs/.env.local.example`](../../storefront-nextjs/.env.local.example)

## Public delivery APIs used

All requests are tenant-scoped via headers set in `cms-client.ts`:
`X-Tenant-Subdomain` and `X-Tenant-ID`.

| Function | Endpoint | Notes |
| --- | --- | --- |
| `resolvePage` | `GET /api/cms/pages` | `lang` required; `pageType`, `pageLabelOrId`, `code` optional |
| `fetchSiteConfig` | `GET /api/cms/site` | Site metadata, `defaultLanguage`, `enabledLanguages`, `isRtl` |
| `fetchProduct` | `GET /api/cms/products/{uid}` | Product detail payload |
| `fetchProductsByCategory` | `GET /api/cms/products/category/{categoryUid}` | Paged list |
| `searchProducts` | `GET /api/cms/products/search` | Query param: `q` |
| `fetchCategoryTree` | `GET /api/cms/products/categories` | Category tree |

## Routing and page resolution

| Route | Resolution |
| --- | --- |
| `/` | `fetchSiteConfig()` → `site.defaultLanguage`; if site unavailable → `FALLBACK_LOCALE` (`app/page.tsx`) |
| `/{lang}` | Layout validates `lang` against tenant `supportedLanguages` (via `fetchTenantConfig()`); if tenant config unavailable (403/500), validation is skipped and the middleware's format check is trusted |
| `/{lang}` | `resolvePage(lang)` (homepage) |
| `/{lang}/**` | `resolvePage(lang, "ContentPage", "/{slug}")` |
| `/{lang}/products/{uid}` | `resolvePage(lang, "ProductPage", undefined, uid)` + `fetchProduct(uid)` |
| `/{lang}/c/{categoryUid}` | `resolvePage(lang, "CategoryPage", undefined, categoryUid)` + `fetchProductsByCategory` |
| `/{lang}/search?q=...` | `resolvePage(lang, "SearchResultPage")` + `searchProducts(q)` |
| `/{lang}/maintenance` | Rendered by `app/[lang]/[[...slug]]/page.tsx` (maintenance fallback view) |

Missing pages use `app/[lang]/not-found.tsx`.

### Maintenance mode

Maintenance mode is enforced at the edge in [`middleware.ts`](../../storefront-nextjs/middleware.ts):

- Calls `GET /api/cms/site` with `cache: "no-store"` on each request.
- If `maintenanceMode === true`, redirects all page routes to `/{lang}/maintenance`.
- `/robots.txt`, `/sitemap.xml`, `/_next/*`, and `/{lang}/maintenance` are excluded.
- Fail-open: if the CMS call fails, no redirect is applied.

The maintenance page itself is rendered in the catch-all route:
`app/[lang]/[[...slug]]/page.tsx`.

### Language validation layers

| Layer | Source | Behaviour when unavailable |
| --- | --- | --- |
| `middleware.ts` | Regex `[a-z]{2,3}` | Always runs |
| `app/page.tsx` | `fetchSiteConfig()` → `defaultLanguage` | Falls back to `FALLBACK_LOCALE` |
| `app/[lang]/layout.tsx` | `fetchTenantConfig()` → `supportedLanguages` | Skips validation (trusts middleware) |

`fetchTenantConfig()` calls `GET /api/tenants/current/detail` which requires authentication; the storefront has no token, so this endpoint returns 403. The call is wrapped in a `try/catch` that silently returns `null`. The tenant language routing will only enforce restrictions if this endpoint becomes publicly accessible or a dedicated public endpoint is added.

## CMS render pipeline

Template-driven rendering — equivalent of Spartacus's `cx-storefront` + `cx-page-slot` pattern.

1. `CmsPage` calls `buildSlotMap(page)` → `{ slotName: slot }` map from `contentSlots` (or legacy `slots`). The backend sets `contentSlot[].slotId = slotName + "Slot"` and `contentSlot[].position = positionEnum` (e.g. `"TOP"`). `buildSlotMap` derives the key from `slotId` by stripping the `"Slot"` suffix — so `"Section1Slot"` → `"Section1"` — matching the position strings used in template components.
2. `resolveTemplate(page.template)` looks up the template component from `templateRegistry`.
   - Unknown template → falls back to `DefaultTemplate` (renders all slots as a vertical stack) + `console.warn` (development only).
3. The resolved `TemplateComponent` receives `{ slotMap, page }` props.
4. Each template reads its slot list from `TEMPLATE_CONFIGS` in `template-configs.ts` and renders `<CmsSlot position="..." slotMap={slotMap} />` for each slot.
5. `CmsSlot` renders the components for that position; returns `null` + `console.warn` (development only) if the position isn't in `slotMap`.
6. `CmsComponent` delegates to the registry (`components/cms/registry`) using `component.type` from delivery.
7. Unknown component types render with `UnknownComponent`.

`CmsImage` resolves responsive media URLs via `buildMediaUrl`.

### Template registry

Located in `components/cms/templates/index.ts`.

| Template name | File | Slot positions | Page type |
|---|---|---|---|
| `LandingPageTemplate` | `LandingPageTemplate.tsx` | Section1, Section2, Section3 | Home / campaign |
| `ContentPageTemplate` | `ContentPageTemplate.tsx` | TopContent, BodyContent, SideContent | Generic content |
| `CategoryPageTemplate` | `CategoryPageTemplate.tsx` | TopContent, ProductGrid | Category listing |
| `ProductDetailsPageTemplate` | `ProductDetailsPageTemplate.tsx` | Summary, Tabs, CrossSelling | Product detail |
| `SearchResultsPageTemplate` | `SearchResultsPageTemplate.tsx` | TopContent, Results | Search results |
| `ErrorPageTemplate` | `ErrorPageTemplate.tsx` | MiddleContent | 500 error |
| `NotFoundPageTemplate` | `NotFoundPageTemplate.tsx` | MiddleContent | 404 not found |
| `DefaultTemplate` | `DefaultTemplate.tsx` | all slots in slotMap | Fallback (unknown templates) |

Template names match `page_template.uid` values in the database exactly — do not rename.

Slot names and positions are centrally defined in `template-configs.ts` (`TEMPLATE_CONFIGS`). Each template reads its own config entry at render time.

To add a new template:
1. Add an entry to `TEMPLATE_CONFIGS` in `template-configs.ts` with slot names and positions.
2. Create `components/cms/templates/MyTemplate.tsx` implementing `TemplateProps` (read slots from `TEMPLATE_CONFIGS.MyTemplate`).
3. Register it in `templateRegistry` in `index.ts`.

### CSS targeting

Template name appears **once** on the template's outer `<div>` (`cms-page {templateName}`), not repeated on every slot.
`CmsSlot` emits `class="cms-slot {position}"` only.

```html
<!-- Rendered structure -->
<div class="cms-page LandingPageTemplate space-y-8">
  <section class="cms-slot Section1" data-slot-position="Section1"> ... </section>
  <section class="cms-slot Section2" data-slot-position="Section2"> ... </section>
</div>
```

CSS scoping patterns:

```css
/* All slots in a specific template */
.cms-page.LandingPageTemplate .cms-slot { ... }

/* A specific slot across all templates */
.cms-slot.Section1 { ... }

/* A specific slot in a specific template */
.cms-page.LandingPageTemplate .cms-slot.Section1 { ... }
```

`CmsPage` mounts a `BodyClassSetter` client component (`components/BodyClassSetter.tsx`) that adds `page-{uid}` to `<body>` on hydration and removes it on unmount. This enables page-type–level CSS scoping:

```css
/* Target only the homepage */
body.page-homepage .hero { ... }
```

## i18n — Tenant-driven locale configuration

Locale configuration is **fully dynamic**, driven by the tenant's `SiteDeliveryResponse`:

- `defaultLanguage` — fallback redirect target (e.g. `"TR"`)
- `enabledLanguages[]` — list of `{ code, nativeName, isRtl }` objects the tenant has enabled

### Layers

| Layer | Responsibility |
| --- | --- |
| `middleware.ts` | **Permissive format check only** — accepts any `[a-z]{2,3}` segment; no hardcoded locale list |
| `app/[lang]/layout.tsx` | **Runtime validation** — fetches site config, checks `lang` against `enabledLanguages`; redirects to `defaultLanguage` if unsupported |
| `app/layout.tsx` | Reads `x-lang` header set by middleware; derives `dir` from `isRtlByConfig()` using site config |

### `lib/i18n.ts` helpers

| Export | Purpose |
| --- | --- |
| `FALLBACK_LOCALE` | Hard fallback (`"tr"`) for when site config is unavailable |
| `isValidLocaleFormat(v)` | Middleware format gate: `/^[a-z]{2,3}$/` |
| `toUrlLocale(code)` | API `"TR"` → URL `"tr"` |
| `toApiLocale(lang)` | URL `"tr"` → API `"TR"` |
| `isRtlByConfig(lang, enabledLanguages)` | Reads `isRtl` from the matching `LanguageInfo` entry |
| `resolveMessageLocale(lang)` | Maps lang to an available messages file; unknown langs fall back to `FALLBACK_LOCALE` |
| `normalizeLanguage(v)` | Legacy alias used by `cms-client.ts` — uppercases locale for API calls |
| `withLocalePath(locale, path)` | Builds localized links (`/tr/search`) |

### next-intl (UI chrome translations)

Used for static UI strings (navigation labels, page headings, error messages). **Not used for CMS content** — those come from the API with the `lang` param.

- Plugin registered in `next.config.ts` via `createNextIntlPlugin("./i18n/request.ts")`.
- `i18n/request.ts` resolves the correct message file via `resolveMessageLocale`.
- Message files: `messages/tr.json`, `messages/en.json`. Unknown tenant locales fall back to `tr.json`.
- Server components use `getTranslations("Namespace")`, with the result bound to `translate`.
- Client components use `useTranslations("Namespace")` inside `NextIntlClientProvider` (provided by `[lang]/layout.tsx`).

Message namespaces:

| Namespace | Keys | Used in |
| --- | --- | --- |
| `Navigation` | `home`, `search` | `Navigation.tsx` |
| `Search` | `title`, `emptyQuery` | `search/page.tsx` |
| `Product` | `sku` | `products/[uid]/page.tsx` |
| `NotFound` | `title`, `description` | `not-found.tsx` |
| `Maintenance` | `title`, `description` | `[[...slug]]/page.tsx` |

### Language Switcher

`components/layout/LanguageSwitcher.tsx` renders links for all of the tenant's `enabledLanguages`. It replaces the `[lang]` segment in the current pathname to build the target URL. Hidden when `enabledLanguages.length <= 1`. Rendered from `Header.tsx`.

## SEO metadata

`buildPageMetadata` derives title/description/canonical/robots/OG image from:

- CMS page fields (`title`, `description`, `robotTag`, `canonicalUrl`)
- Site config (`siteTitle`, `siteDescription`, `ogImageUrl`)

Each route uses `generateMetadata` to combine `resolvePage` and `fetchSiteConfig`.

## Environment configuration

Read from `.env.local.example` and `lib/utils.ts`:

- `NEXT_PUBLIC_CMS_API_URL` (base URL for CMS delivery)
- `TENANT_SUBDOMAIN` / `NEXT_PUBLIC_TENANT_SUBDOMAIN`
- `TENANT_ID` / `NEXT_PUBLIC_TENANT_ID`
- `NEXT_OUTPUT=export` enables static export in `next.config.ts`

## Caching and revalidation

`cms-client.ts` uses React `cache()` with Next fetch options. `fetchSiteConfig` is deduplicated across `app/layout.tsx` and `app/[lang]/layout.tsx` within the same render.

| Function | Strategy |
| --- | --- |
| `resolvePage`, `fetchProduct` | `revalidate: 30s` |
| `fetchSiteConfig`, `fetchCategoryTree` | `revalidate: 300s` |
| `searchProducts` | `cache: "no-store"` |

## Security and tenant isolation

Delivery calls are public but tenant-scoped by headers set in `cms-client.ts`.
Locale validation happens at the layout level (not middleware) to allow tenant-specific enabledLanguages to drive redirects without edge API calls.

## Implementation guide

### 1) Homepage

- Resolve page with `resolvePage(lang)`.
- Build metadata via `generateMetadata` + `fetchSiteConfig`.
- Render with `CmsPage`.

### 2) Content page

- Build `pageLabelOrId` as `"/" + slug.join("/")`.
- Resolve with `resolvePage(lang, "ContentPage", pageLabelOrId)`.
- Render with `CmsPage`.

### 3) Product page

- Resolve CMS template with `resolvePage(lang, "ProductPage", undefined, uid)`.
- Fetch product detail via `fetchProduct(uid)`.
- Render CMS slots + product section.

### 4) Adding a new UI string

1. Add the key to `messages/tr.json` and `messages/en.json`.
2. Call `const translate = await getTranslations("Namespace")` in the server component.
3. Use `translate("key")` in JSX.

### 5) Enabling a new language for a tenant

No code changes required. Add the language via the admin Site Settings UI. The storefront reads `enabledLanguages` from the API at runtime. If no `messages/{lang}.json` exists, the UI falls back to `tr.json`; CMS content is served in the requested language by the backend.

## Error handling

### `cms-client.ts` request behaviour

| Status | Behaviour |
| --- | --- |
| 404 | Returns `null` |
| 429 or 5xx | Logs `[CMS] Request failed {status}: {url}` to server console; returns `null` |
| Other non-2xx | Throws `Error` (propagates to page) |

`null` from `resolvePage` causes `ContentPage` to call `notFound()`, showing `app/[lang]/not-found.tsx` instead of crashing.

`fetchTenantConfig()` wraps its `request()` call in `try/catch` and always returns `null` on any error (including the expected 403 from the auth-protected endpoint).

### Error boundary

`app/[lang]/error.tsx` is a `"use client"` error boundary for any uncaught errors inside the locale subtree. It shows a Türkçe error message with a retry button. Root-level errors (outside `[lang]`) are not caught here.

## Component Registry

Located in `components/cms/registry/index.ts`. Maps `component.type` (from delivery API) to a React renderer.

| Component type | Renderer | Notes |
|---|---|---|
| `HeaderComponent` | `HeaderCmsComponent` | Brand logo + nav + language switcher (async server component) |
| `FooterComponent` | `FooterCmsComponent` | Footer links + copyright |
| `NavigationComponent` | `NavigationCmsComponent` | Navigation-aware renderer; delegates to `NavigationRenderer` or `StaticNavRenderer` based on `navigationType` |
| `FeatureCardComponent` | `PortfolioGridRenderer` | Portfolio/feature card grid |
| Other known types | `TextBlockRenderer` | Simple title/subtitle/description block |
| Unknown types | `UnknownComponent` | Dev-visible fallback showing component type |

### NavigationComponent

`NavigationCmsComponent` handles the `NavigationComponent` system type introduced in CMS-181.

**`navigationType` render strategy:**

| `navigationType` | Renderer | Output |
|---|---|---|
| `MAINMENU` (or undefined) | `NavigationRenderer` | Hierarchical nav tree with children support |
| `STATICPAGE` | `StaticNavRenderer` | Flat link list — root entries + all children's entries merged |

**`searchBox` flag:**

When `component.searchBox === true`, a `SearchOverlay` button is rendered alongside the navigation.
- Clicking the search icon opens a `<dialog>` overlay.
- Submitting the search form navigates to `/{lang}/search?q={query}`.
- Escape key or clicking the backdrop closes the overlay.

**Files:**

- `components/cms/navigation/NavigationCmsComponent.tsx` — main renderer
- `components/cms/navigation/StaticNavRenderer.tsx` — flat link list for `STATICPAGE`
- `components/cms/search/SearchOverlay.tsx` — client component for search dialog

## Current limitations and extension points

- Product, category, and search pages include minimal UI beyond CMS slots.
- Adding new UI languages requires a new `messages/{lang}.json` file and an entry in `AVAILABLE_MESSAGES` in `lib/i18n.ts`.
