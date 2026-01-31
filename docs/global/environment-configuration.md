# Environment Configuration

Multi-environment setup for AdminCraft with dev, stage, and prod configurations.

## Source of Truth

### Backend (Spring Boot)

| File | Purpose |
|------|---------|
| `../../backend/src/main/resources/application.yml` | Base configuration (shared settings) |
| `../../backend/src/main/resources/application-dev.yml` | Development overrides |
| `../../backend/src/main/resources/application-stage.yml` | Staging overrides |
| `../../backend/src/main/resources/application-prod.yml` | Production overrides |

### Frontend (Angular)

| File | Purpose |
|------|---------|
| `../../storefront/src/environments/environment.ts` | Development config |
| `../../storefront/src/environments/environment.stage.ts` | Staging config |
| `../../storefront/src/environments/environment.prod.ts` | Production config |
| `../../storefront/angular.json` | Build configurations |

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

| Setting | Dev | Stage | Prod |
|---------|-----|-------|------|
| DB Port | `3307` (Docker) | `3306` | `3306` |
| DB Credentials | defaults | env vars | env vars |
| JWT Secret | default | `${JWT_SECRET}` | `${JWT_SECRET}` |
| SQL Logging | `true` | `false` | `false` |
| Swagger UI | enabled | enabled | disabled |
| Log Level | `DEBUG` | `INFO` | `INFO` |
| Auto-sync | `false` | `true` | `true` |

### Frontend

| Setting | Dev | Stage | Prod |
|---------|-----|-------|------|
| `production` | `false` | `false` | `true` |
| `apiBaseUrl` | `/api` (proxy) | `https://s1.api.admincraft.com` | `https://api.admincraft.com` |
| `apiTimeout` | `30000` | `30000` | `30000` |
| `supportedLanguages` | `['tr', 'en']` | `['tr', 'en']` | `['tr', 'en']` |
| `defaultLanguage` | `en` | `en` | `en` |
| `maxRetryAttempts` | `0` | `0` | `3` |
| Source Maps | yes | yes | no |
| Optimization | no | yes | yes |

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

| Variable | Description | Required |
|----------|-------------|----------|
| `JWT_SECRET` | JWT signing key (min 64 chars) | Yes |
| `DB_USERNAME` | Database username | Yes |
| `DB_PASSWORD` | Database password | Yes |
| `DB_HOST` | Database host | Yes |
| `PLATFORM_DB_URL` | Full JDBC URL for platform DB | Yes |
| `DB_PORT` | Database port | No (default: 3306) |
| `APP_FRONTEND_BASE_URL` | Frontend URL pattern | No |
| `PLATFORM_DOMAIN` | Platform domain | No |

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

1. **Docker MySQL uses port 3307**: The `docker-compose.yml` maps `3307:3306`, so dev connects to `localhost:3307`

2. **Profile file naming**: Must be exactly `application-{profile}.yml` (e.g., `application-dev.yml`)

3. **Angular proxy**: Dev mode uses `/api` which proxies to `localhost:8080` via `proxy.conf.json`

4. **No hot-reload for environment changes**: Frontend requires rebuild when environment values change

5. **Spring property override order**:
   - `application.yml` (base)
   - `application-{profile}.yml` (profile-specific)
   - Environment variables (highest priority)
