# Environment Configuration

Multi-environment setup for AdminCraft with dev, stage, and prod configurations.

## Source of Truth

### Backend (Spring Boot)

| File                                                     | Purpose                              |
| -------------------------------------------------------- | ------------------------------------ |
| `../../backend/src/main/resources/application.yml`       | Base configuration (shared settings) |
| `../../backend/src/main/resources/application-dev.yml`   | Development overrides                |
| `../../backend/src/main/resources/application-stage.yml` | Staging overrides                    |
| `../../backend/src/main/resources/application-prod.yml`  | Production overrides                 |

### Frontend (Angular)

| File                                                     | Purpose              |
| -------------------------------------------------------- | -------------------- |
| `../../storefront/src/environments/environment.ts`       | Development config   |
| `../../storefront/src/environments/environment.stage.ts` | Staging config       |
| `../../storefront/src/environments/environment.prod.ts`  | Production config    |
| `../../storefront/angular.json`                          | Build configurations |

## Rules and Invariants

### Backend

1. **Profile hierarchy**: `application.yml` loads first, then `application-{profile}.yml` overrides
2. **Default profile**: `dev` (set via `SPRING_PROFILES_ACTIVE` env var)
3. **Credentials**: Dev uses defaults, Stage/Prod require environment variables
4. **No defaults for sensitive values in Stage/Prod**: `JWT_SECRET`, `DB_USERNAME`, `DB_PASSWORD` must be set

### Frontend

1. **File replacement**: Angular replaces `environment.ts` with profile-specific file at build time
2. **Consistent structure**: All environment files must have the same properties
3. **No runtime configuration**: API URLs are baked in at build time

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
| Auto-sync      | `false`  | `true`          | `true`          |

### Frontend

| Setting              | Dev            | Stage                           | Prod                         |
| -------------------- | -------------- | ------------------------------- | ---------------------------- |
| `production`         | `false`        | `false`                         | `true`                       |
| `apiBaseUrl`         | `/api` (proxy) | `https://s1.api.admincraft.com` | `https://api.admincraft.com` |
| `apiTimeout`         | `30000`        | `30000`                         | `30000`                      |
| `supportedLanguages` | `['tr', 'en']` | `['tr', 'en']`                  | `['tr', 'en']`               |
| `defaultLanguage`    | `en`           | `en`                            | `en`                         |
| `maxRetryAttempts`   | `0`            | `0`                             | `3`                          |
| Source Maps          | yes            | yes                             | no                           |
| Optimization         | no             | yes                             | yes                          |

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

# Frontend (uses development config)
cd storefront
npm run start:dev
```

### Running with Specific Profile

```powershell
# Backend with stage profile
mvn spring-boot:run -Dspring-boot.run.profiles=stage

# Frontend with stage config
npm run start:stage
```

### Build for Production

```powershell
# Backend (JAR with prod profile at runtime)
mvn clean package -DskipTests
java -jar target/*.jar --spring.profiles.active=prod

# Frontend (optimized build)
npm run build:prod
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
| `APP_FRONTEND_BASE_URL` | Frontend URL pattern           | No                 |
| `PLATFORM_DOMAIN`       | Platform domain                | No                 |

### Example Stage Deployment

```powershell
$env:JWT_SECRET = "your-64-char-secret-key-here..."
$env:DB_USERNAME = "stage_user"
$env:DB_PASSWORD = "stage_pass"
$env:DB_HOST = "stage-db.internal"
$env:PLATFORM_DB_URL = "jdbc:mysql://stage-db.internal:3306/platform_management?useSSL=true"

mvn spring-boot:run -Dspring-boot.run.profiles=stage
```

## Gotchas

1. **MySQL standard port (3306)**: Docker and backend both use 3306 by default. If this port is occupied by a local MySQL service, you must stop the local service.

2. **Profile file naming**: Must be exactly `application-{profile}.yml` (e.g., `application-dev.yml`)

3. **Angular proxy**: Dev mode uses `/api` which proxies to `localhost:8080` via `proxy.conf.json`

4. **No hot-reload for environment changes**: Frontend requires rebuild when environment values change

5. **Spring property override order**:
   - `application.yml` (base)
   - `application-{profile}.yml` (profile-specific)
   - Environment variables (highest priority)

---

## Docker Multi-Environment Setup

AdminCraft uses Docker Compose with environment-specific override files.

### File Structure

```
AdminCraft/
├── docker-compose.yml          # Base config (MySQL only)
├── docker-compose.dev.yml      # Dev overrides (+ phpMyAdmin)
├── docker-compose.prod.yml     # Prod overrides (+ Backend + Frontend + Traefik)
├── .env.example                # Environment template
├── .env.dev                    # Local dev values (gitignored)
├── .env.prod                   # Production values (gitignored)
└── docker/
    ├── mysql/                  # MySQL config (existing)
    ├── backend/Dockerfile      # Spring Boot image
    └── frontend/
        ├── Dockerfile          # Angular + Nginx image
        └── nginx.conf          # Nginx configuration
```

### Environment Overview

| Environment    | Services                             | Use Case                                     |
| -------------- | ------------------------------------ | -------------------------------------------- |
| **Dev**        | MySQL + phpMyAdmin                   | Local development (Backend/Frontend via IDE) |
| **Prod/Stage** | MySQL + Backend + Frontend + Traefik | VPS deployment with SSL                      |

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

- Frontend: `https://admincraft.example.com`
- Backend API: `https://api.admincraft.example.com`

### Environment Variables

Create `.env.dev` and `.env.prod` from `.env.example`:

```powershell
# Copy template
cp .env.example .env.dev
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

### Traefik Configuration

Production uses Traefik v3 for:

- Automatic HTTPS with Let's Encrypt
- HTTP to HTTPS redirect
- Reverse proxy to services

**DNS Requirements:**

- `admincraft.example.com` → VPS IP
- `api.admincraft.example.com` → VPS IP

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
docker logs admincraft-mysql

# Wait for MySQL health check
docker compose -f docker-compose.yml -f docker-compose.prod.yml --env-file .env.prod up -d --wait
```

**SSL certificate not issued:**

- Verify DNS A records point to VPS IP
- Check Traefik logs: `docker logs admincraft-traefik`
- Verify ports 80/443 are open on VPS firewall
