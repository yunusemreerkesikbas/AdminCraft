# Commerce

## Purpose

Commerce is the tenant module foundation for customer account, cart, checkout, payment, order, fulfillment, and transactional commerce flows.

The current implementation includes the module foundation, anonymous cart foundation, and backend customer account foundation. It does not implement checkout, payment, order, fulfillment, customer-cart merge, or storefront UI yet.

Commerce depends on Product Catalog. A tenant cannot provision or sync commerce without `product`.

Product Catalog now owns the commerce-ready sellable product foundation: reusable variant options, product variants, variant SKU, gross price, VAT rate, stock, and active state. Commerce cart/order work should reference product variants instead of base products.

## Database

Migration paths:

- Platform catalog seed: [`../../backend/src/main/resources/db/platform/R__seed_modules.sql`](../../backend/src/main/resources/db/platform/R__seed_modules.sql)
- Tenant commerce migrations: [`../../backend/src/main/resources/db/tenant/commerce`](../../backend/src/main/resources/db/tenant/commerce)

Current tenant migrations:

- `V1.0.0__baseline.sql` is intentionally no-op. It creates Flyway history for the commerce module without adding business tables.
- `V1.0.1__cart_foundation.sql` creates anonymous cart and cart item tables.
- `V1.0.2__customer_account_foundation.sql` creates commerce customer, refresh token, consent, address, and social identity skeleton tables.

Module execution order is documented in [`../global/migrations.md`](../global/migrations.md). Commerce runs after `product`.

## Anonymous cart API

Base path: `/api/commerce/cart`

The cart API is public and tenant-scoped. It uses `X-Cart-Token` for anonymous cart identity. New cart tokens are returned in the response body and only a SHA-256 token hash is stored in the tenant database.

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

## Customer account API

Base paths:

- `/api/commerce/customers/auth`
- `/api/commerce/customers`

Customer account is separate from admin `User` authentication. Customer access tokens use commerce-specific JWT token types and are accepted only by commerce customer endpoints.

Auth endpoints:

- `POST /api/commerce/customers/auth/register`: creates an email/password customer, required legal/privacy consent snapshots, optional marketing consent snapshots, and a refresh token cookie.
- `POST /api/commerce/customers/auth/login`: signs in an active customer.
- `POST /api/commerce/customers/auth/refresh`: rotates the HttpOnly refresh cookie and returns a new access token.
- `POST /api/commerce/customers/auth/logout`: revokes the refresh cookie token when present and clears the cookie.

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

## Admin API

Commerce does not expose a tenant-scoped admin API yet.

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

Anonymous cart and customer account are the first public commerce APIs. Storefront UI, checkout, payment, customer-cart merge, and order APIs remain backlog work.

## Frontend integration

Admin shell paths:

- Route registration: [`../../storefront/src/app/app.routes.ts`](../../storefront/src/app/app.routes.ts)
- Commerce layout shell: [`../../storefront/src/app/modules/admin/custom/commerce/commerce-layout.component.ts`](../../storefront/src/app/modules/admin/custom/commerce/commerce-layout.component.ts)
- Module constants: [`../../storefront/src/app/core/navigation/navigation-modules.constants.ts`](../../storefront/src/app/core/navigation/navigation-modules.constants.ts)
- Provisioning dialog dependency behavior: [`../../storefront/src/app/shared/components/module-provision-dialog/module-provision-dialog.component.ts`](../../storefront/src/app/shared/components/module-provision-dialog/module-provision-dialog.component.ts)

The `/commerce` admin route exists and is guarded by `requiredModule: 'commerce'`, but no sidebar navigation item is registered yet. The first real commerce admin page should add navigation.

## Security & tenant isolation

- Provisioning endpoints are SUPER_ADMIN-only through `ProvisioningController`.
- The admin `/commerce` route is tenant-user guarded and also protected by `moduleGuard`.
- Customer account auth endpoints are public but tenant-scoped; tenant resolution is still required.
- Customer profile and address endpoints require commerce customer authentication and do not accept admin JWTs as customer identity.
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
