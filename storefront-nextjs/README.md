# Craftive Demo Storefront (Next.js)

Headless demo/reference storefront for the platform CMS delivery APIs, built with the Next.js App Router. This repository ships the shared core storefront implementation that is deployed directly for the demo tenant and forked per tenant for theme-specific storefronts.

## Requirements

- Node.js 20+

## Environment

For local backend development, create `.env.local` with:

```bash
NEXT_PUBLIC_CMS_API_URL=http://127.0.0.1:8080/api
TENANT_SUBDOMAIN=demo
```

Stage and prod values live in `.env.staging` and `.env.production` as documented in [`../docs/global/environment-configuration.md`](../docs/global/environment-configuration.md).

Command contract:

- `yarn start` / `npm run start` uses local development mode and keeps Next.js `.env.local` override behavior.
- `yarn start:stage` / `npm run start:stage` runs the local dev server against stage settings from `.env.staging`.
- `yarn start:prod` / `npm run start:prod` runs the local dev server against prod settings from `.env.production`.
- `yarn serve` / `npm run serve` starts the built SSR server with production settings.
- Explicit `*:stage` and `*:prod` scripts preload their target env file before Next.js starts, so `.env.local` does not override them.
- Local stage/prod scripts also clear `TENANT_HOSTNAME`, so the app serves on `localhost` while still talking to the target API.

Search Console HTML tag verification is deployment-scoped:

- this repository keeps `GOOGLE_SITE_VERIFICATION` in tracked `.env.staging` and `.env.production` files for the demo/reference storefront
- tenant storefront repositories must define their own `GOOGLE_SITE_VERIFICATION` value in their own repo/build config
- do not treat platform `.env.stage` / `.env.prod` or platform secrets as tenant-specific verification storage

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
npm run start
```

Examples:

```bash
yarn start:stage
yarn start:prod
```

## Validation

```bash
npm run lint
npm run build
npm run build:stage
```

## Documentation

Detailed implementation guide: [`../docs/storefront-nextjs/README.md`](../docs/storefront-nextjs/README.md)

## Tenant Fork Model

- This project is the demo/reference storefront published by the platform repository.
- Tenant storefronts fork this codebase, preserve the shared CMS/runtime contract, and replace the visual theme implementation under `components/theme/`.
- Core CMS integration, tenant headers, locale handling, and SEO infrastructure stay aligned with the base project.
- Search Console verification tokens are not shared across tenant storefronts; each tenant repository owns its own deployment/build token when verification is needed.
