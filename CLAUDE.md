# SaaS CMS Platform - Clean Architecture Roadmap

## 📋 Project Overview

**Architecture:** Clean Architecture + Multi-Language Support  
**Target Market:** Turkish & International markets  
**Language Support:** Turkish, English (extensible)  
**Development Approach:** Feature-driven, layer-by-layer implementation  

## 🏗️ Clean Architecture Layers

```text
┌─────────────────────────────────────────────────────────────┐
│                 🖥️  PRESENTATION LAYER                      │
│     Controllers, DTOs, Mappers, Validators, i18n           │
├─────────────────────────────────────────────────────────────┤
│                 📋 APPLICATION LAYER                        │
│     Services, Use Cases, Commands, Queries, Events         │
├─────────────────────────────────────────────────────────────┤
│                   🎯 DOMAIN LAYER                           │
│        Entities, Enums, Interfaces, Business Rules         │
├─────────────────────────────────────────────────────────────┤
│                 🔧 INFRASTRUCTURE LAYER                     │
│    Repositories, External APIs, File Storage, Database     │
└─────────────────────────────────────────────────────────────┘
```

## 🌐 Multi-Language Architecture

### **Language Support Strategy:**

- **Backend:** Spring Boot i18n (MessageSource)
- **Frontend Admin:** Angular i18n (@angular/localize)
- **Site Content:** Database-level multi-language support
- **Default Languages:** Turkish (tr), English (en)

### **Implementation Levels:**

1. **System Messages:** Error messages, validation messages, UI labels
2. **Content Management:** Multi-language content creation
3. **Site Publishing:** Language-specific site rendering
4. **Admin Interface:** Localized admin panel

## 📁 Project Structure (Clean Architecture)

```text
src/main/java/com/backend/
├── 🖥️ presentation/
│   ├── controller/
│   │   ├── TenantController.java
│   │   ├── ContentController.java
│   │   ├── MediaController.java
│   │   └── UserController.java
│   ├── dto/
│   │   ├── request/
│   │   ├── response/
│   │   └── mapper/
│   └── config/
│       ├── WebConfig.java
│       └── LocaleConfig.java
├── 📋 application/
│   ├── service/
│   │   ├── TenantService.java
│   │   ├── ContentService.java
│   │   ├── MediaService.java
│   │   └── UserService.java
│   ├── usecase/
│   │   ├── CreateTenantUseCase.java
│   │   ├── PublishContentUseCase.java
│   │   └── UploadMediaUseCase.java
│   ├── command/
│   ├── query/
│   └── event/
├── 🎯 domain/
│   ├── entity/
│   │   ├── Tenant.java
│   │   ├── Content.java
│   │   ├── ContentType.java
│   │   ├── MediaFile.java
│   │   └── User.java
│   ├── enums/
│   ├── exception/
│   ├── repository/
│   └── service/
├── 🔧 infrastructure/
│   ├── persistence/
│   │   ├── entity/
│   │   ├── repository/
│   │   └── config/
│   ├── external/
│   ├── storage/
│   └── i18n/
└── 🌐 shared/
    ├── common/
    ├── utils/
    └── constants/
```

## 🗺️ Sprint Roadmap (Clean Architecture + i18n)

### 🏁 **PHASE 1: MVP FOUNDATION (8-10 weeks)**

#### **Sprint 1: Architecture Foundation + i18n Setup (2 weeks)**

**Goal:** Clean Architecture foundation with multi-language infrastructure

**Infrastructure Layer:**

- [ ] Spring Boot 3.5.3 project setup with Clean Architecture structure
- [ ] Multi-tenant database configuration (database per tenant)
- [ ] JPA entities with multi-language support
- [ ] i18n configuration (MessageSource, LocaleResolver)
- [ ] Database migration scripts

**Domain Layer:**

- [ ] Core domain entities (Tenant, Content, User, MediaFile)
- [ ] Domain enums (TenantStatus, ContentStatus, UserRole, Language)
- [ ] Business exceptions
- [ ] Repository interfaces

**i18n Foundation:**

- [ ] Backend message bundles (messages_tr.properties, messages_en.properties)
- [ ] Language detection middleware
- [ ] Multi-language validation messages
- [ ] Date/time localization

**Development Environment:**

- [ ] Docker setup with multi-language support
- [ ] GitHub Actions CI/CD pipeline
- [ ] Environment configuration

**Output:** Clean Architecture foundation with i18n ready

#### **Sprint 4: Page Builder**

**Supersedes:** The previous "Content Management Core + Multi-Language
Content" plan below. We replace `content`/`content-type` with a modular
Page Builder to power static pages (corporate, services, etc.) with
parent/child categories.

**Goal:** Deliver a multi-language Page Builder enabling non-technical
users to compose pages from sections and blocks, organize them under
hierarchical categories, and publish with SEO.

**Scope:**

- **Domain Layer:**
  - Entities: `Page`, `PageCategory` (self-referencing `parentId`),
    `PageSection`, `PageBlock`.
  - Enums: `PageStatus {DRAFT, PUBLISHED, ARCHIVED, SCHEDULED}`,
    `SectionType`, `BlockType`.
  - Multi-language fields at `Page` level (title, slug, seo).
  - Ordering: `displayOrder` on sections/blocks.

- **Application Layer:**
  - Services: `PageService`, `PageBuilderService`, `CategoryService`.
  - Use cases:
    - Create/Update/Delete Page
    - Add/Update/Reorder/Remove Section
    - Add/Update/Reorder/Remove Block
    - Publish/Unpublish/Schedule Page
    - List Pages by Category and Language
    - Preview Page (draft view)
  - Events: `PagePublishedEvent`, `PageUpdatedEvent`.

- **Infrastructure Layer:**
  - Repositories (JPA + `@EntityGraph` for nested load):
    - `PageRepository`, `PageCategoryRepository`,
      `PageSectionRepository`, `PageBlockRepository`.
  - DB migrations (MySQL):
    - `pages` (language, tenant_id, slug unique per language, status,
      seo fields, scheduled_at, published_at)
    - `page_categories` (parent_id nullable, tenant_id, slug unique)
    - `page_sections` (page_id, type, display_order, data JSON)
    - `page_blocks` (section_id, type, display_order, data JSON)
  - Indices for slugs, language, status, tenant.

- **Presentation Layer (REST):**
  - Controllers: `PageController`, `PageCategoryController`.
  - DTOs: request/response records with compact validation.
  - Responses wrapped in `ApiResponse`.
  - Error handling via `GlobalExceptionHandler` (i18n messages).

- **Admin Frontend (Angular 19):**
  - Module: `pages` with builder UI.
  - Components (selectors follow `<spa-*>`):
    - `<spa-page-builder>`: canvas, drag-drop layout
    - `<spa-page-section>`: section editor
    - `<spa-page-block>`: block editor
    - `<spa-page-categories>`: tree view (parent/child)
  - Features:
    - Drag & drop via Angular CDK
    - i18n (tr/en), SEO fields editor
    - Live preview, autosave draft, publish flow
    - Category tree management (CRUD, reorder)
  - Conventions:
    - Use `forNext` utility
    - Private methods with `#`
    - Strong typings, ≤4 params per function

- **i18n & SEO:**
  - Backend messages in `messages_tr.properties` and `messages_en.properties`.
  - Per-language slug validation and canonical URL.
  - Optional hreflang generation at publish time.

- **Security & Performance:**
  - RBAC: admin/editor permissions for builder actions.
  - Caching: page read cache per tenant+language+slug.
  - N+1-safe fetching for page render paths.

**API Endpoints (Initial):**

- `GET /api/pages?language=tr&categoryId=&status=`
- `GET /api/pages/{id}`
- `GET /api/pages/slug/{language}/{slug}`
- `POST /api/pages` (create page)
- `PUT /api/pages/{id}` (metadata, seo)
- `PUT /api/pages/{id}/publish` | `/unpublish` | `/schedule`
- `POST /api/pages/{id}/sections` | `PUT /sections/{sectionId}` |
  `PATCH /sections/reorder`
- `POST /api/sections/{sectionId}/blocks` | `PUT /blocks/{blockId}` |
  `PATCH /blocks/reorder`
- `GET /api/page-categories/tree` | `POST /api/page-categories` |
  `PUT /api/page-categories/{id}` | `PATCH /api/page-categories/reorder`

**Deliverables:**

- Backend: entities, repositories, services, controllers, migrations.
- Angular: builder module with categories tree and basic blocks
  (hero, text, image, features grid, faq).
- i18n strings, validation messages, sample data.
- Documentation: usage guide and API docs.

**Acceptance Criteria:**

- Create a page (tr/en), add sections/blocks, assign category,
  publish, and fetch by slug per language.
- Category tree supports parent/child and reordering.
- Page preview renders draft structure without publish.
- All endpoints return `ApiResponse` and localized errors.
- Performance: list pages < 300ms on test data; render < 200ms
  cached; N+1 avoided on page fetch.

**Timeline:** 2.5 weeks

- Week 1: Backend schema, repositories, services, CRUD APIs.
- Week 2: Angular builder UI (sections/blocks, preview, categories).
- Half week: SEO/i18n polish, tests, docs, sample pages.

#### **Sprint 10: UI Components Management (Type-Based Routing Refactor)**

**Goal:** Complete CRUD for site UI components with type-based routing,
semantic URLs, typed DTOs, and strong validation. Refactor from generic
endpoints to type-specific RESTful architecture. Ensure perfect tenant
isolation and Clean Architecture compliance.

**Architecture Change:**

- **Old**: `/api/components?type=TYPE` (generic endpoint with filtering)
- **New**: `/api/components/{type}` (type-based RESTful endpoints)

**Decisions:**

- Component types: `NAVBAR`, `LOGO`, `CTA`, `BRANDS`, `FAQ`, `BREADCRUMB`.
- Status: `ACTIVE` / `INACTIVE` plus `visible` flag.
- `sortOrder`: optional, default `0`.
- Type-based routing: Each component type has dedicated endpoints.
- URL-Request consistency: URL type **must** match request body type.
- Translations stored as map: `translations: Record<lang, T>` (e.g. `tr`, `en`).
- Unique constraint: `(tenantId, type, key)` must be unique.
- Admin UI uses sidebar children under "UI Component Yönetimi" with
  type-specific routes: `/:tenant/component/type/:type`.
- Type-specific validation: Each component type has data structure rules.
- Performance optimized: Type-based database indexes.

**Backend Refactor:**

- **Domain Layer:**
  - Enums: `ComponentType { NAVBAR, LOGO, CTA, BRANDS, FAQ, BREADCRUMB }`,
    `ComponentStatus { ACTIVE, INACTIVE }`.
  - Entities:
    - `Component`: id, tenantId, type, key, status, visible, sortOrder(0),
      createdAt, updatedAt. Unique (tenantId, type, key).
    - `ComponentTranslation`: component_id, language, title, subtitle, data.
  - Business Rules:
    - Type-specific data validation per component type.
    - URL-Request type consistency validation.

- **Application Layer:**
  - Service: `ComponentService` with type-specific methods:
    - `createByType(tenantId, type, request)`
    - `updateByType(tenantId, type, id, request)`
    - `deleteByType(tenantId, type, id)`
    - `getByType(tenantId, type, id)`
    - `listByType(tenantId, type)`
  - Type-specific validation:
    - NAVBAR: requires `items` array with `label` and `url`
    - CTA: requires `title`, `buttonText`, `buttonUrl`
    - FAQ: requires `items` array with `question` and `answer`
    - BRANDS: requires `brands` array with `name` and `logoUrl`
    - LOGO: requires `logoUrl`, optional `altText`
    - BREADCRUMB: requires `separator`, optional `homeText`

- **Infrastructure Layer:**
  - Repositories: `ComponentRepository` with optimized type-based queries:
    - `findByTenantIdAndType(tenantId, type)`
    - `findByTenantIdAndTypeAndId(tenantId, type, id)`
    - `existsByTenantIdAndTypeAndKey(tenantId, type, key)`
  - Database optimizations:
    - Composite indexes: `(tenant_id, type)`, `(tenant_id, type, status)`
    - Check constraints for type and status validation.

- **Presentation Layer (REST):**
  - Controller: `ComponentController` with type-based endpoints:
    - `GET /api/components/{type}` → list by type
    - `GET /api/components/{type}/{id}` → get by type and ID
    - `POST /api/components/{type}` → create by type
    - `PUT /api/components/{type}/{id}` → update by type
    - `DELETE /api/components/{type}/{id}` → delete by type
  - Type validation: URL type must match request body type.
**Frontend Refactor (Angular 19):**

- **Routing Architecture:**
  - Navigation: under `UI Component Yönetimi` add children for each type:
    - Navbar → `/:tenant/components/navbar`
    - Logo → `/:tenant/components/logo`
    - CTA → `/:tenant/components/cta`
    - Brands → `/:tenant/components/brands`
    - FAQ → `/:tenant/components/faq`
    - Breadcrumb → `/:tenant/components/breadcrumb`
  - Route structure:
    - `/:tenant/components/navbar` (navbar list page)
    - `/:tenant/components/navbar/new` (create navbar form)
    - `/:tenant/components/navbar/:id` (edit navbar form)

- **Service Layer:**
  - Type-specific service methods:
    - `listNavbar()`, `createNavbar()`, `updateNavbar()`, `deleteNavbar()`
    - `listCta()`, `createCta()`, `updateCta()`, `deleteCta()`
    - Generic method: `listByType(type)`, `createByType(type, data)`
  - Service endpoints map to new API structure:
    - `GET /api/components/navbar` for navbar list
    - `POST /api/components/cta` for CTA creation

- **Component Architecture:**
  - Type-specific list components: `<spa-navbar-list>`, `<spa-cta-list>`
  - Type-specific form components: `<spa-navbar-form>`, `<spa-cta-form>`
  - Type-specific editors: `<spa-navbar-editor>`, `<spa-cta-editor>`
  - Shared base components for common functionality
  - page layout için listeleme sayfasındaki görünüm storefront\src\app\modules\admin\apps\ecommerce\inventory\list\inventory.component.html görünümünde, create ve edit görünümleri ise storefront\src\app\modules\admin\pages\settings\settings.component.html dosyasındaki gibi olsun .
  - create/edit layoutundaki solda bulunan tab'ları general ve lang(TR/EN) olarak oluşturabiliriz.

- **Form Validation:**
  - Type-specific validation rules in form components
  - URL-Request type consistency validation
  - Component data structure validation (navbar items, CTA fields, etc.)
  - Real-time validation with error display

- **State Management:**
  - Type-specific stores or services for each component type
  - Optimistic updates with error rollback
  - Toast notifications for CRUD operations

**API Contract Refactor:**

- **Request DTO:**

  ```typescript
  interface ComponentRequest {
    tenantId: number;
    type: ComponentType; // Must match URL type
    key: string;
    status?: 'ACTIVE' | 'INACTIVE';
    visible?: boolean;
    sortOrder?: number;
    translations: {
      [lang: string]: {
        title?: string;
        subtitle?: string;
        data?: string; // Type-specific JSON structure
      };
    };
  }
  ```

- **Response DTO:**

  ```typescript
  interface ComponentResponse {
    id: number;
    tenantId: number;
    type: ComponentType;
    key: string;
    status: 'ACTIVE' | 'INACTIVE';
    visible: boolean;
    sortOrder: number;
    tr?: ComponentTranslation;
    en?: ComponentTranslation;
  }
  ```

**Database Schema Updates:**

- **Performance Indexes:**

  ```sql
  -- Type-based query optimization
  KEY idx_ui_component_tenant_type (tenant_id, type)
  KEY idx_ui_component_tenant_type_status (tenant_id, type, status)
  KEY idx_ui_component_tenant_type_sort (tenant_id, type, sort_order, status)
  ```

- **Validation Constraints:**

  ```sql
  CONSTRAINT chk_ui_component_type
    CHECK (type IN ('NAVBAR', 'LOGO', 'CTA', 'BRANDS', 'FAQ', 'BREADCRUMB'))
  CONSTRAINT chk_ui_component_status
    CHECK (status IN ('ACTIVE', 'INACTIVE'))
  ```

**Migration Strategy:**

1. **Phase 1:** Backend API refactor (controller, service, repository)
2. **Phase 2:** Database index optimization
3. **Phase 3:** Frontend routing and service updates
4. **Phase 4:** Component-specific form editors
5. **Phase 5:** Testing and validation

**Acceptance Criteria:**

- ✅ Type-based endpoints: `/api/components/{type}` for all 6 component types
- ✅ URL-Request type consistency enforced with 400 error on mismatch
- ✅ Unique `(tenantId, type, key)` constraint enforced; 409 on conflict
- ✅ Frontend routing: `/:tenant/components/{type}` with type-specific UI
- ✅ Type-specific data validation per component type
- ✅ Performance: type-based queries < 200ms with proper indexing
- ✅ Perfect tenant isolation across all operations
- ✅ Clean Architecture compliance maintained
- ✅ No breaking changes to existing translations structure

#### **Sprint 11: Tenant Supported Languages**

**Goal:** Manage site languages inline on Tenant screen; centralize language
and translation orchestration; wire component translations to tenant languages.

**Decisions (final):**

- Supported language enum (platform catalog): `tr` (default at tenant
  creation), `en`, `es`, `ar`, `ru`.
- Admin UI is language-agnostic; public site uses PATH strategy (`/{lang}/…`).
- Tenant languages are managed via inline multi-select on
  `tenants-list` (no separate page):
  - TR shown in multi-select only if it is not the `defaultLanguage`.
  - Changing `defaultLanguage` auto-adds that language to `supported` if
    missing.
- Separate endpoint for tenant languages: `PATCH /api/languages/tenant`.
- RTL impact: only public site for now; Admin UI remains LTR.
- Component translations: default language translation NOT mandatory on create.
  Reads may fallback to default. No delete translation endpoint; data retained
  (hidden if language unsupported).

**Backend Scope (Staged Plan):**

- Stage 1 (Non-breaking):
  - Keep `ComponentResponse` with `tr`/`en` fields.
  - Introduce `TranslationService` to orchestrate all translation CRUD,
    batch reads, and fallback. `ComponentServiceImpl` delegates to it.
  - Expand `LanguageService`:
    - `getSupportedLanguages(tenantId)` with short TTL cache
    - `validateTranslationKeys(tenantId, translations)`
    - `resolveEffectiveLanguage(tenantId, requested)`
  - Component create/update validates `translations.keys ⊆ supported` via
    `LanguageService`; translations persisted via `TranslationService`.
  - Site read uses `resolveEffectiveLanguage`+fallback via `TranslationService`.

- Stage 2 (Breaking - implemented):
  - `ComponentResponse` artık `translations: Map<string, TranslationDto>`
    döner (TR/EN alanları kaldırıldı).
  - Admin UI dinamik dil sekmelerini `translations` map’inden üretmelidir.

**API (Presentation):**

- `GET /languages` → platform catalog.
- `GET /languages/tenant` → `{ defaultLanguage, supported[] }`.
- `PATCH /languages/tenant` (admin-only) with rules:
  - `supported` not empty; contains `defaultLanguage`.
  - `supported ⊆ {tr,en,es,ar,ru}`; ISO 639-1 check.
  - Tenant isolation; cache invalidation after update.

**API (Response) – Component (Stage 2):**

```typescript
interface ComponentResponse {
  id: number;
  tenantId: number;
  type: ComponentType;
  key: string;
  status: 'ACTIVE' | 'INACTIVE';
  visible: boolean;
  sortOrder: number;
  translations: Record<string, {
    title?: string;
    subtitle?: string;
    data?: string; // type-specific JSON
  }>;
}
```

**Application:**

- `LanguageService`: resolve + validation + caching (short TTL).
- `TranslationService`: centralized translation CRUD, batch read, fallback
  (metotlar: upsertTranslations, findByComponentIdsAndLanguage,
  findByComponentIdAndLanguages, getForLanguageWithFallback, deleteByComponentId).
- `ComponentService`: yalnızca orkestrasyon yapar; dil-özel (TR/EN) kod yok.

**Infrastructure:**

- Use repository batch queries to avoid N+1.
- Indexes already present on component tables (tenant,type,status,…).

**Frontend (Admin) Scope:**

- `tenants-list.component` inline form:
  - Multi-select for `supportedLanguages` (EN/ES/AR/RU). TR is implicit if
    it is the default; otherwise selectable.
  - Separate select for `defaultLanguage`; on change, auto-add to
    `supported`.
  - On create: default `tr`, supported `["tr"]`.
  - On save: call `PATCH /languages/tenant`; then refresh language
    facade/state so other forms (components) update tabs dynamically.
- HTTP interceptor continues to set `Accept-Language` from active language.
- Component forms render language tabs from tenant `supportedLanguages`.

**Validation Rules:**

- Tenant: `default ∈ supported`, `supported ⊆ {tr,en,es,ar,ru}`, ≥1 language.
- Components: `translations.keys ⊆ supported`; default translation not
  required; unknown language → 400.

**API Examples:**

- `GET /languages/tenant` →
  `{ "defaultLanguage": "tr", "supported": ["tr","en"] }`
- `PATCH /languages/tenant` →
  `{ "defaultLanguage": "en", "supported": ["tr","en","ar"] }`

**Acceptance Criteria:**

- Admin can pick multiple supported languages inline on tenant form; TR is
  auto-included when default; default change auto-adds to supported.
- Backend enforces validation and tenant isolation; caches invalidated on
  update.
- Component create/update rejects translations outside supported set; reads
  fallback to default when requested translation does not exist.
- Stage 2: `ComponentResponse.translations` map’i yayında; Admin UI bu map’e
  göre dinamik sekmelerle çalışır.
- Public site only exposes paths for supported languages.

**Notes:** No translation deletion endpoint in this sprint; data retained and
not shown if language is unsupported. RTL applies only to public site.

#### **Sprint 11 – Phase 2: Navbar i18n Menu Management (Config-Driven, No JSON Editing in UI)**

**Goal:** Manage Navbar with dynamic language tabs (per tenant languages), menu
items with 3-level hierarchy, language-specific URL and SEO fields, and fully
type-safe admin forms without any JSON editing.

**Decisions:**

- Language-based URL: `translations[lang].url` (no global URL)
- SEO fields (language-based): `seoTitle`, `seoDescription`, `seoKeywords`
- Identifiers: user-provided `uid` + backend-generated `uuid`
- Reorder: for now via `sortOrder` input (DnD can come later)
- Internal link: free-text URL for now
- Hierarchy: up to 3 levels; no item count limit
- Component root adds `styleClasses` (global styling hooks)

**Backend Scope:**

- DB (MySQL):
  - `components` (existing) → new column: `style_classes VARCHAR(255) NULL`
  - `component_items`:
    - `id BIGINT PK AI`, `component_id BIGINT FK components(id)`
    - `uid VARCHAR(100) NOT NULL` (user-provided, UNIQUE per component)
    - `uuid CHAR(36) NOT NULL` (backend-generated)
    - `parent_id BIGINT NULL` (self FK)
    - `level TINYINT NOT NULL DEFAULT 1` (CHECK level BETWEEN 1 AND 3)
    - `visible BOOLEAN DEFAULT TRUE`, `sort_order INT DEFAULT 0`
    - `created_at`, `updated_at`
    - Constraints/Indexes: `UNIQUE(component_id, uid)`,
      `INDEX(component_id, parent_id)`, `INDEX(level)`
  - `component_item_translations`:
    - `id BIGINT PK AI`, `item_id BIGINT FK component_items(id)`
    - `language VARCHAR(5) NOT NULL` (ISO 639-1)
    - `title VARCHAR(200) NULL`, `subtitle TEXT NULL`
    - `url VARCHAR(255) NULL`
    - `seo_title VARCHAR(60) NULL`, `seo_description VARCHAR(160) NULL`,
      `seo_keywords VARCHAR(255) NULL`
    - Constraints/Indexes: `UNIQUE(item_id, language)`,
      `INDEX(item_id, language)`, `INDEX(language)`
  - `tenant_supported_languages.language` length must be `VARCHAR(5)`

- Repositories:
  - `ComponentItemRepository`, `ComponentItemTranslationRepository`
  - Use `@EntityGraph` to avoid N+1 on tree/children fetches

- Services (Application):
  - `ComponentItemService`:
    - `listTree(componentId)`, `createItem(componentId, dto)`
    - `updateItem(itemId, dto)`, `deleteItem(itemId)`
    - `reorder(componentId, changes[])` (bulk parent/sort updates)
  - `TranslationService`: upsert and batch read
  - Rules: `translations.keys ⊆ tenant.supportedLanguages`, `level ≤ 3`,
    prevent cyclic parents, ensure `uid` uniqueness

- Endpoints (type-based subresources):
  - `GET /api/components/navbar/{id}/items/tree`
  - `POST /api/components/navbar/{id}/items`
  - `PUT /api/components/navbar/items/{itemId}`
  - `DELETE /api/components/navbar/items/{itemId}`
  - `PATCH /api/components/navbar/{id}/items/reorder`
  - `PUT /api/components/{type}/{id}` (includes component root `styleClasses`)

**Frontend Scope (Angular 19):**

- Config-driven schema (e.g., `components.config.ts`):
  - NAVBAR `globalFields`: `visible`, `sortOrder`, `styleClasses`
  - NAVBAR `i18nFields`: `title`, `subtitle`, `url`, `seoTitle`,
    `seoDescription`, `seoKeywords`
- Language tabs generated dynamically from `TenantLanguagesService`
- NavbarForm:
  - General tab: global fields
  - Language tabs: i18n fields (schema-driven; no JSON editor)
  - Item management page: tree list + create/edit dialogs (forms)
- Validation:
  - Required, maxLength, pattern (uid), `level ≤ 3`, default-language fallback
- UX:
  - Reorder via input; recompute `level` on parent change
  - Parent selector shows only valid candidates (depth guard)

**API Contracts (summary):**

- Component (root) update (existing endpoint):
  - Request: `{ ..., styleClasses?: string, translations: Record<lang, {...}> }`
- Item create/update:
  - Request: `{ uid: string, parentId?: number, visible?: boolean,
      sortOrder?: number,
      translations: Record<lang, {
        title?: string, subtitle?: string, url?: string,
        seoTitle?: string, seoDescription?: string, seoKeywords?: string
      }> }`
  - Response: item + translations map (aligned with Stage 2)

**Validation & Security:**

- Tenant isolation: `X-Tenant-ID` header; repositories filter by tenant
- URL/SEO required rules enforced by type-specific validators
- RBAC: editor/admin permissions

**Acceptance Criteria:**

- Navbar language tabs update dynamically when tenant languages change
- Navbar items can be created/updated/deleted up to 3 levels deep
- Ordering managed by `sortOrder`; parent changes are validated safely
- No JSON editing; all fields via type-safe form controls
- Endpoints follow type-based architecture; no N+1 issues

#### **Sprint 11 – Phase 3: Navbar API Simplification (Flat Items, Tree Removed)**

**Goal:** Remove the `/items/tree` endpoint. Return NAVBAR data as a flat
`items[]` list inside `GET /api/components/{type}/{id}`. The client builds
the hierarchy using `parentId`.

**Decisions (final):**

- `/api/components/navbar/{id}/items/tree` is removed.
- Combined response under `GET /api/components/{type}/{id}`.
- Root payload is reduced to `{ id, tenantId, type, key, uid, uuid }`.
- Root fields removed: `status, visible, sortOrder, styleClasses, translations`.
- `items[]` is a flat list; each item carries `parentId` for hierarchy.
- No `depth/expand/visible` query parameters.
- `visible` exists only on item level as a boolean.
- Ordering: `parentId NULLS FIRST`, then `level ASC`, then `sortOrder ASC`, then `id ASC`.
- Multi‑tenant isolation enforced via `X-Tenant-ID`; type mismatch → 400.

**Endpoint (final):**

- `GET /api/components/{type}/{id}`
  - Header: `X-Tenant-ID: <tenantId>`
  - Response (sample):

```json
{
  "result": "SUCCESS",
  "message": "ui.component.get.success",
  "data": {
    "id": 1,
    "tenantId": 1,
    "type": "NAVBAR",
    "key": "main",
    "uid": "main-navbar",
    "uuid": "8f9b5f2c-3f8e-4b2a-8f2e-0a1e9f2a9b00",
    "items": [
      {
        "id": 101,
        "uid": "services",
        "uuid": "0e53c1a6-2d9a-4a2c-a2c2-9c8b6f6c0001",
        "parentId": null,
        "level": 1,
        "visible": true,
        "sortOrder": 2,
        "translations": {
          "tr": {
            "title": "Hizmetler",
            "subtitle": null,
            "url": "/hizmetler",
            "seoTitle": "Hizmetler",
            "seoDescription": "Tüm hizmetlerimiz",
            "seoKeywords": "hizmet, solar, bakım"
          },
          "en": {
            "title": "Services",
            "subtitle": null,
            "url": "/services",
            "seoTitle": "Services",
            "seoDescription": "All services",
            "seoKeywords": "service, solar, maintenance"
          }
        }
      }
    ]
  }
}
```

**Backend Scope:**

- Controller: remove `/items/tree`; return `items[]` within `/{type}/{id}`.
- Service: fetch flat items by componentId with tenant filter; apply ordering;
  load translations via batch/`@EntityGraph` (no N+1).
- DTO/Mapper: simplify root; implement the `items[]` schema.
- Cache: unified response cache key `tenantId+type+id`; invalidate on item
  mutations.
- Security: validate `X-Tenant-ID`; ensure URL–body type consistency.
- Tests: remove `/items/tree` tests; add unified response tests.

**Frontend (Admin) Scope:**

- Service: stop calling `/items/tree`; consume `items[]` from `/{type}/{id}`.
- Models: update root + `items[]` types (including SEO fields).
- UI: build parent/child select options from `items[]`; show `visible` as a
  boolean; no depth/expand.

**Acceptance Criteria:**

- `GET /api/components/{type}/{id}` returns root + `items[]` in a single call.
- All `/items/tree` usages are removed from the project.
- Admin UI correctly builds hierarchy and ordering from `items[]`.
- No tenant isolation leaks; type mismatch returns 400.
- No N+1 queries; target latency < 200ms on sample data.

**Migration Notes:**

- Replace frontend usages of `/items/tree` with `/{type}/{id}` response.
- Update Postman collection and API docs per the new contract.

#### **Sprint 6: Admin Toast Notifications (1 week)**

**Goal:** Implement a dynamic, reusable toast notification system for the
Admin UI using ngx-toastr with i18n, accessibility, and error handling.

**Decisions:**

- Library: ngx-toastr
- Config: via `provideToastr` global config
  (position=top-right, duration=4000ms, maxOpened=3, autoDismiss, newestOnTop)
- No custom DI tokens for defaults
- No custom notifications container component
- Icons: optional (Angular Material `mat-icon` when needed)
- Error interceptor: Global, rule-based filtering with lightweight dedupe
- Telemetry: optional; Dev enabled, Prod off by default

**Scope (Admin Frontend - Angular 19):**

- `NotificationService`: typed facade over ngx-toastr using Transloco for i18n
- `errorToastInterceptor`: global interceptor with rule-based filtering
  and 2s deduplication per key
- No per-request suppress mechanism (no headers, no HttpContextToken);
  suppression is handled by rules (e.g., 401 and validation errors)
- i18n: accepts raw text or Transloco keys with params
- Accessibility: ARIA live region, focusable actions, high contrast
- Conventions: use `forNext`, `take(1)`, private methods with `#`,
  selectors prefixed with `<spa-*>`

**Features:**

- Positions: top/bottom-left/right/center
- Variants: success, warning, alert/error, info
- Per-toast overrides: duration, position, icon, action
- Max-opened with oldest auto-dismiss; duplicate prevention
- Optional: close button, tap-to-dismiss, progress bar, sticky

**Error Interceptor Rules:**

- 5xx, network: alert toast; dedupe 2s
- 401: no toast; trigger auth flow
- 403: warning/alert toast (permission)
- 400/422: if validation body present → no toast (UI shows inline);
  else warning/info toast
- No header or context based opt-outs (kept simple and local to rules)

**Telemetry:**

- Dev: 100% sampling; Stage: 20%; Prod: 0% (configurable)
- Structured events: type, position, duration, source, action result
- In-memory ring buffer (<=100) + `NotificationService.events$`
- Optional message masking for PII safety

**Deliverables:**

- Installed ngx-toastr and themed styles
- `NotificationService` and global interceptor wired with filters
- i18n integration and sample usages (Settings, Builder, Category CRUD)
- Unit tests for service API and interceptor rules
- Developer docs: usage, config, a11y, migration notes

**Acceptance Criteria:**

- Toasts render with correct variant, position, and duration
- i18n keys resolve with parameters; AA contrast respected
- Max-opened and duplicate-prevent work as configured
- Interceptor surfaces errors per rules without noise
- No memory leaks (`take(1)`, async pipe); tests green

#### **Sprint 9: Site Settings Module (1 week)**

**Goal:** Build the Site Settings module (backend service + Admin UI
forms) with language-aware fields and a simple API contract.

**Scope:**

- **Domain Layer:**
  - Entity: `SiteSetting` (key/value JSON, optional `language`)
  - Aggregate: `SiteSettingsAggregate` (typed view over keys)
  - Enum: `SettingType {TEXT, NUMBER, BOOLEAN, JSON, URL, I18N_TEXT}`

- **Application Layer:**
  - Service: `SiteSettingsService`
  - Use cases:
    - `getAdminSettings()` returns saved values only (no fallback)
    - `updateGlobalSettings(dto)`
    - `upsertI18nSettings(language, dto)`
  - DTOs (records with compact validation):
    - `SiteSettingsGlobalDto`
    - `SiteSettingsI18nDto`
    - `SiteSettingsResponseDto` (merged/global + i18n)

- **Infrastructure Layer:**
  - Table: `site_settings` (already designed in schema)
    - Columns: id, setting_key, setting_value JSON, language NULLable,
      setting_type, category, is_public, sort_order, updated_by, updated_at
  - Repository: `SiteSettingRepository extends JpaRepository`
    - `findByLanguage(language)`
    - `findBySettingKeyAndLanguage(key, lang)`
    - `findByLanguageIsNull()` (global)
  
- **Presentation Layer (REST):**
  - `@RestController` `SiteSettingsController` → `/api/site-settings`
  - Endpoints:
    - `GET /api/site-settings` → returns `global` + `languages` map
    - `PATCH /api/site-settings` → partial updates (global and/or languages)
  - Responses: `ApiResponse`
  - Validation: URL/email, length limits, enum checks
  - Errors: `GlobalExceptionHandler` (i18n)

- **Admin Frontend (Angular 19):**
  - Module: `@admin` (components will live under `@admin/`)
  - Pages: follow `@settings/` page structure (routing/layout/tabs)
  - Components (selectors `<spa-*>`):
    - `<spa-site-settings-form>` (tabs: General, Contact, Social, SEO)
    - `<spa-site-social-links>`
    - `<spa-site-seo-settings>`
    - `<spa-site-address-form>`
  - State: service/store with `GET /api/site-settings` (global + languages)
  - Conventions: `forNext`, `take(1)`, `#` private methods, strong typings
  - Notifications: `NotificationService` + error interceptor

**Settings Fields (revised):**

- Global (language-agnostic, single address):
  - `contactEmail`, `contactPhone`, `whatsappPhone`
  - `address` { line1, line2?, city, state?, postalCode, country,
    geo { lat, lng }, mapEmbedUrl? }
  - `businessHours` (day/time ranges)
  - `social` { facebook?, instagram?, x?, linkedin?, youtube?, tiktok? }
  - `canonicalBaseUrl`
  - `robots` (default `index,follow`)
  - Note: Logo/Favicon fields are deferred; will be handled by Media module.

- Language-specific (per `lang`):
  - `siteName` (display title)
  - `tagline`
  - `seo` { title, description, keywords[], ogTitle?, ogDescription?,
    ogImageMediaId?, twitterCard? }
  - `footerText`, `headerTopbarText`
  - `addressLocalized` (optional)

**DTOs (records):**

```java
public record SiteSettingsGlobalDto(
    String contactEmail,
    String contactPhone,
    String whatsappPhone,
    AddressDto address,
    BusinessHoursDto businessHours,
    SocialLinksDto social,
    String canonicalBaseUrl,
    String robots
) {}

public record SiteSettingsI18nDto(
    String siteName,
    String tagline,
    SeoDefaultsDto seo,
    String footerText,
    String headerTopbarText,
    AddressLocalizedDto addressLocalized
) {}

public record SiteSettingsResponseDto(
    SiteSettingsGlobalDto global,
    SiteSettingsI18nDto i18n,
    String language
) {}
```

**API Examples:**

- `GET /api/site-settings` →

```json
{
  "global": {
    "contactEmail": "info@site.com",
    "contactPhone": "+90 555 000 00 00",
    "address": { "line1": "Büyükdere Cd.", "city": "İstanbul", "country": "TR" },
    "social": { "facebook": "https://facebook.com/brand" },
    "canonicalBaseUrl": "https://www.site.com",
    "robots": "index,follow"
  },
  "languages": {
    "tr": {
      "siteName": "Site Adı",
      "tagline": "Slogan",
      "seo": { "title": "Başlık TR", "description": "Açıklama TR", "keywords": ["k1","k2"] },
      "footerText": "Alt bilgi TR"
    },
    "en": {
      "siteName": "Site Name",
      "tagline": "Tagline",
      "seo": { "title": "Title EN", "description": "Description EN", "keywords": ["k1","k2"] },
      "footerText": "Footer EN"
    }
  }
}
```

- `PATCH /api/site-settings` (kısmi güncelleme örneği) →

```json
{
  "global": {
    "contactEmail": "hello@site.com"
  },
  "languages": {
    "en": {
      "seo": { "title": "Updated Title EN" }
    }
  }
}
```

**Performance Notes:**

- Add DB indices on `(setting_key, language)` and `(language)`.

**Security:**

- Admin endpoints require `admin` role
- Public endpoint returns only `is_public=true` keys
- Input validation with whitelists for allowed hosts in URLs (OWASP)

**Deliverables:**

- Backend: entity, repository, service, controller, migrations, i18n messages
- Admin Angular: settings module ve formlar (toasts ile)

**Acceptance Criteria:**

- Admin can update global and language-specific settings; validations
  enforced; toasts show success/failure
- Admin tarafında dil seçimleri ile i18n alanları yönetilebilir
- Dil alanları eksikse tenant default’a fallback (admin GET’te)
- All responses wrapped in `ApiResponse`; errors localized

**Timeline:** 1 week (Admin Only)

- Days 1-2: Backend schema, service, controller
- Days 3-4: Admin UI forms and API wiring
- Day 5: Testler, dokümantasyon

**Open Questions:**

1) Do we need multi-location addresses (array) instead of a single one?
2) Should `siteName` always be per-language (move from global)?
3) Any additional integrations (reCAPTCHA, Hotjar, Intercom) to include?
4) Should settings changes support draft/preview before publish?
5) Privacy policy / terms URLs per language to include in settings?
6) Do we require per-environment integration IDs (Dev/Stage/Prod)?
7) Any other social platforms to add by default?

// =================================================================
// SaaS CMS Platform - Application Services Examples
// Clean Architecture + Multi-Language Support
// =================================================================

// =================================================================
// APPLICATION LAYER OVERVIEW
// =================================================================

/*
Application Services in Clean Architecture:

1. Orchestrate business workflows
2. Coordinate domain objects
3. Handle transactions
4. Publish domain events
5. Convert between DTOs and domain objects
6. Implement use cases
7. Handle multi-language business logic

Key Principles:

- No business logic (that's in Domain layer)
- Thin layer - just orchestration
- Transaction boundaries
- Error handling and validation
- Multi-tenant and multi-language aware
*/
