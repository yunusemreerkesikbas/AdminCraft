# List Views: Pagination, Sorting, Search

Admin list pages should use server-side pagination, sorting, and search. This avoids slow client-side filtering and keeps behavior consistent across modules.

## Backend contract (recommended)

- List endpoints accept:
  - `page` (0-based)
  - `size`
  - `sort` (e.g., `createdAt,desc`)
  - `search` (optional)
- Responses return a page wrapper (content + meta) and a sort configuration for UI.

Examples in code:

- Navigation root nodes: `GET /api/navigation/nodes` in `NavigationController`
- Page templates: `GET /api/page-templates` in `PageTemplateController`
- Media list: `GET /api/media` in `MediaController`

## Frontend implementation

Use the standardized patterns and shared components:

- Prefer a shared grid component for consistent table layouts:
  - `storefront/src/app/shared/components/spa-admin-grid/`
- Keep search debounced and server-driven.
- Ensure the list component cleans up subscriptions (`take(1)` for one-shot requests).

## Reorder behavior

For sortable collections:

- Use the shared reorder component (`SpaReorderListComponent`) for drag-and-drop.
- Backends should expose a dedicated reorder endpoint (commonly `PUT .../reorder`).

