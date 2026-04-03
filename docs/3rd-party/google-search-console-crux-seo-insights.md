# Google Search Console + Chrome UX Report (CrUX)

## Purpose

Google Search Console and Chrome UX Report (CrUX) power the tenant SEO and performance snapshots rendered in Site Dashboard Overview.

Current scope:

- tenant-scoped `SEO snapshot` in Overview
- tenant-scoped `Performance snapshot` in Overview
- Search Console visibility metrics for the last 28 days
- Search Console URL inspection signals for the resolved public site URL
- CrUX desktop p75 metrics, trend, and radial health score
- runtime enablement through `/config`

Out of scope:

- Google Analytics 4 reporting
- Lighthouse or PageSpeed opportunity/audit details
- browser-side direct calls to Search Console or CrUX
- generic SEO strategy or keyword planning

## Source of truth

Backend:

- Controller: [`backend/src/main/java/com/backend/presentation/controller/SiteController.java`](../../backend/src/main/java/com/backend/presentation/controller/SiteController.java)
- Application service: [`backend/src/main/java/com/backend/application/service/SiteInsightsServiceImpl.java`](../../backend/src/main/java/com/backend/application/service/SiteInsightsServiceImpl.java)
- Response DTO: [`backend/src/main/java/com/backend/presentation/dto/response/SiteInsightsSummaryResponse.java`](../../backend/src/main/java/com/backend/presentation/dto/response/SiteInsightsSummaryResponse.java)
- Search Console adapter: [`backend/src/main/java/com/backend/infrastructure/seo/GoogleSearchConsolePortAdapter.java`](../../backend/src/main/java/com/backend/infrastructure/seo/GoogleSearchConsolePortAdapter.java)
- CrUX adapter: [`backend/src/main/java/com/backend/infrastructure/performance/CruxHistoryPortAdapter.java`](../../backend/src/main/java/com/backend/infrastructure/performance/CruxHistoryPortAdapter.java)
- Shared Google token provider: [`backend/src/main/java/com/backend/infrastructure/google/GoogleServiceAccountAccessTokenProvider.java`](../../backend/src/main/java/com/backend/infrastructure/google/GoogleServiceAccountAccessTokenProvider.java)
- Tenant config management: [`backend/src/main/java/com/backend/application/service/impl/config/ConfigPropertiesAdminServiceImpl.java`](../../backend/src/main/java/com/backend/application/service/impl/config/ConfigPropertiesAdminServiceImpl.java)
- Global config management: [`backend/src/main/java/com/backend/application/service/impl/config/ConfigGlobalPropertiesAdminServiceImpl.java`](../../backend/src/main/java/com/backend/application/service/impl/config/ConfigGlobalPropertiesAdminServiceImpl.java)
- Runtime env binding: [`backend/src/main/resources/application.yml`](../../backend/src/main/resources/application.yml)

Frontend:

- Overview rendering: [`storefront/src/app/modules/admin/custom/site/tabs/overview/site-overview.component.ts`](../../storefront/src/app/modules/admin/custom/site/tabs/overview/site-overview.component.ts)
- Overview template: [`storefront/src/app/modules/admin/custom/site/tabs/overview/site-overview.component.html`](../../storefront/src/app/modules/admin/custom/site/tabs/overview/site-overview.component.html)
- Site dashboard service: [`storefront/src/app/modules/admin/custom/site/site.service.ts`](../../storefront/src/app/modules/admin/custom/site/site.service.ts)
- Site dashboard types: [`storefront/src/app/modules/admin/custom/site/site.types.ts`](../../storefront/src/app/modules/admin/custom/site/site.types.ts)

Related docs:

- Site Dashboard: [`../modules/site-dashboard.md`](../modules/site-dashboard.md)
- Config Control Panel: [`../modules/config-control-panel.md`](../modules/config-control-panel.md)
- Environment configuration: [`../global/environment-configuration.md`](../global/environment-configuration.md)
- Google Analytics 4 (GA4): [`google-analytics-ga4.md`](google-analytics-ga4.md)

## Admin API

Authenticated tenant endpoint:

- `GET /api/sites/insights/summary`

Response shape:

```json
{
  "resolvedUrl": "https://www.example.com",
  "resolvedOrigin": "https://www.example.com",
  "lastSyncedAt": "2026-04-03T12:15:00",
  "seo": {
    "status": "READY|NOT_CONFIGURED|DISABLED|ACCESS_ERROR|NO_DATA",
    "propertyUrl": "sc-domain:example.com",
    "range": "LAST_28_DAYS",
    "cards": [
      {
        "metric": "clicks",
        "value": 120,
        "previousValue": 90,
        "deltaPercentage": 33.3,
        "deltaDirection": "up"
      }
    ],
    "trend": [
      { "date": "2026-04-01", "clicks": 8, "impressions": 140 }
    ],
    "inspection": {
      "verdict": "PASS",
      "coverageState": "Submitted and indexed",
      "robotsTxtState": "ALLOWED",
      "indexingState": "INDEXING_ALLOWED",
      "pageFetchState": "SUCCESSFUL",
      "lastCrawlTime": "2026-04-02T08:30:00",
      "googleCanonical": "https://www.example.com",
      "userCanonical": "https://www.example.com",
      "sitemaps": ["https://www.example.com/sitemap.xml"]
    },
    "lastSyncedAt": "2026-04-03T12:10:00"
  },
  "performance": {
    "status": "READY|NOT_CONFIGURED|DISABLED|ACCESS_ERROR|NO_DATA",
    "targetScope": "URL|ORIGIN",
    "target": "https://www.example.com",
    "formFactor": "DESKTOP",
    "score": {
      "value": 92,
      "label": "HEALTHY|ATTENTION|CRITICAL"
    },
    "metrics": [
      {
        "metric": "lcp",
        "value": 1980,
        "displayValue": "1.98s",
        "assessment": "GOOD|NEEDS_IMPROVEMENT|POOR|UNKNOWN"
      }
    ],
    "trend": [
      {
        "startDate": "2026-03-07",
        "endDate": "2026-04-03",
        "lcp": 1980,
        "inp": 175,
        "cls": 0.06,
        "ttfb": 660
      }
    ],
    "lastSyncedAt": "2026-04-03T12:15:00"
  }
}
```

SEO card order:

- `clicks`
- `impressions`
- `ctr`
- `position`

Performance metric order:

- `lcp`
- `inp`
- `cls`
- `ttfb`

Status meanings:

- `READY`: data exists and the Overview snapshot can be rendered
- `NOT_CONFIGURED`: required configuration is missing for that snapshot
- `DISABLED`: the global or tenant SEO insights switch is off
- `ACCESS_ERROR`: the provider call failed or the configured Search Console property is invalid
- `NO_DATA`: access works, but the provider has no reportable data for the current target or period

There is no public delivery API for SEO or CrUX reporting in Craftive. Reporting is admin-only.

## Frontend integration

SEO and performance are rendered inside Site Dashboard Overview, not as standalone admin pages.

Current behavior:

- Overview requests `GET /api/sites/insights/summary`
- SEO snapshot renders KPI cards, a clicks/impressions trend, and inspection rows when `seo.status = READY`
- Performance snapshot renders metric rows, a trend chart, and an ApexCharts radial score when `performance.status = READY`
- non-ready states render dedicated setup, disabled, no-data, or access-error content
- performance prefers URL-level CrUX history and falls back to origin-level history when URL data is unavailable

Overview currently surfaces:

- Search Console clicks, impressions, CTR, and average position
- Search Console inspection verdict, indexing, robots, crawl, canonical, and sitemap signals
- CrUX desktop LCP, INP, CLS, and TTFB
- CrUX health score with `HEALTHY`, `ATTENTION`, or `CRITICAL`
- provider target scope badge and last synced timestamp

## Configuration model

Tenant `/config` keys:

- `seo.insights.enabled`
- `seo.search_console.property_url`

Global `/config` key:

- `platform.seo.insights.enabled`

Effective enablement rule:

- the SEO and performance snapshots are active only when both `platform.seo.insights.enabled` and `seo.insights.enabled` are `true`

Property rules:

- `seo.search_console.property_url` accepts `sc-domain:example.com`
- `seo.search_console.property_url` also accepts full URL properties such as `https://www.example.com/`
- Search Console property selection is tenant-specific
- performance does not require a tenant property, but it still obeys the same master gate

Public URL resolution:

- custom domain is preferred when the tenant has one
- otherwise the backend derives a URL from the configured frontend base URL and tenant subdomain
- performance returns `NOT_CONFIGURED` if no public URL and origin can be resolved

## Security & tenant isolation

API access:

- endpoint lives under `SiteController`
- controller access is authenticated and tenant-scoped
- current controller-level access allows `TENANT_ADMIN` and `VIEWER`

Tenant isolation rules:

- tenant context is resolved before the controller via `TenantFilter`
- Search Console property URL is resolved from tenant config store, not request payload
- the request cannot override the Search Console property or CrUX target
- public URL and origin are derived from the active tenant context

Google access model:

- backend uses one shared Google service account for Search Console API access
- each tenant Search Console property must explicitly grant that service account access
- CrUX is queried from the backend with the configured API key

## Third-party runtime model

Search Console runtime:

- calls Google Search Console API from the backend only
- uses the shared backend service identity through `GoogleServiceAccountAccessTokenProvider`
- service account credential comes from runtime secret, not from `/config`
- supported secret names:
  - `APP_ANALYTICS_GA4_SERVICE_ACCOUNT_JSON`
  - `APP_ANALYTICS_GA4_SERVICE_ACCOUNT_JSON_BASE64`

CrUX runtime:

- calls Chrome UX Report History API from the backend only
- uses backend environment variable `APP_SEO_CRUX_API_KEY`
- caches provider responses in-memory for a short TTL

This creates a two-provider backend-only integration:

- Search Console: backend -> Search Console API via shared Google service account + tenant property URL
- CrUX: backend -> CrUX History API via API key + resolved tenant public URL/origin

Storefront ownership verification:

- Search Console property ownership for `storefront-nextjs` can be managed per deployment with `GOOGLE_SITE_VERIFICATION`
- the storefront renders `<meta name="google-site-verification" ...>` from `app/layout.tsx` when that env var is set
- use the Search Console `HTML tag` method and store only the token value in env

## Implementation guide

### Connect Search Console and CrUX for a tenant

1. Add the shared backend service account email to the tenant Search Console property.
2. In tenant `/config`, set:
   - `seo.insights.enabled = true`
   - `seo.search_console.property_url = <sc-domain or full-url property>`
3. In global `/config`, ensure `platform.seo.insights.enabled = true`.
4. In the backend environment, set `APP_SEO_CRUX_API_KEY`.
5. Confirm the tenant has a resolvable public URL through custom domain or frontend base URL rules.

### Validate the integration

1. Open Site Dashboard Overview for the tenant.
2. Confirm the SEO snapshot loads Search Console KPI cards and inspection state.
3. Confirm the performance snapshot renders desktop CrUX metrics and radial score.
4. If URL-level CrUX data is missing, confirm the snapshot still loads with `targetScope = ORIGIN`.

### Diagnose common states

- `DISABLED`: check `platform.seo.insights.enabled` and `seo.insights.enabled`
- `NOT_CONFIGURED` on SEO: add `seo.search_console.property_url`
- `NOT_CONFIGURED` on performance: verify the tenant public URL can be resolved
- `ACCESS_ERROR`: verify Search Console property format, shared service-account access, and `APP_SEO_CRUX_API_KEY`
- `NO_DATA`: provider access works, but Search Console or CrUX does not yet have enough reportable data for the current target
