# ImpEx — Data Import

## Purpose

ImpEx provides an on-demand SQL execution interface for `TENANT_ADMIN` and `SUPER_ADMIN` users against the **currently resolved tenant database only**. It allows bulk data seeding (pages, slots, components, i18n records, etc.) via the Admin UI without manual DB access or provisioning hooks. Inspired by SAP Hybris HAC ImpEx, but SQL-native instead of CSV. Platform-wide data changes belong in **Flyway** (`db/platform`) or controlled DBA processes, not ImpEx.

Execution is **manual only** — there is no automatic trigger on provisioning or tenant creation.

---

## Admin API

**Controller:** [`../../backend/src/main/java/com/backend/presentation/controller/ImpExController.java`](../../backend/src/main/java/com/backend/presentation/controller/ImpExController.java)

### `POST /api/impex/execute`

Executes a SQL script submitted in the request body.

**Request body:**

```json
{
  "sqlContent": "-- #CRAFTIVE_IMPEX\n\nINSERT INTO ..."
}
```

| Field        | Type   | Constraint              |
|--------------|--------|-------------------------|
| `sqlContent` | String | `@NotBlank`, max 100 000 chars |

**Responses:**

| Status | Condition |
|--------|-----------|
| `200 OK` | All statements succeeded (`status: "SUCCESS"`) |
| `207 Multi-Status` | At least one statement failed (`status: "PARTIAL"`) |
| `400 Bad Request` | Missing `-- #CRAFTIVE_IMPEX` marker |
| `500 Internal Server Error` | Unexpected execution error |

**Response body** (`ApiResponse<ImpExResult>`):

```json
{
  "result": "SUCCESS",
  "message": "All 12 statements executed successfully",
  "data": {
    "status": "SUCCESS",
    "totalStatements": 12,
    "executedStatements": 12,
    "failedStatements": 0,
    "results": [
      {
        "index": 1,
        "preview": "INSERT INTO components (uuid, uid, component_type_id...",
        "success": true,
        "affectedRows": 1,
        "errorMessage": null
      }
    ],
    "executedAt": "2026-03-01T14:22:00"
  }
}
```

**DTOs:**

- [`../../backend/src/main/java/com/backend/application/dto/impex/ImpExRequest.java`](../../backend/src/main/java/com/backend/application/dto/impex/ImpExRequest.java)
- [`../../backend/src/main/java/com/backend/application/dto/impex/ImpExResult.java`](../../backend/src/main/java/com/backend/application/dto/impex/ImpExResult.java)
- [`../../backend/src/main/java/com/backend/application/dto/impex/StatementResult.java`](../../backend/src/main/java/com/backend/application/dto/impex/StatementResult.java)

---

## Service

**Interface:** [`../../backend/src/main/java/com/backend/application/service/impex/ImpExService.java`](../../backend/src/main/java/com/backend/application/service/impex/ImpExService.java)

**Implementation:** [`../../backend/src/main/java/com/backend/application/service/impex/ImpExServiceImpl.java`](../../backend/src/main/java/com/backend/application/service/impex/ImpExServiceImpl.java)

Execution pipeline:

1. Validate `-- #CRAFTIVE_IMPEX` marker — throws `ImpExInvalidScriptException` if missing.
2. Reject MySQL conditional comments (`/*! ... */`) that can smuggle DDL/DML past naive checks.
3. Strip SQL comments (line and block) **before** splitting on semicolons, then split on unquoted `;`.
4. For each statement: check blocked keywords, allowed statement prefixes, and **sensitive-table** rules (deny-list on resolved table name, including quoted/schema-qualified targets).
5. Execute via `JdbcTemplate.update()` against the tenant `DataSource` from `TenantContext` (tenant must be resolved for all roles).
6. Soft-fail: a failing statement is recorded in `results` but does not stop execution of the remaining statements.
7. Derive `status` (`"SUCCESS"` / `"PARTIAL"`) from the failed count.
8. In `finally`, write a row to platform `impex_audit` (truncated SQL, correlation id, client IP, counts, duration) — failures to persist audit are logged but do not roll back tenant DML already applied.

---

## SQL Format

Every script submitted to ImpEx must follow these rules:

### Required marker

The first line must contain the marker (anywhere in the content):

```sql
-- #CRAFTIVE_IMPEX
```

Requests without this marker are rejected with `400`.

### Allowed statements

| Allowed | Blocked |
|---------|---------|
| `INSERT` | `DELETE` |
| `UPDATE` | `DROP` |
| `SELECT` | `TRUNCATE` |
| | `ALTER` |
| | `CREATE` |
| | `RENAME` |
| | `REPLACE` |

Blocked statements are not executed — they appear in `results` with `success: false` and a localized error message.

### Idempotency

All scripts should be written with `ON DUPLICATE KEY UPDATE` so they can be run multiple times safely:

```sql
-- #CRAFTIVE_IMPEX

INSERT INTO pages (uuid, uid, template_id, status, robot_tag, page_type, is_home, created_by)
SELECT 'f0000001-0000-0000-0000-000000000001', 'homepage', pt.id,
  'PUBLISHED', 'INDEX_FOLLOW', 'LANDING', TRUE, NULL
FROM page_templates pt WHERE pt.uid = 'LandingPageTemplate'
ON DUPLICATE KEY UPDATE status = VALUES(status);
```

Rules:
- Never hardcode database IDs — resolve via `uid` subqueries.
- `uuid` must never appear in `ON DUPLICATE KEY UPDATE` (it is the idempotency key).
- `created_by`: use `NULL` — the `users` table in tenant DB has no `is_system` column.

---

## Frontend Integration

**Component:** [`../../storefront/src/app/modules/admin/custom/impex/impex.component.ts`](../../storefront/src/app/modules/admin/custom/impex/impex.component.ts)

**Template:** [`../../storefront/src/app/modules/admin/custom/impex/impex.component.html`](../../storefront/src/app/modules/admin/custom/impex/impex.component.html)

**Service:** [`../../storefront/src/app/modules/admin/custom/impex/impex.service.ts`](../../storefront/src/app/modules/admin/custom/impex/impex.service.ts)

**Route:** `/{lang}/impex` — registered in [`../../storefront/src/app/app.routes.ts`](../../storefront/src/app/app.routes.ts)

**Endpoint key:** `impexExecute` in [`../../storefront/src/app/modules/admin/api-endpoints.ts`](../../storefront/src/app/modules/admin/api-endpoints.ts)

UI flow:
1. User pastes SQL into the textarea (`spa-textarea`, plain text, monospace).
2. Clicks **Execute** → confirmation dialog opens.
3. On confirm → `POST /api/impex/execute` with the raw SQL.
4. Notification shown using `response.message` from the backend (localized, no frontend fallback).
5. Result panel shows per-statement outcome (index, preview, rows affected or error message).
6. Detail rows are collapsed by default; expanded via toggle.

---

## Security & Tenant Isolation

- **Auth:** `TENANT_ADMIN` or `SUPER_ADMIN` role required (`@PreAuthorize("hasAnyRole('TENANT_ADMIN','SUPER_ADMIN')")`).
- **SUPER_ADMIN usage:** Platform admins use **Platform → ImpEx** with an explicit **tenant workspace** (same headers / hostname rules as other tenant APIs). SQL always runs on that tenant's database; there is no “no tenant selected → platform DB” path.
- **Tenant isolation:** `TenantFilter` resolves the tenant before the request reaches the controller. `ImpExServiceImpl` requires a non-blank tenant DB name and obtains a `DataSource` from `MultiTenantConnectionProvider` — no cross-tenant leakage at the JDBC level.
- **No tenant_id columns:** Consistent with the database-per-tenant model; isolation is at the connection level.
- **Statement whitelist:** DML writes are restricted to `INSERT` and `UPDATE`. DDL and destructive operations are blocked at the service layer regardless of role.
- **Sensitive tables:** Updates/inserts targeting a configured deny-list of logical table names (e.g. identity and security tables) are rejected even when the SQL uses schema-qualified or backtick-quoted identifiers.
- **Size limit:** `sqlContent` is capped at 100 000 characters (`@Size(max = 100_000)`).
- **Error truncation:** JDBC error messages are truncated at 500 characters before being stored in `StatementResult.errorMessage`.
- **Rate limiting:** Controller-level Resilience4j limits apply (see `application.yml`); multi-replica caveats remain an ops concern.

---

## Implementation Guide

### Running a data seed

```text
1. Write SQL with -- #CRAFTIVE_IMPEX marker
2. Use INSERT ... SELECT ... ON DUPLICATE KEY UPDATE pattern
3. Open /{lang}/impex in Admin UI
4. Paste SQL → Execute → Confirm
5. Review per-statement results; re-run is safe (idempotent)
```

### Diagnosing a PARTIAL result

When `status` is `"PARTIAL"`, inspect `results` where `success: false`:

- `errorMessage` contains the truncated JDBC exception message.
- Common cause: FK violation (referenced `uid` does not exist yet) — fix ordering of statements.
- Blocked statement: `errorMessage` starts with `"Operation not allowed"` — remove or replace the statement.

> **Semicolons and comments:** The pipeline strips line (`--`) and block (`/* */`) comments before splitting on unquoted `;`, which avoids most historical “semicolon inside a comment line” splits. Prefer avoiding `;` inside comment text anyway for readability and tooling compatibility.

### Adding a new script pattern

Scripts have no file-based registration. Any valid SQL can be submitted. For repeatable seeds that belong to the codebase, keep them in version-controlled `.sql` files and paste into the UI as needed. There is no automatic classpath scanning.

---

## Version-Controlled Reference Scripts

ImpEx scripts for demo/content data are stored under `backend/src/main/resources/impex/`. These are **not executed automatically** — they serve as versioned reference documents that an admin can paste into the UI when setting up a new tenant with sample data.

### Ownership model

Fresh tenant databases start with **empty theme-owned CMS data**. The following tables are populated only when an admin runs the relevant ImpEx scripts for the chosen theme:

- `page_templates`
- `template_slots`
- `pages`
- `page_slots`
- `components`
- `slot_components`
- theme navigation/chrome content

The `base/` folder now contains only theme-neutral catalogs and optional non-theme sample data. The `theme/liko/` folder owns all Liko CMS structure and content.

### Execution order (fresh tenant — Liko example)

```text
1. base/base_site_settings.sql           — default site settings and i18n
2. base/base_media_formats.sql           — system media format presets
3. base/base_component_types.sql         — reusable component type catalog
4. base/base_entry_field_definitions.sql — reusable field schema per base component type
5. base/base_product_types.sql           — product types and attribute definitions
6. theme/liko/liko_foundation.sql        — theme-owned page templates, template slots, shared chrome components, navigation
7. [upload media via Admin UI]           — upload all assets referenced by the selected theme pages
8. theme/liko/homepage.sql               — homepage components, type remapping, slot wiring, homepage media UID alignment, shared logo UIDs
9. theme/liko/about_page.sql             — optional About page + about media UID alignment
10. theme/liko/service_page.sql          — optional Service page + service-specific component types/field definitions + media UID alignment
11. base/base_mail_marketing_tenant.sql  — optional tenant mail marketing sample data
```

### Execution order (fresh tenant — Mulayim portfolio homepage)

```text
1. base/base_site_settings.sql           — default site settings and i18n
2. base/base_media_formats.sql           — system media format presets
3. base/base_product_types.sql           — product types and attribute definitions
4. theme/mulayim/mulayim_foundation.sql  — theme-owned page templates, template slots, shared chrome components, navigation
5. theme/mulayim/site_settings_technical.sql — Mulayim site settings + technical dashboard defaults (site_settings, site_technical_settings)
6. [upload media via Admin UI]           — upload port-1.jpg through port-8.jpg, logo.png, logo-white.png
7. theme/mulayim/portfolio_homepage.sql  — homepage IntroBannerBlock, PortfolioCardGrid, StatementCtaBlock, Section1-Section3 slot wiring, media UID alignment
8. theme/mulayim/portfolio_page.sql      — /portfolio listing page hero and filterable 4-column grid
9. theme/mulayim/portfolio_detail_pages.sql — all /portfolio/{slug} detail pages using PortfolioDetailPageTemplate and PortfolioDetailsComponent
10. [upload brand logos via Admin UI]    — upload brand-1.jpg through brand-15.jpg (references logo wall assets); must complete before the next step so media UID alignment runs correctly
11. theme/mulayim/references_page.sql    — /references landing page hero, 15-logo references wall, StatementCtaBlock reuse
```

Mulayim foundation/homepage scripts include their required generic component type seeds. Running `base_component_types.sql` and `base_entry_field_definitions.sql` first is still safe, but not required for the Mulayim path.
The Mulayim homepage intentionally defines only `Section1`, `Section2`, and `Section3`. Additional vertical content can be added by binding more components into an existing section slot with a higher `sort_order`.
Mulayim also defines `PortfolioDetailPageTemplate` with a single `MainContent` slot for gallery-first project detail pages.
Header and footer navigation links for `/references` are owned by `theme/mulayim/mulayim_foundation.sql`, so rerunning foundation after adding the references page keeps shell navigation in sync.

Platform sample data (e.g. mail-marketing platform seeds) must be applied with **DBA / migration tooling** against `platform_management`, not via ImpEx in the application (ImpEx is tenant-scoped only).

### Folder layout

- `backend/src/main/resources/impex/base/`
  - Theme-neutral catalogs and optional shared sample data
- `backend/src/main/resources/impex/theme/liko/`
  - Theme-owned templates, chrome, navigation, page data, slot wiring and semantic media UID alignment

### Theme script responsibilities

- `theme/liko/liko_foundation.sql`
  - Creates all theme-owned page templates and template slots
  - Seeds shared storefront chrome components
  - Seeds navigation nodes, entries and navigation bindings
- `theme/liko/homepage.sql`
  - Seeds homepage components, i18n, entries and entry i18n
  - Migrates homepage components from generic base types to theme-specific renderer types
  - Creates homepage page, page_i18n, page_slots, shared header/footer slots and slot bindings
  - Aligns homepage media UIDs and updates `sites.logo_media_uid` / `sites.logo_dark_media_uid`
- `theme/liko/about_page.sql`
  - Seeds the `/about-us` page using `ContentPageTemplate`
  - Includes semantic media UID alignment for about assets
- `theme/liko/service_page.sql`
  - Seeds the `/service` page using `ContentPageTemplate`
  - Owns service-specific component types and `entry_field_definitions`
  - Includes semantic media UID alignment for service assets

### Media UID alignment

Component entry `custom_data` fields reference semantic UIDs like `homepage-hero-bg`, `about-hero-bg`, and `service-hero-bg`. Media uploads still generate runtime UIDs like `cmsitem_########`, so page scripts that include media UID alignment must be executed **after the related files are uploaded**.

These alignment statements are idempotent:

- homepage logo and media mappings live in `theme/liko/homepage.sql`
- about page mappings live in `theme/liko/about_page.sql`
- service page mappings live in `theme/liko/service_page.sql`

`base/base_mail_marketing_tenant.sql` still requires the tenant module `mail_marketing` to be provisioned.

### Platform reference script

`base/base_mail_marketing_platform.sql` (if present) is a **reference** script for operators who apply SQL directly to `platform_management`. It is **not** executed through the Admin ImpEx UI against the platform database.

### What remains in Flyway (R__ repeatable migrations)

These seeds are structural / system data and still run automatically via Flyway:

| File | Purpose |
|------|---------|
| `platform/R__seed_modules.sql` | Module catalog |
| `platform/R__seed_platform_admin.sql` | Initial platform admin user |

Content and theme-owned schema data (site settings, formats, component catalogs, page templates, components, navigation and sample pages) are managed via the `impex/` directory and applied on-demand.
