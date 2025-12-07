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
- ✅ TenantContext: ThreadLocal with `tenantId` + `tenantDbName`
- ✅ TenantFilter: Validate active, set/clear in finally
- ✅ HikariCP: LRU cache (max 10 pools, 5 conn, 30m idle)

**Security:**

- Validate tenant active before ANY operation
- MDC logging: `tenantId`, `tenantDb`, `correlationId`
- Truncate API errors (500 chars), log full stacktrace

### Database Migrations (Flyway)

**Platform** (`db/platform/`): Auto-run on startup

- `V1__baseline.sql` (versioned DDL)
- `R__seed_modules.sql` (repeatable seeds)

**Tenant** (`db/tenant/{module}/`): Programmatic via ProvisioningService

- `core/V1__baseline.sql` (required)
- `pagebuilder/V3__baseline.sql` (module-specific)
- `component_library/V4__component_library_baseline.sql` (module-specific)

**Rules:**

- `hibernate.ddl-auto=none` (Flyway owns schema)
- utf8mb4 / utf8mb4_unicode_ci
- NO idempotent DDL logic (Flyway handles it)
- CREATE DATABASE is ONLY string-concatenated SQL

**Versioning Strategy (IMPORTANT):**

- **Global Sequential Versioning**: All tenant modules share a single version sequence
- Flyway scans ALL module locations together and enforces unique version numbers
- Version allocation:
  - **V1-V2**: `core` module (users, roles, sites)
  - **V3**: `pagebuilder` module (pages, categories)
  - **V4-V5**: `component_library` module (components, types)
  - **V6+**: Reserved for future modules

**When adding a new tenant module:**

1. Check the latest version across ALL modules in `db/tenant/`
2. Start new module at next available version (e.g., V6, V7, ...)
3. Use descriptive names: `V6__new_module_baseline.sql`
4. Update this documentation with version allocation

### Clean Architecture

**Layer Responsibilities:**

- **Domain**: Entities (BaseEntity, BaseI18nEntity), Repository interfaces, Enums
- **Application**: Services, Commands/Queries, Use Cases (business logic orchestration)
- **Infrastructure**: Repository implementations, Config, Multi-tenancy (TenantFilter, TenantContext)
- **Presentation**: Controllers, Request DTOs, Response DTOs

**Dependency Rule:**

- Presentation → Application → Domain ← Infrastructure
- Domain has NO dependencies on other layers
- Application layer should NOT import from Infrastructure or Presentation

**i18n Pattern:**

- Base entity: Language-agnostic fields
- i18n entity: Extend BaseI18nEntity, @ManyToOne to base
- API: `/api/{resource}/{id}/i18n/{language}` (PUT/GET)
- UI: Tabs (General | Türkçe | English)

### Module Management

**Active Modules (5):**

- `core` (required): users, roles, sites
- `pagebuilder`: pages + page_categories (mandatory)
- `site_settings`: global/i18n config
- `media`: media files with i18n
- `component_library`: reusable UI components with i18n

**Manual Sync Process:**

When adding new module:

1. `db/platform/R__seed_modules.sql` → INSERT
2. `domain/enums/ModuleCode.java` → Enum entry
3. `core/navigation/navigation-modules.constants.ts` → Constant
4. `db/tenant/{module}/V*.sql` → Migration

**Rules:**

- page_categories NOT shown in provision dialog (part of pagebuilder)
- Navigation shows when required module enabled
- All modules type: 'core' (no b2c/b2b grouping)

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
- Rate limiting:
  - Provisioning: 5 req/min per tenant
  - CMS Delivery: 100 req/min per tenant

**Public API (CMS Delivery):**

- Endpoints: `/api/cms/components/{uid}`, `/api/cms/components?uids=`
- Tenant resolution via `X-Tenant-ID` or `X-Tenant-Subdomain` headers
- TenantFilter handles validation (not whitelisted)
- No authentication required, rate limited

### Gotchas

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

```

---
**Paths**: `backend/src/main/java/com/backend/`, `storefront/src/app/`, `backend/src/main/resources/db/`  
**Docs**: `.backendrules`, `.frontendrules`, `.codereviewer`, `plans/`
