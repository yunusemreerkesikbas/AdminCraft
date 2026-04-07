# Google Analytics 4 (GA4)

## Purpose

Google Analytics 4 powers the tenant analytics snapshot rendered in Site Dashboard Overview.

Current scope:

- tenant-scoped KPI cards in Overview
- 7-day active-user trend
- runtime enablement through `/config`
- shared backend reporting access through Google Analytics Data API
- per-tenant storefront event collection through GA4 measurement IDs

Out of scope:

- Search Console integration
- CrUX performance integration
- Google Tag Manager container management
- generic analytics strategy or reporting beyond the current Overview snapshot

## Source of truth

Backend:

- Controller: [`backend/src/main/java/com/backend/presentation/controller/SiteController.java`](../../backend/src/main/java/com/backend/presentation/controller/SiteController.java)
- Application service: [`backend/src/main/java/com/backend/application/service/SiteAnalyticsServiceImpl.java`](../../backend/src/main/java/com/backend/application/service/SiteAnalyticsServiceImpl.java)
- Google Analytics adapter: [`backend/src/main/java/com/backend/infrastructure/analytics/GoogleAnalyticsPortAdapter.java`](../../backend/src/main/java/com/backend/infrastructure/analytics/GoogleAnalyticsPortAdapter.java)
- Google Analytics properties binding: [`backend/src/main/java/com/backend/infrastructure/analytics/GoogleAnalyticsProperties.java`](../../backend/src/main/java/com/backend/infrastructure/analytics/GoogleAnalyticsProperties.java)
- Tenant config management: [`backend/src/main/java/com/backend/application/service/impl/config/ConfigPropertiesAdminServiceImpl.java`](../../backend/src/main/java/com/backend/application/service/impl/config/ConfigPropertiesAdminServiceImpl.java)
- Global config management: [`backend/src/main/java/com/backend/application/service/impl/config/ConfigGlobalPropertiesAdminServiceImpl.java`](../../backend/src/main/java/com/backend/application/service/impl/config/ConfigGlobalPropertiesAdminServiceImpl.java)

Frontend:

- Overview container and GA4 rendering: [`storefront/src/app/modules/admin/custom/site/tabs/overview/site-overview.component.ts`](../../storefront/src/app/modules/admin/custom/site/tabs/overview/site-overview.component.ts)
- Site dashboard service: [`storefront/src/app/modules/admin/custom/site/site.service.ts`](../../storefront/src/app/modules/admin/custom/site/site.service.ts)
- Site dashboard types: [`storefront/src/app/modules/admin/custom/site/site.types.ts`](../../storefront/src/app/modules/admin/custom/site/site.types.ts)

Related docs:

- Site Dashboard: [`../modules/site-dashboard.md`](../modules/site-dashboard.md)
- Config Control Panel: [`../modules/config-control-panel.md`](../modules/config-control-panel.md)
- Environment configuration: [`../global/environment-configuration.md`](../global/environment-configuration.md)
- Headless storefront env contract: [`../storefront-nextjs/README.md`](../storefront-nextjs/README.md)

## Admin API

Authenticated tenant endpoint:

- `GET /api/sites/analytics/summary`

Response shape:

```json
{
  "status": "READY|NOT_CONFIGURED|DISABLED|ACCESS_ERROR|NO_DATA",
  "propertyId": "123456789",
  "range": "LAST_7_DAYS",
  "cards": [
    {
      "metric": "activeUsers",
      "value": 42,
      "previousValue": 30,
      "deltaPercentage": 40.0,
      "deltaDirection": "up"
    }
  ],
  "trend": [
    { "date": "2026-04-02", "value": 2 }
  ],
  "lastSyncedAt": "2026-04-02T23:58:05"
}
```

Current KPI card order:

- `activeUsers`
- `screenPageViews`
- `newUsers`
- `engagementRate`

Status meanings:

- `READY`: reporting data exists and cards/trend can be rendered
- `NOT_CONFIGURED`: tenant GA4 property is missing
- `DISABLED`: global or tenant analytics flag is off
- `ACCESS_ERROR`: property ID is invalid or Google API access fails
- `NO_DATA`: access works, but GA4 does not yet have reportable data for the selected range

There is no public delivery API for GA4 reporting in Craftive. Reporting is admin-only.

## Frontend integration

GA4 is rendered inside Site Dashboard Overview, not as a standalone admin page.

Current behavior:

- Overview requests `GET /api/sites/analytics/summary`
- cards render only when `status = READY`
- non-ready states render dedicated setup, disabled, no-data, or access-error content
- chart data is normalized before rendering so empty values do not produce `NaN`
- single-point trends render safely without requiring multiple GA4 data points

Overview currently surfaces:

- KPI cards with previous-period delta
- 7-day active-user chart
- property badge
- last synced timestamp
- CTA to `/config?subdomain={tenantSubdomain}` when tenant setup is incomplete

Storefront event collection is separate from admin rendering:

- headless storefront uses `NEXT_PUBLIC_GA_ID`
- admin Angular app does not send GA4 events for tenant storefront traffic

## Configuration model

Tenant `/config` keys:

- `analytics.ga4.enabled`
- `analytics.ga4.property_id`

Global `/config` key:

- `platform.analytics.ga4.enabled`

Effective enablement rule:

- GA4 snapshot is active only when both `platform.analytics.ga4.enabled` and `analytics.ga4.enabled` are `true`

Identifier rules:

- `analytics.ga4.property_id` must be the numeric GA4 Property ID
- `NEXT_PUBLIC_GA_ID` is the Measurement ID used by the headless storefront
- `G-XXXX...` is a Measurement ID, not a Property ID

Practical split:

- tenant config selects which GA4 property is queried
- global config acts as the platform-wide master switch
- backend environment provides Google API credentials

## Security & tenant isolation

API access:

- endpoint lives under `SiteController`
- controller access is authenticated and tenant-scoped
- current controller-level access allows `TENANT_ADMIN` and `VIEWER`

Tenant isolation rules:

- tenant context is resolved before the controller via `TenantFilter`
- property ID is resolved from tenant config store, not request payload
- a tenant cannot supply another tenant's property ID through the analytics summary endpoint

Google access model:

- backend uses one shared Google service account for reporting
- each tenant GA4 property must explicitly grant that service account access in Google Analytics
- access is enforced by Google property permissions and by Craftive tenant context

## Third-party runtime model

Backend reporting:

- calls Google Analytics Data API through the shared backend service identity
- credential comes from runtime secret, not from `/config`
- supported secret names:
  - `APP_ANALYTICS_GA4_SERVICE_ACCOUNT_JSON`
  - `APP_ANALYTICS_GA4_SERVICE_ACCOUNT_JSON_BASE64`

Storefront collection:

- tenant storefront sends traffic/events with `NEXT_PUBLIC_GA_ID`
- each tenant storefront can use its own GA4 measurement ID
- GA4 script always loads but is gated by **Google Consent Mode v2** when `cookieConsent.enabled = true`

This creates a two-part integration:

- collection: tenant storefront -> GA4 via Measurement ID
- reporting: backend -> GA4 Data API via shared service account + tenant Property ID

## Implementation guide

### Connect GA4 for a tenant

1. Create or select the tenant GA4 property in Google Analytics.
2. Add the shared backend service account email to that property with at least `Viewer` access.
3. In tenant `/config`, set:
   - `analytics.ga4.enabled = true`
   - `analytics.ga4.property_id = <numeric-property-id>`
4. In global `/config`, ensure `platform.analytics.ga4.enabled = true`.
5. In the tenant storefront deployment, set `NEXT_PUBLIC_GA_ID` to the tenant Measurement ID.

### Validate the integration

1. Open the tenant storefront and generate traffic.
2. Confirm data appears in GA4 Realtime.
3. Open Site Dashboard Overview.
4. Confirm `GET /api/sites/analytics/summary` progresses from `NO_DATA` to `READY` once GA4 report data is available.

### Diagnose common states

- `DISABLED`: check global `platform.analytics.ga4.enabled` and tenant `analytics.ga4.enabled`
- `NOT_CONFIGURED`: add `analytics.ga4.property_id`
- `ACCESS_ERROR`: verify Property ID format and Google property access for the shared service account
- `NO_DATA`: traffic is not yet available in GA4 standard reporting for the selected range

## Cookie Consent & Consent Mode v2

Cookie consent management lives in the storefront, not in GA4 configuration. The implementation follows Google's Consent Mode v2 specification.

### Component

`storefront-nextjs/components/cookie-consent/CookieConsentManager.tsx` — client component rendered inside `NextIntlClientProvider` in `app/[lang]/layout.tsx`.

Props: `gaId`, `cookieConsentEnabled`, `cookieConsentText`

### Consent state machine

| localStorage value | State | Meaning |
| --- | --- | --- |
| absent | `null` | No decision yet — banner shown |
| `"true"` | `true` | Accepted — GA4 collects data |
| `"false"` | `false` | Rejected — GA4 restricted |

`undefined` is the initial hydration state — component renders nothing until `useEffect` reads localStorage.

### Consent Mode v2 flow

1. `app/layout.tsx` injects an inline `<script>` in `<head>` **before** GTM/GA4 when `(gtmId || gaId) && cookieConsent.enabled`:
   ```js
   gtag('consent', 'default', {
     analytics_storage: 'denied',
     ad_storage: 'denied',
     ad_user_data: 'denied',
     ad_personalization: 'denied',
     wait_for_update: 500
   });
   ```

2. GA4 script always loads (`shouldLoadGA = !!gaId`) — denied state blocks cookie setting and user tracking, not the script itself.

3. On mount: if user previously accepted, `gtag('consent', 'update', { ...granted })` is called immediately (restores consent within `wait_for_update` window for GTM).

4. On accept: `gtag('consent', 'update', { ...granted })` — GA4 begins tracking.

5. On reject: `gtag('consent', 'update', { ...denied })` — explicit denied state, banner does not reappear.

### When `cookieConsent.enabled = false`

Consent default script is skipped. GA4 loads without restrictions — no banner, no consent gating.

### localStorage key

`craftive_cookie_consent` — stored in tenant storefront browser storage. No expiry is set; it persists until cleared.

### Admin configuration

Tenant admins configure cookie consent from Site Dashboard → Technical tab:
- Toggle: `cookieConsentEnabled` (stored in `site_technical_settings`)
- Text: per-language banner text (stored in `site_settings` with key `i18n.cookie.consent.text`)

The CMS delivery API (`GET /api/cms/site?lang=...`) returns the resolved single-language text in `cookieConsent.text`.

## TODO

Future phase:

- define a shared Craftive `dataLayer` / event contract for tenant storefronts
- review marketing funnel events such as CTA clicks, form start, form submit, and demo request
