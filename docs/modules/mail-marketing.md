# Mail Marketing (Tenant + Platform Newsletter)

## Purpose

Mail Marketing provides newsletter collection, campaign delivery, and transactional email flows in two scopes:

- **Tenant scope** (`mail_marketing` module): tenant-specific subscribers, template content, Postmark provider config, and transactional user welcome emails.
- **Platform scope** (control-plane): platform-level subscribers and SUPER_ADMIN campaign and transactional operations.

### Template types

| Type | Scope | Kind | Subscribable |
|------|-------|------|-------------|
| `NEWSLETTER_DEFAULT` | Both | Campaign | Yes |
| `VERSION_UPGRADE` | Both | Campaign | Yes |
| `TENANT_USER_WELCOME` | Tenant only | Transactional | No |

Campaign types deliver to active newsletter subscribers. `TENANT_USER_WELCOME` is a transactional type sent to a single recipient — not through campaign infrastructure.

Each template type has fixed language rows (`TR`, `EN`) editable by SUPER_ADMIN (platform) or TENANT_ADMIN (tenant) via the admin panel. Default content is auto-seeded on first access.

> **Note:** Demo request confirmation (`DEMO_REQUEST_CONFIRMATION`) is **not** a DB-managed template type. It is rendered directly from Thymeleaf files (`email/demo-request-confirmation-{lang}.html`) and is not editable via the admin panel.

Subscription relation is the source of truth for audience metadata:

- `source` is stored per `subscriber + templateType`
- `preferredLanguage` is stored per `subscriber + templateType`
- `permission` is stored per `subscriber + templateType` (campaign send allow/deny)

---

## Database

### Platform migrations

- Platform baseline: [`../../backend/src/main/resources/db/platform/V46__create_platform_mail_marketing.sql`](../../backend/src/main/resources/db/platform/V46__create_platform_mail_marketing.sql)
- Platform subscriptions + template-type backfill: [`../../backend/src/main/resources/db/platform/V47__add_platform_newsletter_subscriptions.sql`](../../backend/src/main/resources/db/platform/V47__add_platform_newsletter_subscriptions.sql)
- Platform subscription source/language model: [`../../backend/src/main/resources/db/platform/V48__add_platform_subscription_source_and_language.sql`](../../backend/src/main/resources/db/platform/V48__add_platform_subscription_source_and_language.sql)
- Platform subscription permission model: [`../../backend/src/main/resources/db/platform/V49__add_platform_subscription_permission.sql`](../../backend/src/main/resources/db/platform/V49__add_platform_subscription_permission.sql)

### Tenant migrations (`mail_marketing` module)

- Baseline: [`../../backend/src/main/resources/db/tenant/mail_marketing/V1.0.0__baseline.sql`](../../backend/src/main/resources/db/tenant/mail_marketing/V1.0.0__baseline.sql)
- Add `TENANT_USER_WELCOME` template rows: [`../../backend/src/main/resources/db/tenant/mail_marketing/V1.1.0__add_tenant_user_welcome_template.sql`](../../backend/src/main/resources/db/tenant/mail_marketing/V1.1.0__add_tenant_user_welcome_template.sql)

### Platform tables (`platform_management`)

- `platform_email_templates`
- `platform_newsletter_subscribers`
- `platform_newsletter_subscriber_subscriptions`
- `platform_mail_campaigns`
- `platform_mail_outbox`

### Tenant tables (tenant DB, module `mail_marketing`)

- `email_templates`
- `newsletter_subscribers`
- `newsletter_subscriber_subscriptions`
- `mail_provider_config`
- `mail_campaigns`
- `mail_outbox`

### Source-of-truth services

- Tenant service: [`../../backend/src/main/java/com/backend/application/service/TenantMailMarketingService.java`](../../backend/src/main/java/com/backend/application/service/TenantMailMarketingService.java)
- Platform service: [`../../backend/src/main/java/com/backend/application/service/PlatformMailMarketingService.java`](../../backend/src/main/java/com/backend/application/service/PlatformMailMarketingService.java)

### Platform entity conventions

All 5 platform mail marketing entities (`PlatformEmailTemplate`, `PlatformMailCampaign`, `PlatformMailOutbox`, `PlatformNewsletterSubscriber`, `PlatformNewsletterSubscriberSubscription`) extend `BaseEntity`, which provides:

- `id` (BIGINT AUTO_INCREMENT)
- `uuid` (CHAR 36, auto-generated)
- `uid` (VARCHAR 50, auto-generated)
- `createdAt`, `updatedAt` (auto-managed by `@PrePersist`/`@PreUpdate`)
- `createdBy`, `updatedBy` (Long — platform admin user ID, nullable)

`PlatformMailCampaign` additionally stores the email of the sender as `createdByEmail` (String). The tenant-scoped `MailCampaign` stores the user ID as `createdByUserId` (Long).

### Query performance notes

Subscriber relation list methods load `subscriber` via `@EntityGraph(attributePaths = "subscriber")` to avoid N+1:

- Platform relation repository: [`../../backend/src/main/java/com/backend/infrastructure/persistence/platform/repository/PlatformNewsletterSubscriberSubscriptionRepository.java`](../../backend/src/main/java/com/backend/infrastructure/persistence/platform/repository/PlatformNewsletterSubscriberSubscriptionRepository.java)
- Tenant relation repository: [`../../backend/src/main/java/com/backend/infrastructure/persistence/repository/NewsletterSubscriberSubscriptionJpaRepository.java`](../../backend/src/main/java/com/backend/infrastructure/persistence/repository/NewsletterSubscriberSubscriptionJpaRepository.java)

---

## Admin API

### Tenant admin

Controller: [`../../backend/src/main/java/com/backend/presentation/controller/TenantMailMarketingController.java`](../../backend/src/main/java/com/backend/presentation/controller/TenantMailMarketingController.java)

Base path: `/api/mail`

- `GET /api/mail/templates/types` — returns all fixed types including `NEWSLETTER_DEFAULT`, `VERSION_UPGRADE`, `TENANT_USER_WELCOME`
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
- `POST /api/mail/campaigns/send` — body: `{ templateType: string }`
- `GET /api/mail/campaigns/{id}`
- `GET /api/mail/campaigns?templateType=...&size=5` — recent campaign list
- `GET /api/mail/campaigns/{id}/outbox` — outbox entries for a campaign in `FAILED`, `PENDING`, or `PROCESSING` status
- `GET /api/mail/subscribers/admin/export?templateType=...` — CSV download (all subscribers)

### Platform admin

Controller: [`../../backend/src/main/java/com/backend/presentation/controller/PlatformMailMarketingController.java`](../../backend/src/main/java/com/backend/presentation/controller/PlatformMailMarketingController.java)

Base path: `/api/platform/mail`

- `GET /api/platform/mail/templates/types` — returns `NEWSLETTER_DEFAULT`, `VERSION_UPGRADE`
- `GET /api/platform/mail/templates/types/{templateType}`
- `PUT /api/platform/mail/templates/types/{templateType}/translations/{language}`
- `GET /api/platform/mail/subscribers?templateType=...`
- `GET /api/platform/mail/subscribers/admin?page=...&size=...&sort=...&search=...`
- `POST /api/platform/mail/subscribers/admin`
- `GET /api/platform/mail/subscribers/admin/{id}`
- `PUT /api/platform/mail/subscribers/admin/{id}`
- `DELETE /api/platform/mail/subscribers/admin/{id}` (soft delete: status + permission revocation)
- `POST /api/platform/mail/campaigns/send` — body: `{ templateType: string }`
- `GET /api/platform/mail/campaigns/{id}`
- `GET /api/platform/mail/campaigns?templateType=...&size=5` — recent campaign list
- `GET /api/platform/mail/campaigns/{id}/outbox` — outbox entries for a campaign in `FAILED`, `PENDING`, or `PROCESSING` status
- `GET /api/platform/mail/subscribers/admin/export?templateType=...` — CSV download (all subscribers)

### Campaign send behavior

- `templateType` identifies the template group (e.g. `NEWSLETTER_DEFAULT`).
- Backend loads all active translation rows for the template type.
- Recipients filtered by: `status = ACTIVE`, matching `template_key`, `permission = TRUE`.
- Delivery language resolved per subscriber from `preferredLanguage` on the subscription relation → EN fallback → first active translation.
- Admin no longer selects a language — the backend resolves the correct language for each subscriber automatically.
- `TENANT_USER_WELCOME` is a transactional type — it is **not** subscribable and **not** sent via campaign infrastructure.

---

## Public delivery APIs

### Platform public newsletter

Controller: [`../../backend/src/main/java/com/backend/presentation/controller/PlatformPublicNewsletterController.java`](../../backend/src/main/java/com/backend/presentation/controller/PlatformPublicNewsletterController.java)

Base path: `/api/platform/public/newsletter`

- `POST /api/platform/public/newsletter/subscribe`
  - Body: `{ email, source?, templateType, locale?, honeypot?, formStartedAt? }`
  - `templateType` must be a subscribable type (`NEWSLETTER_DEFAULT`, `VERSION_UPGRADE`).
  - `locale` drives `preferredLanguage` (`TR`/`EN`) on the subscription relation and the language of the confirmation email.
  - `honeypot` and `formStartedAt` are lightweight anti-bot inputs used before persistence.
  - Success and handled business errors are returned as localized backend `ApiResponse.message`; storefront shows this message directly.
  - Sends HTML confirmation email (`newsletter-confirm-tr.html` / `newsletter-confirm-en.html`).
- `GET /api/platform/public/newsletter/confirm?token=...` — activates subscriber (status → `ACTIVE`)
- `POST /api/platform/public/newsletter/unsubscribe` — soft unsubscribe via token

### Tenant public newsletter

Controller: [`../../backend/src/main/java/com/backend/presentation/controller/TenantPublicNewsletterController.java`](../../backend/src/main/java/com/backend/presentation/controller/TenantPublicNewsletterController.java)

Base path: `/api/public/newsletter`

- `POST /api/public/newsletter/subscribe` — body: `{ email, source?, templateType }`
- `GET /api/public/newsletter/confirm?token=...`
- `POST /api/public/newsletter/unsubscribe`

---

## Transactional email flows

### Demo request confirmation (platform)

Triggered automatically when a landing page demo request is submitted.

- Entry point: [`../../backend/src/main/java/com/backend/application/service/impl/PlatformDemoRequestServiceImpl.java`](../../backend/src/main/java/com/backend/application/service/impl/PlatformDemoRequestServiceImpl.java) — `submit()` calls `PlatformMailMarketingService.sendDemoRequestConfirmation()` after DB save.
- Rendered from Thymeleaf: `email/demo-request-confirmation-tr.html` / `email/demo-request-confirmation-en.html`.
- Template variable: `${name}` (full name of the requester).
- Language resolved from persisted `entity.getLocale()` (normalized at save time).
- Confirmation mail send is triggered in `afterCommit`; DB save is not rolled back by mail delivery failure.

### TENANT_USER_WELCOME (tenant)

Triggered when a new tenant user account is created (activation email).

- Entry point: [`../../backend/src/main/java/com/backend/application/service/impl/EmailServiceImpl.java`](../../backend/src/main/java/com/backend/application/service/impl/EmailServiceImpl.java) — `sendEmailVerificationEmail()`.
- If `tenantContext.isSet()` **and** tenant module `MAIL_MARKETING` is enabled: looks up active `TENANT_USER_WELCOME` template in `email_templates`.
  - Found and active → renders via `TemplateVariableRenderer`, sends via tenant mail routing (`TenantMailMarketingService`).
  - Not found or inactive → renders system Thymeleaf template (`email-verify-tr.html` / `email-verify-en.html`) and still sends via tenant mail routing.
- Template content: TENANT_ADMIN edits via `/:lang/mail-marketing → TENANT_USER_WELCOME`.
- Template variables: `{{name}}`, `{{verificationLink}}`, `{{expiryHours}}`

### VERSION_UPGRADE auto-subscribe on provisioning

When tenant provisioning completes successfully, the tenant's `adminEmail` is automatically added as an `ACTIVE` platform subscriber of `VERSION_UPGRADE`.

- Entry point: [`../../backend/src/main/java/com/backend/application/service/AsyncProvisioningExecutor.java`](../../backend/src/main/java/com/backend/application/service/AsyncProvisioningExecutor.java) — `autoSubscribeTenantAdminToVersionUpdates()` called after `updateTenantStatus(ACTIVE)`.
- Calls `PlatformMailMarketingService.autoSubscribeTenantAdmin(email, preferredLanguage)`.
- `preferredLanguage` resolved from `tenant.defaultLanguage`.
- Subscription source set to `"TENANT_ONBOARDING"`.
- Failure is caught and logged; it does not fail the provisioning job.
- SUPER_ADMIN can then send VERSION_UPGRADE campaigns to reach all provisioned tenant admins.

---

## Email HTML templates

System-level Thymeleaf HTML templates (code-only, not admin-configurable):

| File | Purpose | Variables |
|------|---------|-----------|
| `email-verify-tr.html` / `en.html` | Fallback activation email (no custom template) | `verificationLink`, `expiryHours` |
| `login-otp-tr.html` / `en.html` | OTP code | `otpCode`, `expiryMinutes` |
| `password-reset-tr.html` / `en.html` | Password reset link | `resetLink`, `expiryHours` |
| `newsletter-confirm-tr.html` / `en.html` | Newsletter subscription confirmation | `confirmLink` |

All templates also receive `fromName` automatically from `MailConfigPort` (resolved via `GlobalRuntimeConfigService` → `app.email.from-name`).

Template location: `backend/src/main/resources/templates/email/`

Renderer: [`../../backend/src/main/java/com/backend/infrastructure/email/EmailTemplateRenderer.java`](../../backend/src/main/java/com/backend/infrastructure/email/EmailTemplateRenderer.java) implements [`../../backend/src/main/java/com/backend/domain/port/EmailTemplateRendererPort.java`](../../backend/src/main/java/com/backend/domain/port/EmailTemplateRendererPort.java).

`EmailTemplateRendererPort` exposes two render methods:
- `render(EmailContext)` — for system emails (uses `EmailType` to resolve template name)
- `render(String templateName, Map<String, Object> variables, Language language)` — for direct template rendering (used by newsletter confirm flow)

DB-stored templates (`platform_email_templates`, `email_templates`) are rendered via [`../../backend/src/main/java/com/backend/application/service/mail/TemplateVariableRenderer.java`](../../backend/src/main/java/com/backend/application/service/mail/TemplateVariableRenderer.java) using `{{key}}` syntax.

---

## Frontend integration

### Admin storefront

API endpoint keys:

- Tenant: `mailTemplateTypes`, `mailTemplateTypeDetail`, `mailTemplateTypeTranslation`, `mailSubscribers`, `mailProviderConfig`, `mailCampaignSend`, `mailCampaignById`, `mailCampaignList`, `mailCampaignOutbox`, `mailSubscribersExport`
- Platform: `platformMailTemplateTypes`, `platformMailTemplateTypeDetail`, `platformMailTemplateTypeTranslation`, `platformMailSubscribers`, `platformMailCampaignSend`, `platformMailCampaignById`, `platformMailCampaignList`, `platformMailCampaignOutbox`, `platformMailSubscribersExport`
- File: [`../../storefront/src/app/modules/admin/api-endpoints.ts`](../../storefront/src/app/modules/admin/api-endpoints.ts)

Admin routes:

- Tenant: `/:lang/mail-marketing`
  - List: [`../../storefront/src/app/modules/admin/custom/mail-marketing/tenant-mail-template-list.component.ts`](../../storefront/src/app/modules/admin/custom/mail-marketing/tenant-mail-template-list.component.ts)
  - Detail: [`../../storefront/src/app/modules/admin/custom/mail-marketing/tenant-mail-marketing.component.ts`](../../storefront/src/app/modules/admin/custom/mail-marketing/tenant-mail-marketing.component.ts)
- Platform: `/:lang/platform-mail`
  - List: [`../../storefront/src/app/modules/admin/custom/mail-marketing/platform-mail-template-list.component.ts`](../../storefront/src/app/modules/admin/custom/mail-marketing/platform-mail-template-list.component.ts)
  - Detail: [`../../storefront/src/app/modules/admin/custom/mail-marketing/platform-mail-marketing.component.ts`](../../storefront/src/app/modules/admin/custom/mail-marketing/platform-mail-marketing.component.ts)

Detail page sidebar contains three cards:

1. **Send Campaign** — single button; no language selection (backend resolves per subscriber).
2. **Last Campaign** — status badge, progress bar, sent/failed stats, refresh button.
3. **Campaign History** — last 5 campaigns as clickable rows. Each row shows status, date, and sent/failed counts. Clicking opens the Outbox Dialog.

**Outbox Dialog** ([`mail-campaign-outbox-dialog.component.ts`](../../storefront/src/app/modules/admin/custom/mail-marketing/mail-campaign-outbox-dialog.component.ts)): opens on campaign row click, calls `GET /campaigns/{id}/outbox`, and lists non-final entries (`PENDING`/`PROCESSING`) plus failed entries (`FAILED`) with status and details. Accepts `getCampaignOutbox` as a function via `MAT_DIALOG_DATA` — keeping the dialog scope-agnostic (platform/tenant).

**Subscriber list** ([`mail-subscriber-list.component.ts`](../../storefront/src/app/modules/admin/custom/mail-marketing/subscribers/mail-subscriber-list.component.ts)): header actions include an "Export CSV" button that calls `GET /subscribers/admin/export`, downloads the response blob as a `.csv` file using a temporary `<a>` element.

### Landing page (newsletter signup)

Newsletter subscription section added between FAQ and CTA banner.

- Component: [`../../landing/components/sections/NewsletterSection.tsx`](../../landing/components/sections/NewsletterSection.tsx)
- API function: `subscribePlatformNewsletter(email, locale, honeypot, formStartedAt)` in [`../../landing/lib/platform-api.ts`](../../landing/lib/platform-api.ts)
  - `POST /api/platform/public/newsletter/subscribe` with `templateType: "NEWSLETTER_DEFAULT"`, `source: "LANDING_NEWSLETTER"`, `honeypot`, `formStartedAt`
  - Backend blocks suspicious submits when the hidden honeypot field is filled or the form arrives unrealistically fast after render.
  - Edge protection: Traefik rate limit is applied to the public subscribe endpoint.
  - Submit success/error text comes from backend `ApiResponse.message`; landing content keeps only client-originated fallback copy (for example network failure).
- Content: `newsletter` key in [`../../landing/content/home.en.json`](../../landing/content/home.en.json) and [`../../landing/content/home.tr.json`](../../landing/content/home.tr.json)
- Error class: `NewsletterSubscribeError` (exported from `platform-api.ts`)

---

## Security & tenant isolation

- Tenant admin APIs require `ROLE_TENANT_ADMIN`.
- Platform admin APIs require `ROLE_SUPER_ADMIN`.
- Tenant UI route uses `tenantAdminGuard + moduleGuard(requiredModule=mail_marketing)`.
- Platform UI route uses `superAdminGuard`.
- Tenant public newsletter APIs are tenant-scoped by `TenantFilter`.
- Platform public newsletter APIs are explicitly excluded from tenant scoping.
- `TENANT_USER_WELCOME` cannot be subscribed to via the public API (`SUBSCRIBABLE_TEMPLATE_TYPES` constant excludes it). Subscription attempts return `400 mail.marketing.template.type.invalid`.

### Provider behavior

**Platform mail** (OTP, password reset, email verify, newsletter confirm, demo request confirmation) routing is controlled by `app.email.provider` in global config (`/api/config/admin/global/properties`):

| Value | Behavior |
|-------|----------|
| `console` | Logs non-PII delivery metadata (`messageId`, `channel`, `status`) — no real delivery (default) |
| `postmark` | Sends via `TenantPostmarkEmailSender` using `app.email.postmark.server-token` (secret/encrypted) |

If `provider=postmark` but token is not set, falls back to console with a WARN log.

**Tenant mail** (campaigns, newsletter confirm, `TENANT_USER_WELCOME` when `MAIL_MARKETING` module is enabled) routing is controlled exclusively by tenant's own `mail_provider_config` table — independent of platform global config:

| `provider` field | `is_active` | Behavior |
|-----------------|-------------|----------|
| `CONSOLE` | any | Console fallback |
| `POSTMARK` | `false` | Console fallback |
| `POSTMARK` | `true` + token set | Sends via Postmark with tenant's server token |

New tenants default to `CONSOLE`. Tenant admin sets `provider=POSTMARK` + token via `PUT /api/mail/provider-config`.

**Console summary logging** — every mail flow logs relevant data at the service layer regardless of provider:

```text
[MAIL] otp dispatch requested | recipient=use****er@example.com
[MAIL] password-reset dispatch requested | recipient=use****er@example.com
[MAIL] email-verify dispatch requested | recipient=use****er@example.com
[MAIL] newsletter-confirm dispatch requested | recipient=use****er@example.com
[MAIL] demo-request-confirm dispatch requested | recipient=use****er@example.com
[MAIL] TENANT_USER_WELCOME dispatch requested | recipient=use****er@example.com | vars=[name, verificationLink, expiryHours]
[MAIL:CONSOLE] messageId=... channel=console status=accepted
```

### Language assignment

- Tenant subscribe: `preferredLanguage = tenant.defaultLanguage` (TR or EN fallback)
- Platform subscribe: `preferredLanguage` resolved from `locale` request field; defaults to `EN`
- Changing tenant default language does not rewrite existing subscription language

---

## Implementation guide

### 1) Edit a template type from admin panel

1. Open `/:lang/mail-marketing` (tenant) or `/:lang/platform-mail` (platform).
2. List page shows all fixed template types with language coverage and subscriber count.
3. Open a row to detail and edit `TR` / `EN` subject and content independently.
4. Save via `PUT /templates/types/{templateType}/translations/{language}`.

For the `TENANT_USER_WELCOME` transactional type: the saved content is used on the next send. Changes take effect immediately — no campaign needed.

### 2) Landing newsletter subscription flow

1. Visitor enters email in `NewsletterSection` on the landing page.
2. `subscribePlatformNewsletter()` calls `POST /api/platform/public/newsletter/subscribe` with `templateType=NEWSLETTER_DEFAULT`, `source=LANDING_NEWSLETTER`, `locale`, and lightweight anti-bot fields (`honeypot`, `formStartedAt`).
3. Backend rejects suspicious requests (filled honeypot or too-fast submit) before touching subscriber persistence.
4. Traefik also rate-limits the public subscribe endpoint per source IP.
5. Valid requests create or update the `NEWSLETTER_DEFAULT` relation with `preferredLanguage` from locale and `permission=false`, then send the HTML confirmation email (`newsletter-confirm-{lang}.html`) via platform mail sender.
6. If the email is already actively subscribed to `NEWSLETTER_DEFAULT`, the endpoint is idempotent: no duplicate relation is created and no new confirmation mail is sent.
7. Visitor clicks confirm link → `GET /api/platform/public/newsletter/confirm?token=...` → subscriber status becomes `ACTIVE` and pending newsletter relations are activated (`permission=true`).
8. Storefront displays the localized backend `ApiResponse.message` for handled submit outcomes; frontend copy is reserved for client-only failures.
9. Subscriber is now included in future `NEWSLETTER_DEFAULT` campaign recipients.

### 3) Demo request → automatic confirmation mail

1. Landing form submits to `POST /api/platform/public/demo-requests`.
2. `PlatformDemoRequestServiceImpl.submit()` saves the request to DB.
3. `PlatformMailMarketingService.sendDemoRequestConfirmation(email, fullName, locale)` is registered in `afterCommit` and runs only after DB commit.
4. Renders `email/demo-request-confirmation-{lang}.html` via Thymeleaf with `${name}` variable.
5. Sends via platform mail sender. Mail delivery does not affect request persistence because send runs post-commit.

### 4) Tenant user welcome with custom template

1. Admin creates a new user → system triggers `EmailServiceImpl.sendEmailVerificationEmail()`.
2. Service checks `tenantContext.isSet()` and module enablement (`MAIL_MARKETING`). If enabled in tenant context:
   - Looks up `TENANT_USER_WELCOME` in `email_templates` for the resolved language.
   - If active template found → renders `{{name}}`, `{{verificationLink}}`, `{{expiryHours}}` and sends.
   - If not found → falls back to system `email-verify-{lang}.html` Thymeleaf template.
3. Tenant admin customizes the template from `/:lang/mail-marketing → TENANT_USER_WELCOME`.

### 5) VERSION_UPGRADE campaign to all tenant admins

1. On tenant provisioning success, `AsyncProvisioningExecutor` calls `autoSubscribeTenantAdmin(tenant.adminEmail, preferredLanguage)`.
2. Admin email is added as `ACTIVE` platform subscriber of `VERSION_UPGRADE` (source: `TENANT_ONBOARDING`).
3. SUPER_ADMIN opens `/:lang/platform-mail → VERSION_UPGRADE`, edits release notes (TR/EN), clicks Send Campaign.
4. Backend reads `templateType=VERSION_UPGRADE`, finds all active translations, resolves the correct language per subscriber.
5. All provisioned tenant admin emails receive the version announcement in their preferred language.

### 6) Campaign history and outbox drill-down

1. Admin opens a template detail page — the Campaign History card automatically loads the last 5 campaigns via `GET /campaigns?templateType=X&size=5`.
2. Each campaign row shows status badge, date, and sent/failed counts.
3. Admin clicks a row → Outbox Dialog opens, fetches `GET /campaigns/{id}/outbox`.
4. Dialog lists all FAILED outbox entries (email + error message). If no failures, shows a success empty state.

### 7) Subscriber CSV export

1. Admin opens the subscriber list for any scope (tenant or platform).
2. Clicks "Export CSV" in the page header.
3. Frontend calls `GET /subscribers/admin/export` (optionally with `?templateType=...`).
4. Backend streams a `text/csv` response; frontend downloads it as `subscribers.csv` via a temporary `<a>` element.
