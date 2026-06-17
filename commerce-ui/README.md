# Commerce UI

Standalone Next.js commerce storefront shell for Craftive tenants.

## Status

This project is intentionally a clean commerce foundation. It keeps the shared tenant, locale, and HTTP helper approach from `storefront-nextjs`, but removes the CMS renderer, CMS theme, SmartEdit, and CMS-driven routes.

Implemented in this slice:

- `/[lang]` locale routing with `tr` and `en` message catalogs
- tenant header injection through `proxy.ts`
- minimal app shell and commerce route skeletons
- clean Tailwind v4 global CSS
- typed cart API client, localStorage cart token handling, cart provider, cart page wiring, and header cart badge
- product detail delivery client, real product detail rendering, variant/quantity selection, and add-to-cart from real variants

Not implemented yet:

- product listing/search API integration
- checkout, payment, and customer account state
- final commerce visual design
- legal snapshot rendering

## Environment

Required:

- `NEXT_PUBLIC_COMMERCE_API_URL`
- `TENANT_SUBDOMAIN` or `TENANT_ID`

Optional:

- `TENANT_HOSTNAME`
- `TRUST_X_FORWARDED_HOST`
- `NEXT_IMAGE_DOMAINS`
- `NEXT_PUBLIC_GA_ID`
- `NEXT_PUBLIC_GTM_ID`
- `NEXT_PUBLIC_GOOGLE_SITE_VERIFICATION`

## Commands

```bash
npm install
npm run dev
npm run build
npm run lint
```
