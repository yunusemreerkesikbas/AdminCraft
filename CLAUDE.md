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

### 🚀 Multi‑Language Page Builder (i18n) — Backend & Frontend Foundations

Goal: Deliver the base multi‑language Page Builder foundation with clean
contracts, sustainable DB design, and Admin UI hooks. Establish uid/uuid
conventions and base/extend approach for both backend and frontend.

Scope (this sprint):

- Tenant languages management (GET/PUT) + provisioning trigger (POST)
- Page i18n model: base tables and endpoints for language‑specific fields
- Admin UI: dynamic language tabs from supported languages; “Start
  Provisioning” dialog
- Root redirect based on Accept‑Language on first visit, cookie afterwards
- No sitemap in this sprint (deferred)

Key conventions:

- uuid: server‑generated `UUID.randomUUID()` stored per row
- uid: human‑readable stable identifier; if not provided on create,
  generate `cmsitem_XXXXXXXX` (8 digits)
- Base/extend approach:
  - Backend: base entities/DTOs/services expose common fields (id, uuid,
    uid, tenantId, audit), feature modules extend/compose
  - Frontend: base models and service facades; feature facades extend with
    domain specifics; shared i18n/language context service

URL and language routing:

- Storefront uses subpath strategy: `/:lang/...`
- Root `/` → 302 to cookie lang if present; else first supported in
  Accept‑Language; else tenant default language
- Unsupported lang → 302 to `/{defaultLanguage}/`
- Cookie: `lang`, TTL 1 year

Tenant languages API:

- `GET /api/tenants/{tenantId}/languages`
- `PUT /api/tenants/{tenantId}/languages` with validation `default ∈ supported`
- `POST /api/tenants/{tenantId}/languages/provision` to queue i18n scaffolds
- `GET /api/provisioning/jobs/{jobId}` for progress

Provisioning (DB‑first, async):

- Purpose: when languages are added, create empty i18n “containers” only
  (no auto content copy)
- A queue table tracks jobs; a DB procedure/event processor iterates
  relevant entities and inserts missing i18n rows with `status=DRAFT`
- Affected areas: `page_i18n`, `menu_i18n`, `site_settings_i18n`

Page Builder data model (simplified):

- `pages` (language‑agnostic)
  - id (PK), uuid (UUID), uid (string), tenant_id, category_id?, status,
    featured_image?, style_classes?, is_home, sort_order, created_at,
    updated_at, created_by, updated_by
  - Constraints: unique (tenant_id, uid)
- `page_i18n` (per language)
  - id (PK), uuid (UUID), uid (string), page_id (FK), tenant_id,
    language (ISO), url_path?, title?, subtitle?, meta_title?,
    meta_description?, description?, description_html?, status, published_at?,
    scheduled_at?, updated_at
  - Constraints: unique (tenant_id, language, url_path) when url_path not null;
    unique (tenant_id, page_id, language); unique (tenant_id, uid)

uid/uuid rules:

- Backend sets `uuid=UUID.randomUUID()` on insert
- If `uid` is empty on create, backend generates `cmsitem_XXXXXXXX`
- `uid` is immutable by default (can be relaxed later via dedicated flow)

Page Builder Admin API (i18n focus):

- `GET /api/pages?tenantId=` → list language‑agnostic pages (id, uuid, uid,
  tenantId, status, etc.)
- `GET /api/pages/{pageId}/i18n/{lang}?tenantId=` → fetch i18n container
- `PUT /api/pages/{pageId}/i18n/{lang}` → upsert i18n fields (requires
  validation on lengths and url safety)
- `POST /api/pages/{pageId}/publish/{lang}` → publish/schedule per language

Public content API (read):

- `GET /public/:tenantHost/:lang/pages/:urlPath` → returns published i18n,
  with `isFallbackLanguage` if default language is served

Admin UI behavior:

- `supported_languages` multi‑select; `default_language` single‑select from
  that set; validations enforced
- After save, prompt a “Start Provisioning” dialog listing newly added langs
- Builder screens render dynamic language tabs from `supported_languages`

Out of scope (this sprint):

- Sitemap generation, hreflang, canonical policies
- AI pre‑translation / transliteration
- URL change history and redirects

Acceptance criteria (summary):

- Languages can be managed; provisioning queues and creates empty i18n rows
- Page i18n endpoints CRUD work with `uid/uuid` present in responses
- Publishing per language requires `urlPath` unique per (tenant, lang)
- Root redirect and unsupported lang rules behave as specified
- Admin UI shows language tabs and a provisioning prompt after language save

Risks and mitigations:

- Provisioning on large datasets: async job with progress; idempotent upsert
- Cross‑tenant leakage: tenant context required in all queries and caches
- Host/tenant resolution: prioritize custom domain > subdomain, strict 404

### 🔄 Generic Dynamic i18n Framework & Rollout Plan

Objective: Build a reusable, generic i18n framework once and apply it across
all modules. Page Builder is the pilot. Subsequent modules adopt the same
patterns with minimal duplication.

Principles:

- i18n side tables per aggregate: `<entity>_i18n` with consistent columns
  `(id, uuid, uid, <entity>_id, tenant_id, language, ...localized fields...)`
- Provisioning creates only empty i18n rows for new languages (no data copy)
- Uniform REST contracts: `GET/PUT /{entity}/{id}/i18n/{lang}`
- Validation and DTOs follow the same rules (lengths, URL safety, enums)
- Angular Admin: dynamic language tabs sourced from tenant config

Base components to reuse:

- Backend
  - BaseEntity: `id, uuid, uid, tenantId, createdAt, updatedAt`
  - BaseI18nEntity: `id, uuid, uid, tenantId, language, updatedAt`
  - BaseI18nService<T, Ti18n>
  - BaseI18nRepository<Ti18n>
  - LanguageContext + TenantContext utilities
- Frontend
  - LanguageContextService (current lang, supported, default)
  - I18nTabsComponent schema‑driven tabs
  - BaseCrudService<T> and BaseI18nService<TI18n>

Rollout phases:

1) Page Builder (this sprint)
2) Site Settings (`site_settings_i18n`)
3) Categories (`page_category_i18n`) and other CMS modules
4) UI Components (generic i18n JSON payloads)
5) Search and caching layers adjusted to include `tenantId+lang`

KPIs:

- Add a new language to a tenant and create all i18n containers in < 2 min
- Publish a page in a new language without schema/code changes
- Zero cross‑tenant language leakage in logs and metrics

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
- Conventions: use  `take(1)`, private methods with `#`,
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
  - Conventions:  `take(1)`, `#` private methods, strong typings
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

#### Sprint 11: Reusable ItemDialog for CRUD (MatDialog + Dynamic i18n Tabs)

Goal: Deliver a reusable, schema-driven dialog to handle Create/Edit with
General and dynamic Language tabs (TR/EN for now), consistent UX, and
clean separation of concerns.

Decisions:

- Languages source: manual ['tr','en'] for this sprint; will be fetched from
  tenant configuration in a future sprint.
- Save flow: Dialog emits DTO only; caller performs API call (create/update).
- Notifications: Use existing NotificationService (Transloco-aware toasts).
- Dialog config (modalData): disableClose (default true), width (default
  '720px'), optional height, optional styleClasses (mapped to panelClass).
- ESC behavior: When disableClose is true, ESC is disabled.
- After save: Show success toast; keep dialog open (user closes manually).
- Submit UX: Disable Save button while submitting (prevent double clicks).
- Validations: No custom URL/email/slug validators in this sprint; a general
  validation service will be added later.
- File inputs: Not in scope; only text/number/select/checkbox/date.
- Components placed under shared with <spa-*> selectors.

Scope (Admin Frontend - Angular 19):

- Module: ItemDialogModule exporting <spa-item-dialog> (MatDialog + MatTabGroup)
- Component: ItemDialogComponent (<spa-item-dialog>)
- Services:
  - ItemDialogService: opens dialog with a single options object; returns
    Observable<DTO | null>
  - ItemFormBuilderService: builds ReactiveForms from schema (general + i18n)
- Types:
  - ItemDialogMode = 'create' | 'edit'
  - ItemDialogOptions<TDto, TId>
  - ModalData (disableClose, width, height, styleClasses)
  - ItemDialogSchema { general[], i18n[] }
  - Field configs: GeneralFieldConfig, LangFieldConfig
- Integration: List pages call ItemDialogService; on result, perform API via
  facade, show toast, and refresh the list; dialog remains open.
- Conventions: take(1), strong typings, private helpers with #.

API Contract (summary):

```ts
export type ItemDialogMode = 'create' | 'edit';

export interface ModalData {
  disableClose?: boolean; // default: true
  width?: string;         // default: '720px'
  height?: string;
  styleClasses?: string[];
}

export interface ItemDialogOptions<TDto, TId = string> {
  titleKey: string;
  mode: ItemDialogMode;
  schema: ItemDialogSchema;
  languages: ReadonlyArray<string>; // e.g., ['tr','en']
  initial?: Partial<TDto>;
  id?: TId;
  modalData?: ModalData;
}

export interface ItemDialogSchema {
  general: ReadonlyArray<GeneralFieldConfig>;
  i18n: ReadonlyArray<LangFieldConfig>;
}

export interface GeneralFieldConfig {
  key: string;
  type: 'text' | 'textarea' | 'select' | 'number' | 'checkbox' | 'date';
  labelKey: string;
  required?: boolean;
  options?: ReadonlyArray<{ value: string | number; labelKey: string }>;
  maxLength?: number;
}

export interface LangFieldConfig extends GeneralFieldConfig {}
```

Deliverables:

- ItemDialogModule + <spa-item-dialog> component
- ItemDialogService, ItemFormBuilderService
- DTO emit flow wired to feature facades; NotificationService integration
- i18n keys for dialog base labels added to langTR.ts/langEN.ts:
  - admin.dialog.title.create, admin.dialog.title.edit
  - admin.dialog.tabs.general
  - admin.dialog.tabs.languages.tr, admin.dialog.tabs.languages.en
- Sample integration in one list view (open dialog, call facade, toast,
  refresh list)

Acceptance Criteria:

- General and dynamic language tabs render from schema and `languages` input
- Dialog emits DTO on Save; caller invokes create/update; dialog stays open
- Save button disabled during submit; ESC disabled when disableClose=true
- Success toast shown via NotificationService; no memory leaks (take(1))
- Functions ≤ 4 params (use options objects); strong typings; <spa-*> selectors

Timeline (6–7 days):

- Days 1–2: UX/API finalization; module + component shell; tabs layout
- Day 3: FormBuilder and schema wiring (general + i18n)
- Day 4: Service integration (emit DTO), NotificationService usage
- Day 5: Sample list integration and refinements
- Day 6–7: Unit tests and documentation

Out of Scope:

- Backend changes, custom validators, media inputs, tenant-based languages fetch
