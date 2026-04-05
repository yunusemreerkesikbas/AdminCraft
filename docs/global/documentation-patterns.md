# Documentation Patterns (How to Add/Update Docs)

This repository’s documentation lives under `docs/` and is organized by **global guides** and **module docs**.
Third-party integration docs may also live under `docs/3rd-party/` when the main owner is an external provider and the page is not a good fit for either `global/` or `modules/`.

## Principles

- Keep everything **English-only**.
- Prefer **short, factual** docs over long narratives.
- Organize by **module**, not by sprints/epics.
- Documentation must be **codebase-backed**: every endpoint/contract claim should be verifiable in code.
- Do **not** include local development setup instructions (that belongs elsewhere).

## Where a new document belongs

- `docs/global/*.md`
  - Cross-cutting concepts used by multiple modules (architecture, security, i18n, list patterns, dialogs, validation, auth).
- `docs/modules/*.md`
  - A specific tenant module or platform feature (media, pagebuilder, component library, provisioning, delivery, navigation, etc.).
- `docs/3rd-party/*.md`
  - An external provider integration that has a single external owner (for example Google Analytics) and needs implementation-backed operational documentation.

Rule of thumb:

- If it affects **many modules**, it is `global/`.
- If it has a **single main owner**, it is a `modules/` page.
- If it is centered on an **external provider contract**, it can be `3rd-party/`.

## Required structure (module docs)

Use this section order unless there is a strong reason not to:

1. **Purpose**
2. **Database** (migrations path under `backend/src/main/resources/db/...` when applicable)
3. **Admin API** (tenant-scoped, authenticated)
4. **Public delivery APIs** (if any)
5. **Frontend integration** (paths only)
6. **Security & tenant isolation** (who can call, tenant resolution, special cases)
7. **Implementation guide** (2–3 practical flows)

## Required structure (global docs)

Use this section order:

1. **What it is / why it exists**
2. **Source of truth** (code locations)
3. **Rules and invariants** (things that must always be true)
4. **Common patterns** (how we implement it)
5. **Gotchas** (edge cases)

## Linking rules (critical)

- Inside `docs/README.md`:
  - Link to global docs as `global/<file>.md`
  - Link to module docs as `modules/<file>.md`
  - Link to third-party docs as `3rd-party/<file>.md`
- Inside `docs/global/*`:
  - Link to other global docs as `<file>.md`
  - Link to module docs as `../modules/<file>.md`
  - Link to third-party docs as `../3rd-party/<file>.md`
- Inside `docs/modules/*`:
  - Link to global docs as `../global/<file>.md`
  - Link to other module docs as `<file>.md`
  - Link to third-party docs as `../3rd-party/<file>.md`
- Inside `docs/3rd-party/*`:
  - Link to global docs as `../global/<file>.md`
  - Link to module docs as `../modules/<file>.md`
  - Link to other third-party docs as `<file>.md`
- When linking to code from `docs/global/*` or `docs/modules/*`:
  - Use `../../backend/...` and `../../storefront/...` (because both folders are two levels up from `docs/global` and `docs/modules`)
- Never use `docs/...` in links inside files under `docs/` (it breaks when rendered from that folder).

## How to add a new doc (checklist)

1. Create the new markdown file under the correct folder (`docs/global`, `docs/modules`, or `docs/3rd-party`).
2. Use the required structure above and keep headings consistent.
3. Add **source-of-truth links** to controllers/services/DTOs/migrations.
4. Verify endpoints against controllers:
   - Confirm the controller `@RequestMapping(...)`
   - Confirm HTTP method mappings and full paths (remember backend context path is `/api`)
5. Verify security assumptions:
   - Check `TenantFilter` categorization (public/platform/tenant)
   - Check controller-level `@PreAuthorize` rules where relevant
6. Verify link correctness (no `docs/...` links inside `docs/`).
7. Update indexes:
   - Add the new doc to `docs/README.md`
   - If it is a top-level entry point doc, also add it to `.docs.md`

## Naming conventions

- File names: kebab-case (e.g. `security-multi-tenancy.md`)
- Titles: clear product names (e.g. “Media Library (DAM)”)
- Use the system terms consistently:
  - Platform DB: `platform_management`
  - Tenant DBs: `ac_subdomain_{id}`
  - Tenant resolution: headers/hostname via `TenantFilter`
