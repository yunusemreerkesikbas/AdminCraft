# Commerce

## Purpose

Commerce is the tenant module foundation for customer account, cart, checkout, payment, order, fulfillment, and transactional commerce flows.

The current implementation includes the module foundation, anonymous cart foundation, backend customer account foundation, customer-cart bridge, checkout foundation, payment attempt foundation, hosted iyzico CheckoutForm sandbox init/callback foundation, backend order finalization after successful payment, customer order read APIs, and read-only tenant admin commerce operations. It does not implement fulfillment, transactional notifications, mutable admin order operations, or storefront UI yet.

Commerce depends on Product Catalog. A tenant cannot provision or sync commerce without `product`.

Product Catalog now owns the commerce-ready sellable product foundation: reusable variant options, product variants, variant SKU, gross price, VAT rate, stock, and active state. Commerce cart/order work should reference product variants instead of base products.

## MVP implementation status

Implemented foundations:

- Commerce module registration, provisioning dependency on Product Catalog, tenant migration baseline, and admin route shell.
- Product Catalog variant foundation for reusable options/values, product variants, SKU, gross price, VAT, stock, and publish validation.
- Anonymous cart API with token hash storage, price snapshots, stock checks, and customer-aware cart access.
- Commerce customer account auth, refresh/logout, profile, consents, and address book.
- Customer-cart bridge with merge-on-auth and authenticated customer cart ownership.
- Customer-only checkout draft/ready snapshot, address snapshots, shipping totals, and live revalidation.
- Internal payment attempt lifecycle with checkout totals snapshot, pending expiry, owner checks, and i18n response messages.
- Hosted iyzico CheckoutForm sandbox initialization and callback handling through a payment provider port.
- Customer-facing paid order creation after successful payment, daily order number counters, idempotent callback finalization, cart clear, checkout completion, stock decrement, and legal snapshot placeholders.
- Tenant admin read-only commerce operations: dashboard summary, order list/detail, payment attempt history, and commerce sidebar navigation.

Not implemented yet:

- Mutable admin order operations such as status transitions, fulfillment, shipment tracking, cancellation/return decisions, refunds, and internal notes.
- Storefront order history.
- Full legal template rendering and rendered legal snapshot capture.
- Cancellation/return requests, manual fulfillment, shipping tracking, refunds, and transactional email/SMS.
- Storefront commerce UI for product purchase flow, account, checkout, payment return, and order history.
- Guest checkout, coupons/promotions, advanced analytics, multi-currency, and additional payment providers.

## Database

Migration paths:

- Platform catalog seed: [`../../backend/src/main/resources/db/platform/R__seed_modules.sql`](../../backend/src/main/resources/db/platform/R__seed_modules.sql)
- Tenant commerce migrations: [`../../backend/src/main/resources/db/tenant/commerce`](../../backend/src/main/resources/db/tenant/commerce)

Current tenant migrations:

- `V1.0.0__baseline.sql` is intentionally no-op. It creates Flyway history for the commerce module without adding business tables.
- `V1.0.1__cart_foundation.sql` creates anonymous cart and cart item tables.
- `V1.0.2__customer_account_foundation.sql` creates commerce customer, refresh token, consent, address, and social identity skeleton tables.
- `V1.0.3__customer_cart_bridge.sql` adds nullable customer ownership to carts for merge-on-auth and authenticated cart access.
- `V1.0.4__checkout_foundation.sql` creates customer checkout and checkout item snapshot tables.
- `V1.0.5__payment_attempt_foundation.sql` creates internal payment attempt snapshot tables.
- `V1.0.6__order_foundation.sql` creates paid order snapshot tables and daily order number counters.
- `V1.0.7__commerce_admin_read_indexes.sql` adds read indexes for admin order and payment attempt operations.

Module execution order is documented in [`../global/migrations.md`](../global/migrations.md). Commerce runs after `product`.

## Anonymous cart API

Base path: `/api/commerce/cart`

The cart API is public and tenant-scoped. Anonymous requests use `X-Cart-Token` for cart identity. Authenticated commerce customer requests may use the same API with a customer access token and no cart token; the customer's active cart is the canonical cart. New anonymous cart tokens are returned in the response body and only a SHA-256 token hash is stored in the tenant database.

- `POST /api/commerce/cart`: creates an empty anonymous cart.
- `GET /api/commerce/cart`: returns the cart for `X-Cart-Token`.
- `POST /api/commerce/cart/items`: adds a product variant; missing token creates a new cart.
- `PATCH /api/commerce/cart/items/{itemUid}`: updates quantity.
- `DELETE /api/commerce/cart/items/{itemUid}`: removes one item.
- `DELETE /api/commerce/cart`: clears the cart.

Cart rules:

- Anonymous cart TTL is 30 days.
- Quantity must be between `1` and `99`.
- Same variant lines are merged by increasing quantity.
- Cart does not reserve stock; add/update checks current stock.
- Cart item stores gross price and VAT snapshots.
- Cart read compares snapshot price with current variant price and returns `priceChanged`.
- Invalid, cleared, or expired cart tokens behave as cart not found.
- Customer JWT cart requests do not implicitly merge a stale `X-Cart-Token`; the authenticated customer's active cart wins.
- Customer cart tokens are not minted just to expose a raw token. `cartToken` can be `null` for customer-cart responses.

## Customer account API

Base paths:

- `/api/commerce/customers/auth`
- `/api/commerce/customers`

Customer account is separate from admin `User` authentication. Customer access tokens use commerce-specific JWT token types and are accepted only by commerce customer endpoints.

Auth endpoints:

- `POST /api/commerce/customers/auth/register`: creates an email/password customer, required legal/privacy consent snapshots, optional marketing consent snapshots, optionally merges `X-Cart-Token`, and sets a refresh token cookie.
- `POST /api/commerce/customers/auth/login`: signs in an active customer and optionally merges `X-Cart-Token`.
- `POST /api/commerce/customers/auth/refresh`: rotates the HttpOnly refresh cookie and returns a new access token.
- `POST /api/commerce/customers/auth/logout`: revokes the refresh cookie token when present and clears the cookie.

Register/login responses can include optional `cart` and `cartMerge` data. `cartMerge.status` is one of `NONE`, `LINKED`, `MERGED`, `PARTIAL`, or `SOURCE_NOT_FOUND`.

Profile and address endpoints require `ROLE_COMMERCE_CUSTOMER`:

- `GET /api/commerce/customers/me`
- `PATCH /api/commerce/customers/me`
- `GET /api/commerce/customers/addresses`
- `POST /api/commerce/customers/addresses`
- `PATCH /api/commerce/customers/addresses/{addressUid}`
- `DELETE /api/commerce/customers/addresses/{addressUid}`
- `POST /api/commerce/customers/addresses/{addressUid}/default-delivery`
- `POST /api/commerce/customers/addresses/{addressUid}/default-billing`

Customer account rules:

- Customer email is unique per tenant database through normalized lowercase email.
- Email verification is state-only in this slice; login is not blocked when `emailVerified=false`.
- Refresh tokens are stored only as SHA-256 hashes.
- Address book is TR-first flexible: `countryIso` defaults to `TR`, city/district are strings, phone is required, and corporate invoice addresses require company name, tax number, and tax office.
- Google login is not implemented yet; social identity schema exists for a future OAuth slice.

## Checkout API

Base path: `/api/commerce/checkout`

Checkout is customer-only and requires a commerce customer JWT. It does not create orders or payments.

- `POST /api/commerce/checkout`: starts checkout from the authenticated customer's active cart and expires previous open checkouts.
- `GET /api/commerce/checkout/current`: returns the current checkout with live cart/product validation flags.
- `PATCH /api/commerce/checkout/{checkoutUid}/addresses`: updates delivery/billing address snapshots and recalculates totals.

Checkout rules:

- Checkout TTL is 24 hours.
- Address UID fields are optional. Missing delivery/billing UID falls back to default delivery/default billing address.
- `billingSameAsDelivery=true` uses the delivery address as the billing snapshot.
- Empty cart, unavailable variant, and insufficient stock block checkout start.
- Checkout item prices and VAT are snapshotted from live product variants at checkout start.
- Checkout reads do not mutate the database; cart/price/stock differences are returned through validation flags and warning message keys.
- Shipping uses `commerce.shipping.enabled`, `commerce.shipping.standard_fee`, and `commerce.shipping.free_shipping_threshold`; invalid or missing config safely falls back to `0 TRY`.

## Payment attempt API

Base path: `/api/commerce/payments`

Payment attempt is customer-only and requires a commerce customer JWT. Hosted iyzico CheckoutForm sandbox initialization is supported. Successful payment callbacks finalize a customer-facing paid order on the backend.

- `POST /api/commerce/payments/attempts`: creates a pending internal payment attempt for a ready checkout.
- `GET /api/commerce/payments/attempts/{attemptUid}`: returns the authenticated customer's payment attempt.
- `POST /api/commerce/payments/attempts/{attemptUid}/initialize`: initializes hosted iyzico CheckoutForm and returns `paymentPageUrl`.
- `POST /api/commerce/payments/iyzico/checkout-form/callback`: public tenant-scoped iyzico callback endpoint; updates the payment attempt and redirects to configured return URLs.

Payment attempt rules:

- `commerce.payment.enabled=true` is required. Missing provider config defaults to `iyzico`; unsupported providers are rejected.
- Attempt TTL is 30 minutes.
- The attempt snapshots checkout totals and currency.
- Creating a new attempt expires previous pending attempts for the same customer checkout.
- Checkout must still be `READY`, unexpired, and live-valid against cart/product price and stock.
- Expired or checkout-changed pending attempts are returned as `EXPIRED`.
- Initialize is single-use. An attempt with a provider token cannot be initialized again.
- `INITIALIZING` is an internal reservation status used to prevent concurrent hosted payment initialization for the same attempt.
- iyzico SDK credentials are tenant config values. `commerce.payment.iyzico.api_key` and `commerce.payment.iyzico.secret_key` must be stored as secret config values.
- `commerce.payment.iyzico.base_url` defaults to `https://sandbox-api.iyzipay.com`.
- `commerce.payment.iyzico.default_identity_number`, `commerce.payment.return_success_url`, and `commerce.payment.return_failure_url` are required for initialization/callback.
- `app.commerce.payment.callback-base-url` configures the public backend callback origin used for iyzico CheckoutForm initialization. It may contain `%s` for the tenant subdomain and must route to the backend `/api` context path.
- Callback result retrieval verifies the iyzico response signature. Success marks the attempt `SUCCEEDED`, creates an idempotent `PAID` order, decrements stock when available, clears the cart, marks checkout `COMPLETED`, and redirects to the configured success URL. Failure marks the attempt `FAILED`.
- Order numbers use `commerce.order.number_prefix` when configured, default to `ORD`, and follow `ORD-YYYYMMDD-000001` with a tenant-local daily counter.
- If stock can no longer be deducted after provider success, the paid order is still created with `requiresAttention=true` and no negative stock is written. Refund/reversal remains an operational follow-up slice.
- Legal snapshot fields exist on the order with `NOT_CAPTURED` status; full legal template rendering remains backlog work.

## Customer order API

Base path: `/api/commerce/orders`

Customer order reads are customer-only and require a commerce customer JWT. The API only returns orders owned by the authenticated customer; cross-customer order UIDs behave as not found. Raw payment provider fields such as provider transaction IDs are not exposed through customer-facing responses.

- `GET /api/commerce/orders`: returns a paginated order summary list with `page`, `size`, and whitelisted `sort` support. Default sort is `createdAt,desc`; allowed sort fields are `createdAt`, `total`, `orderNumber`, and `status`.
- `GET /api/commerce/orders/{orderUid}`: returns order detail with items, shipping, delivery/billing address snapshots, legal snapshot status, totals, and attention flags.

Customer-cart bridge rules:

- Register/login may receive optional `X-Cart-Token`; invalid, expired, cleared, or already-owned source carts do not fail authentication and return `SOURCE_NOT_FOUND`.
- If the customer has no active cart, the active anonymous cart is linked to the customer.
- If the customer already has an active cart, source cart items are merged into the customer cart.
- Same variant quantities are summed when the resulting quantity stays within `1..99` and current stock/sellability checks pass.
- Problematic source lines are skipped, auth still succeeds, and `cartMerge.warningMessageKeys` carries frontend-displayable i18n keys.
- After a merge attempt into an existing customer cart, the source anonymous cart is marked `CLEARED` to prevent double-merge.

## Admin API

Base path: `/api/commerce/admin`

Commerce admin APIs are tenant-scoped, require tenant admin authentication, and expose read-only operational visibility. They do not accept commerce customer JWTs and do not mutate order/payment state.

- `GET /api/commerce/admin/dashboard`: returns today and last-7-days order count/revenue, attention order count, and failed payment attempt count.
- `GET /api/commerce/admin/orders`: returns a paginated order list with `page`, `size`, whitelisted `sort`, optional `search`, optional `status`, and optional `requiresAttention`.
- `GET /api/commerce/admin/orders/{orderUid}`: returns admin order detail with customer, totals, items, address snapshots, payment attempt summary, provider transaction id, legal snapshot status, and attention flags.
- `GET /api/commerce/admin/payment-attempts`: returns a paginated payment attempt history with `page`, `size`, whitelisted `sort`, optional `search`, and optional `status`.

Provisioning is handled by the platform provisioning API:

- `GET /api/provisioning/modules/catalog`
- `POST /api/provisioning/tenants/{tenantId}/provision`
- `POST /api/provisioning/tenants/{tenantId}/sync-migrations`

Source of truth:

- [`../../backend/src/main/java/com/backend/domain/enums/ModuleCode.java`](../../backend/src/main/java/com/backend/domain/enums/ModuleCode.java)
- [`../../backend/src/main/java/com/backend/application/service/ProvisioningServiceImpl.java`](../../backend/src/main/java/com/backend/application/service/ProvisioningServiceImpl.java)
- [`../../backend/src/main/java/com/backend/application/service/TenantMigrationService.java`](../../backend/src/main/java/com/backend/application/service/TenantMigrationService.java)
- [`../../backend/src/main/java/com/backend/infrastructure/web/TenantStartupMigrator.java`](../../backend/src/main/java/com/backend/infrastructure/web/TenantStartupMigrator.java)

## Public delivery APIs

Anonymous cart, customer account, customer-cart bridge, checkout, payment attempt, backend order finalization, customer order read APIs, and read-only operational admin APIs are the first commerce APIs/workflows. Storefront UI and mutable operational admin APIs remain backlog work.

## Frontend integration

Admin shell paths:

- Route registration: [`../../storefront/src/app/app.routes.ts`](../../storefront/src/app/app.routes.ts)
- Commerce layout shell: [`../../storefront/src/app/modules/admin/custom/commerce/commerce-layout.component.ts`](../../storefront/src/app/modules/admin/custom/commerce/commerce-layout.component.ts)
- Module constants: [`../../storefront/src/app/core/navigation/navigation-modules.constants.ts`](../../storefront/src/app/core/navigation/navigation-modules.constants.ts)
- Provisioning dialog dependency behavior: [`../../storefront/src/app/shared/components/module-provision-dialog/module-provision-dialog.component.ts`](../../storefront/src/app/shared/components/module-provision-dialog/module-provision-dialog.component.ts)

The `/commerce` admin route is guarded by `requiredModule: 'commerce'` and exposes read-only tenant admin pages:

- `/commerce/dashboard`
- `/commerce/orders`
- `/commerce/orders/:orderUid`
- `/commerce/payment-attempts`

Sidebar navigation is registered for tenant admins with the commerce module enabled.

## Security & tenant isolation

- Provisioning endpoints are SUPER_ADMIN-only through `ProvisioningController`.
- The admin `/commerce` route is tenant-admin guarded and also protected by `moduleGuard`.
- Customer account auth endpoints are public but tenant-scoped; tenant resolution is still required.
- Customer profile and address endpoints require commerce customer authentication and do not accept admin JWTs as customer identity.
- Cart endpoints remain public for anonymous carts and can optionally authenticate commerce customer JWTs for customer carts. Admin JWTs are not used as cart customer identity.
- Checkout endpoints require commerce customer authentication and do not accept anonymous or admin JWT identity.
- Commerce uses tenant DB isolation. Do not add `tenant_id` columns to tenant commerce tables.
- Commerce services call `CommerceModuleAccessGuard` before tenant-scoped business operations.

## Implementation guide

### Provision commerce for a tenant

1. Ensure the tenant has `core` and `product`.
2. Start provisioning with:
   - `{ "modules": ["core", "product", "commerce"] }`
3. Poll the provisioning job until it succeeds.
4. Confirm `commerce` appears in the tenant module list.

### Sync commerce migrations for an existing product tenant

1. Ensure `product` is already enabled for the tenant.
2. Enable/register `commerce` through the normal provisioning flow.
3. Run sync migrations if needed.

Startup auto-sync skips commerce and logs a warning when platform state has `commerce` enabled without `product`.

### Add the first commerce feature slice

1. Add tenant schema changes under `db/tenant/commerce`.
2. Keep business logic in `application/commerce`.
3. Keep domain concepts under `domain/commerce`.
4. Keep persistence adapters under `infrastructure/persistence/commerce`.
5. Add admin endpoints under `presentation/commerce` only when the feature needs a public admin contract.
