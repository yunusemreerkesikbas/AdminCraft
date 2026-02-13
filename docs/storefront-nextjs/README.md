# Storefront Next.js Boilerplate

Headless storefront for AdminCraft CMS delivery APIs using the Next.js App Router.

## Purpose

- Provide a reference storefront that resolves CMS pages and renders slots/components.
- Keep routes locale-prefixed and SEO metadata derived from CMS page + site config.
- Support SSR by default and static export when `NEXT_OUTPUT=export` is set.

## Source of truth

- App routes: [`../../storefront-nextjs/app`](../../storefront-nextjs/app)
- Locale routing + middleware: [`../../storefront-nextjs/middleware.ts`](../../storefront-nextjs/middleware.ts), [`../../storefront-nextjs/lib/i18n.ts`](../../storefront-nextjs/lib/i18n.ts)
- CMS client + types: [`../../storefront-nextjs/lib/cms-client.ts`](../../storefront-nextjs/lib/cms-client.ts), [`../../storefront-nextjs/lib/types.ts`](../../storefront-nextjs/lib/types.ts)
- CMS render pipeline: [`../../storefront-nextjs/components/cms`](../../storefront-nextjs/components/cms)
- Layout shell: [`../../storefront-nextjs/app/[lang]/layout.tsx`](../../storefront-nextjs/app/[lang]/layout.tsx), [`../../storefront-nextjs/components/layout`](../../storefront-nextjs/components/layout)
- SEO metadata: [`../../storefront-nextjs/lib/seo.ts`](../../storefront-nextjs/lib/seo.ts)
- Runtime config: [`../../storefront-nextjs/next.config.ts`](../../storefront-nextjs/next.config.ts)
- Environment example: [`../../storefront-nextjs/.env.local.example`](../../storefront-nextjs/.env.local.example)

## Public delivery APIs used

All requests are tenant-scoped via headers set in `cms-client.ts`:
`X-Tenant-Subdomain` and `X-Tenant-ID`.

| Function | Endpoint | Notes |
| --- | --- | --- |
| `resolvePage` | `GET /api/cms/pages` | `lang` required; `pageType`, `pageLabelOrId`, `code` optional |
| `fetchSiteConfig` | `GET /api/cms/site` | Site metadata for SEO + layout |
| `fetchProduct` | `GET /api/cms/products/{uid}` | Product detail payload |
| `fetchProductsByCategory` | `GET /api/cms/products/category/{categoryUid}` | Paged list |
| `searchProducts` | `GET /api/cms/products/search` | Query param: `q` |
| `fetchCategoryTree` | `GET /api/cms/products/categories` | Category tree |

## Routing and page resolution

| Route | Resolution |
| --- | --- |
| `/` | Redirects to `/{DEFAULT_LOCALE}` (`app/page.tsx`) |
| `/{lang}` | `resolvePage(lang)` (homepage) |
| `/{lang}/**` | `resolvePage(lang, "ContentPage", "/{slug}")` |
| `/{lang}/products/{uid}` | `resolvePage(lang, "ProductPage", undefined, uid)` + `fetchProduct(uid)` |
| `/{lang}/c/{categoryUid}` | `resolvePage(lang, "CategoryPage", undefined, categoryUid)` + `fetchProductsByCategory` |
| `/{lang}/search?q=...` | `resolvePage(lang, "SearchResultPage")` + `searchProducts(q)` |

Missing pages use `app/[lang]/not-found.tsx`.

## CMS render pipeline

1. `CmsPage` normalizes slots from `page.contentSlots` and legacy `page.slots`.
2. Each `CmsSlot` renders components in order.
3. `CmsComponent` delegates to the registry (`components/cms/registry`).
4. Unknown types render with `UnknownComponent`.

`CmsImage` resolves responsive media URLs via `buildMediaUrl`.

## i18n and locale routing

- Locales are defined in `lib/i18n.ts` (`DEFAULT_LOCALE = "tr"`).
- `middleware.ts` redirects missing/unsupported locales to the default.
- `normalizeLanguage` uppercases the locale for CMS delivery calls.
- `withLocalePath` helps build localized links.

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

`cms-client.ts` uses `cache()` with Next fetch options:

- `resolvePage`, `fetchProduct` revalidate every 30s
- `fetchSiteConfig`, `fetchCategoryTree` revalidate every 300s
- `searchProducts` uses `cache: "no-store"`

## Security and tenant isolation

Delivery calls are public but tenant-scoped by headers set in `cms-client.ts`.
Locale information is injected via middleware for consistent server-side routing.

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

## Current limitations and extension points

- CMS component registry is empty; all components render `UnknownComponent`.
- Navigation is static (`components/layout/Navigation.tsx`).
- Product, category, and search pages include minimal UI beyond CMS slots.
