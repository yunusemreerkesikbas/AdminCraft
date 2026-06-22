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
- product listing/search route with paginated public delivery integration and detail links
- customer auth/account foundation with memory-only access token state, refresh-cookie restore, login/register/logout, read-only profile summary, and cart merge state update
- address book, checkout, payment return, and order history UI foundations
- legal snapshot detail rendering and cancellation/return request UI foundations

Not implemented yet:

- final commerce visual design
- production hardening around completed foundation flows
- İleti Merkezi SMS provider adapter

## UI model convention

Commerce UI components receive presentation data through `model` props. Do not introduce new `copy`, `Copy`, or `ViewModel` contracts for component input data.

Rules:

- Put UI model types and factory functions next to the owning feature component, using kebab-case file names such as `components/orders/orders-model.ts`.
- Name contracts as `*Model` (`OrdersModel`, `CheckoutModel`, `AddressBookModel`) and pass them as `model={model}`.
- Build translated models with pure `create*Model(...)` factory functions. These factories accept translator functions and must not import `next-intl/server`.
- Keep backend API DTOs in `lib/commerce/**/types.ts`; do not mix API response/request contracts with UI presentation models.
- Compose shared nested models instead of duplicating translation mapping. For example, account and checkout flows should reuse `createAddressBookModel(...)`.

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
