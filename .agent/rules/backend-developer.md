---
trigger: model_decision
description: Senior Java Developer: SOLID, DRY, KISS, YAGNI, OWASP best practices. Spring Boot 3, Java 21, Spring Data JPA, Lombok, MySQL, Flyway
---

# Backend Developer — Spring Boot 3.3.5 / Java 21

## Stack

Spring Boot 3.3.5, Java 21, Spring Data JPA, Lombok, MySQL, Flyway, Resilience4j

---

## Clean Architecture

```
Presentation → Application → Domain ← Infrastructure
```

### Layer Violation Rules (CRITICAL)

| From Layer         | Can Import          | CANNOT Import                |
| ------------------ | ------------------- | ---------------------------- |
| **Presentation**   | Application, Domain | Infrastructure               |
| **Application**    | Domain              | Presentation, Infrastructure |
| **Domain**         | Nothing             | ALL other layers             |
| **Infrastructure** | Domain, Application | Presentation                 |

### Package Structure

```
com.backend.presentation     → Controllers, Request/Response DTOs
com.backend.application      → Services, Use Cases
com.backend.domain           → Entities, Repository Interfaces, Enums
com.backend.infrastructure   → Repository Implementations, Config
```

---

## Multi-Tenant (Database-per-Tenant)

- Platform DB: `platform_management` (control plane)
- Tenant DBs: `ac_subdomain_{id}` (data plane, isolated)
- ❌ NO `tenant_id` columns in tenant entities
- ✅ HikariCP cache (LRU: max 10 pools, 5 conn, 30m idle)
- ✅ TenantContext: ThreadLocal with `tenantId` + `tenantDbName`
- ✅ TenantFilter: validate active, set/clear in finally
- ✅ MDC: `tenantId`, `tenantDb`, `correlationId`

---

## Naming Conventions

| Element      | Convention            | Example                               |
| ------------ | --------------------- | ------------------------------------- |
| Class        | PascalCase            | `PageService`, `MediaController`      |
| Interface    | PascalCase            | `PageRepository`, `TenantContextPort` |
| Method       | camelCase             | `findByUid()`, `createPage()`         |
| Variable     | camelCase             | `pageStatus`, `tenantId`              |
| Constant     | SCREAMING_SNAKE       | `MAX_FILE_SIZE`, `DEFAULT_LANGUAGE`   |
| Package      | lowercase             | `com.backend.application.service`     |
| Entity       | Singular noun         | `Page`, `User`, `Media`               |
| DTO Request  | PascalCase + Request  | `PageCreateRequest`                   |
| DTO Response | PascalCase + Response | `PageDetailResponse`                  |
| Enum         | PascalCase            | `PageStatus`, `Language`              |
| Enum Value   | SCREAMING_SNAKE       | `PUBLISHED`, `IN_PROGRESS`            |

---

## Domain Layer

**Tenant entities**: Extend `BaseEntity`, NO tenant_id column

```java
@Entity
@Table(name = "pages")
public class Page extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String uid;

    @Enumerated(EnumType.STRING)
    private PageStatus status;
}
```

**i18n entities**: Extend `BaseI18nEntity`

```java
@Entity
@Table(name = "page_i18n")
public class PageI18n extends BaseI18nEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", nullable = false)
    private Page page;

    private String title;
    private String urlPath;
}
```

**Platform entities**: Use `@Qualifier("platformDataSource")`

---

## Application Layer

**Services**: Constructor injection, Response DTOs only

```java
@Service
@RequiredArgsConstructor
public class PageServiceImpl implements PageService {
    private final PageRepository pageRepository;
    private final PageI18nRepository i18nRepository;

    @Transactional
    public PageResponse create(PageCreateRequest request) {
        // Business logic here
        var page = new Page();
        page.setUid(UuidUidGenerator.generateUid("page"));
        return PageResponse.from(pageRepository.save(page));
    }
}
```

---

## Presentation Layer

**Controllers**: Return `ResponseEntity<ApiResponse<T>>`

```java
@RestController
@RequestMapping("/api/pages")
@RequiredArgsConstructor
public class PageController {
    private final PageService pageService;

    @PostMapping
    public ResponseEntity<ApiResponse<PageResponse>> create(
            @Valid @RequestBody PageCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(pageService.create(request)));
    }
}
```

---

## Flyway Migrations

- **Platform**: `db/platform/V1__baseline.sql`, `R__seed.sql`
- **Tenant**: `db/tenant/{module}/V*__*.sql`
- Global sequential versioning across modules
- `hibernate.ddl-auto=none`
- `utf8mb4` / `utf8mb4_unicode_ci`
- ❌ NO idempotent DDL logic
- Only `CREATE DATABASE` can use string concatenation

---

## Security (OWASP)

- ✅ Bean Validation: `@NotNull`, `@Size`, `@Valid`
- ✅ JPQL parameterized queries only
- ❌ Never log passwords, tokens, PII
- ✅ Truncate API errors (500 chars)
- ✅ Log full stacktrace with `correlationId`
- ✅ Rate limiting: 5 req/min (provisioning)

---

## Reusable Code (DRY Principle)

Define repeating code blocks in common locations:

| Location                           | Purpose                                        |
| ---------------------------------- | ---------------------------------------------- |
| `domain/entity/BaseEntity`         | UUID/UID generation, audit fields              |
| `domain/entity/BaseI18nEntity`     | i18n base with language field                  |
| `domain/exception/`                | ResourceNotFoundException, ValidationException |
| `application/dto/`                 | Common request/response DTOs                   |
| `infrastructure/config/`           | Shared configurations                          |
| `application/service/*ServiceImpl` | Reusable service methods                       |

```java
// ✅ Correct: Extend base entities
public class Page extends BaseEntity { }
public class PageI18n extends BaseI18nEntity { }

// ✅ Correct: Use shared exceptions
throw new ResourceNotFoundException("Page", uid);

// ❌ Wrong: Duplicate utility code
// Move to common service or helper class
```

---

## Business Logic (CRITICAL)

**All business logic must be in the Application Layer (services)!**

| Application Layer            | Presentation Layer |
| ---------------------------- | ------------------ |
| ✅ Calculations              | ❌                 |
| ✅ Validations               | ❌                 |
| ✅ Data transformation       | ❌                 |
| ✅ Business rules            | ❌                 |
| ✅ Aggregations              | ❌                 |
| ✅ Complex filtering/sorting | ❌                 |

```java
// ✅ CORRECT: Service handles calculation
@Service
public class MediaServiceImpl {
    public MediaStatsResponse getStats() {
        return MediaStatsResponse.builder()
            .totalCount(repository.count())              // Backend calculates
            .totalSize(repository.sumFileSize())         // Backend aggregates
            .byFormat(repository.countByFormat())        // Backend groups
            .build();
    }
}

// ❌ WRONG: Controller contains business logic
@GetMapping("/stats")
public ResponseEntity<?> getStats() {
    var items = service.findAll();
    var total = items.stream().mapToLong(Media::getSize).sum(); // NO!
}
```

---

## Response DTO Pattern

**Always send calculated/formatted data to frontend:**

```java
// ✅ CORRECT: Response includes calculated fields
public record MediaDetailResponse(
    Long id,
    String uid,
    String fileName,
    long fileSize,
    String formattedSize,           // Backend formats: "2.5 MB"
    LocalDateTime createdAt,
    String createdAtFormatted,      // Backend formats: "3 gün önce"
    int usageCount,                 // Backend counts references
    List<MediaFormatResponse> formats
) {
    public static MediaDetailResponse from(Media media, int usageCount) {
        return new MediaDetailResponse(
            media.getId(),
            media.getUid(),
            media.getFileName(),
            media.getFileSize(),
            FileUtils.formatFileSize(media.getFileSize()),
            media.getCreatedAt(),
            TimeUtils.formatRelative(media.getCreatedAt()),
            usageCount,
            media.getFormats().stream().map(MediaFormatResponse::from).toList()
        );
    }
}
```

---

## Quick Checklist

| Category      | Rule                                     |
| ------------- | ---------------------------------------- |
| Architecture  | Application layer = business logic       |
| Injection     | Constructor only (no @Autowired)         |
| Multi-tenancy | NO tenant_id columns                     |
| Platform      | @Qualifier("platformDataSource")         |
| Context       | TenantFilter try-finally + MDC           |
| Transactions  | @Transactional for multi-step            |
| Queries       | @EntityGraph to avoid N+1                |
| DTOs          | Request/Response suffixes                |
| Responses     | Include calculated/formatted data        |
| Shared Code   | Extend BaseEntity, use common exceptions |
| Business      | ✅ All logic in Application layer        |
| Comments      | ❌ No code comments                      |
| Logging       | ❌ No System.out.println                 |
| Defensive     | ❌ No defensive programming              |
