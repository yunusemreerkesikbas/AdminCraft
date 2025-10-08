<!-- 7faa6db8-17fd-41b4-9ead-d3d7ba760c37 70bd1a54-3772-4734-93f2-118fc76f8a0f -->
# Refactor: Page Categories CRUD only; remove Sections/Blocks

## Scope

- Keep only CRUD for `page-categories`.
- Remove non-CRUD `page-categories` endpoints: tree, children, move, reorder.
- Remove Page Builder sections/blocks (controllers, services, frontend usage).
- Keep Page I18N endpoints as-is.

## Backend Changes

- Delete `backend/src/main/java/com/backend/presentation/controller/PageBuilderController.java` and its service interface/impl:
  - `backend/src/main/java/com/backend/application/service/PageBuilderService.java`
  - `backend/src/main/java/com/backend/application/service/impl/PageBuilderServiceImpl.java`
- In `backend/src/main/java/com/backend/presentation/controller/PageCategoryController.java`:
  - Remove methods/endpoints:
    - `GET /page-categories/tree`
    - `GET /page-categories/children`
    - `PUT /page-categories/{id}/move`
    - `PUT /page-categories/reorder`
- In `backend/src/main/java/com/backend/application/service/PageCategoryService.java` and its impl:
  - Remove signatures and implementations for:
    - `listChildrenLocalized(...)`
    - `move(tenantId, id, newParentId)`
    - `reorder(tenantId, parentId, orderedIds)`
  - Drop helper methods only used by above (e.g., `updateDescendantPaths`, level/path bulk updates).
- Ensure remaining CRUD endpoints continue to return tenant-scoped data; do not touch Page I18N endpoints/controllers.
- Optional cleanup: Remove now-unused repository methods tied to children/tree/move/reorder.

## Frontend Changes

- Update `storefront/src/app/modules/admin/api-endpoints.ts`:
  - Remove keys (marked for removal):
    - `pageCategoryTree`, `pageCategoryChildren`, `pageCategoryMove`, `pageCategoryReorder`
    - `pageBuilderSections`, `pageBuilderSectionById`, `pageBuilderBlocks`, `pageBuilderBlockById`

Code reference of lines to delete:

  ```61:74:storefront/src/app/modules/admin/api-endpoints.ts
  // ----- PAGE BUILDER: CATEGORIES -----
  pageCategories: 'page-categories',
  pageCategoryById: 'page-categories/${id}',
  pageCategoryTree: 'page-categories/tree',
  pageCategoryChildren: 'page-categories/children',
  pageCategoryMove: 'page-categories/${id}/move',
  pageCategoryReorder: 'page-categories/reorder',
  
  // ----- PAGE BUILDER: SECTIONS & BLOCKS -----
  pageBuilderSections: 'page-builder/sections',
  pageBuilderSectionById: 'page-builder/sections/${id}',
  pageBuilderBlocks: 'page-builder/blocks',
  pageBuilderBlockById: 'page-builder/blocks/${id}',
  ```

- In `storefront/src/app/modules/admin/custom/pages/page-builder.service.ts`:
  - Remove methods using removed endpoints:
    - `reorderCategories`, `moveCategory`
    - `listSections`, `addSection`, `updateSection`, `deleteSection`
    - `listBlocks`, `addBlock`, `updateBlock`, `deleteBlock`
  - Remove related imports and types usage.
- In `storefront/src/app/modules/admin/custom/pages/page-builder.types.ts`:
  - Remove interfaces tied to sections/blocks (`PageSectionDto`, `PageBlockDto`) and requests, if present.
- In `storefront/src/app/modules/admin/custom/pages/categories/page-categories.component.ts`:
  - Replace tree/children/move/reorder logic with flat CRUD list using `listCategories`, `createCategory`, `updateCategory`, `deleteCategory`.
  - Remove drag-drop handlers and any usage of `reorderCategories`/`moveCategory`.
- In `storefront/src/app/modules/admin/custom/pages/services/category-schema-builder.service.ts`:
  - Keep only create/update schema; remove move/reorder schema or actions if any.
- Search and remove any other references to sections/blocks in components, dialogs, or services.

## i18n and Postman Cleanup

- Remove i18n keys used only by deleted endpoints (e.g., page.category.move.*, page.category.reorder.*, page.section.*, page.block.*).
- Update `AdminCraft_Complete_Postman_Collection.json` to delete requests for tree/children/move/reorder and sections/blocks.

## Keep As-Is

- All `----- PAGE BUILDER: PAGE I18N -----` endpoints and backend logic remain unchanged.

## Acceptance

- Backend compiles; only CRUD endpoints exposed for `page-categories`.
- Frontend builds with no references to removed endpoints; page categories screen supports basic CRUD only.
- Page I18N flows continue to work.

### To-dos

- [ ] Remove PageBuilderController and PageBuilderService (interface/impl)
- [ ] Remove tree/children/move/reorder from PageCategoryController and service
- [ ] Delete sections/blocks and non-CRUD category keys in api-endpoints.ts
- [ ] Remove sections/blocks and move/reorder methods from PageBuilderService
- [ ] Adjust PageCategoriesComponent to flat CRUD; remove tree/move/reorder UI
- [ ] Delete PageSectionDto/PageBlockDto and related types
- [ ] Remove i18n keys and Postman requests for deleted endpoints
- [ ] Run lints/build, fix any errors and missing imports