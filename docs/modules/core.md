# Core

## Purpose

Core is the required tenant module that provides foundational tenant data-plane features such as:

- **Users and roles** - User management with role-based access control (RBAC). See [`user-management.md`](user-management.md)
- **System bootstrapping seeds** - System user and role initialization
- **Navigation storage** - See [`navigation.md`](navigation.md)
- **Sites** - Site configuration and activity tracking. See [`site-dashboard.md`](site-dashboard.md)

Provisioning semantics:

- In provisioning catalog, `core` is the umbrella selection.
- Runtime migration execution expands core provisioning to:
  - `core`, `media`, `component_library`, `pagebuilder`
- `product` remains the only optional catalog module outside core umbrella.

## Database

**Migrations**: `backend/src/main/resources/db/tenant/core/`

| Migration | Description |
|-----------|-------------|
| `V1__baseline.sql` | Initial schema (users, roles) |
| `R__seed_roles.sql` | Repeatable role data |
| `R__seed_system_user.sql` | System user initialization |
| `V17__navigation_nodes.sql` | Navigation hierarchy |
| `V18__navigation_i18n.sql` | Navigation internationalization |
| `V19__create_sites_table.sql` | Sites table |
| `V20__create_site_activity.sql` | Site Dashboard activity tracking |
| `V21__create_site_technical_settings.sql` | Technical settings (robots.txt, scripts) |
| `V25__remove_preferred_language.sql` | Remove user-level language preference |

## Core Features

### User Management

Full documentation: [`user-management.md`](user-management.md)

**Highlights**:
- CRUD operations with role-based access
- Password management (reset, change)
- Account status (activate/deactivate)
- Account lock mechanism (brute-force protection)
- Paginated, searchable, sortable list

**Base path**: `/api/users`

### Navigation

Full documentation: [`navigation.md`](navigation.md)

**Highlights**:
- Hierarchical navigation nodes
- i18n support for navigation entries
- Drag-and-drop reordering

### Site Dashboard

Full documentation: [`site-dashboard.md`](site-dashboard.md)

**Highlights**:
- Unified site management interface
- Activity tracking
- Technical settings (robots.txt, scripts)

## Admin UI

Admin features built on core data:

| Feature | Location |
|---------|----------|
| User Management | `storefront/src/app/modules/admin/custom/users/` |
| Site Dashboard | `storefront/src/app/modules/admin/custom/site/` |
| Navigation | `storefront/src/app/modules/admin/custom/navigation/` |
| Page Builder | `storefront/src/app/modules/admin/custom/pages/` |
| Media Library | `storefront/src/app/modules/admin/custom/media/` |
| Component Library | `storefront/src/app/modules/admin/custom/components/` |
| Site Settings (integrated) | `storefront/src/app/modules/admin/custom/settings/` |

## Security & Tenant Isolation

- Core data lives in tenant databases (`ac_tenant_{id}`) and is always tenant-scoped
- Tenant resolution is enforced by [`TenantFilter.java`](../../backend/src/main/java/com/backend/infrastructure/tenant/TenantFilter.java)
- User management security details in [`user-management.md`](user-management.md#security--tenant-isolation)

## Implementation Guide

### Adding a New Core-Owned Tenant Feature

1. Add tenant migrations under `backend/src/main/resources/db/tenant/core/` using Flyway versioning
2. Expose APIs in presentation layer controllers under `/api/*` (context path configured in `application.yml`)
3. Keep all business logic in the application layer; do not couple application services to servlet APIs
4. Follow DTO pattern: separate Request/Response DTOs from domain entities
5. Use `@Valid` for automatic Bean Validation
6. Extend `CrudHttpService` and `BasePaginatedListComponent` for consistent CRUD operations

## Related Documentation

- User Management: [`user-management.md`](user-management.md)
- Navigation: [`navigation.md`](navigation.md)
- Site Dashboard: [`site-dashboard.md`](site-dashboard.md)
- List patterns: [`../global/list-pagination-search.md`](../global/list-pagination-search.md)
- Dialogs and UI: [`../global/dialogs-and-ui.md`](../global/dialogs-and-ui.md)
