# GitHub Copilot Instructions for Craftive

## Project Context

Craftive is a **multi-tenant platform** built with Clean Architecture principles.

**Stack:**
- Backend: Spring Boot 3.3.5, Java 21, MySQL (database-per-tenant)
- Frontend: Angular 19, TypeScript 5.6.3, Signals, RxJS
- Architecture: Clean Architecture with strict layer boundaries

**Key Principle:** Physical tenant isolation via separate databases (`ac_subdomain_{id}`), NO `tenant_id` columns.

---

## CRITICAL RULES (Always Check First)

### 1. Clean Architecture Boundaries (NEVER Violate)

```
Presentation → Application → Domain ← Infrastructure
```

**Import Rules:**
- ❌ **REJECT**: Application importing Infrastructure
- ❌ **REJECT**: Domain importing ANY other layer
- ❌ **REJECT**: Application importing Presentation
- ✅ **ACCEPT**: Presentation → Application → Domain
- ✅ **ACCEPT**: Infrastructure → Domain, Application

**Example violations to reject:**
```java
// ❌ Application layer
import com.backend.infrastructure.persistence.*; // REJECT

// ❌ Domain layer
import com.backend.application.*; // REJECT
```

### 2. Multi-Tenancy (Database-per-Tenant)

- ❌ **NO `tenant_id` columns** (physical isolation only)
- ✅ Use `TenantContext` for tenant scope
- ✅ Platform entities: `@Qualifier("platformDataSource")`
- ✅ Set/clear context in `try-finally` blocks

### 3. Business Logic Placement

**ALL business logic MUST be in Application layer (Services), NOT in:**
- ❌ Controllers (Presentation)
- ❌ Frontend Components
- ❌ Repository implementations

**Rule:** Backend calculates, frontend displays.

---

## Technology Version Enforcement

### Backend (Spring Boot 3.3.5 / Java 21)

✅ Use:
- `jakarta.*` packages (NOT `javax.*`)
- Constructor injection (NO `@Autowired`)
- Record DTOs: `record PageRequest(String title) {}`
- Pattern matching: `if (obj instanceof String s)`

❌ Reject:
- `@Autowired` field/setter injection
- `javax.*` imports
- Deprecated APIs

### Frontend (Angular 19 / TypeScript 5.6.3)

✅ Use:
- Control flow: `@if`, `@for`, `@switch` (NOT `*ngIf`, `*ngFor`)
- Signals: `itemsSig = signal<Item[]>([])`
- Standalone components (NO NgModules)
- Input signals: `id = input.required<number>()`
- Private fields: `#service = inject(Service)`

❌ Reject:
- Old directives: `*ngIf`, `*ngFor`, `*ngSwitch`
- `public` access modifiers (use `protected` or `#private`)
- `console.log` statements
- Getter/setter methods

---

## Naming Conventions (Enforce)

**Backend:**
- DTOs: `PageCreateRequest`, `PageDetailResponse`
- Services: `PageServiceImpl`
- Entities: Singular (`Page`, not `Pages`)

**Frontend:**
- Components: `SpaPageListComponent` (spa- prefix)
- Signals: `itemsSig`, `isLoadingSig`
- Observables: `items$`, `user$`
- Private: `#privateField`

---

## Security (OWASP - Enforce)

✅ Require:
- Bean Validation: `@NotNull`, `@Size`, `@Valid`
- JPQL parameterized queries only
- No logging of passwords, tokens, PII
- Rate limiting on provisioning

❌ Reject:
- SQL string concatenation (except CREATE DATABASE)
- Missing `@Valid` on controllers
- Logging sensitive data

---

## Code Quality Checklist

**Before approving any PR, verify:**

- [ ] No Clean Architecture violations
- [ ] No `tenant_id` columns in entities
- [ ] Business logic in Application layer only
- [ ] Angular 19 syntax (no old directives)
- [ ] Proper naming conventions
- [ ] Security validations present
- [ ] No console.log or System.out.println

---

## Integration Points

- Workflows: `docker compose up -d`, `mvn spring-boot:run`, `npm run start`
- Swagger: `http://localhost:8080/api/swagger-ui/index.html`
- Docs: `docs/global/` for architecture patterns
- Modules: Defined in `ModuleCode.java` enum

---

**For detailed review rules:** `.github/instructions/code-reviewer.instructions.md`
