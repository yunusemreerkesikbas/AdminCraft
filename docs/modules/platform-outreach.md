# Platform Outreach

## Purpose

Platform Outreach provides SUPER_ADMIN with a cold outreach campaign system to send targeted B2B emails to manually managed contacts via custom HTML templates.

Unlike **Platform Mail** (newsletter-style, subscription-based), Outreach is contact-list-based — contacts are explicitly added by the admin, not via a public subscribe flow.

| Concern | Platform Mail | Platform Outreach |
|---------|--------------|-------------------|
| Audience | Subscribers who opted in via a public form | Manually managed B2B contacts |
| Template | Fixed template types (`NEWSLETTER_DEFAULT`, `VERSION_UPGRADE`) | Freeform WYSIWYG HTML templates |
| Campaign trigger | Admin selects template type → backend resolves all active subscribers | Admin picks a template + explicit contact list |
| Language resolution | Per-subscriber `preferredLanguage` on the subscription relation | Language is set on the template itself |
| Public API | Yes (`/api/platform/public/newsletter/...`) | No |

---

## Database

### Migration

- [`../../backend/src/main/resources/db/platform/V2.3__create_platform_outreach.sql`](../../backend/src/main/resources/db/platform/V2.3__create_platform_outreach.sql)

### Tables (`platform_management` schema)

| Table | Purpose |
|-------|---------|
| `platform_outreach_contacts` | B2B contact directory |
| `platform_outreach_templates` | Custom HTML email templates |
| `platform_outreach_campaigns` | Campaign headers (name, template ref, status, counts) |
| `platform_outreach_campaign_contacts` | Outbox — one row per contact per campaign |

#### `platform_outreach_contacts`

- `email` — unique, index
- `status` — `ACTIVE` / `UNSUBSCRIBED`, indexed
- Standard `BaseEntity` fields: `id`, `uuid`, `uid`, `createdAt`, `updatedAt`, `createdBy`, `updatedBy`

#### `platform_outreach_templates`

- `subject` — base subject (may contain `{{name}}` / `{{company}}` placeholders)
- `content` — `MEDIUMTEXT` HTML body with `{{key}}` placeholders
- `language` — `TR` or `EN` (free string on the row; single-language per template)
- `is_active` — soft-disable; inactive templates cannot be used in new campaigns

#### `platform_outreach_campaigns`

- `template_id` — FK to `platform_outreach_templates` (`ON DELETE SET NULL`)
- `status` — `DRAFT` → `SENDING` → `SENT` / `FAILED`
- `total_count`, `sent_count`, `failed_count` — updated as delivery progresses
- `created_by_email` — email of the SUPER_ADMIN who triggered the send

#### `platform_outreach_campaign_contacts`

- `campaign_id` — FK `ON DELETE CASCADE`
- `contact_id` — FK `ON DELETE CASCADE`
- `status` — `PENDING` → `SENT` / `FAILED`
- `rendered_subject`, `rendered_content` — snapshot of the rendered mail at send time
- `provider_message_id` — Postmark message ID on success
- `error_message` — raw error on failure

### Entity conventions

All 4 entities extend `BaseEntity`:

- `id` (BIGINT AUTO_INCREMENT)
- `uuid` (CHAR 36, auto-generated)
- `uid` (VARCHAR 50, auto-generated)
- `createdAt`, `updatedAt` (auto-managed by `@PrePersist`/`@PreUpdate`)
- `createdBy`, `updatedBy` (Long — platform admin user ID, nullable)

### Query performance notes

- `PlatformOutreachCampaignContactRepository.findByCampaignId(Pageable)` uses `JOIN FETCH cc.contact` with an explicit `countQuery` to avoid the Hibernate JOIN FETCH + pagination incompatibility.
- `existsByTemplateId(Long)` — used in `deleteTemplate` to avoid a full table scan.
- `findByIdForUpdate(Long)` — `@Lock(PESSIMISTIC_WRITE)` SELECT FOR UPDATE used in `sendCampaign` to prevent concurrent send race conditions.

---

## Admin API

### Controller

[`../../backend/src/main/java/com/backend/presentation/controller/PlatformOutreachController.java`](../../backend/src/main/java/com/backend/presentation/controller/PlatformOutreachController.java)

Base path: `/api/platform/outreach`  
Auth: `ROLE_SUPER_ADMIN` (all endpoints)

### Contacts

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/contacts/admin` | Paginated list (`page`, `size`, `sort`, `search`) |
| `POST` | `/contacts/admin` | Create contact |
| `GET` | `/contacts/admin/{id}` | Get contact by ID |
| `PUT` | `/contacts/admin/{id}` | Update contact |
| `DELETE` | `/contacts/admin/{id}` | Delete contact |

`GET /contacts/admin` supports `search` (fullName, email, companyName), `sort` (field), and pagination (default size 20, max 500). The campaign creation dialog uses `size=500` to load all contacts for selection.

### Templates

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/templates` | List all templates (name, language, active status — no HTML content) |
| `POST` | `/templates` | Create template |
| `GET` | `/templates/{id}` | Get template with full content |
| `PUT` | `/templates/{id}` | Update template |
| `DELETE` | `/templates/{id}` | Delete template (blocked if used in any campaign) |

`GET /templates` returns `OutreachTemplateSummaryResponse` — without `content` to avoid transferring large HTML payloads in list views. Full content is only returned by `GET /templates/{id}`.

### Campaigns

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/campaigns` | Paginated campaign list (`page`, `size`, `sort`; default size 20, max 100) |
| `POST` | `/campaigns` | Create campaign (name, templateId, contactIds, subjectOverride?) |
| `GET` | `/campaigns/{id}` | Get campaign with template summary |
| `POST` | `/campaigns/{id}/send` | Trigger send (DRAFT → SENDING → SENT/FAILED) |
| `GET` | `/campaigns/{id}/outbox` | Paginated outbox entries (`page`, `size`; default size 50, max 200) |

---

## Campaign lifecycle

```
DRAFT ──[POST /send]──► SENDING ──► SENT
                                └──► FAILED
```

1. `POST /campaigns` creates a campaign in `DRAFT` with one outbox row (`PENDING`) per selected contact.
2. `POST /campaigns/{id}/send` acquires a **pessimistic write lock** on the campaign row. If status is not `DRAFT`, returns `409 Conflict`.
3. Status changes to `SENDING` in a separate committed transaction before mail delivery begins.
4. Each contact's outbox row is processed in an isolated `TransactionTemplate` transaction:
   - Renders subject + content with contact variables (`{{name}}`, `{{company}}`, etc.)
   - Sends via platform mail sender
   - Updates outbox row to `SENT` or `FAILED`
   - Increments `sentCount` or `failedCount` on the campaign
5. On completion, campaign status → `SENT`.
6. If an unrecoverable exception aborts the loop, campaign status → `FAILED` (recovery catch).

### Template variable rendering

Templates support `{{key}}` placeholders resolved per contact:

| Variable | Source |
|----------|--------|
| `{{name}}` | `contact.fullName` |
| `{{company}}` | `contact.companyName` |

Rendered subject and content are stored in `rendered_subject` / `rendered_content` on each outbox row as a delivery snapshot.

### Concurrency

Concurrent `POST /campaigns/{id}/send` requests are serialised by `SELECT ... FOR UPDATE`. The second request sees status `SENDING` (not `DRAFT`) and receives `409 outreach.campaign.already.sending`.

---

## Service

[`../../backend/src/main/java/com/backend/application/service/PlatformOutreachService.java`](../../backend/src/main/java/com/backend/application/service/PlatformOutreachService.java)

All public methods are annotated `@Transactional("platformTransactionManager")`. The inner send loop uses `TransactionTemplate` for per-contact isolation — a single contact delivery failure does not roll back the overall campaign.

---

## Frontend integration

### Admin storefront routes

| Route | Component |
|-------|-----------|
| `/:lang/platform-outreach/contacts` | `SpaPlatformOutreachContactListComponent` |
| `/:lang/platform-outreach/templates` | `SpaPlatformOutreachTemplateListComponent` |
| `/:lang/platform-outreach/campaigns` | `SpaPlatformOutreachCampaignListComponent` |

Route guard: `superAdminGuard`. Parent `platform-outreach` redirects to `contacts`.

### Navigation

`platform.outreach` nav item is `type: 'collapsable'` with three child items (Contacts / Templates / Campaigns). Key: `admin.nav.platformOutreach`, `admin.nav.platformOutreachContacts`, `admin.nav.platformOutreachTemplates`, `admin.nav.platformOutreachCampaigns`.

### API endpoint keys

Defined in [`../../storefront/src/app/modules/admin/api-endpoints.ts`](../../storefront/src/app/modules/admin/api-endpoints.ts):

| Key | Path |
|-----|------|
| `platformOutreachContacts` | `platform/outreach/contacts/admin` |
| `platformOutreachContactById` | `platform/outreach/contacts/admin/${id}` |
| `platformOutreachTemplates` | `platform/outreach/templates` |
| `platformOutreachTemplateById` | `platform/outreach/templates/${id}` |
| `platformOutreachCampaigns` | `platform/outreach/campaigns` |
| `platformOutreachCampaignById` | `platform/outreach/campaigns/${id}` |
| `platformOutreachCampaignSend` | `platform/outreach/campaigns/${id}/send` |
| `platformOutreachCampaignOutbox` | `platform/outreach/campaigns/${id}/outbox` |

### Angular components

All under `storefront/src/app/modules/admin/custom/outreach/`:

| Component | Purpose |
|-----------|---------|
| `platform-outreach-contact-list.component.ts` | Contact CRUD list with search + pagination |
| `platform-outreach-contact-edit-dialog.component.ts` | Create / edit contact dialog |
| `platform-outreach-template-list.component.ts` | Template list (no HTML content in rows) |
| `platform-outreach-template-edit-dialog.component.ts` | Create / edit template with HTML editor |
| `platform-outreach-campaign-list.component.ts` | Campaign list with status badges and Send button |
| `platform-outreach-campaign-create-dialog.component.ts` | Campaign creation: select template + contacts |
| `platform-outreach-campaign-outbox-dialog.component.ts` | Outbox drill-down dialog |

Service: `platform-outreach.service.ts`  
Types: `platform-outreach.types.ts`

---

## Security

- All endpoints require `ROLE_SUPER_ADMIN` via `@PreAuthorize`.
- Admin storefront route is guarded by `superAdminGuard`.
- No public API — contacts are managed exclusively by SUPER_ADMIN.
- Contacts are not subscribable; there is no opt-in or confirmation flow.

---

## Known deferred issues (post-CMS-235)

The following issues were identified during code review (branch `feature/CMS-235`) but deferred to a follow-up:

**P2 — Medium priority**

- `updateContact` — `OutreachContactStatus.valueOf()` throws raw Java exception on invalid input; should return a friendly i18n message.
- `parseSortOrDefault` — no whitelist for sort field names; arbitrary field accepted.
- `getTemplates()` — no pagination and includes inactive templates; should filter `isActive=true` and paginate.
- `createCampaign` — `findByIdIn` silently drops not-found contact IDs; `totalCount` decrements without warning.
- `CreateOutreachTemplateRequest.language` — accepts any string; should be validated against `TR`/`EN`.

**P3 — Minor**

- `language` field is raw `String`; a dedicated `OutreachLanguage { TR, EN }` enum would be safer.
- `DELETE /contacts/{id}` is a hard delete semantically; UNSUBSCRIBED soft-delete pattern would be cleaner.
- Response DTOs expose internal `Long id`; opaque `uid` would be safer for public-facing APIs.

**P0 deferred — Clean Architecture**

- `PlatformOutreachService` imports presentation-layer DTOs directly. Proper fix requires domain POJO entities, port interfaces, and persistence adapters (matching the `PlatformMailMarketingService` pattern). Estimated ~20 new files — tracked as a separate refactor PR.
