# Site Dashboard

> **Status**: ✅ Production-Ready (Core + Phase 1 & 2 Refactoring Complete)
> **Last Updated**: 2026-01-27

## Purpose

Site Dashboard is a unified admin interface that consolidates **Site Management** and **Site Settings** into a single, tabbed dashboard. It provides:

- **Overview**: Site status, statistics, recent activity, and quick actions (with confirmation dialogs)
- **General**: Site name, tagline, contact info (per language, dynamic language support)
- **Address**: Business address and map embed
- **Social**: Social media links
- **SEO**: Meta tags, Open Graph, and search engine settings (per language, dynamic language support)
- **Technical**: robots.txt, sitemap, search engine indexing, cookie consent
- **Security**: Two-factor authentication policy configuration

## Architecture

The Site Dashboard merges functionality from:

- `sites` module (Site entity and basic CRUD)
- `site-settings` capability (SiteSetting key-value configuration, not a provisioning module)
- New `site_technical_settings` table for technical configurations
- New `site_activity` table for activity tracking

## Database

### New Tables

Migrations in `backend/src/main/resources/db/tenant/core/`:

- `V20__create_site_activity.sql` - Activity tracking for dashboard overview
- `V21__create_site_technical_settings.sql` - Technical settings (robots.txt, verification, cookie consent)
- `V36__drop_site_technical_scripts_columns.sql` - Removed custom script columns

### Entities

| Entity                  | Purpose                                                                  |
| ----------------------- | ------------------------------------------------------------------------ |
| `SiteActivity`          | Tracks user actions on pages, components, media, products, site settings |
| `SiteTechnicalSettings` | Stores robots.txt, sitemap config, search engine indexing, cookie consent |

## Admin API (tenant-scoped, authenticated)

### Site Controller Extensions

Controller: [`backend/src/main/java/com/backend/presentation/controller/SiteController.java`](../../backend/src/main/java/com/backend/presentation/controller/SiteController.java)

New endpoints under `/api/sites`:

| Method  | Path               | Description                                           |
| ------- | ------------------ | ----------------------------------------------------- |
| `GET`   | `/sites/overview`  | Dashboard overview (status, stats, activity, actions) |
| `GET`   | `/sites/technical` | Technical settings                                    |
| `PATCH` | `/sites/technical` | Update technical settings                             |
| `GET`   | `/sites/security`  | Security settings (2FA policy)                        |
| `PATCH` | `/sites/security`  | Update security settings                              |

> **Not:** Public robots.txt endpoint'i `SiteController`'da değil, `CmsDeliveryController`'dadır: `GET /api/cms/robots.txt` (no auth, no app-level rate limit). Bkz. [cms-delivery.md](./cms-delivery.md#robotstxt).

### Response DTOs

#### SiteOverviewResponse

```json
{
  "id": 1,
  "status": {
    "state": "published|draft|maintenance",
    "publishedAt": "2024-01-18T09:00:00",
    "lastUpdatedAt": "2024-01-20T14:45:00",
    "lastUpdatedBy": { "id": 5, "email": "admin@acme.com", "fullName": "Ahmet Yılmaz" }
  },
  "stats": {
    "pages": { "total": 24, "published": 20, "draft": 4, "weeklyChange": 3 },
    "components": { "total": 12, "published": 8, "draft": 4, "weeklyChange": 1 },
    "media": { "total": 156, "totalSizeMb": 245.8, "dailyChange": 12 },
    "products": { "total": 89, "published": 82, "draft": 7, "weeklyChange": 5 }
  },
  "recentActivity": [
    {
      "id": 1,
      "action": "CREATED",
      "entityType": "PAGE",
      "entityId": 42,
      "entityName": "About Us",
      "description": "Sayfa \"About Us\" oluşturuldu",
      "user": { "id": 5, "email": "admin@acme.com", "fullName": "Ahmet Yılmaz" },
      "createdAt": "2024-01-20T14:45:00"
    }
  ],
  "actions": {
    "canPublish": true,
    "canPreview": true,
    "canEnableMaintenance": true,
    "canDisableMaintenance": false,
    "previewUrl": "https://acme.admincraft.com?preview=true"
  }
}
```

#### SiteTechnicalResponse

```json
{
  "domain": {
    "subdomain": "acme",
    "platformDomain": "admincraft.com",
    "fullUrl": "https://acme.admincraft.com",
    "customDomain": "www.acme.com",
    "sslEnabled": true
  },
  "searchEngine": {
    "robotsTxt": "User-agent: *\nAllow: /",
    "sitemapEnabled": true,
    "indexingEnabled": true
  },
  "cookieConsent": {
    "enabled": false,
    "text": null
  }
}
```

#### SecuritySettingsResponse

```json
{
  "twoFactor": {
    "policy": "DISABLED",
    "policyDescription": "2FA is disabled for all users"
  }
}
```

**Two-Factor Policy Options**:

| Policy | Description |
|--------|-------------|
| `DISABLED` | 2FA not used, standard login for all users |
| `REQUIRED` | 2FA mandatory for all tenant users |

See [authentication.md](../global/authentication.md) for full 2FA documentation.

## Services

### Application Layer (Backend)

| Service                   | Purpose                                                                                                         |
| ------------------------- | --------------------------------------------------------------------------------------------------------------- |
| `SiteOverviewService`     | Aggregates stats using optimized count queries (avoiding N+1) from Page, Component, Media, Product repositories |
| `SiteTechnicalService`    | Manages technical settings CRUD                                                                                 |
| `SecuritySettingsService` | Manages tenant 2FA policy configuration                                                                         |
| `SiteActivityPublisher`   | Async event publisher for activity tracking                                                                     |

### Activity Tracking

The `SiteActivityPublisher` records activities asynchronously:

```java
@Async
public void publishPageEvent(Long pageId, String pageName, ActivityAction action,
                              Long userId, String userEmail, String userFullName)
```

Supported entity types: `PAGE`, `COMPONENT`, `MEDIA`, `PRODUCT`, `SITE`, `SITE_SETTINGS`, `NAVIGATION`

Retention policy: 30 days (cleanup via scheduled job)

## Frontend Integration (Admin)

### Module Location

`storefront/src/app/modules/admin/custom/site/`

Maintenance mode notifications use the backend `ApiResponse.message` field for success/error messaging.

### Structure

```
site/
├── site.routes.ts
├── site.service.ts                    (Signals-based state management)
├── site.types.ts
├── site-dashboard.component.ts        (Main container with horizontal tabs)
├── site-dashboard.component.html
├── site-dashboard.component.scss
│
└── tabs/
    ├── overview/
    │   ├── site-overview.component.ts   (with ConfirmationService integration)
    │   └── site-overview.component.html
    ├── general/
    │   ├── site-general.component.ts    (custom UI components + NotificationService)
    │   └── site-general.component.html
    ├── address/
    │   ├── site-address.component.ts    (custom UI components + NotificationService)
    │   └── site-address.component.html
    ├── social/
    │   ├── site-social.component.ts     (custom UI components + NotificationService)
    │   └── site-social.component.html
    ├── seo/
    │   ├── site-seo.component.ts        (custom UI components + NotificationService)
    │   └── site-seo.component.html
    ├── technical/
    │   ├── site-technical.component.ts  (custom UI components + NotificationService)
    │   └── site-technical.component.html
    └── security/
        ├── site-security.component.ts   (2FA policy configuration)
        └── site-security.component.html

Shared Utilities (Phase 1):
├── storefront/src/app/shared/utils/
│   └── url-validator.ts                 (XSS protection for preview URLs)
```

### Routes

Route: `/admin/:lang/site`

```typescript
{
  path: 'site',
  canActivate: [tenantAdminGuard, moduleGuard],
  data: { requiredModule: 'core' },
  loadChildren: () => import('app/modules/admin/custom/site/site.routes')
}
```

### API Endpoints

Defined in `storefront/src/app/modules/admin/api-endpoints.ts`:

```typescript
siteOverview: 'sites/overview',
siteTechnical: 'sites/technical',
siteSecuritySettings: 'sites/security',
```

## Security & Tenant Isolation

- All endpoints require `TENANT_ADMIN` role via `@PreAuthorize("hasRole('TENANT_ADMIN')")`
- Tenant context resolved via `TenantFilter` before reaching controllers

### Security & Validation

#### Backend Validation

- **Validation Limits** (enforced in `SiteTechnicalPatchRequest` via `ValidationConstants`):
  - `robotsTxt`: Max 10,000 chars (also listed above)
  - `robotsTxt`: Max 10,000 chars
  - `cookieConsentText`: Max 2,000 chars

#### Frontend Security (Phase 1)

- **URL Validation**: `UrlValidator` utility prevents XSS/URL injection in preview URLs
  - Only `http:` and `https:` protocols allowed
  - Invalid URLs rejected before opening
- **Window Security**: All external links opened with `noopener,noreferrer` flags
- **Memory Safety**: All observable subscriptions properly managed with `take(1)` operator
- **Form Validation**: Automatic validation via custom UI components with `VALIDATION_MESSAGES`

## Migration Notes

### From Legacy Sites + Settings Modules

The Site Dashboard consolidates:

- `sites` module list view → merged into Site Dashboard
- `settings` module forms → merged into Site Dashboard tabs

Legacy routes (`/sites`, `/settings`) remain functional for backward compatibility but may be deprecated in future releases.

## Implementation Checklist

### Backend

- [x] `SiteActivity` entity and migration (V20)
- [x] `SiteTechnicalSettings` entity and migration (V21)
- [x] `SiteActivityRepository` and JPA implementation
- [x] `SiteTechnicalSettingsRepository` and JPA implementation
- [x] `SiteOverviewService` for dashboard stats
- [x] `SiteTechnicalService` for technical settings CRUD
- [x] `SiteActivityPublisher` for async event tracking
- [x] Controller endpoints (`/overview`, `/technical`, `/robots.txt`)
- [x] DTOs (`SiteOverviewResponse`, `SiteTechnicalResponse`, `SiteTechnicalPatchRequest`)

### Frontend

- [x] Site module structure (`site/`)
- [x] Site service with API calls
- [x] Site types and interfaces
- [x] Site Dashboard component with tabs
- [x] Overview tab component
- [x] General tab component
- [x] Address tab component
- [x] Social tab component
- [x] SEO tab component
- [x] Technical tab component
- [x] Security tab component (2FA policy)
- [x] Routes registered in app.routes.ts
- [x] API endpoints registered

### Documentation & Config

- [x] i18n translations (langTR.ts, langEN.ts) - **Complete with Phase 2 enhancements**
- [x] Navigation menu update - **Complete**
- [x] API endpoints registered - **Complete**
- [x] Routes configured - **Complete**

### Code Quality Improvements (Phase 1 - Complete)

- [x] Memory leak fixes (3 service methods with `take(1)`)
- [x] XSS vulnerability fix (URL validator utility created)
- [x] BehaviorSubject to Signals migration (modern Angular patterns)
- [x] Removed redundant `markForCheck()` calls (20 instances across 6 components)
- [x] Fixed loading state race condition (forkJoin implementation)

### User Experience Improvements (Phase 2 - Complete)

- [x] Custom UI components migration (44 form fields)
  - spa-input, spa-textarea, spa-toggle, spa-select
  - 35-40% template code reduction
  - Automatic validation error handling
- [x] NotificationService integration (5 tab components)
  - Success/error toasts for all save operations
- [x] ConfirmationService integration (overview component)
  - Publish confirmation dialog
  - Enable maintenance mode confirmation
  - Disable maintenance mode confirmation
- [x] LanguageContextService integration (general, seo components)
  - Dynamic multi-language tenant support
  - Computed signals for language lists

### Pending (Optional Enhancements)

- [ ] Integration with activity tracking in existing services (framework ready)
- [ ] Unit tests for new services
- [ ] E2E tests for Site Dashboard
- [ ] Phase 3 optional enhancements (computed signals for getters, card styling updates)

### Phase 2: User Experience

| Feature              | Implementation                     | Benefit                     |
| -------------------- | ---------------------------------- | --------------------------- |
| Save Button Placement | Sağ üstte (General, Address, Social, SEO, Technical, Security) | Tutarlı UX, hızlı erişim     |
| Custom UI Components | 44 form fields migrated            | ✅ -35% template code       |
| Auto Validation      | `VALIDATION_MESSAGES` integration  | ✅ No manual error handling |
| User Feedback        | NotificationService (5 components) | ✅ Success/error toasts     |
| Safety Dialogs       | ConfirmationService (3 actions)    | ✅ Prevent accidents        |
| Multi-Language       | LanguageContextService integration | ✅ Dynamic tenant languages |

### Files Modified

- `site.service.ts` - Signals migration + memory leak fixes
- `site-dashboard.component.ts` - forkJoin + removed markForCheck
- `site-overview.component.ts` - XSS fix
- 5 tab components - Removed markForCheck calls
- `url-validator.ts` - **NEW** security utility
