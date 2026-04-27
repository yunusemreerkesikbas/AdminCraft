# DevOps & Deployment

## 1. What it is / why it exists

Craftive runs two isolated environments on DigitalOcean (Frankfurt, FRA1).
Each environment has its own Droplet, Traefik reverse proxy, and Docker Compose project.
Both prod and stage use Cloudflare proxy (orange cloud). Stage uses the single-level `s1-*` subdomain convention so Cloudflare Universal SSL covers all stage services (see Gotchas).

**Platform services** (managed by Craftive): Backend API + Admin Panel.
**Tenant storefront** (`storefront-nextjs/`): Deployable boilerplate. Each tenant storefront can be deployed independently from its own repository onto the same environment droplet with isolated routing.

---

## 2. Source of truth

Prelaunch secret/config readiness checklist: [`../prelaunch.md`](../prelaunch.md)

| Concern                      | File                                                                                                                                                                     |
| ---------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Base services                | [`../../docker-compose.yml`](../../docker-compose.yml)                                                                                                                   |
| Production overrides         | [`../../docker-compose.prod.yml`](../../docker-compose.prod.yml)                                                                                                         |
| Stage overrides              | [`../../docker-compose.stage.yml`](../../docker-compose.stage.yml)                                                                                                       |
| Dev overrides                | [`../../docker-compose.dev.yml`](../../docker-compose.dev.yml)                                                                                                           |
| Next.js SSR image            | [`../../docker/storefront/Dockerfile`](../../docker/storefront/Dockerfile)                                                                                               |
| PR checks                    | [`../../.github/workflows/ci.yml`](../../.github/workflows/ci.yml)                                                                                                       |
| Stage deploy                 | [`../../.github/workflows/deploy-stage.yml`](../../.github/workflows/deploy-stage.yml)                                                                                   |
| Prod deploy                  | [`../../.github/workflows/deploy-prod.yml`](../../.github/workflows/deploy-prod.yml)                                                                                     |
| Droplet setup                | [`../../scripts/server/provision-droplet.sh`](../../scripts/server/provision-droplet.sh)                                                                                 |
| DO Spaces backup             | [`../../scripts/server/configure-rclone.sh`](../../scripts/server/configure-rclone.sh)                                                                                   |
| Compose file copy            | [`../../scripts/server/deploy-files.sh`](../../scripts/server/deploy-files.sh)                                                                                           |
| Tenant storefront deploy     | [`../../scripts/server/deploy-tenant-storefront.sh`](../../scripts/server/deploy-tenant-storefront.sh)                                                                   |
| Tenant storefront remove     | [`../../scripts/server/remove-tenant-storefront.sh`](../../scripts/server/remove-tenant-storefront.sh)                                                                   |
| CORS properties              | [`../../backend/src/main/java/com/backend/infrastructure/config/CorsProperties.java`](../../backend/src/main/java/com/backend/infrastructure/config/CorsProperties.java) |
| CORS wiring                  | [`../../backend/src/main/java/com/backend/infrastructure/config/SecurityConfig.java`](../../backend/src/main/java/com/backend/infrastructure/config/SecurityConfig.java) |
| Backend config (prod)        | [`../../backend/src/main/resources/application-prod.yml`](../../backend/src/main/resources/application-prod.yml)                                                         |
| Backend config (stage)       | [`../../backend/src/main/resources/application-stage.yml`](../../backend/src/main/resources/application-stage.yml)                                                       |
| Env template                 | [`../../.env.example`](../../.env.example)                                                                                                                               |
| Alloy config (prod)          | [`../../observability/alloy.river`](../../observability/alloy.river)                                                                                                     |
| Alloy config (local)         | [`../../observability/alloy-local.river`](../../observability/alloy-local.river)                                                                                         |
| Loki config (local)          | [`../../observability/loki-local.yml`](../../observability/loki-local.yml)                                                                                               |
| Grafana datasource provision | [`../../observability/grafana/provisioning/datasources/datasources.yml`](../../observability/grafana/provisioning/datasources/datasources.yml)                           |
| Dev observability overlay    | [`../../docker-compose.observability.yml`](../../docker-compose.observability.yml)                                                                                       |

---

## 3. Rules and invariants

- **Separate Droplet per environment.** Prod and Stage never share a host. A Stage failure cannot affect Prod.
- **Platform deploys are manual** (`workflow_dispatch`). Tenant storefront repositories may use automatic Stage deploys and manual Prod deploys.
- **Production requires reviewer approval.** The `production` GitHub Environment gate must not be bypassed.
- **Ports 3306, 8080, 3000 are never externally reachable.** UFW allows only 22, 80, 443.
- **Wildcard SSL (`*.craftive.io`) requires DNS-01.** HTTP-01 cannot issue wildcard certs; Cloudflare is the DNS provider.
- **Cloudflare SSL mode must be Full (strict) for prod.** Flexible mode breaks backend TLS validation. Stage DNS records use DNS-only (grey cloud) — Traefik serves Let's Encrypt certs directly.
- **Stage subdomains use single-level `s1-*` convention.** Cloudflare Universal SSL covers `*.craftive.io` (single level only). Stage services use `s1-api`, `s1-app`, `s1-cdn` (hyphen, single-level) so Cloudflare proxy (orange cloud) works. Old two-level patterns (`s1.api`, `s1.app`) caused `ERR_SSL_VERSION_OR_CIPHER_MISMATCH` and required DNS-only mode.
- **Backend requires `spring-boot-starter-actuator`.** Health checks depend on `/api/actuator/health`. Without this dependency, all health checks fail and Traefik marks the backend as unhealthy.
- **Mail health indicator must be disabled** (`management.health.mail.enabled: false`). DigitalOcean blocks outbound SMTP port 587 by default; the mail health check causes a 132s timeout that keeps the backend permanently unhealthy.
- **CORS is profile-driven, not hardcoded.** `allowedOrigins` and `allowedOriginPatterns` come from `CorsProperties` bound to `application-{env}.yml`; `SecurityConfig` must not contain hardcoded origin strings.
- **`deploy` user has docker group only — no sudo.** Workflows SSH as `deploy`, never root.
- **Prod images are tagged with release date** (`release-DD.MM.YYYY` + `latest`). Stage images use `stage-{sha}`.
- **Tenant storefront deploy is isolated per tenant.** Every tenant storefront runs as a separate compose project (`tenant-{env}-{slug}`), with its own router labels and domains.
- **Centralized logs flow to Grafana Cloud Loki via Alloy.** Stage and prod hosts run Alloy as part of compose; Grafana users are account seats (not tenant count).
- **Alloy mounts Docker socket (read-only flag; accepted risk).** Required for container log discovery. The `:ro` flag does not restrict Unix socket API access. Mitigation: Alloy container is isolated to `craftive-network` with no published ports. Future option: use a socket proxy allowlist.

---

## 4. Common patterns

### Environment URLs

| Env   | Service           | URL                               | Cloudflare |
| ----- | ----------------- | --------------------------------- | ---------- |
| Dev   | Backend API       | `http://localhost:8080/api`       | —          |
| Dev   | Admin Panel       | `http://localhost:4200`           | —          |
| Stage | Backend API       | `https://s1-api.craftive.io/api`  | Proxied    |
| Stage | Admin Panel       | `https://s1-app.craftive.io`      | Proxied    |
| Stage | Tenant storefront | `https://s1-{tenant}.craftive.io` | Proxied    |
| Prod  | Backend API       | `https://api.craftive.io/api`     | Proxied    |
| Prod  | Admin Panel       | `https://app.craftive.io`         | Proxied    |
| Prod  | Tenant storefront | `https://{tenant}.craftive.io`    | Proxied    |

`craftive.io` and `www.craftive.io` are served by Cloudflare Pages (landing project) — not Traefik. The Traefik root-redirect rule was removed from `docker-compose.prod.yml`.

> **Note:** Frontend `apiBaseUrl` must include the `/api` context-path suffix (e.g. `https://s1-api.craftive.io/api`, not `https://s1-api.craftive.io`). Without it, requests bypass Spring's DispatcherServlet and CORS headers are not applied.

### GHCR image names and tags

```
ghcr.io/{github.repository_owner}/craftive-backend:{tag}
ghcr.io/{github.repository_owner}/craftive-frontend:{tag}
ghcr.io/{github.repository_owner}/craftive-storefront:{tag}   # demo/reference storefront shipped from this repo
ghcr.io/{github.repository_owner}/<tenant>-storefront:{tag}   # tenant-specific repo output
```

> **Note:** GHCR org is derived from `${{ github.repository_owner }}` — never hardcode. Docker Compose uses `${GHCR_ORG}` variable, set by deploy workflows.

| Source                         | Tag format                      | Example              |
| ------------------------------ | ------------------------------- | -------------------- |
| `stage` branch                 | `stage-{sha}`                   | `stage-a3f9c12`      |
| `release/*` → stage smoke test | `release-DD.MM.YYYY`            | `release-27.02.2026` |
| `release/*` → prod             | `release-DD.MM.YYYY` + `latest` | `release-27.02.2026` |

### Centralized logs (Grafana Cloud Loki + Alloy)

Platform compose includes a `craftive-alloy` service that:

1. Collects Docker container logs from `/var/run/docker.sock`.
2. Collects host ops logs from `/opt/craftive/logs/*.log` (e.g. backup cron logs).
3. Redacts common sensitive patterns (authorization, cookie, password/token-like keys, email, phone).
4. Pushes to Grafana Cloud Loki.

Required `.env.{stage|prod}` variables:

```shell
GRAFANA_CLOUD_LOKI_URL=https://logs-<stack>.grafana.net/loki/api/v1/push
GRAFANA_CLOUD_LOKI_USER=<grafana-cloud-logs-user>
GRAFANA_CLOUD_LOKI_TOKEN=<api-token-with-logs-write>
LOG_ENV=stage|prod
LOG_HOST=<droplet-identifier>
```

TODO — Environment keys (fill in before deploy):

- [ ] `.env.stage` has `GRAFANA_CLOUD_LOKI_URL` set to the correct stage stack endpoint.
- [ ] `.env.stage` has `GRAFANA_CLOUD_LOKI_USER` set.
- [ ] `.env.stage` has `GRAFANA_CLOUD_LOKI_TOKEN` set.
- [ ] `.env.stage` has `LOG_ENV=stage`.
- [ ] `.env.stage` has `LOG_HOST` set (e.g. `do-fra1-stage-01`).
- [ ] `.env.prod` has `GRAFANA_CLOUD_LOKI_URL` set to the correct prod stack endpoint.
- [ ] `.env.prod` has `GRAFANA_CLOUD_LOKI_USER` set.
- [ ] `.env.prod` has `GRAFANA_CLOUD_LOKI_TOKEN` set.
- [ ] `.env.prod` has `LOG_ENV=prod`.
- [ ] `.env.prod` has `LOG_HOST` set (e.g. `do-fra1-prod-01`).

Rollout sequence:

1. Sync files to droplet (`scripts/server/deploy-files.sh` now also copies `observability/alloy.river`).
2. Deploy Stage and verify logs/labels in Grafana.
3. Deploy Production.

#### Local observability testing

An optional compose overlay runs Loki + Grafana + Alloy locally to validate the log pipeline without Grafana Cloud:

```powershell
# Dev + local observability:
docker compose -f docker-compose.yml -f docker-compose.dev.yml -f docker-compose.observability.yml --env-file .env.dev up -d
```

| Service  | URL                                   | Purpose                                 |
| -------- | ------------------------------------- | --------------------------------------- |
| Loki     | `http://localhost:3100/ready`         | Log storage (72h retention, filesystem) |
| Grafana  | `http://localhost:3000` (admin/admin) | Dashboards, Explore queries             |
| Alloy UI | `http://localhost:12345`              | Pipeline graph, component health        |

Loki datasource is auto-provisioned in Grafana. Query `{job="docker"}` in Explore to see container logs.

`alloy-local.river` mirrors the prod config (`alloy.river`) with two differences:

- Pushes to local Loki (`http://loki:3100`) instead of Grafana Cloud (no auth).
- Host log collection removed (Windows dev has no `/opt/craftive/logs`).

PII redaction rules are identical between local and prod configs.

### Branch strategy

```
feature/*  →  PR to stage  →  CI checks  →  merge
stage      →  (accumulated features)  →  PR to master  →  merge
master     →  release/release-DD.MM.YYYY  (release branch cut)
```

### Deploy flow — Stage (ongoing development)

1. GitHub Actions UI → `deploy-stage.yml` → `workflow_dispatch`
2. Input: `branch` — default `stage`, or any `release/release-*` for pre-prod smoke test
3. Tag derivation:
   - `stage` branch → `stage-{git-sha}`
   - `release/release-27.02.2026` → `release-27.02.2026`
4. Build 3 images (backend, frontend, storefront), push to GHCR
5. SSH to Stage Droplet → GHCR login → decode `ENV_STAGE` secret → pull images → `docker compose up -d --force-recreate` → write tag to `.last-deployed-tag`

> **Compose file sync:** The deploy job checks out the branch and copies `docker-compose.yml`, `docker-compose.prod.yml`, `docker-compose.stage.yml` to the droplet via `scp-action` before deploying. This ensures Traefik routing rules and service definitions are always in sync with the repository — no manual `deploy-files.sh` needed for compose changes. 6. Health check via SSH: `docker exec craftive-backend wget -qO- http://localhost:8080/api/actuator/health` → `{"status":"UP"}` (20 attempts, 15s interval, 10min timeout) 7. On failure: automatic rollback — SSH back, redeploy previous tag from `.last-deployed-tag.prev`

> **Why SSH-based health check?** External HTTPS health checks fail during first deploy because Let's Encrypt cert issuance takes time. SSH + `docker exec` bypasses DNS/SSL and checks the backend directly inside the container.

### Deploy flow — Production (release)

```
1. Cut release branch from master:
     git checkout master && git checkout -b release/release-DD.MM.YYYY

2. Smoke test on stage:
     deploy-stage.yml → branch: release/release-DD.MM.YYYY

3. Manual verification on https://s1-api.craftive.io/api/actuator/health

4. Production deploy:
     deploy-prod.yml → branch: release/release-DD.MM.YYYY
     → branch naming convention is `release/release-DD.MM.YYYY` (recommended, not enforced)
     → build backend + frontend + demo storefront, push release-DD.MM.YYYY + latest tags
     → wait for GitHub Environment "production" reviewer approval
     → SSH to Prod Droplet, save current tag → pull and restart → write new tag to `.last-deployed-tag`
     → SSH-based health check (20 attempts, 15s interval) + admin panel smoke test
     → on failure: automatic rollback to previous tag from `.last-deployed-tag.prev`
```

### Demo storefront flow — Platform repository

1. `storefront-nextjs/` in this repository is the demo/reference storefront.
2. Stage and prod platform deploy workflows build and publish `ghcr.io/{owner}/craftive-storefront:{tag}`.
3. The platform compose stack runs this demo storefront centrally for shared demo/reference usage.

### Tenant storefront flow — Separate repository (recommended)

1. Tenant storefront repository builds and pushes its own image to GHCR.
2. Tenant repo deploy workflow SSHs into target droplet and executes:
   - Stage default domain: `s1-<tenant>.craftive.io`
   - Prod default domain: `<tenant>.craftive.io`
3. Deploy command on droplet:

   ```bash
   bash /opt/craftive/scripts/deploy-tenant-storefront.sh \
     stage democompany ghcr.io/{owner}/democompany-storefront:stage-a3f9c12
   ```

4. Optional custom domains can be passed as additional arguments:

   ```bash
   bash /opt/craftive/scripts/deploy-tenant-storefront.sh \
     prod democompany ghcr.io/{owner}/democompany-storefront:release-27.02.2026 \
     democompany.com "www.democompany.com,shop.democompany.com"
   ```

5. Rollback/remove:

   ```bash
   bash /opt/craftive/scripts/remove-tenant-storefront.sh prod democompany
   ```

This model keeps tenant-specific deploy workflows independent from the platform deploy workflow, while still allowing the platform repo to ship a demo/reference storefront.

### Landing flow — Cloudflare Pages (static)

`landing/` is deployed independently from droplet-based platform services.

1. Connect repository to Cloudflare Pages (project: `craftive-landing`).
2. Set root directory to `landing`.
3. Build command: `npm run pages:build`.
4. Output directory: `out`.
5. Set custom domains: `craftive.io` (apex) and `www.craftive.io`.
6. Keep Cloudflare SSL mode `Full (strict)`.
7. Production branch: `landing` (not `master`). Auto-deploy on push to `landing` branch.

Notes:

- `landing/next.config.ts` uses static export (`output: "export"`).
- Primary site URL is controlled by `NEXT_PUBLIC_SITE_URL` at build time (defaults to `https://craftive.io`).
- `craftive.io` and `www.craftive.io` DNS are managed by Cloudflare Pages (apex flatten + CNAME). Do not add A records for these to the prod droplet.
- This flow is separate from Traefik tenant storefront scripts and does not require droplet deployment scripts.

### Tenant storefront repository CI/CD policy

1. Keep each tenant storefront in its own repository (`<tenant>-storefront`).
2. Recommended trigger strategy:
   - Stage deploy: automatic on `stage` branch push
   - Prod deploy: manual `workflow_dispatch` with reviewer approval
3. If Search Console ownership verification is needed, define the verification token in the tenant repository's own CI/build secrets. Do not add tenant-specific verification tokens to the platform repository secrets or `ENV_STAGE` / `ENV_PROD`.
4. Tenant repo deploy jobs should call droplet-side scripts:
   - Deploy/update: `deploy-tenant-storefront.sh`
   - Remove/rollback target removal: `remove-tenant-storefront.sh`

### Tenant DNS patterns

1. Stage default: `s1-<tenant>.craftive.io`
2. Prod default: `<tenant>.craftive.io`
3. Optional custom domains (e.g. `democompany.com`) can be attached as additional router hosts.
4. For custom domains, cert issuance happens via Traefik on first request.

**DNS strategy — Wildcard vs explicit records:**

| Approach                              | When to use                                                                                                                                                           |
| ------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Wildcard `*.craftive.io` → Droplet IP | Recommended. All tenant subdomains auto-resolve. Explicit records (e.g. `s1.app`, `api`) override the wildcard. Unknown tenants hit storefront → backend returns 404. |
| Per-tenant A records                  | If strict control is needed. Requires manual DNS management per tenant (automatable via Cloudflare API).                                                              |

> **Stage DNS records use orange cloud** — all stage service subdomains (`s1-api`, `s1-app`, `s1-cdn`, `s1-{tenant}`) are single-level and work with Cloudflare Universal SSL.
> **Prod tenant subdomains** (`{tenant}.craftive.io`) are single-level and use Cloudflare proxy (orange cloud). Real tenants in prod use whitelabel custom domains (e.g. `democompany.com`), not craftive subdomains.

TODO: Evaluate Cloudflare API integration for automatic DNS record creation when tenants are provisioned.

### Wildcard SSL via DNS-01

```yaml
# docker-compose.prod.yml — Traefik command entries
- "--certificatesresolvers.letsencrypt.acme.dnschallenge=true"
- "--certificatesresolvers.letsencrypt.acme.dnschallenge.provider=cloudflare"
- "--certificatesresolvers.letsencrypt.acme.dnschallenge.resolvers=1.1.1.1:53,8.8.8.8:53"
# Traefik environment — both env vars required:
environment:
  CF_API_TOKEN: ${CF_API_TOKEN}
  CLOUDFLARE_DNS_API_TOKEN: ${CF_API_TOKEN}   # Traefik v3.6 reads this
  DOCKER_API_VERSION: "1.43"                   # Required for Docker Engine 28.x
```

Custom tenant domains (e.g. `democompany.com`) use HTTP-01 — Traefik auto-issues certs on first request.

### CORS per environment

`application-prod.yml`:

```yaml
app:
  cors:
    allowed-origins:
      - https://app.craftive.io
      - https://craftive.io
      - https://www.craftive.io
    allowed-origin-patterns:
      - https://*.craftive.io
```

`application-stage.yml`:

```yaml
app:
  cors:
    allowed-origins:
      - https://s1-app.craftive.io
    allowed-origin-patterns:
      - https://s1-*.craftive.io
```

CMS delivery endpoints (`/cms/**`) are `permitAll()` and accept any origin — tenant storefronts run on arbitrary domains.

### Rate limiting — public platform endpoints

`POST /api/platform/public/demo-requests` and `POST /api/platform/public/newsletter/subscribe` are unauthenticated and should be protected with Traefik rate-limit middleware applied per source IP.

Add the middleware definition to `docker-compose.yml` (or the relevant prod/stage override) under the Traefik `labels` of the backend service:

```yaml
# docker-compose.prod.yml — backend service labels
- "traefik.http.middlewares.demo-request-ratelimit.ratelimit.average=5"
- "traefik.http.middlewares.demo-request-ratelimit.ratelimit.burst=10"
- "traefik.http.middlewares.demo-request-ratelimit.ratelimit.period=10m"
- "traefik.http.middlewares.demo-request-ratelimit.ratelimit.sourcecriterion.ipstrategy.depth=1"
- "traefik.http.routers.backend-public-demo.rule=Host(`api.craftive.io`) && PathPrefix(`/api/platform/public/demo-requests`)"
- "traefik.http.routers.backend-public-demo.middlewares=demo-request-ratelimit@docker"

- "traefik.http.middlewares.newsletter-subscribe-ratelimit.ratelimit.average=15"
- "traefik.http.middlewares.newsletter-subscribe-ratelimit.ratelimit.burst=25"
- "traefik.http.middlewares.newsletter-subscribe-ratelimit.ratelimit.period=10m"
- "traefik.http.middlewares.newsletter-subscribe-ratelimit.ratelimit.sourcecriterion.ipstrategy.depth=1"
- "traefik.http.routers.backend-public-newsletter.rule=Host(`api.craftive.io`) && PathPrefix(`/api/platform/public/newsletter/subscribe`)"
- "traefik.http.routers.backend-public-newsletter.middlewares=newsletter-subscribe-ratelimit@docker"
```

> **Note:** `ipstrategy.depth=1` trusts the first real IP from `X-Forwarded-For` when Cloudflare is the upstream proxy. Adjust `depth` if you add additional proxy hops.

Current thresholds:

- Demo request: **5 requests / 10 minutes / IP** with burst **10**
- Newsletter subscribe: **15 requests / 10 minutes / IP** with burst **25**

Stage inherits the same middleware values from `docker-compose.prod.yml` and only overrides the router host (`s1-api.*`). Keep the newsletter and demo routers aligned with the environment host whenever compose routing changes.

### GitHub Secrets

| Secret                    | Used by                                                                                                        |
| ------------------------- | -------------------------------------------------------------------------------------------------------------- |
| `DROPLET_SSH_PRIVATE_KEY` | Both deploy workflows                                                                                          |
| `PROD_DROPLET_IP`         | `deploy-prod.yml`                                                                                              |
| `STAGE_DROPLET_IP`        | `deploy-stage.yml`                                                                                             |
| `CF_API_TOKEN`            | Traefik DNS-01 (injected via `.env.*`)                                                                         |
| `ENV_PROD`                | `.env.prod` content, base64-encoded — must include `SPACES_ACCESS_KEY` / `SPACES_SECRET_KEY` for prod bucket   |
| `ENV_STAGE`               | `.env.stage` content, base64-encoded — must include `SPACES_ACCESS_KEY` / `SPACES_SECRET_KEY` for stage bucket |

Use separate DO Spaces key pairs for stage and prod (stage key compromise cannot affect prod bucket).

`GITHUB_TOKEN` is auto-injected by GitHub Actions (no explicit secret needed for GHCR push).

### Tenant storefront repo secrets (minimum)

| Secret                    | Used by                                              |
| ------------------------- | ---------------------------------------------------- |
| `DROPLET_SSH_PRIVATE_KEY` | SSH deploy to droplet                                |
| `STAGE_DROPLET_IP`        | Stage deploy workflow                                |
| `PROD_DROPLET_IP`         | Prod deploy workflow                                 |
| `GHCR_TOKEN` (optional)   | If not using default `GITHUB_TOKEN` for package push |

### Tenant storefront workflow templates

`deploy-stage.yml` (automatic on `stage`):

```yaml
name: Tenant Storefront - Deploy Stage

on:
  push:
    branches: [stage]

env:
  REGISTRY: ghcr.io
  ORG: ${{ github.repository_owner }}
  TENANT: democompany

jobs:
  deploy:
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write
    steps:
      - uses: actions/checkout@v4
      - uses: docker/login-action@v3
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}
      - name: Build and push image
        id: build
        run: |
          TAG="stage-${GITHUB_SHA::7}"
          IMAGE="${REGISTRY}/${ORG}/${TENANT}-storefront:${TAG}"
          docker build -t "$IMAGE" .
          docker push "$IMAGE"
          echo "image=$IMAGE" >> $GITHUB_OUTPUT
      - name: Deploy on Stage droplet
        uses: appleboy/ssh-action@v1
        with:
          host: ${{ secrets.STAGE_DROPLET_IP }}
          username: deploy
          key: ${{ secrets.DROPLET_SSH_PRIVATE_KEY }}
          script: |
            bash /opt/craftive/scripts/deploy-tenant-storefront.sh \
              stage ${{ env.TENANT }} "${{ steps.build.outputs.image }}"
```

`deploy-prod.yml` (manual):

```yaml
name: Tenant Storefront - Deploy Prod

on:
  workflow_dispatch:
    inputs:
      image_tag:
        description: "Image tag to deploy (example: release-27.02.2026)"
        required: true

env:
  REGISTRY: ghcr.io
  ORG: ${{ github.repository_owner }}
  TENANT: democompany

jobs:
  deploy:
    runs-on: ubuntu-latest
    environment: production
    steps:
      - name: Deploy on Prod droplet
        uses: appleboy/ssh-action@v1
        with:
          host: ${{ secrets.PROD_DROPLET_IP }}
          username: deploy
          key: ${{ secrets.DROPLET_SSH_PRIVATE_KEY }}
          script: |
            IMAGE="${{ env.REGISTRY }}/${{ env.ORG }}/${{ env.TENANT }}-storefront:${{ github.event.inputs.image_tag }}"
            bash /opt/craftive/scripts/deploy-tenant-storefront.sh \
              prod ${{ env.TENANT }} "$IMAGE"
```

Adjust `TENANT`, organization, and optional custom domains per repository.

#### Canonical redirect (apex → www)

When the tenant has a custom domain with an apex redirect (e.g. `ahmetmulayim.com` → `www.ahmetmulayim.com`), pass a 6th argument to the deploy script:

```bash
bash /opt/craftive/scripts/deploy-tenant-storefront.sh \
  prod mulayim "$IMAGE" \
  www.ahmetmulayim.com "ahmetmulayim.com" "www.ahmetmulayim.com"
# arg4: primary_domain (canonical — served directly)
# arg5: extra_domains_csv (apex — will 301 → canonical)
# arg6: canonical_host (triggers redirect middleware)
```

The script generates a Traefik `redirectregex` middleware that issues a permanent 301 redirect for every domain in `extra_domains_csv` that differs from `canonical_host`. The canonical router routes traffic normally; a separate redirect router handles the apex. Both domains get their own Let's Encrypt certificate via HTTP-01.

In the prod workflow, wire this via the `deploy-tenant-storefront.sh` call:

```yaml
script: |
  bash /opt/craftive/scripts/deploy-tenant-storefront.sh \
    prod ${{ env.TENANT }} "$IMAGE" \
    www.ahmetmulayim.com "ahmetmulayim.com" "www.ahmetmulayim.com"
```

If the tenant storefront also needs Search Console HTML tag verification, define `NEXT_PUBLIC_GOOGLE_SITE_VERIFICATION` in that tenant repository's own tracked storefront env or equivalent build config. This is tenant-repo concern, not platform repo concern.

### First-time server setup

```bash
# Run once per Droplet after creation
sudo bash scripts/server/provision-droplet.sh stage "ssh-ed25519 AAAA..."
sudo bash scripts/server/configure-rclone.sh     # writes /home/deploy/.config/rclone/rclone.conf
bash scripts/server/deploy-files.sh stage <STAGE_IP>

sudo bash scripts/server/provision-droplet.sh prod "ssh-ed25519 AAAA..."
sudo bash scripts/server/configure-rclone.sh     # writes /home/deploy/.config/rclone/rclone.conf
bash scripts/server/deploy-files.sh prod <PROD_IP>
```

`provision-droplet.sh` installs Docker CE, creates the `deploy` user, hardens SSH, configures UFW, sets up daily MySQL backup cron, and installs rclone.
`deploy-files.sh` also copies tenant storefront operation scripts to `/opt/craftive/scripts`.

### Backup

Daily cron (03:00 UTC+3) on each Droplet, run by `deploy` user:

```
mysqldump --all-databases | gzip → DO Spaces: craftive-backups/{env}/YYYY-MM-DD.sql.gz
```

`rclone` credentials are stored in `/home/deploy/.config/rclone/rclone.conf` so cron and manual backup runs use the same user context.
30-day retention enforced via Spaces lifecycle policy.

### Rollback pattern (platform)

Deploy workflows save the previous image tag before each deploy:

- Before deploy: current tag copied to `.last-deployed-tag.prev`
- After successful `up -d`: new tag written to `.last-deployed-tag`

If the health check fails, the `Rollback on failure` step (runs on `if: failure()`) automatically re-deploys the previous tag. Both `deploy-prod.yml` and `deploy-stage.yml` implement this pattern.

Manual rollback (if needed):

Daily cron (03:00 UTC+3) on each Droplet, run by `deploy` user:

```
mysqldump --all-databases | gzip → DO Spaces: craftive-backups/{env}/YYYY-MM-DD.sql.gz
```

`rclone` credentials are stored in `/home/deploy/.config/rclone/rclone.conf` so cron and manual backup runs use the same user context.
30-day retention enforced via Spaces lifecycle policy.

### Rollback pattern (platform)

Deploy workflows save the previous image tag before each deploy:

- Before deploy: current tag copied to `.last-deployed-tag.prev`
- After successful `up -d`: new tag written to `.last-deployed-tag`

If the health check fails, the `Rollback on failure` step (runs on `if: failure()`) automatically re-deploys the previous tag. Both `deploy-prod.yml` and `deploy-stage.yml` implement this pattern.

Manual rollback (if needed):

```bash
# On the droplet
cd /opt/craftive/{prod|stage}
export APP_VERSION=$(cat .last-deployed-tag.prev)
docker compose -f docker-compose.yml -f docker-compose.prod.yml --env-file .env.prod up -d
```

### Rollback pattern (tenant storefront)

1. Re-run tenant deploy with the previous known-good image tag.
2. Verify tenant domain health after rollout.
3. If decommissioning a tenant instance, run `remove-tenant-storefront.sh`.

---

## 5. Gotchas

- **`NEXT_OUTPUT` controls Next.js build output.** `standalone` for Docker SSR (stage demo); `export` for static hosting (tenant self-deploy). Controlled via `process.env.NEXT_OUTPUT` in `next.config.ts` — not a hardcoded config value.
- **`craftive:` is the YAML root key** in `application.yml`. Renaming it would break `@ConfigurationProperties(prefix = "craftive")` bindings without a corresponding Java refactor.
- **Reserved subdomains cannot be assigned to tenants:** `www`, `api`, `app`, `admin`, `s1`, `s2`, `mail`, `docs`, `status`, `blog`, `demo`, `cdn`. Enforce at the tenant subdomain validation layer.
- **Stage wildcard DNS (`*.craftive.io`) points to Stage Droplet.** An explicit prod-hosted tenant subdomain (e.g. `demo.craftive.io`) must have its own A record pointing to the Prod Droplet, otherwise traffic hits Stage.
- **Both Prod and Stage DNS records use Cloudflare proxy (orange cloud).** The actual Droplet IPs should not be published. Stage uses single-level `s1-*` convention so Cloudflare Universal SSL covers all stage subdomains.
- **Multi-level subdomains break Cloudflare Universal SSL.** `*.craftive.io` covers `app.craftive.io` (single-level) but NOT `s1.app.craftive.io` (two levels). Proxied multi-level subdomains cause `ERR_SSL_VERSION_OR_CIPHER_MISMATCH`. Stage domain refactor resolved this by switching to single-level `s1-*` convention (`s1-api`, `s1-app`, `s1-cdn`) — all stage services now work with Cloudflare orange cloud.
- **Backend health check uses `wget`, not `curl`.** The distroless backend image does not include `curl`. Docker Compose and deploy workflows use `wget -qO-` for health checks.
- **Backend `start_period` must be at least 150s.** Spring Boot takes ~120s to start. A shorter `start_period` causes Docker to mark the container as permanently unhealthy before it finishes starting, and Traefik stops routing to it.
- **Deploy uses `--force-recreate`.** Ensures Traefik picks up new container IDs after image updates. Without this, Traefik may route to stale containers after rollback/redeploy cycles.
- **GHCR images are private.** Deploy workflows must authenticate to GHCR on the droplet via `docker login` before pulling. The `GITHUB_TOKEN` is passed as `GHCR_TOKEN` env var through SSH.
- **DigitalOcean blocks outbound SMTP port 587 by default.** Request unblock via DO support ticket for email functionality. Until then, `management.health.mail.enabled` must be `false` to prevent health check timeouts.
- **`docker-compose.stage.yml` is an overlay only.** It adds stage-specific routing (`s1-api.*`, `s1-app.*`, `s1-<tenant>.*`) and the storefront service; always layer it on top of `docker-compose.yml` and `docker-compose.prod.yml`.
- **Traefik v3 dropped `{name:regexp}` HostRegexp syntax.** The stage storefront router uses `ruleSyntax=v2` label to keep the existing `s1-{subdomain:[a-z0-9-]+}` pattern working. Remove this label only after migrating to v3 syntax.
- **Cloudflare Origin Rule (Host Header Override) is Enterprise-only.** Cloudflare proxy sends `Host: s1-cdn.craftive.io` to DO Spaces, which cannot resolve the bucket and returns `AccessDenied`. Origin Rules that override the Host header require Enterprise plan. Solution: use a **Cloudflare Worker** as reverse proxy — it rewrites the hostname to the DO Spaces CDN endpoint before forwarding (`craftive-media-stage.fra1.cdn.digitaloceanspaces.com`). CDN DNS records use `AAAA 100::` (dummy IPv6, Proxied) so the Worker intercepts all requests.
- **DO Spaces CDN must be enabled (without custom domain).** Worker proxies to the DO CDN endpoint (`fra1.cdn.digitaloceanspaces.com`), so origin-level caching is active. Do not add a custom subdomain in DO Spaces CDN settings — custom subdomain requires the domain to be managed on DigitalOcean DNS.
- **S3 objects must be uploaded with `public-read` ACL.** DO Spaces objects are private by default. Without `ObjectCannedACL.PUBLIC_READ` on `PutObjectRequest`, CDN delivery always returns `AccessDenied` regardless of DNS or proxy configuration.
- **Cloudflare Cache Rule hostname must match the actual CDN subdomain exactly.** A rule matching `s1.media.craftive.io` does not apply to `s1-cdn.craftive.io`. Verify the Cache Rule expression after any CDN domain rename. When using Workers, the Worker route (`s1-cdn.craftive.io/*`) takes precedence — Cache Rules apply to Worker responses as normal.
- **Alloy `stage.replace` replaces the capture group, not the full match.** When a regex has a capture group `(...)`, only the captured portion is substituted. Use `(?:...)` for non-capturing groups and place `(...)` only around the value to redact. Example: `"(?:password|secret)\\s*[:=]\\s*(\\S+)"` replaces only the credential value, keeping the keyword intact.
