# Frontend Patterns (Angular 19)

This codebase follows:

- Standalone components
- OnPush change detection
- Signals for local UI state
- RxJS for HTTP streams; use `take(1)` for one-shot calls

## Module locations

Admin feature modules live under:

- `storefront/src/app/modules/admin/custom/`

Examples (confirmed):

- Media: `storefront/src/app/modules/admin/custom/media/`
- Page Builder: `storefront/src/app/modules/admin/custom/pages/`
- Templates: `storefront/src/app/modules/admin/custom/templates/`
- Component Library: `storefront/src/app/modules/admin/custom/components/`
- Tenants (platform): `storefront/src/app/modules/admin/custom/tenants/`

## Dialogs

Prefer shared dialog base components and patterns:

- Dialogs and shared UI: [`dialogs-and-ui.md`](dialogs-and-ui.md)

## List views: pagination + sorting + search

List views should use the standardized server-side pagination/search/sort approach, and avoid client-side heavy computation.
See: [`list-pagination-search.md`](list-pagination-search.md)

