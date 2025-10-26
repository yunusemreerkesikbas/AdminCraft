## AdminCraft — Developer Quickstart

### Tech Stack

- Backend: Spring Boot 3, Java 21, JPA, MySQL, Flyway
- Frontend: Angular 19, TypeScript, Signals
- Architecture: Clean Architecture (presentation → application → domain → infrastructure)
- Multi-Tenancy: Database-per-tenant (`platform_management` + `ac_tenant_{id}`)

### Commands

```bash
# Backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Frontend
cd storefront && npm ci && npm run start

# Docker DB
docker compose up -d
```

### Multi-Tenant (Database-per-Tenant)

**Key Principles:**

- Platform DB (`platform_management`): Control plane (tenants, modules, jobs)
- Tenant DBs (`ac_tenant_{id}`): Data plane (isolated per tenant)
- ❌ NO tenant_id columns (physical isolation)
- ✅ Header: `X-Tenant-ID` (Angular interceptor required)
- ✅ TenantContext: ThreadLocal with `tenantId` + `tenantDbName`
- ✅ TenantFilter: Validate active, set/clear in finally
- ✅ HikariCP: LRU cache (max 10 pools, 5 conn, 30m idle)

**Security:**

- Validate tenant active before ANY operation
- MDC logging: `tenantId`, `tenantDb`, `correlationId`
- Truncate API errors (500 chars), log full stacktrace

### Database Migrations (Flyway)

  **Platform** (`db23latform/`): Auto-run on startup

- `V1__baseline.sql` (versioned DDL)
- `R__seed_modules.sql` (repeatable seeds)

**Tenant** (`db/tenant/{module}/`): Programmatic via ProvisioningService

- `core/V1__baseline.sql` (required)
- `pagebuilder/V1__baseline.sql` (module-specific)

**Rules:**

- `hibernate.ddl-auto=none` (Flyway owns schema)
- utf8mb4 / utf8mb4_unicode_ci
- NO idempotent DDL logic (Flyway handles it)
- CREATE DATABASE is ONLY string-concatenated SQL

### Clean Architecture

**Domain**: Entities (BaseEntity, BaseI18nEntity), Repositories  
**Application**: Commands/Queries (NOT Presentation DTOs), Services, Response DTOs  
**Infrastructure**: Repos impl, Config, Multi-tenancy  
**Presentation**: Controllers (map Request DTOs → Commands/Queries)

**i18n Pattern:**

- Base entity: Language-agnostic fields
- i18n entity: Extend BaseI18nEntity, @ManyToOne to base
- API: `/api/{resource}/{id}/i18n/{language}` (PUT/GET)
- UI: Tabs (General | Türkçe | English)

### Provisioning

**Workflow:**

1. POST `/api/provisioning/tenants/{id}/provision` with `{ modules: ["core", "pagebuilder"] }`
2. Creates ProvisioningJob (pending → running → succeeded/failed)
3. @Async: Create DB → Flyway migrations → Progress (10% → 100%)
4. GET `/api/provisioning/jobs/{jobId}` for polling (every 2s)

**Rules:**

- Core module always required
- Module dependencies auto-selected
- Errors truncated (500 chars), logged with correlationId

### Frontend CRUD Patterns

**Base Classes** (`core/crud/`):

- `CrudHttpService<T>`: Auto-unwrap ApiResponse, CRUD methods
- `CrudStore<T>`: Signals (items, isLoading, error)
- `BaseCrudListComponent<T>`: Lifecycle hooks (beforeLoad, fetchItems, onLoadSuccess)
- `BaseCrudFormComponent<T>`: Hooks (beforeCreate, beforeUpdate)

**Example:**

```typescript
@Injectable({ providedIn: 'root' })
export class PageService extends CrudHttpService<Page, CreateDto, UpdateDto> {
  protected endpoints = { list: 'pages', getById: 'pageById', ... };
}

export class SpaPageListComponent extends BaseCrudListComponent<Page> {
  protected service = inject(PageService);
  protected store = new CrudStore<Page>();
  protected override fetchItems() { return this.service.list(); }
}
```

**Best Practices:**

- OnPush change detection, standalone components
- `spa-` prefix, explicit types, private with `#`
- Use signals or async pipe
- Polling: unsubscribe in ngOnDestroy
- Centralized API_ENDPOINTS

### Code Style

**Backend:**

- Constructor injection (no @Autowired)
- Commands/Queries in application (NOT Presentation DTOs)
- Return Response DTOs only
- @EntityGraph for relationships (avoid N+1)
- @Transactional for multi-step ops
- JPQL parameterized only (prevent SQL injection)

**Frontend:**

- Extend CrudHttpService / BaseCrudListComponent
- Signals: store.isLoading(), filtered()
- take(1) for one-time ops
- Dialog data typed with interfaces

### Security (OWASP)

- Bean Validation on inputs
- JPQL parameterized (no string concat except CREATE DATABASE)
- Never log sensitive data (passwords, tokens, PII)
- Validate tenant active before routing
- Rate limiting (5 req/min on provisioning)

### Gotchas

- Missing X-Tenant-ID → 4xx/empty data
- Platform entities need @Qualifier("platformDataSource")
- Tenant entities: NO tenant_id column
- Flyway migrations: idempotent (R__) or versioned (V__)
- CREATE DATABASE: only string-concatenated SQL allowed
- Polling: must unsubscribe in ngOnDestroy

### Quick Checks

```bash
# Health
curl -s http://localhost:8080/actuator/health | jq

# Module catalog
curl -s http://localhost:8080/api/provisioning/modules/catalog | jq

# Tenant data
curl -s -H "X-Tenant-ID: 1" http://localhost:8080/api/pages | jq
```

---
**Paths**: `backend/src/main/java/com/backend/`, `storefront/src/app/`, `backend/src/main/resources/db/`  
**Docs**: `.backendrules`, `.frontendrules`, `.codereviewer`, `plans/
