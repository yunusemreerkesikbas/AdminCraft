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
| `/` | Redirects to `/{FALLBACK_LOCALE}` (`app/page.tsx`) |
| `/{lang}` | Layout validates `lang` against `site.enabledLanguages`; redirects to `/{site.defaultLanguage}` if not supported |
| `/{lang}` | `resolvePage(lang)` (homepage) |
| `/{lang}/**` | `resolvePage(lang, "ContentPage", "/{slug}")` |
| `/{lang}/products/{uid}` | `resolvePage(lang, "ProductPage", undefined, uid)` + `fetchProduct(uid)` |
| `/{lang}/c/{categoryUid}` | `resolvePage(lang, "CategoryPage", undefined, categoryUid)` + `fetchProductsByCategory` |
| `/{lang}/search?q=...` | `resolvePage(lang, "SearchResultPage")` + `searchProducts(q)` |

Missing pages use `app/[lang]/not-found.tsx`.

## CMS render pipeline

Template-driven rendering — equivalent of Spartacus's `cx-storefront` + `cx-page-slot` pattern.

1. `CmsPage` calls `buildSlotMap(page)` → `{ position: slot }` map from `contentSlots` (or legacy `slots`).
2. `resolveTemplate(page.template)` looks up the template component from `templateRegistry`.
   - Unknown template → `null` render + `console.warn` (development only).
3. The resolved `TemplateComponent` receives `{ slotMap, page }` props.
4. Each template places `<CmsSlot position="..." slotMap={slotMap} template={page.template} />` wherever it wants.
5. `CmsSlot` renders the components for that position; returns `null` if the position isn't in `slotMap`.
6. `CmsComponent` delegates to the registry (`components/cms/registry`) using `component.type` from delivery.
7. Unknown component types render with `UnknownComponent`.

`CmsImage` resolves responsive media URLs via `buildMediaUrl`.

### Template registry

Located in `components/cms/templates/index.ts`.

| Template name | File | Slot positions | Page type |
|---|---|---|---|
| `LandingPageTemplate` | `LandingPageTemplate.tsx` | Section1, Section2, Section3 | Home / campaign |
| `ContentPageTemplate` | `ContentPageTemplate.tsx` | TopContent, BodyContent, BottomContent | Generic content |
| `CategoryPageTemplate` | `CategoryPageTemplate.tsx` | TopContent, ProductGrid | Category listing |
| `ProductDetailsPageTemplate` | `ProductDetailsPageTemplate.tsx` | Summary, Tabs, CrossSelling | Product detail |
| `SearchResultsPageTemplate` | `SearchResultsPageTemplate.tsx` | TopContent, Results | Search results |
| `ErrorPageTemplate` | `ErrorPageTemplate.tsx` | MiddleContent | 500 error |
| `NotFoundPageTemplate` | `NotFoundPageTemplate.tsx` | MiddleContent | 404 not found |

Template names match `page_template.uid` values in the database exactly — do not rename.

To add a new template: create `components/cms/templates/MyTemplate.tsx` implementing `TemplateProps`, then add it to `templateRegistry` in `index.ts`.

### CSS targeting

`CmsSlot` emits `class="cms-slot {templateName} {position}"`. This enables scoped CSS without extra wrapper elements:

```css
/* All slots in a specific template */
.LandingPageTemplate.cms-slot { ... }

/* A specific slot across all templates */
.Section1 { ... }

/* A specific slot in a specific template */
.LandingPageTemplate.Section1 { ... }
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

## Current limitations and extension points

- CMS component registry is minimal; unknown types render `UnknownComponent`.
- Product, category, and search pages include minimal UI beyond CMS slots.
- Adding new UI languages requires a new `messages/{lang}.json` file and an entry in `AVAILABLE_MESSAGES` in `lib/i18n.ts`.
