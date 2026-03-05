# Mail Marketing (Tenant + Platform Newsletter)

## Purpose

Mail Marketing provides newsletter collection and campaign delivery flows in two scopes:

- **Tenant scope** (`mail_marketing` module): tenant-specific subscribers, tenant template content, tenant provider config
- **Platform scope** (control-plane): platform-level subscribers and SUPER_ADMIN campaign operations

Template management is **template-type based** in first phase:

- `NEWSLETTER_DEFAULT`
- `VERSION_UPGRADE`

Each template type has fixed language rows (`TR`, `EN`) and campaigns target only subscribers related to that template type.

Subscription relation is the source of truth for audience metadata:

- `source` is stored per `subscriber + templateType`
- `preferredLanguage` is stored per `subscriber + templateType`
- `permission` is stored per `subscriber + templateType` (campaign send allow/deny)

## Database

Migrations:

- Platform baseline: [`../../backend/src/main/resources/db/platform/V46__create_platform_mail_marketing.sql`](../../backend/src/main/resources/db/platform/V46__create_platform_mail_marketing.sql)
- Platform subscriptions + template-type backfill: [`../../backend/src/main/resources/db/platform/V47__add_platform_newsletter_subscriptions.sql`](../../backend/src/main/resources/db/platform/V47__add_platform_newsletter_subscriptions.sql)
- Platform subscription source/language model: [`../../backend/src/main/resources/db/platform/V48__add_platform_subscription_source_and_language.sql`](../../backend/src/main/resources/db/platform/V48__add_platform_subscription_source_and_language.sql)
- Platform subscription permission model: [`../../backend/src/main/resources/db/platform/V49__add_platform_subscription_permission.sql`](../../backend/src/main/resources/db/platform/V49__add_platform_subscription_permission.sql)
- Tenant baseline: [`../../backend/src/main/resources/db/tenant/mail_marketing/V1__baseline.sql`](../../backend/src/main/resources/db/tenant/mail_marketing/V1__baseline.sql)
- Tenant subscriptions + template-type backfill: [`../../backend/src/main/resources/db/tenant/mail_marketing/V2__add_newsletter_subscriptions.sql`](../../backend/src/main/resources/db/tenant/mail_marketing/V2__add_newsletter_subscriptions.sql)
- Tenant subscription source/language model: [`../../backend/src/main/resources/db/tenant/mail_marketing/V3__add_subscription_source_and_language.sql`](../../backend/src/main/resources/db/tenant/mail_marketing/V3__add_subscription_source_and_language.sql)
- Tenant language correction backfill: [`../../backend/src/main/resources/db/tenant/mail_marketing/V4__fix_subscription_language_from_tenant_default.sql`](../../backend/src/main/resources/db/tenant/mail_marketing/V4__fix_subscription_language_from_tenant_default.sql)
- Tenant subscription permission model: [`../../backend/src/main/resources/db/tenant/mail_marketing/V5__add_subscription_permission.sql`](../../backend/src/main/resources/db/tenant/mail_marketing/V5__add_subscription_permission.sql)

Reference ImpEx scripts:

- Tenant sample data: [`../../backend/src/main/resources/impex/seed_mail_marketing_tenant.sql`](../../backend/src/main/resources/impex/seed_mail_marketing_tenant.sql)
- Platform sample data: [`../../backend/src/main/resources/impex/seed_mail_marketing_platform.sql`](../../backend/src/main/resources/impex/seed_mail_marketing_platform.sql)

Platform tables (`platform_management`):

- `platform_email_templates`
- `platform_newsletter_subscribers`
- `platform_newsletter_subscriber_subscriptions`
- `platform_mail_campaigns`
- `platform_mail_outbox`

Tenant tables (tenant DB, module `mail_marketing`):

- `email_templates`
- `newsletter_subscribers`
- `newsletter_subscriber_subscriptions`
- `mail_provider_config`
- `mail_campaigns`
- `mail_outbox`

Source-of-truth services/entities:

- Tenant service: [`../../backend/src/main/java/com/backend/application/service/TenantMailMarketingService.java`](../../backend/src/main/java/com/backend/application/service/TenantMailMarketingService.java)
- Platform service: [`../../backend/src/main/java/com/backend/application/service/PlatformMailMarketingService.java`](../../backend/src/main/java/com/backend/application/service/PlatformMailMarketingService.java)
- Tenant subscriber relation entity: [`../../backend/src/main/java/com/backend/domain/entity/NewsletterSubscriberSubscription.java`](../../backend/src/main/java/com/backend/domain/entity/NewsletterSubscriberSubscription.java)
- Platform subscriber relation entity: [`../../backend/src/main/java/com/backend/infrastructure/persistence/platform/entity/PlatformNewsletterSubscriberSubscription.java`](../../backend/src/main/java/com/backend/infrastructure/persistence/platform/entity/PlatformNewsletterSubscriberSubscription.java)
- Platform entity↔domain mapper: [`../../backend/src/main/java/com/backend/infrastructure/persistence/platform/mapper/PlatformMailMapper.java`](../../backend/src/main/java/com/backend/infrastructure/persistence/platform/mapper/PlatformMailMapper.java)

### Platform entity conventions

All 5 platform mail marketing entities (`PlatformEmailTemplate`, `PlatformMailCampaign`, `PlatformMailOutbox`, `PlatformNewsletterSubscriber`, `PlatformNewsletterSubscriberSubscription`) extend `BaseEntity`, which provides:

- `id` (BIGINT AUTO_INCREMENT)
- `uuid` (CHAR 36, auto-generated)
- `uid` (VARCHAR 50, auto-generated)
- `createdAt`, `updatedAt` (auto-managed by `@PrePersist`/`@PreUpdate`)
- `createdBy`, `updatedBy` (Long — platform admin user ID, nullable)

`PlatformMailCampaign` additionally stores the email of the sender as `createdByEmail` (String, `created_by_email` column) — distinct from the inherited `createdBy` (Long user ID). The tenant-scoped `MailCampaign` stores the user ID as `createdByUserId` (Long, `created_by` column).

`PlatformNewsletterSubscriber` and `PlatformNewsletterSubscriberSubscription` have a bidirectional `@OneToMany`/`@ManyToOne` relationship. Both entities use `@ToString(exclude=...)` to prevent `StackOverflowError` in Lombok-generated `toString()`.

`preferredLanguage` defaults to `Language.EN.name()` (`"EN"`), consistent with the platform subscribe flow.

## Admin API

Tenant admin controller: [`../../backend/src/main/java/com/backend/presentation/controller/TenantMailMarketingController.java`](../../backend/src/main/java/com/backend/presentation/controller/TenantMailMarketingController.java)

Base path: `/api/mail`

- `GET /api/mail/templates/types`
- `GET /api/mail/templates/types/{templateType}`
- `PUT /api/mail/templates/types/{templateType}/translations/{language}`
- `GET /api/mail/subscribers?templateType=...`
- `GET /api/mail/subscribers/admin?page=...&size=...&sort=...&search=...`
- `POST /api/mail/subscribers/admin`
- `GET /api/mail/subscribers/admin/{id}`
- `PUT /api/mail/subscribers/admin/{id}`
- `DELETE /api/mail/subscribers/admin/{id}` (soft delete: status + permission revocation)
- `GET /api/mail/provider-config`
- `PUT /api/mail/provider-config`
- `POST /api/mail/campaigns/send`
- `GET /api/mail/campaigns/{id}`

Platform admin controller: [`../../backend/src/main/java/com/backend/presentation/controller/PlatformMailMarketingController.java`](../../backend/src/main/java/com/backend/presentation/controller/PlatformMailMarketingController.java)

Base path: `/api/platform/mail`

- `GET /api/platform/mail/templates/types`
- `GET /api/platform/mail/templates/types/{templateType}`
- `PUT /api/platform/mail/templates/types/{templateType}/translations/{language}`
- `GET /api/platform/mail/subscribers?templateType=...`
- `GET /api/platform/mail/subscribers/admin?page=...&size=...&sort=...&search=...`
- `POST /api/platform/mail/subscribers/admin`
- `GET /api/platform/mail/subscribers/admin/{id}`
- `PUT /api/platform/mail/subscribers/admin/{id}`
- `DELETE /api/platform/mail/subscribers/admin/{id}` (soft delete: status + permission revocation)
- `POST /api/platform/mail/campaigns/send`
- `GET /api/platform/mail/campaigns/{id}`

Campaign send behavior:

- `templateId` points to a concrete translation row (`TR` or `EN`) under a template type.
- Recipient query is filtered by:
  - subscriber `status = ACTIVE`
  - subscriber-template relation `template_key = selected template type`
  - subscriber-template relation `permission = TRUE`
- Delivery language is resolved from relation `preferredLanguage`:
  - try preferred language translation (`TR`/`EN`)
  - fallback to `EN`
  - fallback to selected template row

## Public delivery APIs

Tenant public newsletter controller: [`../../backend/src/main/java/com/backend/presentation/controller/TenantPublicNewsletterController.java`](../../backend/src/main/java/com/backend/presentation/controller/TenantPublicNewsletterController.java)

Base path: `/api/public/newsletter`

- `POST /api/public/newsletter/subscribe` (requires `email`, optional `source`, required `templateType`)
- `GET /api/public/newsletter/confirm?token=...`
- `POST /api/public/newsletter/unsubscribe`

Platform public newsletter controller: [`../../backend/src/main/java/com/backend/presentation/controller/PlatformPublicNewsletterController.java`](../../backend/src/main/java/com/backend/presentation/controller/PlatformPublicNewsletterController.java)

Base path: `/api/platform/public/newsletter`

- `POST /api/platform/public/newsletter/subscribe` (requires `email`, optional `source`, required `templateType`)
- `GET /api/platform/public/newsletter/confirm?token=...`
- `POST /api/platform/public/newsletter/unsubscribe`

## Frontend integration

API endpoint keys:

- Tenant: `mailTemplateTypes`, `mailTemplateTypeDetail`, `mailTemplateTypeTranslation`, `mailSubscribers`, `mailProviderConfig`, `mailCampaignSend`, `mailCampaignById`
- Platform: `platformMailTemplateTypes`, `platformMailTemplateTypeDetail`, `platformMailTemplateTypeTranslation`, `platformMailSubscribers`, `platformMailCampaignSend`, `platformMailCampaignById`
- File: [`../../storefront/src/app/modules/admin/api-endpoints.ts`](../../storefront/src/app/modules/admin/api-endpoints.ts)

Admin routes:

- Tenant route: `/:lang/mail-marketing`
  - List route: [`../../storefront/src/app/modules/admin/custom/mail-marketing/tenant-mail-template-list.component.ts`](../../storefront/src/app/modules/admin/custom/mail-marketing/tenant-mail-template-list.component.ts)
  - Detail route: [`../../storefront/src/app/modules/admin/custom/mail-marketing/tenant-mail-marketing.component.ts`](../../storefront/src/app/modules/admin/custom/mail-marketing/tenant-mail-marketing.component.ts)
- Platform route: `/:lang/platform-mail`
  - List route: [`../../storefront/src/app/modules/admin/custom/mail-marketing/platform-mail-template-list.component.ts`](../../storefront/src/app/modules/admin/custom/mail-marketing/platform-mail-template-list.component.ts)
  - Detail route: [`../../storefront/src/app/modules/admin/custom/mail-marketing/platform-mail-marketing.component.ts`](../../storefront/src/app/modules/admin/custom/mail-marketing/platform-mail-marketing.component.ts)

List/detail route modules:

- Tenant: [`../../storefront/src/app/modules/admin/custom/mail-marketing/tenant-mail-marketing.routes.ts`](../../storefront/src/app/modules/admin/custom/mail-marketing/tenant-mail-marketing.routes.ts)
- Platform: [`../../storefront/src/app/modules/admin/custom/mail-marketing/platform-mail.routes.ts`](../../storefront/src/app/modules/admin/custom/mail-marketing/platform-mail.routes.ts)

## Security & tenant isolation

- Tenant admin APIs require `ROLE_TENANT_ADMIN`.
- Platform admin APIs require `ROLE_SUPER_ADMIN`.
- Tenant UI route uses `tenantAdminGuard + moduleGuard(requiredModule=mail_marketing)`.
- Platform UI route uses `superAdminGuard`.
- Tenant public newsletter APIs are tenant-scoped by `TenantFilter`.
- Platform public newsletter APIs are explicitly excluded from tenant scoping in security/tenant filter configuration.

Provider behavior:

- `app.email.provider=console`: mail is bypassed to console sender (dev/stage workflow).
- Non-console: tenant campaign sends require active tenant provider config (`mail_provider_config`) and encrypted tenant Postmark token.
- No provider fallback from tenant to platform sender (fail-closed).

Language assignment policy:

- Tenant subscribe:
  - if tenant `defaultLanguage == TR` => relation `preferredLanguage = TR`
  - else => relation `preferredLanguage = EN`
- Platform subscribe:
  - relation `preferredLanguage = EN`
- Snapshot rule:
  - changing tenant default language does not rewrite existing subscription language
  - only new/updated subscription events use the current default rule

## Implementation guide

### 1) Template-type management from admin grid

1. Open `/:lang/mail-marketing` (tenant) or `/:lang/platform-mail` (platform).
2. List page shows fixed template types (`NEWSLETTER_DEFAULT`, `VERSION_UPGRADE`) with language coverage and subscriber count.
3. Open a row to detail and edit `TR` / `EN` content independently.
4. Save translation via `/templates/types/{templateType}/translations/{language}`.

### 2) Subscriber collection with template relation

1. Public client calls `POST .../newsletter/subscribe` with `templateType`.
2. Backend upserts subscriber and ensures `subscriber <-> template_key` relation row.
3. Subscriber confirms token, status becomes `ACTIVE`.
4. Subscriber appears in template detail lists when querying the same template type.

### 2.1) Subscriber admin CRUD (tenant + platform)

1. Open `/:lang/mail-marketing/subscribers` (tenant) or `/:lang/platform-mail/subscribers` (platform).
2. List page supports server-side pagination/sort/search.
3. Create/edit supports multiple template bindings in one dialog:
   - `templateType`
   - `preferredLanguage`
   - `source`
   - `permission`
4. Email is immutable in edit mode.
5. Delete is soft-delete (`UNSUBSCRIBED` + all binding permissions set to `false`).

### 3) Campaign send by language and template type

1. Admin selects template type detail and triggers send on `TR` or `EN` row.
2. Backend creates campaign/outbox entries and resolves recipients by relation table + `ACTIVE` status.
3. `{{name}}`, `{{email}}`, `{{unsubscribeUrl}}` placeholders are rendered.
4. Campaign counters are read from `GET /campaigns/{id}` for status refresh.
