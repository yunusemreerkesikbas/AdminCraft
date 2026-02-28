# DevOps & Deployment

## 1. What it is / why it exists

Craftive runs two isolated environments on DigitalOcean (Frankfurt, FRA1), fronted by Cloudflare CDN.
Each environment has its own Droplet, Traefik reverse proxy, and Docker Compose project.

**Platform services** (managed by Craftive): Backend API + Admin Panel.
**Tenant storefront** (`storefront-nextjs/`): Deployable boilerplate. Each tenant storefront can be deployed independently from its own repository onto the same environment droplet with isolated routing.

---

## 2. Source of truth

| Concern | File |
|---------|------|
| Base services | [`../../docker-compose.yml`](../../docker-compose.yml) |
| Production overrides | [`../../docker-compose.prod.yml`](../../docker-compose.prod.yml) |
| Stage overrides | [`../../docker-compose.stage.yml`](../../docker-compose.stage.yml) |
| Dev overrides | [`../../docker-compose.dev.yml`](../../docker-compose.dev.yml) |
| Next.js SSR image | [`../../docker/storefront/Dockerfile`](../../docker/storefront/Dockerfile) |
| PR checks | [`../../.github/workflows/ci.yml`](../../.github/workflows/ci.yml) |
| Stage deploy | [`../../.github/workflows/deploy-stage.yml`](../../.github/workflows/deploy-stage.yml) |
| Prod deploy | [`../../.github/workflows/deploy-prod.yml`](../../.github/workflows/deploy-prod.yml) |
| Droplet setup | [`../../scripts/server/provision-droplet.sh`](../../scripts/server/provision-droplet.sh) |
| DO Spaces backup | [`../../scripts/server/configure-rclone.sh`](../../scripts/server/configure-rclone.sh) |
| Compose file copy | [`../../scripts/server/deploy-files.sh`](../../scripts/server/deploy-files.sh) |
| Tenant storefront deploy | [`../../scripts/server/deploy-tenant-storefront.sh`](../../scripts/server/deploy-tenant-storefront.sh) |
| Tenant storefront remove | [`../../scripts/server/remove-tenant-storefront.sh`](../../scripts/server/remove-tenant-storefront.sh) |
| CORS properties | [`../../backend/src/main/java/com/backend/infrastructure/config/CorsProperties.java`](../../backend/src/main/java/com/backend/infrastructure/config/CorsProperties.java) |
| CORS wiring | [`../../backend/src/main/java/com/backend/infrastructure/config/SecurityConfig.java`](../../backend/src/main/java/com/backend/infrastructure/config/SecurityConfig.java) |
| Backend config (prod) | [`../../backend/src/main/resources/application-prod.yml`](../../backend/src/main/resources/application-prod.yml) |
| Backend config (stage) | [`../../backend/src/main/resources/application-stage.yml`](../../backend/src/main/resources/application-stage.yml) |
| Env template | [`../../.env.example`](../../.env.example) |

---

## 3. Rules and invariants

- **Separate Droplet per environment.** Prod and Stage never share a host. A Stage failure cannot affect Prod.
- **Platform deploys are manual** (`workflow_dispatch`). Tenant storefront repositories may use automatic Stage deploys and manual Prod deploys.
- **Production requires reviewer approval.** The `production` GitHub Environment gate must not be bypassed.
- **Ports 3306, 8080, 3000 are never externally reachable.** UFW allows only 22, 80, 443.
- **Wildcard SSL (`*.craftive.io`) requires DNS-01.** HTTP-01 cannot issue wildcard certs; Cloudflare is the DNS provider.
- **Cloudflare SSL mode must be Full (strict).** Flexible mode breaks backend TLS validation.
- **CORS is profile-driven, not hardcoded.** `allowedOrigins` and `allowedOriginPatterns` come from `CorsProperties` bound to `application-{env}.yml`; `SecurityConfig` must not contain hardcoded origin strings.
- **`deploy` user has docker group only — no sudo.** Workflows SSH as `deploy`, never root.
- **Prod images are tagged with release date** (`release-DD.MM.YYYY` + `latest`). Stage images use `stage-{sha}`.
- **Tenant storefront deploy is isolated per tenant.** Every tenant storefront runs as a separate compose project (`tenant-{env}-{slug}`), with its own router labels and domains.

---

## 4. Common patterns

### Environment URLs

| Env | Service | URL |
|-----|---------|-----|
| Dev | Backend API | `http://localhost:8080/api` |
| Dev | Admin Panel | `http://localhost:4200` |
| Stage | Backend API | `https://s1.api.craftive.io` |
| Stage | Admin Panel | `https://s1.app.craftive.io` |
| Prod | Backend API | `https://api.craftive.io` |
| Prod | Admin Panel | `https://app.craftive.io` |

`craftive.io` and `www.craftive.io` redirect to `app.craftive.io` via Traefik `redirectregex` middleware.

### GHCR image names and tags

```
ghcr.io/craftive/craftive-backend:{tag}
ghcr.io/craftive/craftive-frontend:{tag}
ghcr.io/craftive/craftive-storefront:{tag}   # stage only
ghcr.io/craftive/<tenant>-storefront:{tag}   # tenant-specific repo output
```

| Source | Tag format | Example |
|--------|-----------|---------|
| `stage` branch | `stage-{sha}` | `stage-a3f9c12` |
| `release/*` → stage smoke test | `release-DD.MM.YYYY` | `release-27.02.2026` |
| `release/*` → prod | `release-DD.MM.YYYY` + `latest` | `release-27.02.2026` |

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
5. SSH to Stage Droplet, pull and restart services
6. Health check: `https://s1.api.craftive.io/api/actuator/health` → `{"status":"UP"}`

### Deploy flow — Production (release)

```
1. Cut release branch from master:
     git checkout master && git checkout -b release/release-DD.MM.YYYY

2. Smoke test on stage:
     deploy-stage.yml → branch: release/release-DD.MM.YYYY

3. Manual verification on https://s1.api.craftive.io

4. Production deploy:
     deploy-prod.yml → branch: release/release-DD.MM.YYYY
     → branch naming convention is `release/release-DD.MM.YYYY` (recommended, not enforced)
     → build backend + frontend, push release-DD.MM.YYYY + latest tags
     → wait for GitHub Environment "production" reviewer approval
     → SSH to Prod Droplet, pull and restart
     → health check + admin panel smoke test
```

### Tenant storefront flow — Separate repository (recommended)

1. Tenant storefront repository builds and pushes its own image to GHCR.
2. Tenant repo deploy workflow SSHs into target droplet and executes:
   - Stage default domain: `s1-<tenant>.craftive.io`
   - Prod default domain: `<tenant>.craftive.io`
3. Deploy command on droplet:
   ```bash
   bash /opt/craftive/scripts/deploy-tenant-storefront.sh \
     stage democompany ghcr.io/craftive/democompany-storefront:stage-a3f9c12
   ```
4. Optional custom domains can be passed as additional arguments:
   ```bash
   bash /opt/craftive/scripts/deploy-tenant-storefront.sh \
     prod democompany ghcr.io/craftive/democompany-storefront:release-27.02.2026 \
     democompany.com "www.democompany.com,shop.democompany.com"
   ```
5. Rollback/remove:
   ```bash
   bash /opt/craftive/scripts/remove-tenant-storefront.sh prod democompany
   ```

This model keeps platform deploy workflows independent from tenant storefront deploy workflows.

### Tenant storefront repository CI/CD policy

1. Keep each tenant storefront in its own repository (`<tenant>-storefront`).
2. Recommended trigger strategy:
   - Stage deploy: automatic on `stage` branch push
   - Prod deploy: manual `workflow_dispatch` with reviewer approval
3. Tenant repo deploy jobs should call droplet-side scripts:
   - Deploy/update: `deploy-tenant-storefront.sh`
   - Remove/rollback target removal: `remove-tenant-storefront.sh`

### Tenant DNS patterns

1. Stage default: `s1-<tenant>.craftive.io`
2. Prod default: `<tenant>.craftive.io`
3. Optional custom domains (e.g. `democompany.com`) can be attached as additional router hosts.
4. DNS records must point to the correct environment droplet behind Cloudflare proxy.
5. For custom domains, cert issuance happens via Traefik on first request.

### Wildcard SSL via DNS-01

```yaml
# docker-compose.prod.yml — Traefik command entries
- "--certificatesresolvers.letsencrypt.acme.dnschallenge=true"
- "--certificatesresolvers.letsencrypt.acme.dnschallenge.provider=cloudflare"
- "--certificatesresolvers.letsencrypt.acme.dnschallenge.resolvers=1.1.1.1:53,8.8.8.8:53"
# CF_API_TOKEN env var passed from .env.{prod|stage}
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
      - https://s1.app.craftive.io
    allowed-origin-patterns:
      - https://*.craftive.io
```

CMS delivery endpoints (`/cms/**`) are `permitAll()` and accept any origin — tenant storefronts run on arbitrary domains.

### GitHub Secrets

| Secret | Used by |
|--------|---------|
| `DROPLET_SSH_PRIVATE_KEY` | Both deploy workflows |
| `PROD_DROPLET_IP` | `deploy-prod.yml` |
| `STAGE_DROPLET_IP` | `deploy-stage.yml` |
| `CF_API_TOKEN` | Traefik DNS-01 (injected via `.env.*`) |
| `ENV_PROD` | `.env.prod` content, base64-encoded |
| `ENV_STAGE` | `.env.stage` content, base64-encoded |

`GITHUB_TOKEN` is auto-injected by GitHub Actions (no explicit secret needed for GHCR push).

### Tenant storefront repo secrets (minimum)

| Secret | Used by |
|--------|---------|
| `DROPLET_SSH_PRIVATE_KEY` | SSH deploy to droplet |
| `STAGE_DROPLET_IP` | Stage deploy workflow |
| `PROD_DROPLET_IP` | Prod deploy workflow |
| `GHCR_TOKEN` (optional) | If not using default `GITHUB_TOKEN` for package push |

### Tenant storefront workflow templates

`deploy-stage.yml` (automatic on `stage`):
```yaml
name: Tenant Storefront - Deploy Stage

on:
  push:
    branches: [stage]

env:
  REGISTRY: ghcr.io
  ORG: craftive
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
  ORG: craftive
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

### Rollback pattern (tenant storefront)

1. Re-run tenant deploy with the previous known-good image tag.
2. Verify tenant domain health after rollout.
3. If decommissioning a tenant instance, run `remove-tenant-storefront.sh`.

---

## 5. Gotchas

- **`NEXT_OUTPUT` controls Next.js build output.** `standalone` for Docker SSR (stage demo); `export` for static hosting (tenant self-deploy). Controlled via `process.env.NEXT_OUTPUT` in `next.config.ts` — not a hardcoded config value.
- **`admincraft:` is the YAML root key** in `application.yml`. Renaming it would break `@ConfigurationProperties(prefix = "admincraft")` bindings without a corresponding Java refactor.
- **Reserved subdomains cannot be assigned to tenants:** `www`, `api`, `app`, `admin`, `s1`, `s2`, `mail`, `docs`, `status`, `blog`, `demo`, `cdn`. Enforce at the tenant subdomain validation layer.
- **Stage wildcard DNS (`*.craftive.io`) points to Stage Droplet.** An explicit prod-hosted tenant subdomain (e.g. `demo.craftive.io`) must have its own A record pointing to the Prod Droplet, otherwise traffic hits Stage.
- **Cloudflare proxies both Droplets on the same IPs from the public perspective.** The actual Droplet IPs must not be published; always route through Cloudflare orange-cloud records.
- **`docker-compose.stage.yml` is an overlay only.** It adds stage-specific routing (`s1.api.*`, `s1.app.*`) and the storefront service; always layer it on top of `docker-compose.yml` and `docker-compose.prod.yml`.
