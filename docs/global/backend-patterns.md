# Backend Patterns

## Flyway migrations

Tenant migrations live under:

- `backend/src/main/resources/db/tenant/{module}/`

Examples confirmed in the repo:

- `backend/src/main/resources/db/tenant/media/V20__media_baseline.sql`
- `backend/src/main/resources/db/tenant/pagebuilder/V15__page_templates.sql`
- `backend/src/main/resources/db/tenant/core/V17__navigation_nodes.sql`

Platform migrations/seeds live under:

- `backend/src/main/resources/db/platform/`

## Modules catalog sync (manual)

When adding a new tenant module, follow:

- [`backend/docs/MODULE_SYNC_CHECKLIST.md`](../../backend/docs/MODULE_SYNC_CHECKLIST.md)

## API base path

The backend runs under `/api` context path:

- [`backend/src/main/resources/application.yml`](../../backend/src/main/resources/application.yml)

So controller mappings are addressed as `/api/{controllerMapping}`.

## Response wrapper

Controllers return a wrapped response type:

- `ApiResponse<T>` (see usage in controllers, e.g. `CmsDeliveryController` and `MediaController`)

## API response filtering

All API responses automatically exclude null, empty strings, empty arrays, and empty maps to reduce payload size and improve readability.

**Global configuration:**
- `JacksonConfig` sets `JsonInclude.Include.NON_NULL` globally
- Location: `backend/src/main/java/com/backend/infrastructure/config/JacksonConfig.java`

**Response value filtering:**
- Use `ResponseValueFilter` utility class in DTO factory methods:
  - `ResponseValueFilter.filterEmptyString(String)` - Converts empty/whitespace to `null`
  - `ResponseValueFilter.filterEmptyCollection(Collection<T>)` - Converts empty collections to `null`
  - `ResponseValueFilter.filterEmptyMap(Map<K, V>)` - Converts empty maps to `null`
- Location: `backend/src/main/java/com/backend/shared/util/ResponseValueFilter.java`

**Example:**
```java
public static ProductCompositeResponse from(Product entity, Currency currency) {
    Map<String, Object> attributes = ResponseValueFilter.filterEmptyMap(
        entity.getAttributes() != null
            ? entity.getAttributes().stream()
                .collect(Collectors.toMap(...))
            : null
    );
    // ...
}
```

**Filtered values:**
- Empty strings (`""`) → `null` → excluded
- Empty arrays (`[]`) → `null` → excluded
- Empty maps (`{}`) → `null` → excluded
- `null` values → excluded (via Jackson `NON_NULL`)
- **Note**: `false` boolean values are preserved

**Frontend integration:**
- Use nullish coalescing (`??`) instead of logical OR (`||`)
- Example: `product.categories?.map(...) ?? []` instead of `product.categories?.map(...) || []`

## Async jobs (platform operations)

Provisioning and migration sync are implemented as asynchronous jobs:

- `backend/src/main/java/com/backend/presentation/ProvisioningController.java`
- `backend/src/main/java/com/backend/application/service/ProvisioningServiceImpl.java`

## Repository patterns

Repository implementations in `infrastructure/persistence/repository/` should annotate write methods with `@Transactional`:

- `save()`, `saveAll()` → `@Transactional`
- `delete()`, `deleteById()`, custom delete methods → `@Transactional`
- Read methods do not require explicit annotation (Spring Data default is sufficient)

Example:

```java
@Override
@Transactional
public void deleteByProductId(Long productId) {
    jpaRepository.deleteByProductId(productId);
}
```

## Exception handling

Custom exceptions in `domain/exception/`:

| Exception                        | HTTP Status | Use Case                                                           |
| -------------------------------- | ----------- | ------------------------------------------------------------------ |
| `EntityNotFoundException`        | 404         | Entity not found by ID/UID                                         |
| `BusinessRuleViolationException` | 409         | Business rule constraint violated (e.g., delete with dependencies) |

Handlers are in `GlobalExceptionHandler`.
