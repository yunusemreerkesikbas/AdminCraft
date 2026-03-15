# ImpEx — Data Import

## Purpose

ImpEx provides an on-demand SQL execution interface for `TENANT_ADMIN` users (tenant DB) and `SUPER_ADMIN` users (tenant DB when a tenant is selected, or **platform DB** when no tenant is selected). It allows bulk data seeding (pages, slots, components, i18n records, or platform data) via the Admin UI without manual DB access or provisioning hooks. Inspired by SAP Hybris HAC ImpEx, but SQL-native instead of CSV.

Execution is **manual only** — there is no automatic trigger on provisioning or tenant creation.

---

## Admin API

**Controller:** [`../../backend/src/main/java/com/backend/presentation/controller/ImpExController.java`](../../backend/src/main/java/com/backend/presentation/controller/ImpExController.java)

### `POST /api/impex/execute`

Executes a SQL script submitted in the request body.

**Request body:**

```json
{
  "sqlContent": "-- #ADMINCRAFT_IMPEX\n\nINSERT INTO ..."
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
| `400 Bad Request` | Missing `-- #ADMINCRAFT_IMPEX` marker |
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

1. Validate `-- #ADMINCRAFT_IMPEX` marker — throws `ImpExInvalidScriptException` if missing.
2. Split content on `;`, strip comment lines (`--`) and blank lines per statement.
3. For each statement: check against allowed/blocked keyword whitelist.
4. Execute via `JdbcTemplate.update()` — `JdbcTemplate` uses the active tenant `DataSource` already set by `TenantFilter`.
5. Soft-fail: a failing statement is recorded in `results` but does not stop execution of the remaining statements.
6. Derive `status` (`"SUCCESS"` / `"PARTIAL"`) from the failed count.

---

## SQL Format

Every script submitted to ImpEx must follow these rules:

### Required marker

The first line must contain the marker (anywhere in the content):

```sql
-- #ADMINCRAFT_IMPEX
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
-- #ADMINCRAFT_IMPEX

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
- **SUPER_ADMIN usage:** Platform admins access ImpEx from the **Platform → ImpEx** menu. If no tenant is selected, SQL runs against the **platform** database (e.g. for `seed_mail_marketing_platform.sql`). If a tenant is selected, SQL runs against that tenant's database.
- **Tenant isolation:** `TenantFilter` sets the active tenant `DataSource` before the request reaches the controller. `JdbcTemplate` executes against that tenant's database — no cross-tenant leakage is possible.
- **No tenant_id columns:** Consistent with the database-per-tenant model; isolation is at the connection level.
- **Statement whitelist:** DML writes are restricted to `INSERT` and `UPDATE`. DDL and destructive operations are blocked at the service layer regardless of role.
- **Size limit:** `sqlContent` is capped at 100 000 characters (`@Size(max = 100_000)`).
- **Error truncation:** JDBC error messages are truncated at 500 characters before being stored in `StatementResult.errorMessage`.

---

## Implementation Guide

### Running a data seed

```text
1. Write SQL with -- #ADMINCRAFT_IMPEX marker
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

> **Semicolons in SQL comments:** The ImpEx parser splits statements on `;`. A semicolon inside a `--` comment (e.g. `-- (6 uploaded; 7th slot empty)`) is treated as a statement terminator, splitting the next `UPDATE` into an invalid fragment. **Never use `;` inside comment text** — use `,` or `-` instead.

### Adding a new script pattern

Scripts have no file-based registration. Any valid SQL can be submitted. For repeatable seeds that belong to the codebase, keep them in version-controlled `.sql` files and paste into the UI as needed. There is no automatic classpath scanning.

---

## Version-Controlled Reference Scripts

ImpEx scripts for demo/content data are stored under `backend/src/main/resources/impex/`. These are **not executed automatically** — they serve as versioned reference documents that an admin can paste into the UI when setting up a new tenant with sample data.

### Execution order (when seeding a fresh tenant — default theme example content)

```text
1. seed_liko_components.sql        — landing page components (Homepage*), i18n, entries, entry i18n
2. seed_liko_chrome_components.sql — shared Header/Footer components, Home-2 chrome copy, i18n, entries
3. seed_liko_pages_and_slots.sql   — homepage, page_i18n, page_slots, slot_components (Section1-8), shared Header/Footer slot wiring
4. seed_pages_and_slots.sql        — productPage, categoryPage, searchResultsPage, page_i18n, page_slots, shared slots (no slot_components)
5. seed_navigation.sql             — nav nodes, entries, i18n, and navigation bindings for header/footer chrome components
6. [upload media via Admin UI]     — upload all image/video assets in the Media Library before running step 7
7. seed_liko_media_uids.sql        — assigns semantic UIDs to uploaded media so component mediaUid references resolve correctly (see note below)
8. seed_mail_marketing_tenant.sql (optional) — mail templates, subscribers, template subscriptions (`source`, `preferred_language`)
```

> **Media UID alignment (step 7):** Component entry `custom_data` fields reference media by semantic UIDs like `homepage-hero-bg`. When media is uploaded via the Media Library, auto-generated UIDs (`cmsitem_*`) are assigned. `seed_liko_media_uids.sql` corrects this by matching on `original_name` and updating each record's UID to the expected semantic value. It also sets `sites.logo_media_uid` / `sites.logo_dark_media_uid`. Run this script **after** uploading all assets. It is idempotent.

`seed_liko_chrome_components.sql` maps the header/footer copy from `liko-next-js/src/pages/homes/home-2.tsx` into standard CMS component data.

`seed_liko_pages_and_slots.sql` depends on components from steps 1-2 and on Flyway-managed `page_templates` / `template_slots`.

`seed_pages_and_slots.sql` creates pages and page_slots but no slot_components; it does not depend on Seed* components.

`seed_mail_marketing_tenant.sql` requires the tenant module `mail_marketing` to be provisioned through `V5` (Flyway `mail_marketing/V1__baseline.sql` ... `V5__add_subscription_permission.sql`).

### Storefront chrome seeding

The CMS-driven storefront chrome uses shared slots plus standard CMS components. The reference scripts are:

- `seed_liko_chrome_components.sql`
  - Creates `StorefrontHeaderMainNavigation`, `StorefrontHeaderSocialLinks`, `StorefrontHeaderContactInfo`
  - Creates `StorefrontFooterBrandBlock`, `StorefrontFooterSitemapNavigation`, `StorefrontFooterOfficeLinks`, `StorefrontFooterNewsletter`, `StorefrontFooterSocialLinks`
  - Seeds the Home-2 header/footer text, links, newsletter placeholder, and social links through `component_i18n` and `component_entry_i18n`
- `seed_liko_pages_and_slots.sql`
  - Binds the shared `Header` slot (`SharedHeaderSlot`) and shared `Footer` slot (`SharedFooterSlot`) to those components
- `seed_navigation.sql`
  - Creates `LandingMainNavNode` and `LandingFooterNavNode`
  - Attaches those nodes to the navigation-aware chrome components with `UPDATE components ... navigation_node_id = ...`

### Platform reference script

`seed_mail_marketing_platform.sql` is version-controlled under `backend/src/main/resources/impex/` for platform DB sample data. **SUPER_ADMIN** can execute it via the Admin UI: open **Platform → ImpEx**, do **not** select a tenant, paste the script and run. The backend runs it against the platform database.

### What remains in Flyway (R__ repeatable migrations)

These seeds are structural / system data and still run automatically via Flyway:

| File | Purpose |
|------|---------|
| `core/R__seed_system_user.sql` | System user required by FK on `created_by` |
| `core/R__seed_roles.sql` | Default site settings |
| `media/R__seed_media_formats.sql` | Media format definitions |
| `component_library/R__seed_component_types.sql` | Component type catalog |
| `component_library/R__seed_entry_field_definitions.sql` | Field schema per type |
| `pagebuilder/R__seed_page_templates.sql` | Page templates + template slots |

The former content seeds (`R__seed_components.sql`, `R__seed_navigation.sql`, `R__seed_sample_pages.sql`, `R__zz_seed_page_slots.sql`) have been stubbed out and their content moved to the `impex/` directory.
