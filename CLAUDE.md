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

#### **Sprint 2: Tenant Management + Multi-Language Admin (2 weeks)**

**Goal:** Complete tenant lifecycle with localized admin interface

**Domain Layer:**

```java
@Entity
@Table(name = "tenants")
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String subdomain;
    private String companyName;
    private String databaseName;
    
    @Enumerated(EnumType.STRING)
    private TenantStatus status;
    
    @Enumerated(EnumType.STRING)
    private Language defaultLanguage; // TR, EN
    
    private Set<Language> supportedLanguages;
    
    // Business methods
    public boolean canBeActivated() {
        return status == TenantStatus.PENDING;
    }
    
    public void activate() {
        if (!canBeActivated()) {
            throw new TenantCannotBeActivatedException();
        }
        this.status = TenantStatus.ACTIVE;
    }
}
```

**Application Layer:**

- [ ] TenantService with use cases
- [ ] CreateTenantUseCase, ActivateTenantUseCase
- [ ] Tenant DTOs with i18n support
- [ ] Tenant validation with localized messages

**Infrastructure Layer:**

- [ ] TenantRepository implementation
- [ ] Database tenant creation service
- [ ] Tenant configuration persistence

**Presentation Layer:**

- [ ] TenantController with localized responses
- [ ] Angular admin setup with i18n (@angular/localize)
- [ ] Localized tenant management UI
- [ ] Language switcher component

**i18n Features:**

- [ ] Tenant-specific language settings
- [ ] Localized error messages
- [ ] Admin panel language switching
- [ ] Validation message localization

**Output:** Complete tenant management with multi-language admin

#### **Sprint 3: User Management + Authentication (1.5 weeks)**

**Goal:** User authentication and role management with i18n

**Domain Layer:**

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String email;
    private String passwordHash;
    private String fullName;
    
    @Enumerated(EnumType.STRING)
    private UserRole role;
    
    @Enumerated(EnumType.STRING)
    private Language preferredLanguage;
    
    private Long tenantId;
    
    // Business methods
    public boolean canAccessContent(Content content) {
        return this.tenantId.equals(content.getTenantId()) &&
               this.role.hasPermission(Permission.READ_CONTENT);
    }
}
```

**Application Layer:**

- [ ] UserService, AuthenticationService
- [ ] LoginUseCase, CreateUserUseCase
- [ ] JWT token service with language preference
- [ ] Role-based authorization

**Infrastructure Layer:**

- [ ] UserRepository implementation
- [ ] Spring Security configuration
- [ ] JWT token handling

**Presentation Layer:**

- [ ] AuthController with localized responses
- [ ] Login/logout endpoints with i18n
- [ ] User management UI with language preference
- [ ] Role management interface

**i18n Features:**

- [ ] User-specific language preference
- [ ] Localized authentication messages
- [ ] Role-based UI language
- [ ] Personalized welcome messages

**Output:** Complete authentication system with language preferences

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

#### **Sprint 10: UI Components Management**

**Goal:** Deliver minimal CRUD for UI components used on the site front-end
(navbar, logo, CTA, brands, FAQ, breadcrumb) with generic TR/EN translations
and a simple Admin UI. No media management, no search/sort/pagination, and no
RBAC in this sprint.

**Decisions:**

- Component types: `NAVBAR`, `LOGO`, `CTA`, `BRANDS`, `FAQ`, `BREADCRUMB`.
- Status: `ACTIVE` / `INACTIVE` plus `visible` flag.
- `sortOrder`: optional, default `0`.
- Translations: TR/EN for now, modeled generically to enable future languages.
- Unique constraint: `(tenantId, type, key)` must be unique.
- Admin UI always shows both language tabs (TR/EN). Site front-end reads
  language-specific data at runtime.
- Routes: `/:tenant/component` (list), `/:tenant/component/:type` (create/edit).
- Out of scope: media management, list search/sort/pagination, RBAC.

**Scope:**

- **Domain Layer:**
  - Enums: `ComponentType { NAVBAR, LOGO, CTA, BRANDS, FAQ, BREADCRUMB }`,
    `ComponentStatus { ACTIVE, INACTIVE }`.
  - Entities:
    - `Component`: id, tenantId, type, key, status, visible, sortOrder(0),
      createdAt, updatedAt. Unique (tenantId, type, key).
    - `ComponentTranslation`: id, componentId, language, title?, subtitle?,
      `data` JSON (free-form per type for this sprint).

- **Application Layer:**
  - Service: `ComponentService` (+ `ServiceImpl`) with CRUD and unique key
    check. `@Transactional` for multi-step operations.

- **Infrastructure Layer:**
  - Repositories: `ComponentRepository`, `ComponentTranslationRepository`
    (JPQL, `@EntityGraph` where necessary), basic CRUD only.

- **Presentation Layer (REST):**
  - Controller: `ComponentController` returning `ResponseEntity<ApiResponse<?>>`.
  - Endpoints (tenant-aware):
    - `GET /api/components` → list all for tenant (no filters)
    - `GET /api/components/{id}`
    - `POST /api/components`
    - `PUT /api/components/{id}`
    - `DELETE /api/components/{id}`
  - Errors via `GlobalExceptionHandler` with i18n messages.

- **Admin Frontend (Angular 19):**
  - Module: `@custom/components`.
  - Routes: `/:tenant/component` (list), `/:tenant/component/:type` (form).
  - List page: Simple table (no search/sort/pagination), actions to edit/delete.
  - Form page: `fullwidth-2` layout with tabs: General | Türkçe | English.
    - General: type, key, status, visible, sortOrder.
    - Türkçe/English: title, subtitle, free-form `data` (this sprint untyped).
  - Toasts: use existing `NotificationService` for CRUD success/error.

**i18n:**

- Backend: add CRUD success/error keys to `messages_tr.properties` and
  `messages_en.properties`.
- Frontend: Transloco keys for labels and toasts.

**Acceptance Criteria:**

- Create, read, update, delete UI Components per tenant.
- Unique `(tenantId, type, key)` enforced; returns localized errors on conflict.
- Admin list renders items; form saves both TR/EN payloads in one request.
- Toast notifications shown on success/error; controller returns `ApiResponse`.
- `sortOrder` defaults to `0` when not provided.

**Timeline:** 1.5 weeks

- Days 1-2: Enums, entities, repositories
- Days 3-4: Service, controller, i18n messages
- Days 5-7: Angular module, list, form (tabs) with toasts
- Days 8-9: Minimal tests and refinements

#### **Sprint 5: Page Categories + Builder Integration (Unlimited Depth, 1.5 weeks)**

**Goal:** Support unlimited-depth Page Category hierarchy (Materialized
Path) and integrate category selection into the Page Builder form.
Navbar/menu is out of scope.

**Decisions:**

- Child category optional
- Page must have a category (required)
- Page stores a single `categoryId`; the selected node is the category
- On reads, if translation is missing, fallback to tenant default

**Scope:**

- **Domain Layer:**
  - Entities: `PageCategory` (self-ref `parentId`, `path` VARCHAR,
    `level` INT, `sortOrder`, `status`), `PageCategoryTranslation`
    (language, name, slug, description)
  - Relation: `Page` → `PageCategory` (ManyToOne LAZY)
  - Constraints: unique `(tenantId,parentId,language,slug)`, prevent cycles

- **Application Layer:**
  - Service: `CategoryService`
  - Use cases: CRUD, reorder, getTree(lang, rootId?, depth?),
    listChildren(parentId, lang), move(id, newParentId), slug uniqueness,
    compute path/level, i18n fallback in reads

- **Infrastructure Layer:**
  - Repositories: `PageCategoryRepository`,
    `PageCategoryTranslationRepository` (JPQL + `@EntityGraph`)
  - Migrations: add columns `page_categories.path`, `page_categories.level`;
    create `page_category_translations`; add `pages.category_id` FK
  - Indices: tenant, parent, language, slug, sortOrder, path (prefix)

- **Presentation Layer (REST):**
  - `PageCategoryController`:
    - `GET /api/page-categories/tree?lang=tr&rootId=&depth=`
    - `GET /api/page-categories/children?parentId=&lang=tr`
    - `POST /api/page-categories`
    - `PUT /api/page-categories/{id}`
    - `PUT /api/page-categories/{id}/move` (re-parent + path update)
    - `DELETE /api/page-categories/{id}`
    - `PUT /api/page-categories/reorder`
  - `PageController`: `categoryId` required on create/update
  - Responses: `ApiResponse`; errors via `GlobalExceptionHandler`

- **Admin Frontend (Angular 19):**
  - Components: `<spa-page-categories>` (lazy tree CRUD, reorder, move),
    `<spa-page-category-select>` (cascading pickers or tree selector)
  - Integration: inside `<spa-page-builder-form>`; send `categoryId`
  - Conventions: `take(1)`, `forNext`, `#` for private methods

- **Migration Plan:**
  1) Add `path` and `level` columns
  2) Backfill existing categories: root `path='/' + slug`, `level=1`
  3) DFS compute `path/level` for all descendants
  4) Refactor service flows (create/update/move) to recalc path/level

**Acceptance Criteria:**

- Unlimited-depth category tree CRUD, reorder, and move work
- `tree`/`children` endpoints return requested language or fallback
- Page create/update requires `categoryId`
- All endpoints return `ApiResponse`; errors localized

**Out of Scope:** Navbar/menu generation and rendering

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

#### **Sprint 7: Media Management + File Localization (1.5 weeks)**

**Goal:** File upload and media management with language support

**Domain Layer:**

```java
@Entity
@Table(name = "media_files")
public class MediaFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String originalName;
    private String fileName;
    private String filePath;
    private String mimeType;
    private Long fileSize;
    
    // Image properties
    private Integer width;
    private Integer height;
    
    // Multi-language alt text
    private String altTextTr;
    private String altTextEn;
    
    private Long tenantId;
    private Long uploadedBy;
    
    public String getAltText(Language language) {
        return switch (language) {
            case TR -> altTextTr;
            case EN -> altTextEn;
            default -> altTextTr; // fallback
        };
    }
}
```

**Application Layer:**

- [ ] MediaService with language-aware operations
- [ ] UploadMediaUseCase with localized metadata
- [ ] Image processing with multi-language alt text
- [ ] Media organization and search

**Infrastructure Layer:**

- [ ] MediaRepository implementation
- [ ] Local file storage service
- [ ] Image resizing service

**Presentation Layer:**

- [ ] MediaController with localized responses
- [ ] File upload UI with language-specific metadata
- [ ] Media library with language filtering
- [ ] Drag & drop interface

**i18n Features:**

- [ ] Multi-language alt text for images
- [ ] Localized file upload messages
- [ ] Language-specific media organization
- [ ] Translated file categories

**Output:** Complete media management with language support

#### **Sprint 8: Site Publishing + Multi-Language Sites (1.5 weeks)**

**Goal:** Site rendering and publishing with language switching

**Domain Layer:**

```java
@Entity
@Table(name = "sites")
public class Site {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String siteName;
    private String description;
    private Set<Language> enabledLanguages;
    
    @Enumerated(EnumType.STRING)
    private Language defaultLanguage;
    
    private Long tenantId;
    
    public boolean isLanguageEnabled(Language language) {
        return enabledLanguages.contains(language);
    }
}

@Entity
@Table(name = "menus")
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    @Enumerated(EnumType.STRING)
    private Language language;
    
    private Long tenantId;
}
```

**Application Layer:**

- [ ] SiteService, MenuService
- [ ] PublishSiteUseCase with language-specific rendering
- [ ] Multi-language navigation management
- [ ] Site configuration with language settings

**Infrastructure Layer:**

- [ ] SiteRepository, MenuRepository
- [ ] Thymeleaf rendering with i18n
- [ ] Language-aware URL routing

**Presentation Layer:**

- [ ] SiteController with language-specific endpoints
- [ ] Site settings UI with language configuration
- [ ] Menu management with multi-language support
- [ ] Site preview with language switching

**Site Features:**

- [ ] Language-specific home pages
- [ ] Automatic language detection
- [ ] Language switcher widget
- [ ] Fallback language handling

**Output:** Multi-language site publishing with Thymeleaf

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

### 🔄 **PHASE 2: ADVANCED FEATURES (6 weeks)**

#### **Multi-Framework Support + i18n (2 weeks)**

**Goal:** React, Vue, Angular SSR with multi-language support

**Framework Integration:**

- [ ] React SSR with React i18next
- [ ] Vue SSR with Vue I18n
- [ ] Angular SSR with Angular i18n
- [ ] Framework-specific language routing

**Features:**

- [ ] Language-aware component rendering
- [ ] Client-side language switching
- [ ] SEO-friendly language URLs (/tr/, /en/)
- [ ] Hydration with correct language

#### **Advanced Content Features + Translation Workflow (2 weeks)**

**Goal:** Content versioning, scheduling, and translation management

**Features:**

- [ ] Content versioning with language variants
- [ ] Content scheduling per language
- [ ] Translation workflow (draft → review → publish)
- [ ] Translation status tracking
- [ ] Auto-translation integration (Google Translate API)

#### **SEO + Analytics + Localization (2 weeks)**

**Goal:** Advanced SEO and analytics with language support

**Features:**

- [ ] Language-specific sitemaps (/sitemap-tr.xml, /sitemap-en.xml)
- [ ] Hreflang tags for SEO
- [ ] Language-specific analytics tracking
- [ ] Localized meta tags and Open Graph
- [ ] Language-aware search optimization

### 🤖 **PHASE 3: AI INTEGRATION + ADVANCED i18n (8 weeks)**

#### **Sprint 11: AI Content Generation (4 weeks)**

**Goal:** AI-powered content creation with multi-language support

**Features:**

- [ ] GPT/Claude integration for content generation
- [ ] Multi-language content generation
- [ ] Auto-translation suggestions
- [ ] Language-specific SEO optimization
- [ ] AI-powered localization

#### **Sprint 12-13: Advanced Language Features (4 weeks)**

**Goal:** Enterprise-level language features

**Features:**

- [ ] RTL language support (Arabic, Hebrew)
- [ ] Language-specific themes
- [ ] Professional translation workflow
- [ ] Translation quality scoring
- [ ] Advanced localization analytics

## 🌐 Multi-Language Implementation Details

### **Backend i18n Configuration:**

```java
@Configuration
public class InternationalizationConfig {
    
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("i18n/messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
    
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setDefaultLocale(Locale.forLanguageTag("tr"));
        localeResolver.setSupportedLocales(Arrays.asList(
            Locale.forLanguageTag("tr"),
            Locale.forLanguageTag("en")
        ));
        return localeResolver;
    }
}
```

### **Database Schema for Multi-Language:**

```sql
-- Language support in main tables
ALTER TABLE tenants ADD COLUMN default_language ENUM('TR', 'EN') DEFAULT 'TR';
ALTER TABLE tenants ADD COLUMN supported_languages JSON DEFAULT '["TR"]';

ALTER TABLE users ADD COLUMN preferred_language ENUM('TR', 'EN') DEFAULT 'TR';

ALTER TABLE contents ADD COLUMN language ENUM('TR', 'EN') DEFAULT 'TR';
ALTER TABLE contents ADD COLUMN parent_content_id BIGINT NULL; -- For translations

-- Multi-language media alt text
ALTER TABLE media_files ADD COLUMN alt_text_tr TEXT NULL;
ALTER TABLE media_files ADD COLUMN alt_text_en TEXT NULL;

-- Language-specific menus
ALTER TABLE menus ADD COLUMN language ENUM('TR', 'EN') DEFAULT 'TR';

-- Site language configuration
CREATE TABLE site_languages (
    site_id BIGINT NOT NULL,
    language ENUM('TR', 'EN') NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (site_id, language)
);
```

### **Frontend Angular i18n:**

```typescript
// app.module.ts
import { registerLocaleData } from '@angular/common';
import { LOCALE_ID } from '@angular/core';
import localeEn from '@angular/common/locales/en';
import localeTr from '@angular/common/locales/tr';

registerLocaleData(localeEn);
registerLocaleData(localeTr);

// Language service
@Injectable()
export class LanguageService {
  currentLanguage$ = new BehaviorSubject<string>('tr');
  
  setLanguage(language: string) {
    this.currentLanguage$.next(language);
    localStorage.setItem('language', language);
  }
  
  getLanguage(): string {
    return localStorage.getItem('language') || 'tr';
  }
}
```

## 📊 Success Metrics

### **MVP Success Criteria:**

- [ ] 5-10 pilot customers with multi-language sites
- [ ] Both Turkish and English admin interfaces working
- [ ] Content creation in both languages
- [ ] SEO-friendly language URLs
- [ ] Language switching without data loss

### **Technical KPIs:**

- [ ] Language switching response time < 200ms
- [ ] Multi-language content search < 300ms
- [ ] Translation workflow completion rate > 95%
- [ ] Language-specific SEO scores > 90/100

### **Business KPIs:**

- [ ] International customer acquisition > 20%
- [ ] Multi-language site engagement +40%
- [ ] Translation workflow efficiency +60%

## 🚧 Implementation Risks & Mitigation

### **Technical Risks:**

- **Complex language routing:** Use proven i18n libraries
- **Database performance:** Optimize language-specific queries
- **Translation consistency:** Implement validation rules

### **Business Risks:**

- **Translation costs:** Start with machine translation + human review
- **Cultural adaptation:** Partner with local content experts
- **Market complexity:** Focus on TR/EN first, expand later

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
