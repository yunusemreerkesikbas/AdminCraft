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

- **Users**: `GET /api/users` in `UserController` (paginated, searchable across 5 fields)
- Navigation root nodes: `GET /api/navigation/nodes` in `NavigationController` (paginated; admin UI uses a **tree view** that loads roots then lazy-loads children via `GET /nodes/{id}`)
- Page templates: `GET /api/page-templates` in `PageTemplateController`
- Media list: `GET /api/media` in `MediaController`
- Component Library components: `GET /api/components` in `ComponentController`
- Component Library types: `GET /api/components/types` in `ComponentTypeController`

**Tree-style list UIs**: Some admin pages (e.g. Navigation, Product categories) use a **tree view** instead of a paginated grid. They still rely on list or tree endpoints (e.g. `GET /api/navigation/nodes` for roots, `GET /api/navigation/nodes/{id}` for a subtree); the frontend does not use `BasePaginatedListComponent` for those views.

## Frontend implementation

### Modern Pattern (Recommended)

**Use Base Components for Consistent CRUD Operations**:

1. **Extend `BasePaginatedListComponent`**:
   - Provides automatic pagination, search, sort handling
   - Signal-based reactive state management
   - Built-in loading states and error handling
   - Example: `UsersListComponent extends BasePaginatedListComponent`

2. **Use `CrudStore` for State Management**:
   - Extends base store with pagination metadata
   - Signal-based (Angular 19+)
   - Automatic loading indicators
   - Example: `UserStore extends CrudStore<User>`

3. **Extend `CrudHttpService` for API Calls**:
   - Consistent endpoint configuration
   - Built-in pagination, search, sort support
   - Type-safe CRUD operations
   - Example: `UsersService extends CrudHttpService<User, CreateUserRequest, UpdateUserRequest>`

4. **Use Shared UI Components**:
   - `SpaAdminGrid` - Declarative table component with actions
   - `SpaAdminPaginator` - Consistent pagination controls
   - `SpaAdminSortDropdown` - Sort option selector
   - `AdminPageHeader` - Page header with search and actions

**Example Structure** (Users Module):
```typescript
// Store
export class UserStore extends CrudStore<User> {}

// Service
export class UsersService extends CrudHttpService<User, CreateUserRequest, UpdateUserRequest> {
    protected endpoints = {
        list: 'users',
        getById: 'userById',
        create: 'users',
        update: 'userById',
        delete: 'userById'
    };
}

// Component
export class UsersListComponent extends BasePaginatedListComponent<User, CreateUserRequest, UpdateUserRequest> {
    protected override defaultSort = 'createdAt,desc';
    protected override defaultPageSize = 20;

    protected columns = signal<GridColumn<User>[]>([...]);
    protected actions = signal<any[]>([...]);

    constructor(service: UsersService, store: UserStore) {
        super(service, store);
    }
}
```

**Benefits**:
- **93% less template code** (declarative vs imperative)
- **41-77% less component code** (inheritance vs composition)
- **Consistent UX** across all list views
- **Type-safe** operations
- **Automatic state management** via signals

### Legacy Pattern (Avoid for New Code)

- Manual BehaviorSubjects for state
- Client-side filtering/sorting/pagination
- Imperative template with manual table markup
- Direct API calls without base service

**Conventions**:
- Keep search debounced (300ms default in base component)
- Server-driven pagination/search/sort
- Clean up subscriptions with `take(1)` for one-shot requests
- Use signals over BehaviorSubjects (Angular 19+)

## Reorder behavior

For sortable collections:

- Use the shared reorder component (`SpaReorderListComponent`) for drag-and-drop.
- Backends should expose a dedicated reorder endpoint (commonly `PUT .../reorder`).
