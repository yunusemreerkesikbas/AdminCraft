# AdminCraft Documentation

AdminCraft is a multi-tenant SaaS CMS platform built with **Spring Boot (Java 21)** and **Angular 19**.
The platform uses **database-per-tenant** isolation (`platform_management` + `ac_tenant_{id}`) and a Clean Architecture layout.

## How to use these docs

- Start here for architecture and conventions: [`global/architecture.md`](global/architecture.md)
- If you are touching security, always read: [`global/security-multi-tenancy.md`](global/security-multi-tenancy.md)
- For patterns that apply everywhere:
  - Backend conventions: [`global/backend-patterns.md`](global/backend-patterns.md)
  - Frontend conventions: [`global/frontend-patterns.md`](global/frontend-patterns.md)
  - Authentication: [`global/authentication.md`](global/authentication.md)
  - i18n + composite operations: [`global/i18n-and-composite.md`](global/i18n-and-composite.md)
  - Documentation patterns: [`global/documentation-patterns.md`](global/documentation-patterns.md)
  - Dialogs and shared UI: [`global/dialogs-and-ui.md`](global/dialogs-and-ui.md)
  - List views (pagination/sort/search): [`global/list-pagination-search.md`](global/list-pagination-search.md)
  - Validation framework: [`global/validation.md`](global/validation.md)
  - Testing patterns: [`global/testing.md`](global/testing.md)

## Modules (admin APIs)

Tenant modules are defined in [`backend/src/main/java/com/backend/domain/enums/ModuleCode.java`](../backend/src/main/java/com/backend/domain/enums/ModuleCode.java).

- Core: [`modules/core.md`](modules/core.md)
- Page Builder: [`modules/pagebuilder.md`](modules/pagebuilder.md)
- Site Settings: [`modules/site-settings.md`](modules/site-settings.md)
- Media Library (DAM): [`modules/media.md`](modules/media.md)
- Product Catalog: [`modules/product-catalog.md`](modules/product-catalog.md)
- Component Library: [`modules/component-library.md`](modules/component-library.md)

## Platform features (control-plane)

Platform features are not tenant modules, but they are critical for operating the system.

- Tenants, provisioning, module enablement, migration sync: [`modules/platform-provisioning.md`](modules/platform-provisioning.md)

## Public delivery APIs (storefront)

Public APIs are still tenant-scoped (resolved by tenant headers/hostname), but **do not require authentication**.

- CMS delivery (`/api/cms/**`): [`modules/cms-delivery.md`](modules/cms-delivery.md)

## Cross-cutting features

- Navigation (hierarchical nodes/entries): [`modules/navigation.md`](modules/navigation.md)
