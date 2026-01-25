# Site Dashboard

## Purpose

Site Dashboard is a unified admin interface that consolidates **Site Management** and **Site Settings** into a single, tabbed dashboard. It provides:

- **Overview**: Site status, statistics, recent activity, and quick actions
- **General**: Site name, tagline, contact info (per language)
- **Address**: Business address and map embed
- **Social**: Social media links
- **SEO**: Meta tags, Open Graph, and search engine settings (per language)
- **Technical**: robots.txt, verification codes, custom scripts, cookie consent

## Architecture

The Site Dashboard merges functionality from:

- `sites` module (Site entity and basic CRUD)
- `site-settings` module (SiteSetting key-value configuration)
- New `site_technical_settings` table for technical configurations
- New `site_activity` table for activity tracking

## Database

### New Tables

Migrations in `backend/src/main/resources/db/tenant/core/`:

- `V20__create_site_activity.sql` - Activity tracking for dashboard overview
- `V21__create_site_technical_settings.sql` - Technical settings (robots.txt, scripts, verification)

### Entities

| Entity                  | Purpose                                                                  |
| ----------------------- | ------------------------------------------------------------------------ |
| `SiteActivity`          | Tracks user actions on pages, components, media, products, site settings |
| `SiteTechnicalSettings` | Stores robots.txt, verification codes, custom scripts, cookie consent    |

## Admin API (tenant-scoped, authenticated)

### Site Controller Extensions

Controller: [`backend/src/main/java/com/backend/presentation/controller/SiteController.java`](../../backend/src/main/java/com/backend/presentation/controller/SiteController.java)

New endpoints under `/api/sites`:

| Method  | Path                | Description                                           |
| ------- | ------------------- | ----------------------------------------------------- |
| `GET`   | `/sites/overview`   | Dashboard overview (status, stats, activity, actions) |
| `GET`   | `/sites/technical`  | Technical settings                                    |
| `PATCH` | `/sites/technical`  | Update technical settings                             |
| `GET`   | `/sites/robots.txt` | Public robots.txt endpoint                            |

### Response DTOs

#### SiteOverviewResponse

```json
{
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
    "indexingEnabled": true,
    "verification": { "google": "xxx", "bing": null, "yandex": null }
  },
  "scripts": {
    "headScripts": null,
    "bodyStartScripts": null,
    "bodyEndScripts": "<script src='chat.js'></script>"
  },
  "cookieConsent": {
    "enabled": false,
    "text": null
  }
}
```

## Services

### Application Layer

| Service                 | Purpose                                                                                                         |
| ----------------------- | --------------------------------------------------------------------------------------------------------------- |
| `SiteOverviewService`   | Aggregates stats using optimized count queries (avoiding N+1) from Page, Component, Media, Product repositories |
| `SiteTechnicalService`  | Manages technical settings CRUD                                                                                 |
| `SiteActivityPublisher` | Async event publisher for activity tracking                                                                     |

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

### Structure

```
site/
├── site.routes.ts
├── site.service.ts
├── site.types.ts
├── site-dashboard.component.ts       (Main container with horizontal tabs)
├── site-dashboard.component.html
├── site-dashboard.component.scss
│
└── tabs/
    ├── overview/
    │   ├── site-overview.component.ts
    │   └── site-overview.component.html
    ├── general/
    │   ├── site-general.component.ts
    │   └── site-general.component.html
    ├── address/
    │   ├── site-address.component.ts
    │   └── site-address.component.html
    ├── social/
    │   ├── site-social.component.ts
    │   └── site-social.component.html
    ├── seo/
    │   ├── site-seo.component.ts
    │   └── site-seo.component.html
    └── technical/
        ├── site-technical.component.ts
        └── site-technical.component.html
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
siteRobotsTxt: 'sites/robots.txt',
```

## Security & Tenant Isolation

- All endpoints require `TENANT_ADMIN` role via `@PreAuthorize("hasRole('TENANT_ADMIN')")`
- Exception: `/sites/robots.txt` is public (`@PreAuthorize("permitAll()")`)
- Tenant context resolved via `TenantFilter` before reaching controllers

### Security & Validation

- **XSS Protection**: Script fields (head/body) are validated for size but allow script tags for analytics/tools.
- **Validation Limits** (enforced in `SiteTechnicalPatchRequest` via `ValidationConstants`):
  - `verificationCode`: Max 100 chars
  - `robotsTxt`: Max 10,000 chars
  - `headScripts` / `bodyScripts`: Max 50,000 chars
  - `cookieConsentText`: Max 2,000 chars

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
- [x] Routes registered in app.routes.ts
- [x] API endpoints registered

### Pending

- [ ] i18n translations (langTR.ts, langEN.ts)
- [ ] Navigation menu update
- [ ] Integration with activity tracking in existing services
- [ ] Unit tests for new services
- [ ] E2E tests for Site Dashboard
