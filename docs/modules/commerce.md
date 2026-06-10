# Commerce

## Purpose

Commerce is the tenant module foundation for customer account, cart, checkout, payment, order, fulfillment, and transactional commerce flows.

The current implementation is a foundation shell only. It registers the module, protects its future admin route, creates module package boundaries, and prepares Flyway history. It does not implement cart, checkout, customer, payment, order, or storefront commerce behavior yet.

Commerce depends on Product Catalog. A tenant cannot provision or sync commerce without `product`.

## Database

Migration paths:

- Platform catalog seed: [`../../backend/src/main/resources/db/platform/R__seed_modules.sql`](../../backend/src/main/resources/db/platform/R__seed_modules.sql)
- Tenant commerce migrations: [`../../backend/src/main/resources/db/tenant/commerce`](../../backend/src/main/resources/db/tenant/commerce)

Current tenant migration:

- `V1.0.0__baseline.sql` is intentionally no-op. It creates Flyway history for the commerce module without adding business tables.

Module execution order is documented in [`../global/migrations.md`](../global/migrations.md). Commerce runs after `product`.

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

None.

Storefront commerce delivery APIs are backlog work.

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
- Commerce uses tenant DB isolation. Do not add `tenant_id` columns to tenant commerce tables.
- Future commerce services should call `CommerceModuleAccessGuard` before tenant-scoped business operations.

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
