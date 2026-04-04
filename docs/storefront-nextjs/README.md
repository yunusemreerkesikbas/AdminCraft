# Storefront Next.js

Headless demo/reference storefront for the platform CMS delivery APIs using the Next.js App Router. The project in this repository is deployed directly for the demo tenant and serves as the base fork for tenant-specific storefronts.

## Purpose

- Resolve CMS pages and render slots/components from the delivery API.
- Keep routes locale-prefixed (`/{lang}/...`) with SEO metadata derived from CMS page + site config.
- Drive locale support dynamically from the tenant's `SiteDeliveryResponse` (`defaultLanguage`, `enabledLanguages`).
- Support SSR by default and static export when `NEXT_OUTPUT=export` is set.

## Source of truth

- App routes: [`../../storefront-nextjs/app`](../../storefront-nextjs/app)
- Proxy (format check + maintenance): [`../../storefront-nextjs/proxy.ts`](../../storefront-nextjs/proxy.ts)
- Core locale utilities: [`../../storefront-nextjs/lib/core/i18n/locale.ts`](../../storefront-nextjs/lib/core/i18n/locale.ts)
- next-intl request config: [`../../storefront-nextjs/i18n/request.ts`](../../storefront-nextjs/i18n/request.ts)
- UI translation messages: [`../../storefront-nextjs/messages/`](../../storefront-nextjs/messages/)
- Core CMS client + loaders: [`../../storefront-nextjs/lib/core/cms/client.ts`](../../storefront-nextjs/lib/core/cms/client.ts), [`../../storefront-nextjs/lib/core/cms/loaders.ts`](../../storefront-nextjs/lib/core/cms/loaders.ts), [`../../storefront-nextjs/lib/types.ts`](../../storefront-nextjs/lib/types.ts)
- CMS render pipeline: [`../../storefront-nextjs/components/cms`](../../storefront-nextjs/components/cms)
- Layout shell: [`../../storefront-nextjs/app/[lang]/layout.tsx`](../../storefront-nextjs/app/%5Blang%5D/layout.tsx), [`../../storefront-nextjs/components/layout`](../../storefront-nextjs/components/layout)
- SEO metadata: [`../../storefront-nextjs/lib/core/seo/metadata.ts`](../../storefront-nextjs/lib/core/seo/metadata.ts)
- Runtime config: [`../../storefront-nextjs/next.config.ts`](../../storefront-nextjs/next.config.ts)
- Environment example: [`../../storefront-nextjs/.env.local.example`](../../storefront-nextjs/.env.local.example)

## Public delivery APIs used

All requests are tenant-scoped via headers set in `lib/core/config/runtime-env.ts` and `lib/core/http/headers.ts`:
`X-Tenant-Subdomain` and `X-Tenant-ID`.

| Function | Endpoint | Notes |
| --- | --- | --- |
| `getCmsPage` | `GET /api/cms/pages` | `lang` required; `pageType`, `pageLabelOrId`, `code` optional |
| `getSiteConfig` | `GET /api/cms/site` | Site metadata, `defaultLanguage`, `enabledLanguages`, `isRtl`; optional `lang` query param for localized values |
| `getShell` | `GET /api/cms/shell` | Pre-built header/footer layout with sections; `lang` optional; `revalidate: 300s` |
| `getMediaByUids` | `GET /api/cms/media?uids=a,b,c` | Batched media resolution via comma-separated UIDs |
| `getProduct` | `GET /api/cms/products/{uid}` | Product detail payload |
| `getCategoryProducts` | `GET /api/cms/products/category/{categoryUid}` | Paged list |
| `searchProducts` | `GET /api/cms/products/search` | Query param: `q` |
| `getSitemapPages` | `GET /api/cms/pages/sitemap` | Sitemap-eligible published pages; `revalidate: 3600s` |
| `app/robots.ts` | `GET /api/cms/robots.txt` | Plain-text robots.txt; respects `indexingEnabled` from `SiteTechnicalSettings` |

## Routing and page resolution

| Route | Resolution |
| --- | --- |
| `/` | `loadSiteConfig()` → `site.defaultLanguage`; if site unavailable the root page throws (`app/page.tsx`) |
| `/{lang}` | Layout validates `lang` against `loadSiteConfig(lang)` → `site.enabledLanguages`; if site config is unavailable the layout throws instead of silently skipping validation |
| `/{lang}` | `loadHomepage(lang)` |
| `/{lang}/**` | `loadContentPage(lang, "/{slug}")` |
| `/{lang}/products/{uid}` | `loadProductPage(lang, uid)` |
| `/{lang}/c/{categoryUid}` | `loadCategoryPage(lang, categoryUid)` |
| `/{lang}/search?q=...` | `loadSearchPage(lang, q)` |
| `/{lang}/maintenance` | Rendered by `app/[lang]/[[...slug]]/page.tsx` (maintenance fallback view) |

Missing pages use `app/[lang]/not-found.tsx`.

### Maintenance mode

Maintenance mode is enforced at the edge in [`proxy.ts`](../../storefront-nextjs/proxy.ts):

- Calls `GET /api/cms/site` with `cache: "no-store"` on each request.
- If `maintenanceMode === true`, redirects all page routes to `/{lang}/maintenance`.
- `/_next/*`, `/api/*`, `/cms-media/*`, `/robots.txt`, `/sitemap.xml`, and `/{lang}/maintenance` are excluded.
- Fail-open: if the CMS call fails, no redirect is applied.

### Hostname validation

`proxy.ts` also enforces hostname isolation when `TENANT_HOSTNAME` env var is set. Requests whose `host` header does not match `TENANT_HOSTNAME` receive HTTP 404 immediately — before any CMS call. This prevents wildcard DNS rules (e.g. `*.craftive.io → same server`) from serving one tenant's storefront for another tenant's subdomain. Leave `TENANT_HOSTNAME` unset in local dev.

The maintenance page itself is rendered in the catch-all route:
`app/[lang]/[[...slug]]/page.tsx`.

### Language validation layers

| Layer | Source | Behaviour when unavailable |
| --- | --- | --- |
| `proxy.ts` | Regex `[a-z]{2,3}` | Always runs |
| `app/page.tsx` | `loadSiteConfig()` → `defaultLanguage` | Root page throws if site config is unavailable |
| `app/[lang]/layout.tsx` | `loadSiteConfig(lang)` → `enabledLanguages` | Throws if site config is unavailable; otherwise invalid locales hit `notFound()` |

`app/[lang]/layout.tsx` now validates locale access entirely from the public site delivery payload. The storefront no longer depends on an auth-protected tenant detail endpoint for locale enforcement.

## CMS render pipeline

Template-driven rendering — equivalent of Spartacus's `cx-storefront` + `cx-page-slot` pattern.

1. `CmsPage` calls `buildSlotMap(page)` → `{ slotName: slot }` map from `contentSlots`. The backend sets `contentSlot[].slotId = slotName + "Slot"` and `contentSlot[].position = positionEnum` (e.g. `"TOP"`). `buildSlotMap` derives the key from `slotId` by stripping the `"Slot"` suffix — so `"Section1Slot"` → `"Section1"` — matching the slot names used in template components.
2. `resolveTemplate(page.template)` looks up the template component from `templateRegistry`.
   - Unknown template → renders `null`. Template contracts are expected to stay in sync with backend `page_templates`.
3. The resolved `TemplateComponent` receives `{ slotMap, page }` props.
4. `CmsPage` renders shared `Header` and `Footer` shell outside `<main>`, controlled by the page template's `shell` config.
   - If the template enables the header (`shellConfig.header !== false`), `HeaderSlot` renders. It fetches shell data from `GET /api/cms/shell` independently of page slots.
   - If the template enables the footer (`shellConfig.footer !== false`), `FooterSlot` renders the same way.
   - Header/footer components are no longer sourced from page `contentSlots`; they are fetched from the shell endpoint based on `customFields.layoutRole` on the backend.
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
- Theme model types: `components/theme/models.ts` (`HeroModel`, `AboutModel`, `VideoModel`, etc.)
- Theme model builders: `components/theme/builders.ts` (`buildHeroModel`, `buildAboutModel`, `buildVideoModel`, etc.)
- Motion layer: `components/theme/Motion.tsx`
- Section components (visual layer, unchanged): `components/theme/sections/`
- Shared UI primitives: `components/theme/utils/shared.tsx` (`ThemeTag`, `ThemeButton`, `ThemeCircleButton`)
- Icons: `components/theme/utils/icons.tsx`
- Theme CSS: `components/theme/theme.module.css`

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

All 8 landing types use `makeMediaRenderer(buildXxxModel, (m) => <Section model={m} />)` in `cms-components.config.tsx` — there are no separate renderer files for these types. Adding a new type requires: (1) a model type in `components/theme/models.ts`, (2) a `buildXxxModel` in `components/theme/builders.ts`, (3) one `makeMediaRenderer(...)` line in `cms-components.config.tsx` — no template changes.

#### Field mapping

Each renderer calls a `buildXxxModel` function from `components/theme/builders.ts`, which reads CMS content from `component_i18n`, `component_entries`, `entry_i18n`, and `customFields`.

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

The landing page depends on tenant-scoped content imports. Theme-owned page templates, slots and components are not preloaded on tenant creation.

1. Import `backend/src/main/resources/impex/base/base_site_settings.sql`.
2. Import `backend/src/main/resources/impex/base/base_media_formats.sql`.
3. Import `backend/src/main/resources/impex/base/base_component_types.sql`.
4. Import `backend/src/main/resources/impex/base/base_entry_field_definitions.sql`.
5. Import `backend/src/main/resources/impex/base/base_product_types.sql`.
6. Import `backend/src/main/resources/impex/theme/liko/liko_foundation.sql` (theme-owned page templates, template slots, shared header/footer chrome and navigation data).
7. Upload image/video assets in Media Library for the tenant.
8. Import `backend/src/main/resources/impex/theme/liko/homepage.sql` (homepage components, homepage slot wiring, homepage component type migration and homepage media UID alignment).
9. Import `backend/src/main/resources/impex/theme/liko/about_page.sql` (optional) if the tenant should have a sample About page at `/{lang}/about-us`.
10. Import `backend/src/main/resources/impex/theme/liko/service_page.sql` (optional) if the tenant should have a sample Service page at `/{lang}/service`.
11. For assets not covered by the seed, either bind them via the admin `Bind` dialog or use responsive media assignments directly on the component/entry.

If these imports are missing or partial, homepage can render incomplete sections because the storefront resolves strictly from CMS payload.

#### Header and footer

Header and footer are CMS-driven through a dedicated shell endpoint. The backend aggregates all published shell components and pre-builds the layout model; the storefront only renders.

- Entry point: `components/cms/CmsPage.tsx`
- Shell slot renderers: `components/layout/shell/HeaderSlot.tsx`, `components/layout/shell/FooterSlot.tsx`
- Backend delivery endpoint: `GET /api/cms/shell?lang={lang}`
- Presentational shell: `components/theme/layout/SiteHeader.tsx`, `components/theme/layout/SiteFooter.tsx`
- CMS client: `lib/core/cms/client.ts` → `getShell(lang)`

The backend (`ShellDeliveryServiceImpl`) queries all `PUBLISHED` components whose `custom_data.layoutRole` matches `header.*` or `footer.*` and groups them into `header.primaryBlocks`, `header.secondaryBlocks`, `header.mainNavigation` (with pre-built `sections[]`), `footer.primaryBlocks`, and `footer.bottomBlocks`. The storefront receives this pre-grouped structure and renders it directly — no client-side `layoutRole` dispatch.

Navigation sections (`mainNavigation.sections[]`) are built by the backend from the navigation tree: entry-based nodes produce standalone links; child nodes with multiple entries produce dropdown sections.

##### Navigation rendering (CMS page components)

Navigation components placed in page slots (e.g. footer sitemap) are rendered via `NavigationCmsComponent`:

- `navigationType = MAINMENU` → `NavigationRenderer` — hierarchical tree using `entries[].resolvedHref`
- `navigationType = STATICPAGE` → `StaticNavRenderer` — flat list directly from `navigationNode.flatLinks[]`

Locale-prefixing is performed entirely by the backend (`NavigationDeliveryUtils`). The storefront does **not** resolve hrefs — it renders the pre-computed `resolvedHref` and `flatLinks[]` from the delivery response. `nav-utils.ts` (`resolveNavigationEntry`) simply maps `resolvedHref` to a `LayoutLinkModel`; no `lang` parameter is needed.

##### Shared layout slot contract

Components are no longer sourced from page slots. Any `PUBLISHED` component with a matching `customFields.layoutRole` is included by the shell endpoint, regardless of slot binding.

| Area | `layoutRole` | Component type | Example UID | Purpose |
| --- | --- | --- | --- | --- |
| Header | `header.mainNavigation` | `NavigationComponent` | `StorefrontHeaderMainNavigation` | Main off-canvas menu tree (with `sections[]`) |
| Header | `header.primary.*` or `header.contactInfo` | `CMSLinkComponent` | `StorefrontHeaderContactInfo` | Primary info blocks (contact, CTA) |
| Header | `header.*` (other) | `CMSLinkComponent` | `StorefrontHeaderSocialLinks` | Secondary info blocks (social links) |
| Footer | `footer.brandBlock` | `CMSParagraphComponent` | `StorefrontFooterBrandBlock` | Brand text / intro copy |
| Footer | `footer.sitemapNavigation` | `NavigationComponent` | `StorefrontFooterSitemapNavigation` | Footer sitemap column |
| Footer | `footer.officeLinks` | `CMSLinkComponent` | `StorefrontFooterOfficeLinks` | Address / phone / email column |
| Footer | `footer.newsletter` | `CMSLinkComponent` | `StorefrontFooterNewsletter` | Newsletter title + placeholder/button labels via entry `custom_data` |
| Footer | `footer.socialLinks` or `footer.bottom.*` | `CMSLinkComponent` | `StorefrontFooterSocialLinks` | Copyright bar social links (bottomBlocks) |

##### Shared layout field mapping

| Area | Data source |
| --- | --- |
| Header logo / languages | `GET /api/cms/site` (via `getSiteConfig`) |
| Header main navigation + sections | `GET /api/cms/shell` → `header.mainNavigation.sections[]` (pre-built by backend) |
| Header primary/secondary blocks | `GET /api/cms/shell` → `header.primaryBlocks[]` / `header.secondaryBlocks[]` |
| Footer primary blocks | `GET /api/cms/shell` → `footer.primaryBlocks[]` |
| Footer bottom blocks (social, copyright) | `GET /api/cms/shell` → `footer.bottomBlocks[]` |
| Links within each block | `block.links[]` — resolved and localized by backend (`LayoutLinkDelivery`) |
| Newsletter fields | `block.newsletterPlaceholder`, `block.newsletterButtonLabel` (from first entry's `custom_data`) |

#### Tenant-aware media proxy

Tenant media files cannot be fetched safely by the browser or Next/Image via raw backend URLs alone because `GET /api/media/files/{fileName}` still depends on tenant context.

- `lib/core/media/url.ts` rewrites CMS media file paths from `/api/media/...` to storefront-local `/cms-media/...`
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

### `lib/core/i18n/locale.ts` helpers

| Export | Purpose |
| --- | --- |
| `FALLBACK_LOCALE` | Hard fallback (`"tr"`) for when site config is unavailable |
| `isValidLocaleFormat(v)` | Middleware format gate: `/^[a-z]{2,3}$/` |
| `toUrlLocale(code)` | API `"TR"` → URL `"tr"` |
| `toApiLocale(lang)` | URL `"tr"` → API `"TR"` |
| `isRtlByConfig(lang, enabledLanguages)` | Reads `isRtl` from the matching `LanguageInfo` entry |
| `requireMessageLocale(lang)` | Maps lang to a bundled messages file; throws if the locale format is invalid or no message catalog exists |
| `normalizeLanguage(v)` | `string \| undefined` → API locale string; `undefined` returns `""` (filtered by `buildUrl`) |
| `withLocalePath(locale, path)` | Builds localized links (`/tr/search`) |

### next-intl (Shared layout translations)

Used for static UI strings (navigation labels, page headings, error messages). **Not used for CMS content** — those come from the API with the `lang` param.

- Plugin registered in `next.config.ts` via `createNextIntlPlugin("./i18n/request.ts")`.
- `i18n/request.ts` resolves the correct message file via `requireMessageLocale`.
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

`components/theme/layout/SiteHeader.tsx` renders links for all of the tenant's `enabledLanguages`. It replaces the `[lang]` segment in the current pathname to build the target URL. Hidden when `enabledLanguages.length <= 1`.

## SEO metadata

`buildPageMetadata` (`lib/core/seo/metadata.ts`) derives title/description/canonical/robots/OG/hreflang from:

- CMS page fields (`title`, `description`, `robotTag`, `canonicalUrl`)
- Site config (`seo.*`, `searchEngine.defaultRobots`, `canonicalBaseUrl`, `enabledLanguages`, `defaultLanguage`, `ogImageUrl`)

### Robots resolution

`parseRobotsTag(tag, defaultRobots)` resolves in priority order:

1. Page-level `robotTag` (e.g. `NOINDEX_FOLLOW`)
2. `site.searchEngine.defaultRobots` (set via Site Settings → `global.robots`)
3. Built-in constant `"noindex,nofollow"` (`SAFE_DEFAULT_ROBOTS` in `lib/core/seo/metadata.ts`)

### Hreflang

`buildAlternateLanguages` emits `<link rel="alternate" hreflang="...">` for every enabled language plus `x-default` (pointing to the default language). Skipped when `canonicalBaseUrl` is missing or only one language is enabled.

### JSON-LD structured data

- `lib/core/seo/schema.ts` — `buildOrganizationSchema(site)` and `buildWebSiteSchema(site, lang)` builders.
- `app/layout.tsx` — injects `WebSite` schema on every page (when `canonicalBaseUrl` is set).
- `app/[lang]/[[...slug]]/page.tsx` — injects `Organization` schema on the homepage only.

### Dynamic sitemap

`app/sitemap.ts` generates `/sitemap.xml` from `getSitemapPages(lang)` for each enabled language. Returns `[]` when `searchEngine.sitemapEnabled` is false or `canonicalBaseUrl` is unset.

Page inclusion is fully backend-driven — `GET /api/cms/pages/sitemap` returns only eligible published pages. The homepage (canonical_url `/`) is included by the backend when published; priority is derived from `canonicalUrl`:

- `"/"` → `priority: 1`
- anything else → `priority: 0.8`

### robots.txt

`app/robots.ts` fetches the backend `GET /api/cms/robots.txt` response as plain text and converts it into Next.js `MetadataRoute.Robots`. This preserves:

- Admin-configured custom rules (e.g. `Disallow: /admin/`)
- `Sitemap: /sitemap.xml` directive
- Multi user-agent blocks

Fail-safe: any network error or non-2xx response returns `User-agent: *\nDisallow: /`.

`app/robots.ts` is the active Next.js Metadata API entrypoint for robots generation in this storefront.

Each route uses `generateMetadata` together with the composite page loaders from `lib/core/cms/loaders.ts`.

## Environment configuration

Read from `.env.local.example` and `lib/core/config/runtime-env.ts`:

| Variable | Required | Notes |
|---|---|---|
| `NEXT_PUBLIC_CMS_API_URL` | Yes | Base URL for CMS delivery API (baked into client bundle at build time) |
| `TENANT_SUBDOMAIN` | **Yes** | Required for proxy routing (`proxy.ts` reads only this var). Also sent as `X-Tenant-Subdomain` on every CMS request. Omitting this causes all routes to return 404. |
| `TENANT_ID` | No | Sends `X-Tenant-ID` header for CMS identification only. Does **not** replace `TENANT_SUBDOMAIN` for proxy routing. Set both if you prefer ID-based CMS auth alongside subdomain routing. |
| `TENANT_HOSTNAME` | No | Expected hostname for this deployment. Requests from other hostnames return 404 (see Hostname validation). Leave unset in local dev. |
| `NEXT_IMAGE_DOMAINS` | No | Comma-separated hostnames allowed for `next/image` optimization. CMS API hostname and localhost are auto-included. |
| `NEXT_OUTPUT=export` | No | Enables static export mode in `next.config.ts`. |
| `NEXT_PUBLIC_GA_ID` | No | Google Analytics measurement ID. |
| `NEXT_PUBLIC_GTM_ID` | No | Google Tag Manager container ID. |
| `NEXT_PUBLIC_GOOGLE_SITE_VERIFICATION` | No | Search Console HTML tag token. Must use `NEXT_PUBLIC_` prefix — see note below. Local development may define it in `.env.development`. In this repository, the demo/reference storefront keeps the stage and prod values in tracked `.env.staging` and `.env.production` files. Tenant storefront repositories manage their own value in their own repo/build config. |

`TENANT_SUBDOMAIN` is always required for the proxy. `TENANT_ID` is optional and affects only the CMS `X-Tenant-ID` header.

### Local multi-environment startup

- `npm run start` uses `.env.development` and keeps `.env.local` overrides active for local backend work.
- `npm run start:stage` starts the local dev server against `.env.staging` so you can test the stage API locally.
- `npm run start:prod` starts the local dev server against `.env.production` so you can test the prod API locally.
- `npm run serve` / `npm run serve:stage` / `npm run serve:prod` are for running a previously built SSR server with the matching env profile.
- Explicit `*:stage` and `*:prod` scripts preload their target env file before Next.js boots, so `.env.local` does not override them.
- Local stage/prod scripts clear `TENANT_HOSTNAME` before boot so localhost requests are not rejected by hostname isolation.

### Deployment model

`storefront-nextjs` is a **single-tenant per deployment** project. Each deployment knows its own tenant statically via env vars — there is no runtime hostname-to-tenant resolution.

| Deployment | `TENANT_SUBDOMAIN` | `TENANT_HOSTNAME` |
|---|---|---|
| Platform demo (stage) | `demo` | `s1-demo.craftive.io` |
| Platform demo (prod) | `demo` | `demo.craftive.io` |
| Tenant fork (stage) | `acme` | `s1-acme.craftive.io` |
| Tenant fork (whitelabel) | `acme` | `acme.com` |

For tenant forks, only `components/theme/` changes. Env vars and deployment config are set independently per fork instance.

Search Console verification follows the same deployment model:

- each storefront deployment can expose one `NEXT_PUBLIC_GOOGLE_SITE_VERIFICATION` token
- stage and prod may use different tokens
- if the variable is unset, no verification meta tag is rendered
- use the Search Console `HTML tag` method and copy only the `content` attribute value
- this repository commits the demo/reference storefront tokens in `storefront-nextjs/.env.staging` and `storefront-nextjs/.env.production`
- tenant storefront repositories must define their own verification token in their own repo/build config; the platform demo values are not reused for tenant storefronts

> **Why `NEXT_PUBLIC_` is required:** `app/layout.tsx` is a Server Component and reads env vars at **request time**. In a Docker 2-stage build, `next build` copies `.env.production` into the `.next/standalone` output. At runtime the container only has `NODE_ENV=production` and no other env files, so Next.js loads `.env.production` — regardless of which env file was passed to the build step via `dotenv-cli`. Without `NEXT_PUBLIC_`, a stage build always serves the production key. With `NEXT_PUBLIC_`, Next.js/SWC **inlines the value as a literal string at build time**, making the deployed output independent of runtime env file loading.

## Caching and revalidation

Leaf CMS client functions in `lib/core/cms/client.ts` use plain fetch helpers. Request deduplication lives in Next.js fetch memoization, while composite route loaders in `lib/core/cms/loaders.ts` use React `cache()`.

| Function | `cache()` | Strategy | Reason |
| --- | --- | --- | --- |
| `getCmsPage` / `getSiteConfig` / `getShell` | ❌ | `revalidate: 30s/300s` | Fetch-only helpers; rely on Next.js request memoization |
| `loadHomepage` / `loadContentPage` / `loadProductPage` | ✅ | Composite loader cache | Shared between `generateMetadata` and page render |
| `loadShellData` | ✅ | Composite loader cache | Shared by `HeaderSlot` and `FooterSlot` |
| `getSitemapPages` | ❌ | `revalidate: 3600s` | Called once per language in `sitemap.ts` |
| `getMediaByUids` | ❌ | `revalidate: 300s` | Not called multiple times per render |
| `searchProducts` | ❌ | `cache: "no-store"` | Dynamic query — must be fresh on every request |

## Security and tenant isolation

Delivery calls are public but tenant-scoped by headers set in `lib/core/http/headers.ts`.
Locale validation happens at the layout level (not middleware) to allow tenant-specific enabledLanguages to drive redirects without edge API calls.

## Implementation guide

### 1) Homepage

- Resolve page via `loadHomepage(lang)`.
- Build metadata from the same loader result.
- Render with `CmsPage`.

### 2) Content page

- Build `pageLabelOrId` as `"/" + slug.join("/")`.
- Resolve with `loadContentPage(lang, pageLabelOrId)`.
- Render with `CmsPage`.

### 3) Product page

- Resolve CMS template + product detail with `loadProductPage(lang, uid)`.
- Render CMS slots + product section.

### 4) Adding a new UI string

1. Add the key to `messages/tr.json` and `messages/en.json`.
2. Call `const translate = await getTranslations("Namespace")` in the server component.
3. Use `translate("key")` in JSX.

### 5) Enabling a new language for a tenant

No code changes required. Add the language via the admin Site Settings UI. The storefront reads `enabledLanguages` from the API at runtime. If no `messages/{lang}.json` exists, the UI falls back to `tr.json`; CMS content is served in the requested language by the backend.

## Error handling

### `lib/core/http/fetch-json.ts` request behaviour

| Status | Behaviour |
| --- | --- |
| 404 | Returns `null` |
| 429 or 5xx | Logs `[CMS] Request failed {status}: {url}` to server console; returns `null` |
| Other non-2xx | Throws `Error` (propagates to page) |

`null` from `getCmsPage` is converted to `notFound()` by loader invariants, showing `app/[lang]/not-found.tsx` instead of crashing.

`getSiteConfig()` is shared by the root layout, locale layout, page metadata, shell loader, and maintenance guard. When it returns `null`, invariant helpers throw because the storefront cannot determine locale, metadata, or shell configuration safely.

### Error boundary

`app/[lang]/error.tsx` is a `"use client"` error boundary for any uncaught errors inside the locale subtree. `app/global-error.tsx` catches root-level failures.

## Component Registry

Located in `components/cms/cms-components.config.tsx` (config) and `components/cms/registry/index.ts` (dispatcher). Maps `component.type` (from delivery API) to a React renderer. Landing page types are wired inline via `makeMediaRenderer` from `components/cms/renderers/renderer-factory.tsx` — no separate renderer files. Generic types have individual renderer files under `components/cms/renderers/`. To add a new type: add a model type to `components/theme/models.ts`, add a `buildXxxModel` to `components/theme/builders.ts`, then add one `makeMediaRenderer(buildXxxModel, (m) => <Section model={m} />)` line in `cms-components.config.tsx`.

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
| `ContentHeroComponent` | `buildContentHeroModel` | Content page hero |
| `ServiceHeroComponent` | `buildServiceHeroModel` | Service page hero |
| `ServiceCardsGridComponent` | `buildServiceModel` | Service cards grid |
| `ServicePanelComponent` | `buildServicePanelModel` | Service detail panel |
| `SplitMediaIntroComponent` | `buildSplitMediaIntroModel` | Split media intro |
| `PeopleCarouselComponent` | `buildPeopleCarouselModel` | People carousel |
| `StatsGridComponent` | `buildStatsGridModel` | Statistics grid |
| `LogoMarqueeComponent` | `buildLogoMarqueeModel` | Logo marquee |
| `BrandGridComponent` | `buildBrandGridModel` | Brand grid |
| `ImageMarqueeComponent` | `buildImageMarqueeModel` | Image marquee |
| `AwardsShowcaseComponent` | `buildAwardsShowcaseModel` | Awards showcase |
| `BigTextCtaComponent` | `buildBigTextCtaModel` | Big text CTA |

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
| Unknown types | `UnknownComponent` | Dev: red dashed box; prod: `null` |

Header and footer are not rendered through the component registry. `CmsPage` resolves them at the slot level via `HeaderSlot` / `FooterSlot`.

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

**Solution:** `theme/liko/homepage.sql` updates media record UIDs to match the semantic names the homepage component entries reference, identified by `original_name`:

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
- Header and footer render from `GET /api/cms/shell`; if no published components have a matching `layoutRole`, the shell endpoint returns nothing and no shared layout is rendered.
- Instagram section works with partial image sets; full parity is achieved when the tenant imports the complete expected media set.
- Adding new UI languages requires a new `messages/{lang}.json` file and an entry in `BUNDLED_MESSAGE_LOCALES` in `lib/core/i18n/locale.ts`.
- Navigation node UIDs `LandingMainNavNode` (header main menu) and `LandingFooterNavNode` (footer sitemap) must be created in each tenant's CMS.

### Replacing the theme

`storefront-nextjs` is the demo/reference storefront and tenant base project. Fork the project and replace `components/theme/` for each tenant's visual identity. The CMS infrastructure (`components/cms/`, `components/layout/shell` adapters, `lib/`) is never modified.

**Theme boundary:** `components/theme/` is the only directory that changes between forks.

```
components/theme/
  builders.ts          ← model builder functions (contract with CMS pipeline)
  models.ts            ← TypeScript model types (contract with builders)
  sections/            ← 14 visual section components
  utils/               ← shared UI primitives and icons
  Motion.tsx           ← landing page animation layer
  ContentPageMotion.tsx
  theme.module.css
  content-page.module.css
  layout/
    SiteHeader.tsx     ← visual header component
    SiteFooter.tsx     ← visual footer component
    shell.module.css   ← header/footer styles
```

**To create a new theme:**

1. Replace `components/theme/` with your own implementation.
2. `builders.ts` — keep the same exported function names (`buildHeroModel`, etc.) as they are the contract with `cms-components.config.tsx`.
3. `models.ts` — keep the same type names; they are referenced by builders.
4. `sections/` — write your own UI section components.
5. `layout/SiteHeader.tsx` + `SiteFooter.tsx` — write your own header/footer; they receive typed props (`LayoutBlockDelivery[]`, `NavigationSectionDelivery[]`) from `lib/types.ts` as delivered by `GET /api/cms/shell`.
6. `cms-components.config.tsx` — only touch this file to register new CMS component types; existing wiring stays unchanged.

The `Motion` animation layer (`components/theme/Motion.tsx`) is rendered directly by `LandingPageTemplate.tsx`. To disable or replace it, edit the template.
