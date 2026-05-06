# Craftive Documentation

Craftive is a platform for **customizable project solutions** built with **Spring Boot (Java 21)**, **Angular 19**, and a **Next.js headless storefront**.
The platform uses **database-per-tenant** isolation (`platform_management` + `ac_subdomain_{id}`) and a Clean Architecture layout.

## How to use these docs

- Start here for architecture and conventions: [`global/architecture.md`](global/architecture.md)
- If you are touching security, always read: [`global/security-multi-tenancy.md`](global/security-multi-tenancy.md)
- For patterns that apply everywhere:
  - Prelaunch checklist: [`prelaunch.md`](prelaunch.md)
  - Environment configuration: [`global/environment-configuration.md`](global/environment-configuration.md)
  - Backend conventions: [`global/backend-patterns.md`](global/backend-patterns.md)
  - Viewer role coverage: [`global/viewer-role-coverage.md`](global/viewer-role-coverage.md)
  - Frontend conventions: [`global/frontend-patterns.md`](global/frontend-patterns.md)
  - Authentication: [`global/authentication.md`](global/authentication.md)
  - i18n + composite operations: [`global/i18n-and-composite.md`](global/i18n-and-composite.md)
  - Documentation patterns: [`global/documentation-patterns.md`](global/documentation-patterns.md)
  - Dialogs and shared UI: [`global/dialogs-and-ui.md`](global/dialogs-and-ui.md)
  - List views (pagination/sort/search): [`global/list-pagination-search.md`](global/list-pagination-search.md)
  - Validation framework: [`global/validation.md`](global/validation.md)
  - Database migrations: [`global/migrations.md`](global/migrations.md)
  - Testing patterns: [`global/testing.md`](global/testing.md)
  - DevOps & deployment: [`global/devops.md`](global/devops.md)

## Third-party integrations

- Google Analytics 4 (GA4): [`3rd-party/google-analytics-ga4.md`](3rd-party/google-analytics-ga4.md)
- Google Search Console + Chrome UX Report (CrUX): [`3rd-party/google-search-console-crux-seo-insights.md`](3rd-party/google-search-console-crux-seo-insights.md)

## Modules (admin APIs)

Tenant modules are defined in [`backend/src/main/java/com/backend/domain/enums/ModuleCode.java`](../backend/src/main/java/com/backend/domain/enums/ModuleCode.java).

- Core: [`modules/core.md`](modules/core.md)
- User Management: [`modules/user-management.md`](modules/user-management.md)
- **Site Dashboard**: [`modules/site-dashboard.md`](modules/site-dashboard.md) - Unified site management interface
- Page Builder: [`modules/pagebuilder.md`](modules/pagebuilder.md) _(core-managed capability in provisioning)_
- SmartEdit (admin WYSIWYG editor): [`modules/smartedit.md`](modules/smartedit.md) _(layered on top of Page Builder + CMS Delivery)_
- Site Settings: [`modules/site-settings.md`](modules/site-settings.md) _(integrated into Site Dashboard; not a provisioning module)_
- Media Library (DAM): [`modules/media.md`](modules/media.md) _(core-managed capability in provisioning)_
- Product Catalog: [`modules/product-catalog.md`](modules/product-catalog.md)
- Mail Marketing: [`modules/mail-marketing.md`](modules/mail-marketing.md) _(optional provisioning module)_
- Component Library: [`modules/component-library.md`](modules/component-library.md) _(core-managed capability in provisioning)_

Provisioning catalog note:
`core`, `product`, and `mail_marketing` are selectable modules in `/api/provisioning/modules/catalog`. `core` expands to core-managed capabilities at execution time. See [`modules/platform-provisioning.md`](modules/platform-provisioning.md).

Tenant module API note:
`/api/tenants/{id}/modules` and `/api/tenants/current/modules` expose only user-facing enabled modules. Runtime core dependencies (`media`, `component_library`, `pagebuilder`) are not returned as tenant feature flags.
Legacy internal rows for those core dependencies are removed by platform repair migration `V1.0.1__repair_internal_tenant_modules.sql`.

## Platform features (control-plane)

Platform features are not tenant modules, but they are critical for operating the system.

- Tenants, provisioning, module enablement, migration sync: [`modules/platform-provisioning.md`](modules/platform-provisioning.md)
- Platform Dashboard, Tenant Detail, Platform Settings, **landing demo requests (SUPER_ADMIN inbox)**: [`modules/platform-admin.md`](modules/platform-admin.md)
- Platform newsletter + mail campaign flows (+ admin UI routes `/:lang/mail-marketing`, `/:lang/platform-mail`): [`modules/mail-marketing.md`](modules/mail-marketing.md)
- Config Control Panel (`/config`) for reCAPTCHA recovery + global runtime overrides (email + platform reCAPTCHA keys): [`modules/config-control-panel.md`](modules/config-control-panel.md)

## Public delivery APIs (storefront)

Public APIs are still tenant-scoped (resolved by tenant headers/hostname), but **do not require authentication**.

- CMS delivery (`/api/cms/**`): [`modules/cms-delivery.md`](modules/cms-delivery.md)
- Tenant public contact ingest (`POST /api/public/contact-requests`): [`modules/cms-delivery.md`](modules/cms-delivery.md#public-contact-requests) — reCAPTCHA when enabled, optional `CF-Connecting-IP` trust and per-IP / per-tenant rate limits via `app.security.*` (see [`global/environment-configuration.md`](global/environment-configuration.md))

**Platform public (no tenant context)** — used by the marketing `landing/` site and CMS tooling, not tenant databases:

- `GET /api/platform/cms/config` — public reCAPTCHA flags / site key (see [`modules/platform-admin.md`](modules/platform-admin.md))
- `POST /api/platform/public/demo-requests` — landing contact/demo form ingest (same doc: **Landing demo requests**)

### Headless storefront (`storefront-nextjs/`)

Next.js 16 App Router demo/reference storefront consuming the CMS delivery APIs.

- Storefront guide: [`storefront-nextjs/README.md`](storefront-nextjs/README.md)
- This repository deploys the demo/reference storefront in stage and prod. Tenant storefronts fork this project and customize the theme layer while keeping the shared core CMS/runtime contract.
- Homepage body and shared chrome are CMS-driven. `LandingPageTemplate` renders Sections 1–8 via the generic `CmsSlot → CmsComponent → registry` pipeline; each slot dispatches by `component.type` to a dedicated async RSC renderer. Shared `Header` / `Footer` slots use the chrome adapter layer.
- Fresh tenant databases start with **empty CMS content tables** for theme-owned data. `page_templates`, `template_slots`, `pages`, `page_slots`, `components`, `slot_components`, and theme navigation/chrome content are populated manually via ImpEx after tenant creation.
- Required tenant seed/import flow for the default landing page (ImpEx, manual via Admin UI `/{lang}/impex`):
  - ImpEx: `impex/base/base_site_settings.sql` → `impex/base/base_media_formats.sql` → `impex/base/base_component_types.sql` → `impex/base/base_entry_field_definitions.sql` → `impex/base/base_product_types.sql` → `impex/theme/liko/liko_foundation.sql` → `[media upload]` → `impex/theme/liko/homepage.sql` → `impex/theme/liko/about_page.sql` (optional) → `impex/theme/liko/service_page.sql` (optional)
  - Mulayim portfolio homepage + listing + details: `impex/base/base_site_settings.sql` → `impex/base/base_media_formats.sql` → `impex/base/base_product_types.sql` → `impex/theme/mulayim/mulayim_foundation.sql` → `[media upload]` → `impex/theme/mulayim/portfolio_homepage.sql` → `impex/theme/mulayim/portfolio_page.sql` → `impex/theme/mulayim/portfolio_detail_pages.sql`. The Mulayim theme scripts seed their required generic component types, so `base_component_types.sql` and `base_entry_field_definitions.sql` are no longer required for this theme path. Mulayim defines only the slots each template needs, and later vertical sections can be added as additional components in an existing slot by `sort_order`.
- SSR by default; static export mode available via `NEXT_OUTPUT=export`
- Locale routing is **tenant-driven**: supported languages and default language come from `GET /api/cms/site`; no hardcoded locale list in the app
- UI chrome translations via `next-intl`; CMS content translations via `lang` API param
- Multi-environment scripts: `npm run start`, `npm run start:stage`, `npm run start:prod`, `npm run build`, `npm run build:stage`, `npm run serve`, etc.
- Environment configuration: [`global/environment-configuration.md`](global/environment-configuration.md)

### Marketing landing (`landing/`)

Static Next.js landing project for `craftive.io`.

- **Demo / contact flow**: [`landing/components/modals/DemoRequestModal.tsx`](../landing/components/modals/DemoRequestModal.tsx) and API helpers [`landing/lib/platform-api.ts`](../landing/lib/platform-api.ts) call `GET /api/platform/cms/config` (reCAPTCHA) then `POST /api/platform/public/demo-requests` with action **`landing_demo_request`** when platform reCAPTCHA is enabled (`platform_settings` / config overrides). Payload: `fullName`, `email`, optional `phone` (same digit rules as platform phone validation), `message`, `locale`, optional `recaptchaToken`. Both requests send **`Accept-Language`** aligned with the page locale (`tr` / `en`) so `ApiResponse.message` and validation text resolve correctly. Success and error copy for submit are taken from the API (`message` plus optional `data.followUpNote` on success); the UI keeps only chrome labels and client-only fallbacks (network, missing env, reCAPTCHA script in the browser).
- **Newsletter flow**: [`landing/components/sections/NewsletterSection.tsx`](../landing/components/sections/NewsletterSection.tsx) posts to `POST /api/platform/public/newsletter/subscribe` with `email`, `templateType`, `source`, `locale`, plus lightweight anti-bot fields (`honeypot`, `formStartedAt`). Backend rejects suspicious submissions (filled honeypot / too-fast submit), resolves the final submit success/error text into `ApiResponse.message`, and storefront shows that message directly. This flow intentionally uses **double opt-in + lightweight anti-bot checks**, not platform reCAPTCHA.
- **Build-time env**: `NEXT_PUBLIC_CRAFTIVE_API_URL` = API origin **without** `/api` suffix (e.g. `http://localhost:8080`); see [`landing/.env.local.example`](../landing/.env.local.example). For Cloudflare Pages, set the same variable for the build. Backend CORS must list every **browser Origin** you use: the Cloudflare default (`https://<project>.pages.dev` and preview hosts `https://*.<project>.pages.dev`) is not the same origin as a custom domain (`https://craftive.io`, `https://craftive.io`, etc.). Add each to `app.cors` for the target API profile (see `application-stage.yml` / `application-prod.yml`).
- **Admin**: SUPER_ADMIN only — `GET /api/platform/demo-requests`, UI `/:lang/demo-requests` (see [`modules/platform-admin.md`](modules/platform-admin.md)).
- Deploy target: Cloudflare Pages (static export, no SSR runtime dependency)
- Output: `out/` directory
- Domain: `craftive.io` documented as the primary marketing hostname; you may attach `craftive.io` / `www.craftive.io` on Cloudflare Pages instead. The default Pages URL (`*.pages.dev`) still appears in the dashboard and for preview deployments—keep CORS in sync with whichever origins actually load the site.
- Operational guide: [`global/devops.md`](global/devops.md)
- Environment overview: [`global/environment-configuration.md`](global/environment-configuration.md)

## Cross-cutting features

- Navigation (hierarchical nodes/entries): [`modules/navigation.md`](modules/navigation.md)
- ImpEx (on-demand SQL data import): [`modules/impex.md`](modules/impex.md)
