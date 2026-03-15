# Storefront Next.js

Headless storefront for AdminCraft CMS delivery APIs using the Next.js App Router.

## Purpose

- Resolve CMS pages and render slots/components from the delivery API.
- Keep routes locale-prefixed (`/{lang}/...`) with SEO metadata derived from CMS page + site config.
- Drive locale support dynamically from the tenant's `SiteDeliveryResponse` (`defaultLanguage`, `enabledLanguages`).
- Support SSR by default and static export when `NEXT_OUTPUT=export` is set.

## Source of truth

- App routes: [`../../storefront-nextjs/app`](../../storefront-nextjs/app)
- Proxy (format check + maintenance): [`../../storefront-nextjs/proxy.ts`](../../storefront-nextjs/proxy.ts)
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
| `fetchMediaByUids` | `GET /api/cms/media?uids=a,b,c` | Batched media resolution via comma-separated UIDs |
| `fetchProduct` | `GET /api/cms/products/{uid}` | Product detail payload |
| `fetchProductsByCategory` | `GET /api/cms/products/category/{categoryUid}` | Paged list |
| `searchProducts` | `GET /api/cms/products/search` | Query param: `q` |
| `app/robots.ts` | `GET /api/cms/robots.txt` | Plain-text robots.txt; respects `indexingEnabled` from `SiteTechnicalSettings` |

## Routing and page resolution

| Route | Resolution |
| --- | --- |
| `/` | `fetchSiteConfig()` → `site.defaultLanguage`; if site unavailable → `FALLBACK_LOCALE` (`app/page.tsx`) |
| `/{lang}` | Layout validates `lang` against `fetchSiteConfig()` → `site.enabledLanguages`; if site config is unavailable the layout throws instead of silently skipping validation |
| `/{lang}` | `resolvePage(lang)` (homepage) |
| `/{lang}/**` | `resolvePage(lang, "ContentPage", "/{slug}")` |
| `/{lang}/products/{uid}` | `resolvePage(lang, "ProductPage", undefined, uid)` + `fetchProduct(uid)` |
| `/{lang}/c/{categoryUid}` | `resolvePage(lang, "CategoryPage", undefined, categoryUid)` + `fetchProductsByCategory` |
| `/{lang}/search?q=...` | `resolvePage(lang, "SearchResultPage")` + `searchProducts(q)` |
| `/{lang}/maintenance` | Rendered by `app/[lang]/[[...slug]]/page.tsx` (maintenance fallback view) |

Missing pages use `app/[lang]/not-found.tsx`.

### Maintenance mode

Maintenance mode is enforced at the edge in [`proxy.ts`](../../storefront-nextjs/proxy.ts):

- Calls `GET /api/cms/site` with `cache: "no-store"` on each request.
- If `maintenanceMode === true`, redirects all page routes to `/{lang}/maintenance`.
- `/_next/*`, `/api/*`, `/cms-media/*`, `/robots.txt`, `/sitemap.xml`, and `/{lang}/maintenance` are excluded.
- Fail-open: if the CMS call fails, no redirect is applied.

The maintenance page itself is rendered in the catch-all route:
`app/[lang]/[[...slug]]/page.tsx`.

### Language validation layers

| Layer | Source | Behaviour when unavailable |
| --- | --- | --- |
| `proxy.ts` | Regex `[a-z]{2,3}` | Always runs |
| `app/page.tsx` | `fetchSiteConfig()` → `defaultLanguage` | Falls back to `FALLBACK_LOCALE` |
| `app/[lang]/layout.tsx` | `fetchSiteConfig()` → `enabledLanguages` | Throws if site config is unavailable; otherwise invalid locales hit `notFound()` |

`app/[lang]/layout.tsx` now validates locale access entirely from the public site delivery payload. The storefront no longer depends on an auth-protected tenant detail endpoint for locale enforcement.

## CMS render pipeline

Template-driven rendering — equivalent of Spartacus's `cx-storefront` + `cx-page-slot` pattern.

1. `CmsPage` calls `buildSlotMap(page)` → `{ slotName: slot }` map from `contentSlots`. The backend sets `contentSlot[].slotId = slotName + "Slot"` and `contentSlot[].position = positionEnum` (e.g. `"TOP"`). `buildSlotMap` derives the key from `slotId` by stripping the `"Slot"` suffix — so `"Section1Slot"` → `"Section1"` — matching the slot names used in template components.
2. `resolveTemplate(page.template)` looks up the template component from `templateRegistry`.
   - Unknown template → falls back to `DefaultTemplate` (renders all slots as a vertical stack) + `console.warn` (development only).
3. The resolved `TemplateComponent` receives `{ slotMap, page }` props.
4. `CmsPage` handles shared `Header` and `Footer` slots outside `<main>`.
   - If a `Header` slot exists and contains renderable components, it renders `ThemeHeaderSlot`.
   - If a `Footer` slot exists and contains renderable components, it renders `ThemeFooterSlot`.
   - Empty or unrecognized chrome slots render nothing.
5. Each template renders `<CmsSlot slotName="..." slotMap={slotMap} />` for each of its body slots. `LandingPageTemplate` does this for Section1–Section8 alongside the `<Motion />` animation layer — no longer a special case.
6. `CmsSlot` renders the components for that slot; returns `null` + `console.warn` (development only) if the slot name isn't in `slotMap`.
7. `CmsComponent` delegates to the registry (`components/cms/registry`) using `component.type` from delivery.
8. Unknown component types render with `UnknownComponent` (dev: red dashed box; prod: `null`).

`CmsImage` resolves responsive media URLs via `buildMediaUrl`.

### Template registry

Located in `components/cms/templates/index.ts`.

| Template name | File | Slot positions | Page type |
|---|---|---|---|
| `LandingPageTemplate` | `LandingPageTemplate.tsx` | Section1, Section2, Section3, Section4, Section5, Section6, Section7, Section8 | Home / campaign |
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
1. Create `components/cms/templates/configs/my-page.template.ts` with slot names and positions.
2. Import and register it in `template-configs.ts` (`TEMPLATE_CONFIGS` map + `TemplateName` union).
3. Create `components/cms/templates/MyTemplate.tsx` implementing `TemplateProps` (read slots from `TEMPLATE_CONFIGS.MyTemplate`).
4. Register it in `templateRegistry` in `index.ts`.

### CMS-driven homepage (plugin theme)

Homepage uses the same generic `CmsSlot → CmsComponent → registry` pipeline as every other template. Each slot's component is dispatched by `component.type` to a dedicated renderer — no styleClass or UID dispatch.

- Entry point: `components/cms/templates/LandingPageTemplate.tsx`
- Central registry config: `components/cms/cms-components.config.tsx` (Spartacus `SPA_CMSCOMPONENTS_CONFIG` equivalent)
- Renderer factory: `components/cms/renderers/renderer-factory.tsx` — `makeMediaRenderer(buildFn, renderFn)` produces an async RSC that collects mediaUids, fetches media, builds a typed model, and delegates rendering to the given JSX factory
- Theme model types: `components/theme/default/models.ts` (`HeroModel`, `AboutModel`, `VideoModel`, etc.)
- Theme model builders: `components/theme/default/builders.ts` (`buildHeroModel`, `buildAboutModel`, `buildVideoModel`, etc.)
- Motion layer: `components/theme/default/Motion.tsx`
- Section components (visual layer, unchanged): `components/theme/default/sections/`
- Shared UI primitives: `components/theme/default/utils/shared.tsx` (`ThemeTag`, `ThemeButton`, `ThemeCircleButton`)
- Icons: `components/theme/default/utils/icons.tsx`
- Theme CSS: `components/theme/default/theme.module.css`

#### Slot contract

Dispatch is by `component.type` only — no styleClass or UID matching.

| Slot | Expected component UID | Component type | Builder | UI section |
| --- | --- | --- | --- | --- |
| `Section1` | `HomepageHeroBanner` | `HeroBannerComponent` | `buildHeroModel` | Hero |
| `Section2` | `HomepageAboutSection` | `AboutBannerComponent` | `buildAboutModel` | About |
| `Section3` | `HomepageVideoSection` | `VideoSectionComponent` | `buildVideoModel` | Video |
| `Section4` | `HomepageServiceSection` | `ServiceCardComponent` | `buildServiceModel` | Services |
| `Section5` | `HomepageProjectSection` | `ProjectCardComponent` | `buildProjectsModel` | Projects |
| `Section6` | `HomepageAwardSection` | `AwardBannerComponent` | `buildAwardModel` | Award |
| `Section7` | `HomepageMarqueeText` | `MarqueeTextComponent` | `buildMarqueeModel` | Marquee |
| `Section8` | `HomepageInstagramSection` | `InstagramSectionComponent` | `buildInstagramModel` | Instagram |

All 8 landing types use `makeMediaRenderer(buildXxxModel, (m) => <Section model={m} />)` in `cms-components.config.tsx` — there are no separate renderer files for these types. Adding a new type requires: (1) a model type in `components/theme/default/models.ts`, (2) a `buildXxxModel` in `components/theme/default/builders.ts`, (3) one `makeMediaRenderer(...)` line in `cms-components.config.tsx` — no template changes.

#### Field mapping

Each renderer calls a `buildXxxModel` function from `components/theme/default/builders.ts`, which reads CMS content from `component_i18n`, `component_entries`, `entry_i18n`, and `customFields`.

| Section | Main fields | Entry/custom field usage |
| --- | --- | --- |
| Hero | `title`, `subtitle`, `description` | prefers entry/component `responsive`; falls back to `mediaUid`, `buttonUrl`, `buttonText` |
| About | `title`, `subtitle`, `description` | 3 entries; each prefers entry `responsive`, falls back to `mediaUid`; entry 2 title used as inner label |
| Video | `title`, `subtitle`, `description` | `videoUrl`; poster prefers entry/component `responsive`, falls back to `mediaUid` |
| Services | `title`, `subtitle` | each entry uses `title`, `description`, prefers entry `responsive`, falls back to `mediaUid` |
| Projects | component wrapper only | each entry uses `title`, `subtitle`, `linkUrl`, prefers entry `responsive`, falls back to `mediaUid` |
| Award | `title`, `subtitle`, `description` | prefers entry/component `responsive`, falls back to `mediaUid`, `buttonUrl`, `buttonText` |
| Marquee | `description` | tokenized with `-` separators |
| Instagram | `title`, `subtitle`, `description` | entries prefer `responsive`, fall back to `mediaUid`; first available image becomes main fallback if full 8-image set is absent |

#### Import / seed order

The landing page depends on both template-slot migration and tenant-scoped content imports.

1. Restart backend so `backend/src/main/resources/db/tenant/pagebuilder/R__seed_page_templates.sql` is applied by Flyway.
2. Import `backend/src/main/resources/impex/seed_liko_components.sql` from the Admin UI ImpEx screen (example landing page components, initially typed as generic `SimpleBannerComponent` / `FeatureCardComponent`).
3. Import `backend/src/main/resources/impex/seed_landing_component_types.sql` from the Admin UI ImpEx screen (creates `HeroBannerComponent`, `AboutBannerComponent`, `VideoSectionComponent`, `AwardBannerComponent`, `ServiceCardComponent`, `ProjectCardComponent`, `InstagramSectionComponent`, `MarqueeTextComponent` and **migrates the 8 homepage components** to their type-specific renderers). Must run after step 2.
4. Import `backend/src/main/resources/impex/seed_liko_chrome_components.sql` from the Admin UI ImpEx screen (shared header/footer components + Home-2 chrome copy).
5. Import `backend/src/main/resources/impex/seed_liko_pages_and_slots.sql` from the Admin UI ImpEx screen (homepage, shared header/footer slots, slot-component bindings).
6. Import `backend/src/main/resources/impex/seed_pages_and_slots.sql` if the tenant also needs the default content/category/product/search pages.
7. Import `backend/src/main/resources/impex/seed_navigation.sql` to create `LandingMainNavNode` and `LandingFooterNavNode`, then bind them to the chrome navigation components.
8. Upload image/video assets in Media Library for the tenant.
9. Import `backend/src/main/resources/impex/seed_liko_media_uids.sql` to assign semantic UIDs (`homepage-hero-bg`, `homepage-project-1`, etc.) to the uploaded files. This aligns media with the `mediaUid` references embedded in component entry `custom_data`. See [Media UID alignment](#media-uid-alignment) below.
10. For assets not covered by the seed (video poster, service icons), either bind them via the admin `Bind` dialog or use responsive media assignments directly on the component/entry.

If these imports are missing or partial, homepage can render incomplete sections because the storefront resolves strictly from CMS payload.

#### Header and footer

Header and footer are CMS-driven through shared `Header` / `Footer` slots and then adapted into the storefront chrome layer.

- Slot entry point: `components/cms/CmsPage.tsx`
- Chrome slot renderers: `components/layout/theme-chrome/ThemeHeaderSlot.tsx`, `components/layout/theme-chrome/ThemeFooterSlot.tsx`
- Slot adapter: `components/layout/theme-chrome/cms-adapter.ts`
- Presentational chrome: `components/layout/theme-chrome/ThemeHeaderChrome.tsx`, `components/layout/theme-chrome/ThemeFooterChrome.tsx`

The chrome adapter prefers the expected component `uid`, then `type + styleClass`, mirroring the homepage section adapter strategy.

##### Shared chrome slot contract

| Slot | Expected component UID | Component type | Style class | Purpose |
| --- | --- | --- | --- | --- |
| `Header` | `StorefrontHeaderMainNavigation` | `NavigationComponent` | `header-main-navigation` | Main off-canvas menu tree |
| `Header` | `StorefrontHeaderSocialLinks` | `CMSLinkComponent` | `header-social-links` | Social links in the off-canvas meta area |
| `Header` | `StorefrontHeaderContactInfo` | `CMSLinkComponent` | `header-contact-info` | Contact links + CTA copy |
| `Footer` | `StorefrontFooterBrandBlock` | `CMSParagraphComponent` | `footer-brand-block` | Brand text / intro copy |
| `Footer` | `StorefrontFooterSitemapNavigation` | `NavigationComponent` | `footer-sitemap-navigation` | Footer sitemap column |
| `Footer` | `StorefrontFooterOfficeLinks` | `CMSLinkComponent` | `footer-office-links` | Address / phone / email column |
| `Footer` | `StorefrontFooterNewsletter` | `CMSLinkComponent` | `footer-newsletter` | Newsletter title + placeholder/button labels via entry `custom_data` |
| `Footer` | `StorefrontFooterSocialLinks` | `CMSLinkComponent` | `footer-social-links` | Copyright bar social links |

##### Chrome field mapping

| Area | Data source |
| --- | --- |
| Header logo / languages | `GET /api/cms/site` |
| Header main navigation | `navigationNode` on `StorefrontHeaderMainNavigation` |
| Header social / contact links | `component_entries` + `component_entry_i18n.custom_data.linkUrl` |
| Header CTA text | `component_i18n.description` on `StorefrontHeaderContactInfo` |
| Footer brand text | `component_i18n.description` on `StorefrontFooterBrandBlock` |
| Footer sitemap | `navigationNode` on `StorefrontFooterSitemapNavigation` |
| Footer office links | `component_entries` + `component_entry_i18n.custom_data.linkUrl` |
| Footer newsletter UI copy | `component_i18n.title` + `component_entry_i18n.custom_data.inputPlaceholder/buttonLabel` |
| Footer social links | `component_entries` + `component_entry_i18n.custom_data.linkUrl` |

#### Tenant-aware media proxy

Tenant media files cannot be fetched safely by the browser or Next/Image via raw backend URLs alone because `GET /api/media/files/{fileName}` still depends on tenant context.

- `lib/utils.ts` rewrites CMS media file paths from `/api/media/...` to storefront-local `/cms-media/...`
- `app/cms-media/[...path]/route.ts` proxies the request to backend media endpoints with tenant headers and `force-cache`
- The route adds `X-Tenant-Subdomain` or `X-Tenant-ID` before streaming the file
- This keeps `next/image` compatible with tenant-scoped media delivery and avoids `Tenant identifier required` failures

**`/api/media` pass-through:**

`app/api/media/[...path]/route.ts` exists as a Next.js App Router catch-all to prevent `/api/media/files/{uuid}.jpg` requests from falling through to the `[lang]/[[...slug]]` catch-all page. Without this handler, `api` would pass the `isValidLocaleFormat` regex check and trigger a spurious CMS page lookup (`lang=API`). The route proxies identically to `cms-media` — same upstream path, same tenant headers, same `force-cache` strategy. `proxy.ts` also explicitly early-returns for `/api/*` requests in both the guard block and the `matcher` config.

### CSS targeting

Template name appears **once** on the template's outer `<div>` (`cms-page {templateName}`), not repeated on every slot.
`CmsSlot` emits `class="cms-slot {slotName}"` only.

```html
<!-- Rendered structure -->
<div class="cms-page LandingPageTemplate space-y-8">
  <section class="cms-slot Section1" data-slot-name="Section1"> ... </section>
  <section class="cms-slot Section2" data-slot-name="Section2"> ... </section>
  <section class="cms-slot Section3" data-slot-name="Section3"> ... </section>
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
| `proxy.ts` | **Permissive format check only** — accepts any `[a-z]{2,3}` segment; no hardcoded locale list |
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
| `normalizeLanguage(v)` | `string \| undefined` → API locale string; `undefined` returns `""` (filtered by `buildUrl`) |
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

| Function | `cache()` | Strategy | Reason |
| --- | --- | --- | --- |
| `resolvePage` | ✅ | `revalidate: 30s` | Called in both `generateMetadata` and page component |
| `fetchSiteConfig` | ✅ | `revalidate: 300s` | Called in 6+ files per render |
| `fetchProduct` | ✅ | `revalidate: 30s` | Called in both `generateMetadata` and page component |
| `fetchProductsByCategory` | ✅ | `revalidate: 30s` | Called in both `generateMetadata` and page component |
| `fetchMediaByUids` | ❌ | `revalidate: 300s` | Not called multiple times per render |
| `searchProducts` | ❌ | `cache: "no-store"` | Dynamic query — must be fresh on every request |

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

`fetchSiteConfig()` is shared by the root layout, locale layout, page metadata, and maintenance guard. When it returns `null`, those surfaces throw because the storefront cannot determine locale, metadata, or shell configuration safely.

### Error boundary

`app/[lang]/error.tsx` is a `"use client"` error boundary for any uncaught errors inside the locale subtree. It shows a Türkçe error message with a retry button. Root-level errors (outside `[lang]`) are not caught here.

## Component Registry

Located in `components/cms/cms-components.config.tsx` (config) and `components/cms/registry/index.ts` (dispatcher). Maps `component.type` (from delivery API) to a React renderer. Landing page types are wired inline via `makeMediaRenderer` from `components/cms/renderers/renderer-factory.tsx` — no separate renderer files. Generic types have individual renderer files under `components/cms/renderers/`. To add a new type: add a model type to `components/theme/default/models.ts`, add a `buildXxxModel` to `components/theme/default/builders.ts`, then add one `makeMediaRenderer(buildXxxModel, (m) => <Section model={m} />)` line in `cms-components.config.tsx`.

### Landing page — type-specific (via `makeMediaRenderer` factory)

| Component type | Builder | UI section |
|---|---|---|
| `HeroBannerComponent` | `buildHeroModel` | Hero full-screen |
| `AboutBannerComponent` | `buildAboutModel` | About 3-column |
| `VideoSectionComponent` | `buildVideoModel` | Video embed |
| `AwardBannerComponent` | `buildAwardModel` | Award dark bg |
| `ServiceCardComponent` | `buildServiceModel` | Accordion list |
| `ProjectCardComponent` | `buildProjectsModel` | Horizontal scroll |
| `InstagramSectionComponent` | `buildInstagramModel` | Floating images |
| `MarqueeTextComponent` | `buildMarqueeModel` | Scrolling marquee |

### Generic renderers

| Component type | Renderer | Notes |
|---|---|---|
| `NavigationComponent` | `NavigationCmsComponent` | Delegates to `NavigationRenderer` or `StaticNavRenderer` based on `navigationType` |
| `SimpleBannerComponent` | `GenericBannerRenderer` | title + image + CTA (general-purpose fallback) |
| `FeatureCardComponent` | `PortfolioGridRenderer` | Portfolio/feature card grid |
| `CMSParagraphComponent` | `ParagraphRenderer` | Plain text / HTML block |
| `CMSLinkComponent` | `LinkRenderer` | Entry-based link list |
| `CMSImageComponent` | `ImageRenderer` | Responsive image via `CmsImage` |
| `RotatingImagesComponent` | `CarouselRenderer` | Simple horizontal image carousel |
| `CustomerReviewComponent` | `TextBlockRenderer` | Placeholder |
| `ImageMapComponent` | `TextBlockRenderer` | Placeholder |
| `PricingTableComponent` | `TextBlockRenderer` | Placeholder |
| Unknown types | `UnknownComponent` | Dev: red dashed box; prod: `null` |

Header and footer are not rendered through the component registry. `CmsPage` resolves them at the slot level via `ThemeHeaderSlot` / `ThemeFooterSlot`.

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

### Media UID alignment

Component entries store media references as `mediaUid` strings in `component_entry_i18n.custom_data` (e.g. `{"mediaUid": "homepage-hero-bg"}`). The storefront batch-fetches these via `GET /api/cms/media?uids=homepage-hero-bg&...`. When media is uploaded through the admin Media Library, the system assigns auto-generated UIDs (`cmsitem_26597598`). These do not match the semantic UIDs the component seed expects, so images do not appear on the storefront.

**Solution:** `seed_liko_media_uids.sql` updates media record UIDs to match the semantic names the component entries reference, identified by `original_name`:

| Semantic UID | `original_name` | Section |
|---|---|---|
| `homepage-hero-bg` | `hero-bg-1.jpg` | Hero |
| `homepage-about-1/2/3` | `ab-1.jpg`, `ab-2.jpg`, `ab-3.jpg` | About |
| `homepage-project-1…7` | `project-1.jpg` … `project-7.jpg` | Projects |
| `homepage-award-1` | `award-1.png` | Award |
| `homepage-instagram-1…6` | `insta-1.jpg`, `insta-2.jpg`, `insta-inner-3/5/6/7.jpg` | Instagram |
| `site-logo-light` | `logo-white.png` | Site logo (dark bg) |
| `site-logo-dark` | `logo.png` | Site logo (light bg) |

The script also updates `sites.logo_media_uid` and `sites.logo_dark_media_uid` to the aligned UIDs.

**Not covered** (upload + bind manually): `homepage-video-poster`, `homepage-service-icon-1…4`, `homepage-instagram-7`.

**Alternative path:** For new media uploads, the admin `Bind` dialog writes a responsive media set assignment (`responsive_media_sets` + FK on component/entry), which the storefront reads via `component.responsive` / `entry.responsive`. This path bypasses `mediaUid` entirely and is preferred for new content.

**Responsive media width/height extraction:** Each renderer passes `component.responsive` (or entry `responsive`) to `toMediaModel` in `lib/cms-utils.ts`. `toMediaModel` receives either a flat `CmsMediaDelivery` object (from the `cms/media` batch) or a nested `ResponsiveMediaDelivery` `{ desktop: { url, width, height }, mobile }`. The mapper reads `width`/`height` from `record.width ?? desktopRecord?.width` to handle both shapes. Without this fallback the width/height would be `NaN` and the image would not render.

---

## Current limitations and extension points

- Product, category, and search pages include minimal UI beyond CMS slots.
- Header and footer render only from CMS shared slots; an empty or incomplete `Header` / `Footer` slot produces no chrome output.
- Instagram section works with partial image sets; full parity is achieved when the tenant imports the complete expected media set.
- Adding new UI languages requires a new `messages/{lang}.json` file and an entry in `AVAILABLE_MESSAGES` in `lib/i18n.ts`.
- Navigation node UIDs `LandingMainNavNode` (header main menu) and `LandingFooterNavNode` (footer sitemap) must be created in each tenant's CMS.

### Replacing the default theme

To swap the visual theme for the landing page sections, replace the section components in `components/theme/default/sections/`, model types in `components/theme/default/models.ts`, and builder functions in `components/theme/default/builders.ts`. Generic CMS utilities in `lib/cms-utils.ts` are theme-independent and remain unchanged.

The `Motion` animation layer (`components/theme/default/Motion.tsx`) is rendered directly by `LandingPageTemplate.tsx`. To disable or replace it, edit the template.
