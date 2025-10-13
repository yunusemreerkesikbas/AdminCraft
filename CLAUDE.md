## AdminCraft — Developer Quickstart (Clean Architecture, Multi‑Tenant, i18n)

### Tech & Layout

- Backend: Spring Boot 3, Java 21, Spring Data JPA, MySQL
- Frontend: Angular 19, TypeScript
- Clean Architecture: presentation → application → domain → infrastructure
- Paths: `backend/src/main/java/com/backend/...`, resources: `backend/src/main/resources/...`, admin UI: `storefront/`

### Common commands

- Backend dev (no seed):

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

- Backend dev with seed (loads `schema-page-builder.sql` + `data.sql` once):

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev,seed
```

- Full build & tests:

```bash
mvn clean verify
```

- Run jar:

```bash
java -Dspring.profiles.active=dev -jar backend/target/<artifact>.jar
```

- Frontend serve:

```bash
cd storefront && npm ci && npm run start
```

- Local DB (optional):

```bash
docker compose up -d
```

### Multi‑tenant essentials

- Always send `X-Tenant-ID` on admin API calls (Angular HTTP interceptor required).
- `TenantContext` uses ThreadLocal; cleared per request; tenant is extracted from header/subdomain/JWT.
- All entities include `tenant_id`; Hibernate `@Filter` enforces isolation.
- Validate tenant access in services before operations; never leak tenant info in errors/logs.
- Return DTOs only from services; controllers return `ResponseEntity<ApiResponse<T>>`.

### i18n essentials

- Backend messages: `backend/src/main/resources/i18n/messages_{tr|en}.properties` (UTF‑8).
- Frontend admin uses Angular i18n; site content stored per language (e.g., `pages` + `page_i18n`).
- See backend endpoints and flows in `.docs.md` (Page Builder and i18n section).

### Code style (backend)

- Constructor injection, no field `@Autowired`; annotate with `@Service`.
- Use records for DTOs; validate inputs; never return entities; use `Optional` not `null`.
- Mark multi‑step operations `@Transactional`; keep orchestration in application layer.
- Repositories extend `JpaRepository`; prefer `@EntityGraph` to avoid N+1; JPQL with parameters only.
- Security: input validation, parameterized queries, `@PreAuthorize`, rate limiting; never log sensitive data.
- Exceptions handled by `GlobalExceptionHandler`; messages are localized.

### Code style (frontend)

- Explicit types everywhere; private members with `#`; components prefixed with `spa-`.
- Use async pipe or `.pipe(take(1))`; unsubscribe appropriately.
- Centralize API routes (see `API_ENDPOINTS`); ensure DTOs match `ApiResponse<T>` shape.
- Include `X-Tenant-ID` header in an HTTP interceptor for all API requests.

### Workflow & etiquette

- Branches: `feature/CMS-123`, `bugfix/CMS-123`, `chore/CMS-123`.
- Commits: Conventional Commits (`feat:`, `fix:`, `docs:`, `refactor:`...).
- PRs: small, focused; rebase on latest `main` before opening; prefer squash merge.
- Keep layers clean: presentation → application → domain; domain has no external deps.

```
- Focus areas: tenant isolation (filters active), i18n flows, N+1 prevention, transaction boundaries.

### Gotchas
- Seed is disabled by default; use `dev,seed` only when needed. See `README.md`.
- Missing `X-Tenant-ID` will cause 4xx/empty data due to active tenant filters.
- Sanitize any HTML content on the server before persisting or returning.
- Do not expose internal IDs or tenant identifiers in API responses.

### Core paths & files
- Backend code: `backend/src/main/java/com/backend/{presentation|application|domain|infrastructure}`
- Resources: `backend/src/main/resources/{application*.yml,i18n,migrations}`
- Admin UI: `storefront/src/app/...`
- Reference docs: `.docs.md`, `plans/IMPLEMENTATION_STATUS.md`, `README.md`

### Handy API checks
```bash
# Health & basic checks
curl -s http://localhost:8080/actuator/health | jq

# i18n check (backend messages)
curl -s -H "Accept-Language: tr" http://localhost:8080/api/debug/messages

# Tenant header example
curl -s -H "X-Tenant-ID: 1" http://localhost:8080/api/pages/1
```

—
Keep this file concise and high‑signal. If a rule conflicts, prefer the Clean Architecture + multi‑tenant/i18n rules captured here and in `.docs.md`.
