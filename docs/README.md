# AdminCraft Documentation

AdminCraft is a multi-tenant SaaS CMS platform built with **Spring Boot (Java 21)** and **Angular 19**.
The platform uses **database-per-tenant** isolation (`platform_management` + `ac_tenant_{id}`) and a Clean Architecture layout.

## How to use these docs

- Start here for architecture and conventions: [`global/architecture.md`](global/architecture.md)
- If you are touching security, always read: [`global/security-multi-tenancy.md`](global/security-multi-tenancy.md)
- For patterns that apply everywhere:
  - Environment configuration: [`global/environment-configuration.md`](global/environment-configuration.md)
  - Backend conventions: [`global/backend-patterns.md`](global/backend-patterns.md)
  - Frontend conventions: [`global/frontend-patterns.md`](global/frontend-patterns.md)
  - Authentication: [`global/authentication.md`](global/authentication.md)
  - Public tenant config (unauthenticated APIs): [`global/public-tenant-config.md`](global/public-tenant-config.md)
  - i18n + composite operations: [`global/i18n-and-composite.md`](global/i18n-and-composite.md)
  - Documentation patterns: [`global/documentation-patterns.md`](global/documentation-patterns.md)
  - Dialogs and shared UI: [`global/dialogs-and-ui.md`](global/dialogs-and-ui.md)
  - List views (pagination/sort/search): [`global/list-pagination-search.md`](global/list-pagination-search.md)
  - Validation framework: [`global/validation.md`](global/validation.md)
  - Database migrations: [`global/migrations.md`](global/migrations.md)
  - Migration governance: [`global/migration-governance.md`](global/migration-governance.md)
  - Migration runbook: [`global/migration-runbook.md`](global/migration-runbook.md)
  - Testing patterns: [`global/testing.md`](global/testing.md)

## Modules (admin APIs)

Tenant modules are defined in [`backend/src/main/java/com/backend/domain/enums/ModuleCode.java`](../backend/src/main/java/com/backend/domain/enums/ModuleCode.java).

- Core: [`modules/core.md`](modules/core.md)
- User Management: [`modules/user-management.md`](modules/user-management.md)
- **Site Dashboard**: [`modules/site-dashboard.md`](modules/site-dashboard.md) - Unified site management interface
- Page Builder: [`modules/pagebuilder.md`](modules/pagebuilder.md) *(core-managed capability in provisioning)*
- Site Settings: [`modules/site-settings.md`](modules/site-settings.md) *(integrated into Site Dashboard; not a provisioning module)*
- Media Library (DAM): [`modules/media.md`](modules/media.md) *(core-managed capability in provisioning)*
- Product Catalog: [`modules/product-catalog.md`](modules/product-catalog.md)
- Component Library: [`modules/component-library.md`](modules/component-library.md) *(core-managed capability in provisioning)*

Provisioning catalog note:
Core and Product are the only selectable modules in `/api/provisioning/modules/catalog`. Core expands to core-managed capabilities at execution time. See [`modules/platform-provisioning.md`](modules/platform-provisioning.md).

## Platform features (control-plane)

Platform features are not tenant modules, but they are critical for operating the system.

- Tenants, provisioning, module enablement, migration sync: [`modules/platform-provisioning.md`](modules/platform-provisioning.md)
- Platform Dashboard, Tenant Detail, Platform Settings: [`modules/platform-admin.md`](modules/platform-admin.md)

## Public delivery APIs (storefront)

Public APIs are still tenant-scoped (resolved by tenant headers/hostname), but **do not require authentication**.

- CMS delivery (`/api/cms/**`): [`modules/cms-delivery.md`](modules/cms-delivery.md)

## Cross-cutting features

- Navigation (hierarchical nodes/entries): [`modules/navigation.md`](modules/navigation.md)
