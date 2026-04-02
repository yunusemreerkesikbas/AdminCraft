# Environment Configuration

Multi-environment setup for Craftive with dev, stage, and prod configurations.

## Source of Truth

### Backend (Spring Boot)

| File                                                     | Purpose                              |
| -------------------------------------------------------- | ------------------------------------ |
| `../../backend/src/main/resources/application.yml`       | Base configuration (shared settings) |
| `../../backend/src/main/resources/application-dev.yml`   | Development overrides                |
| `../../backend/src/main/resources/application-stage.yml` | Staging overrides                    |
| `../../backend/src/main/resources/application-prod.yml`  | Production overrides                 |

### Frontend — Admin (Angular)

| File                                                     | Purpose              |
| -------------------------------------------------------- | -------------------- |
| `../../storefront/src/environments/environment.ts`       | Development config   |
| `../../storefront/src/environments/environment.stage.ts` | Staging config       |
| `../../storefront/src/environments/environment.prod.ts`  | Production config    |
| `../../storefront/angular.json`                          | Build configurations |

### Frontend — Headless Storefront (Next.js)

| File                                       | Purpose                                        |
| ------------------------------------------ | ---------------------------------------------- |
| `../../storefront-nextjs/.env.development` | Development config (loaded by `next dev`)      |
| `../../storefront-nextjs/.env.staging`     | Staging config (loaded via `dotenv-cli`)       |
| `../../storefront-nextjs/.env.production`  | Production config (loaded by `next build`)     |
| `../../storefront-nextjs/.env.local`       | Local overrides — highest priority, gitignored |

## Rules and Invariants

### Backend

1. **Profile hierarchy**: `application.yml` loads first, then `application-{profile}.yml` overrides
2. **Default profile**: `dev` (set via `SPRING_PROFILES_ACTIVE` env var)
3. **Credentials**: Dev uses defaults, Stage/Prod require environment variables
4. **No defaults for sensitive values in Stage/Prod**: `JWT_SECRET`, `DB_USERNAME`, `DB_PASSWORD` must be set
5. **Reverse proxy / client IP**: Base `application.yml` sets `server.forward-headers-strategy: framework` so `Forwarded` / `X-Forwarded-*` are honored and `HttpServletRequest.getRemoteAddr()` reflects the client when Traefik, Cloudflare, or similar sets those headers. Ensure the proxy overwrites or sanitizes `X-Forwarded-For` to prevent spoofing on direct-to-app access.

#### Tenant runtime configuration (Config Control Panel)

Some settings are **tenant-specific** and must be editable at runtime (e.g. reCAPTCHA recovery settings used by `/config`).

- **Do NOT** store tenant runtime settings in `application.yml` or `application-{profile}.yml`
- Store them in the **tenant database** (key-value store): `config_properties`
- `application.yml` may still define safe **defaults** (used only when the tenant key is missing)

#### Global runtime configuration (Config Control Panel)

Some settings are **global runtime overrides** managed by `CONFIG_SUPER_ADMIN` in `/config`.

- Store them in platform DB key-value store: `platform_config_properties`
- Current whitelist:
  - `app.email.provider`
  - `app.email.from-address`
  - `app.email.from-name`
  - `app.frontend.base-url`
  - `platform.analytics.ga4.enabled`
  - `platform.seo.insights.enabled`
  - `platform.security.recaptcha.enabled`
  - `platform.security.recaptcha.site_key`
  - `platform.security.recaptcha.secret_key` (encrypted)
- `security.recaptcha.threshold` stays in platform settings (not runtime-managed in Config Panel)
- Resolution precedence for these keys:
  1. `platform_config_properties` override
  2. Spring `application*.yml` / environment variable value

#### Shared GA4 service account credential

GA4 dashboard reporting uses a shared backend service identity. This credential is not tenant-scoped and must not be stored in `/config`.

See also: [`../3rd-party/google-analytics-ga4.md`](../3rd-party/google-analytics-ga4.md)
See also: [`../3rd-party/google-search-console-crux-seo-insights.md`](../3rd-party/google-search-console-crux-seo-insights.md)

- tenant `/config` keys:
  - `analytics.ga4.enabled`
  - `analytics.ga4.property_id`
  - `seo.insights.enabled`
  - `seo.search_console.property_url`
- global `/config` key:
  - `platform.analytics.ga4.enabled`
  - `platform.seo.insights.enabled`
- backend environment secret:
  - `APP_ANALYTICS_GA4_SERVICE_ACCOUNT_JSON`
  - or `APP_ANALYTICS_GA4_SERVICE_ACCOUNT_JSON_BASE64`
  - `APP_SEO_CRUX_API_KEY`

Behavior by environment:

- local: set the env var in the same shell or IDE run configuration that starts Spring Boot
- stage/prod: inject the env var from deployment secrets or container environment
- all environments: the same service account can be reused for multiple tenants; each tenant GA4 property must explicitly grant that service account `Viewer` access
- all environments: the same Google service account can also be reused for Search Console; each tenant Search Console property must explicitly grant that service account access

### Frontend — Angular

1. **File replacement**: Angular replaces `environment.ts` with profile-specific file at build time
2. **Consistent structure**: All environment files must have the same properties
3. **No runtime configuration**: API URLs are baked in at build time

### Frontend — Next.js Storefront

1. **Next.js env loading order**: `.env.local` > `.env.{NODE_ENV}` > `.env`
2. **Staging**: Not a native `NODE_ENV` value — use `dotenv-cli` (`dotenv -e .env.staging -- next ...`)
3. **`.env.local` always wins**: Use this for local overrides (tenant ID, API URL, etc.) — never commit it
4. **SSR vs Static export**: Default mode is SSR (`next start`). Set `NEXT_OUTPUT=export` for static HTML export (no server required, but server-only features like `cache()` and `revalidate` are disabled)

### Frontend — Marketing landing (`landing/`)

Static export (e.g. Cloudflare Pages). Public demo/contact form calls the Craftive API from the browser.

| Variable                       | Purpose                                                                                                                                                           |
| ------------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `NEXT_PUBLIC_CRAFTIVE_API_URL` | Backend **origin only** — no `/api` suffix (e.g. `http://localhost:8080`, `https://api.example.com`). Client code appends `/api/...`. Baked in at **build** time. |

See [`landing/.env.local.example`](../../landing/.env.local.example). Contract and CORS: [`modules/platform-admin.md`](../modules/platform-admin.md) (Landing demo requests), index: [`README.md`](../README.md).

## Environment Comparison

### Backend

| Setting        | Dev      | Stage           | Prod            |
| -------------- | -------- | --------------- | --------------- |
| DB Port        | `3306`   | `3306`          | `3306`          |
| DB Credentials | defaults | env vars        | env vars        |
| JWT Secret     | default  | `${JWT_SECRET}` | `${JWT_SECRET}` |
| SQL Logging    | `true`   | `false`         | `false`         |
| Swagger UI     | enabled  | enabled         | disabled        |
| Log Level      | `DEBUG`  | `INFO`          | `INFO`          |
| Auto-sync      | `true`   | `true`          | `true`          |

### Frontend — Angular

| Setting              | Dev            | Stage                            | Prod                          |
| -------------------- | -------------- | -------------------------------- | ----------------------------- |
| `production`         | `false`        | `false`                          | `true`                        |
| `apiBaseUrl`         | `/api` (proxy) | `https://s1-api.craftive.io/api` | `https://api.craftive.io/api` |
| `apiTimeout`         | `30000`        | `30000`                          | `30000`                       |
| `supportedLanguages` | `['tr', 'en']` | `['tr', 'en']`                   | `['tr', 'en']`                |
| `defaultLanguage`    | `en`           | `en`                             | `en`                          |
| `maxRetryAttempts`   | `0`            | `0`                              | `3`                           |
| Source Maps          | yes            | yes                              | no                            |
| Optimization         | no             | yes                              | yes                           |

### Frontend — Next.js Storefront

| Variable                       | Dev                         | Stage                            | Prod                          |
| ------------------------------ | --------------------------- | -------------------------------- | ----------------------------- |
| `NEXT_PUBLIC_CMS_API_URL`      | `http://127.0.0.1:8080/api` | `https://s1-api.craftive.io/api` | `https://api.craftive.io/api` |
| `TENANT_SUBDOMAIN`             | `demo`                      | tenant subdomain                 | tenant subdomain              |
| `NEXT_PUBLIC_TENANT_SUBDOMAIN` | `demo`                      | tenant subdomain                 | tenant subdomain              |
| `TENANT_ID`                    | `28` (local tenant)         | tenant ID                        | tenant ID                     |
| `NEXT_PUBLIC_TENANT_ID`        | `28`                        | tenant ID                        | tenant ID                     |
| `TENANT_HOSTNAME`              | _(not set)_                 | `s1-demo.craftive.io`            | `demo.craftive.io`            |
| `NEXT_IMAGE_DOMAINS`           | _(not set)_                 | `s1-cdn.craftive.io`             | `media.craftive.io`           |

> **`TENANT_HOSTNAME`**: When set, `proxy.ts` validates every incoming request's `host` header against this value. Requests from other hostnames receive HTTP 404. Leave unset in local dev (all traffic from `localhost` is accepted). Required in stage/prod to prevent wildcard DNS rules from serving the wrong tenant's storefront.

Available scripts:

| Script                 | Description                                    |
| ---------------------- | ---------------------------------------------- |
| `npm run dev`          | Dev server with `.env.development`             |
| `npm run dev:stage`    | Dev server with `.env.staging`                 |
| `npm run build`        | SSR production build                           |
| `npm run build:dev`    | SSR build with `.env.development`              |
| `npm run build:stage`  | SSR build with `.env.staging`                  |
| `npm run build:prod`   | SSR production build (same as `build`)         |
| `npm run build:static` | Static export (CSR) with `.env.production`     |
| `npm run start`        | SSR production server                          |
| `npm run start:stage`  | SSR server with `.env.staging`                 |
| `npm run start:static` | Serve `out/` folder (for static export builds) |

### Language Configuration

The `supportedLanguages` and `defaultLanguage` values control the Admin UI language:

- **Used in**: `app.config.ts` (Transloco provider), `language.interceptor.ts`, `http-headers.service.ts`
- **Behavior**: On app startup, the UI language is set to `defaultLanguage`
- **Accept-Language header**: Automatically added to API requests based on active language

### Adding a New Language

1. **Environment files** - Add to `supportedLanguages`:

   ```typescript
   supportedLanguages: ['tr', 'en', 'de'],
   ```

2. **app.config.ts** - Add to `LANGUAGE_CONFIG`:

   ```typescript
   de: {
       label: 'Deutsch',
       key: 'langDE',
       loader: () => import('@modules/admin/i18n/langDE'),
   },
   ```

3. **Create translation file** - `storefront/src/app/modules/admin/i18n/langDE.ts`:

   ```typescript
   export const langDE = {
     // translations...
   };
   ```

## Common Patterns

### Running Locally

```powershell
# Backend (uses dev profile by default)
cd backend
mvn spring-boot:run

# Admin Angular storefront (uses development config)
cd storefront
npm run start:dev

# Next.js headless storefront (uses .env.development + .env.local)
cd storefront-nextjs
npm run dev
```

### Running with Specific Profile

```powershell
# Backend with stage profile
mvn spring-boot:run -Dspring-boot.run.profiles=stage

# Angular with stage config
npm run start:stage

# Next.js with stage config
npm run dev:stage
```

### Build for Production

```powershell
# Backend (JAR with prod profile at runtime)
mvn clean package -DskipTests
java -jar target/*.jar --spring.profiles.active=prod

# Angular (optimized build)
npm run build:prod

# Next.js SSR build
npm run build

# Next.js static (CSR) build
npm run build:static
```

## Required Environment Variables

### Stage/Prod Backend

| Variable                | Description                    | Required           |
| ----------------------- | ------------------------------ | ------------------ |
| `JWT_SECRET`            | JWT signing key (min 64 chars) | Yes                |
| `DB_USERNAME`           | Database username              | Yes                |
| `DB_PASSWORD`           | Database password              | Yes                |
| `DB_HOST`               | Database host                  | Yes                |
| `PLATFORM_DB_URL`       | Full JDBC URL for platform DB  | Yes                |
| `DB_PORT`               | Database port                  | No (default: 3306) |
| `APP_FRONTEND_BASE_URL` | Frontend URL pattern           | Yes                |
| `PLATFORM_DOMAIN`       | Platform domain                | Yes                |
| `EMAIL_FROM_ADDRESS`    | Default sender email           | Yes                |
| `EMAIL_FROM_NAME`       | Default sender name            | Yes                |
| `SPACES_ACCESS_KEY`     | DO Spaces access key (S3)      | Yes (stage/prod)   |
| `SPACES_SECRET_KEY`     | DO Spaces secret key (S3)      | Yes (stage/prod)   |
| `APP_ANALYTICS_GA4_SERVICE_ACCOUNT_JSON` | GA4 service account JSON content | Yes, if GA4 dashboard reporting is enabled |
| `APP_ANALYTICS_GA4_SERVICE_ACCOUNT_JSON_BASE64` | Base64 alternative for GA4 service account JSON | Optional alternative |
| `APP_SEO_CRUX_API_KEY` | CrUX History API key | Yes, if SEO insights performance snapshot is enabled |

### Stage/Prod Observability and Edge

| Variable                   | Description                            | Required |
| -------------------------- | -------------------------------------- | -------- |
| `LOG_ENV`                  | Log environment label (`stage`/`prod`) | Yes      |
| `LOG_HOST`                 | Host label for Loki                    | Yes      |
| `GRAFANA_CLOUD_LOKI_URL`   | Loki ingest URL                        | Yes      |
| `GRAFANA_CLOUD_LOKI_USER`  | Loki username / tenant                 | Yes      |
| `GRAFANA_CLOUD_LOKI_TOKEN` | Loki API token                         | Yes      |
| `DOMAIN`                   | Base platform domain                   | Yes      |
| `ACME_EMAIL`               | Let's Encrypt email                    | Yes      |
| `CF_API_TOKEN`             | Cloudflare DNS challenge token         | Yes      |

### Example Stage Deployment

```powershell
$env:JWT_SECRET = "your-64-char-secret-key-here..."
$env:DB_USERNAME = "stage_user"
$env:DB_PASSWORD = "stage_pass"
$env:DB_HOST = "stage-db.internal"
$env:PLATFORM_DB_URL = "jdbc:mysql://stage-db.internal:3306/platform_management?useSSL=true"
$env:APP_FRONTEND_BASE_URL = "https://s1-%s.craftive.io"
$env:PLATFORM_DOMAIN = "s1.craftive.io"
$env:EMAIL_FROM_ADDRESS = "noreply@craftive.io"
$env:EMAIL_FROM_NAME = "Craftive"
$env:LOG_ENV = "stage"
$env:LOG_HOST = "do-fra1-stage-01"
$env:APP_ANALYTICS_GA4_SERVICE_ACCOUNT_JSON = Get-Content -Raw "C:\\secrets\\ga4-service-account.json"

mvn spring-boot:run -Dspring-boot.run.profiles=stage
```

### Local persistence for GA4 credential

Temporary shell usage:

```powershell
$env:APP_ANALYTICS_GA4_SERVICE_ACCOUNT_JSON = Get-Content -Raw "C:\path\to\ga4-service-account.json"
```

For persistent local development, prefer one of these:

- add the variable to your IDE Run Configuration for the backend
- add the variable to your PowerShell profile if you intentionally want it available in every new shell
- use a local secret loader script that sets the env var before starting Spring Boot

Do not commit the JSON file or inline credential into tracked config files.

## Gotchas

1. **MySQL standard port (3306)**: Docker and backend both use 3306 by default. If this port is occupied by a local MySQL service, you must stop the local service.

2. **Profile file naming**: Must be exactly `application-{profile}.yml` (e.g., `application-dev.yml`)

3. **Angular proxy**: Dev mode uses `/api` which proxies to `localhost:8080` via `proxy.conf.json`

4. **No hot-reload for environment changes**: Frontend requires rebuild when environment values change

5. **Spring property override order**:
   - `application.yml` (base)
   - `application-{profile}.yml` (profile-specific)
   - Environment variables (highest priority)

For whitelisted global runtime keys managed by Config Panel, `platform_config_properties` takes precedence at runtime.

---

## Docker Multi-Environment Setup

Craftive uses Docker Compose with environment-specific override files.

### File Structure

```
Craftive/
├── docker-compose.yml          # Base config (MySQL only)
├── docker-compose.dev.yml      # Dev overrides (+ phpMyAdmin)
├── docker-compose.prod.yml     # Prod overrides (+ Backend + Admin Frontend + Demo Storefront + Traefik + Alloy)
├── docker-compose.stage.yml    # Stage overrides on top of prod
├── .env.example                # Environment template
├── .env.dev                    # Local dev values (gitignored)
├── .env.stage                  # Stage values (gitignored)
├── .env.prod                   # Production values (gitignored)
└── docker/
    ├── mysql/                  # MySQL config (existing)
    ├── backend/Dockerfile      # Spring Boot image
    ├── frontend/
    │   ├── Dockerfile          # Angular + Nginx image
    │   └── nginx.conf          # Nginx configuration
    └── storefront/
        └── Dockerfile          # Next.js demo/reference storefront image
```

### Environment Overview

| Environment    | Services                                                             | Use Case                                     |
| -------------- | -------------------------------------------------------------------- | -------------------------------------------- |
| **Dev**        | MySQL + phpMyAdmin                                                   | Local development (Backend/Frontend via IDE) |
| **Prod/Stage** | MySQL + Backend + Admin Frontend + Demo Storefront + Traefik + Alloy | VPS deployment with SSL and centralized logs |

### Local Development

Runs MySQL and phpMyAdmin. Backend and Frontend are started via IDE for hot-reload.

```powershell
# Start infrastructure
docker compose -f docker-compose.yml -f docker-compose.dev.yml --env-file .env.dev up -d

# Stop
docker compose -f docker-compose.yml -f docker-compose.dev.yml down

# View logs
docker compose -f docker-compose.yml -f docker-compose.dev.yml logs -f
```

**Access Points:**

- MySQL: `localhost:3306`
- phpMyAdmin: `http://localhost:8081`
- Backend (IDE): `http://localhost:8080`
- Frontend (IDE): `http://localhost:4200`

### Production/Stage Deployment

Runs full stack with automatic SSL via Traefik.

```powershell
# Build and start (first time or after code changes)
docker compose -f docker-compose.yml -f docker-compose.prod.yml --env-file .env.prod up -d --build

# Start (without rebuild)
docker compose -f docker-compose.yml -f docker-compose.prod.yml --env-file .env.prod up -d

# Stop
docker compose -f docker-compose.yml -f docker-compose.prod.yml down

# View logs
docker compose -f docker-compose.yml -f docker-compose.prod.yml logs -f

# Rebuild specific service
docker compose -f docker-compose.yml -f docker-compose.prod.yml --env-file .env.prod up -d --build backend
```

**Access Points (with example domain):**

- Admin Frontend: `https://app.craftive.example.com`
- Demo Storefront: `https://craftive.example.com` or `https://{tenant}.craftive.example.com`
- Backend API: `https://api.craftive.example.com`

### Environment Variables

Create `.env.dev`, `.env.stage`, and `.env.prod` from `.env.example`:

```powershell
# Copy template
cp .env.example .env.dev
cp .env.example .env.stage
cp .env.example .env.prod
```

**Required Variables:**

| Variable         | Dev             | Prod/Stage              |
| ---------------- | --------------- | ----------------------- |
| `DB_PASSWORD`    | simple password | strong password         |
| `JWT_SECRET`     | any 64+ chars   | secure random 64+ chars |
| `SPRING_PROFILE` | `dev`           | `prod` or `stage`       |
| `DOMAIN`         | -               | your domain             |
| `ACME_EMAIL`     | -               | your email (for SSL)    |
| `CF_API_TOKEN`   | -               | Cloudflare DNS token    |
| `LOG_ENV`        | -               | `stage` or `prod`       |
| `LOG_HOST`       | -               | host label for Loki     |

### Traefik Configuration

Production uses Traefik v3 for:

- Automatic HTTPS with Let's Encrypt
- HTTP to HTTPS redirect
- Reverse proxy to services

**DNS Requirements:**

- `craftive.example.com` → VPS IP
- `app.craftive.example.com` → VPS IP
- `api.craftive.example.com` → VPS IP
- `*.craftive.example.com` → VPS IP (or explicit tenant records)

### Docker Image Build Arguments

**Backend Dockerfile:**

- Uses Maven wrapper for dependency caching
- Multi-stage build (JDK for build, JRE for runtime)
- Non-root user for security

**Frontend Dockerfile:**

- `BUILD_ENV` arg controls Angular build config
- Values: `dev`, `stage`, `prod` (default: `prod`)
- Multi-stage build (Node for build, Nginx for runtime)

### Troubleshooting

**MySQL won't start:**

```powershell
# Check if port 3306 is in use
netstat -ano | findstr 3306

# If occupied by local MySQL service (mysqld.exe), stop it:
Stop-Service -Name "mysql" -Force
Set-Service -Name "mysql" -StartupType Manual
```

**Backend can't connect to MySQL:**

```powershell
# Verify MySQL is healthy
docker ps
docker logs craftive-mysql

# Wait for MySQL health check
docker compose -f docker-compose.yml -f docker-compose.prod.yml --env-file .env.prod up -d --wait
```

**SSL certificate not issued:**

- Verify DNS A records point to VPS IP
- Check Traefik logs: `docker logs craftive-traefik`
- Verify ports 80/443 are open on VPS firewall
