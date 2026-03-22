# Craftive Demo Storefront (Next.js)

Headless demo/reference storefront for the platform CMS delivery APIs, built with the Next.js App Router. This repository ships the shared core storefront implementation that is deployed directly for the demo tenant and forked per tenant for theme-specific storefronts.

## Requirements

- Node.js 20+

## Environment

Create `.env.local` with:

```bash
NEXT_PUBLIC_CMS_API_URL=https://cms.example.com/api
TENANT_SUBDOMAIN=demo
NEXT_PUBLIC_TENANT_SUBDOMAIN=demo
TENANT_ID=1
NEXT_PUBLIC_TENANT_ID=1
```

For stage and prod deployments, use `.env.staging` and `.env.production` as documented in [`../docs/global/environment-configuration.md`](../docs/global/environment-configuration.md).

## Core structure

- `app/`: App Router routes, metadata, proxy-compatible entrypoints
- `components/cms/`: CMS slot/component/template rendering pipeline
- `components/theme/`: demo theme and tenant/theme-specific visual layer
- `lib/core/config/`: runtime env and tenant context
- `lib/core/http/`: endpoint, header, query, JSON/text/stream fetch helpers
- `lib/core/cms/`: typed CMS client and composite page loaders
- `lib/core/i18n/`: locale helpers
- `lib/core/seo/`: metadata and JSON-LD builders
- `lib/core/media/`: media URL rewrite and tenant-aware proxy helpers

## Routing

All routes are locale-prefixed.

Examples:

- `/tr`
- `/tr/about`
- `/tr/products/ABC123`
- `/tr/c/electronics`
- `/tr/search?q=laptop`

Route orchestration uses loader helpers:

- homepage: `loadHomepage(lang)`
- content page: `loadContentPage(lang, slugPath)`
- product page: `loadProductPage(lang, uid)`
- category page: `loadCategoryPage(lang, categoryUid)`
- search page: `loadSearchPage(lang, query)`
- shell/header/footer: `loadShellData(lang)`

## Development

```bash
npm install
npm run dev
```

## Validation

```bash
npm run lint
npm run build
```

## Documentation

Detailed implementation guide: [`../docs/storefront-nextjs/README.md`](../docs/storefront-nextjs/README.md)

## Tenant Fork Model

- This project is the demo/reference storefront published by the platform repository.
- Tenant storefronts fork this codebase, preserve the shared CMS/runtime contract, and replace the visual theme implementation under `components/theme/`.
- Core CMS integration, tenant headers, locale handling, and SEO infrastructure stay aligned with the base project.
