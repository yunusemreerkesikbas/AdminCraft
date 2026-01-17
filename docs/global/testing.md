# Testing Patterns

## What it is

Backend test infrastructure and conventions for AdminCraft. All modules follow the same testing patterns using JUnit 5 + Mockito + AssertJ with Builder Pattern for test data.

## Source of truth

Test infrastructure:

- Base test class: [`backend/src/test/java/com/backend/testutil/BaseServiceTest.java`](../../backend/src/test/java/com/backend/testutil/BaseServiceTest.java)
- Test data builders: [`backend/src/test/java/com/backend/testutil/builders/`](../../backend/src/test/java/com/backend/testutil/builders/)

Test locations by layer:

- Service unit tests: `backend/src/test/java/com/backend/application/service/impl/`
- Controller integration tests: `backend/src/test/java/com/backend/presentation/controller/`
- DTO validation tests: `backend/src/test/java/com/backend/presentation/dto/`

## Rules and invariants

### Test class structure

1. **Extend `BaseServiceTest`** for all service unit tests (provides TenantContext setup)
2. **Use `@WebMvcTest`** for controller integration tests
3. **Reset ID counters** in `@BeforeEach` for test isolation
4. **Use Builder Pattern** for test data creation

### Naming conventions

| Element | Convention | Example |
|---------|------------|---------|
| Test class | `{ServiceName}Test` | `ProductServiceImplTest` |
| Test method | `{method}_{scenario}_{expected}` | `delete_ThrowsException_WhenHasProducts` |
| Builder | `{Entity}TestDataBuilder` | `ProductTestDataBuilder` |
| Builder factory | `a{Entity}()` or `an{Entity}()` | `aProduct()`, `anAttributeDefinition()` |

### Exception types and HTTP status mapping

| Exception | HTTP Status | Use Case |
|-----------|-------------|----------|
| `IllegalArgumentException` | 400 Bad Request | Input validation, entity not found |
| `IllegalStateException` | 400 Bad Request | Business rule violation (e.g., delete with children) |
| `BusinessRuleViolationException` | **409 Conflict** | Domain-specific business rules (e.g., ProductType with products) |
| `TenantContext` not active | 500 Internal Server Error | Missing tenant context |

### TenantContext validation

Every service method must be tested for `TenantContext.validateActive()`:

```java
@Test
void method_ThrowsException_WhenTenantNotActive() {
    simulateInactiveTenantContext();

    assertThatThrownBy(() -> service.method(args))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Tenant context");
}
```

## Common patterns

### BaseServiceTest usage

```java
class MyServiceImplTest extends BaseServiceTest {

    @Mock
    private MyRepository myRepository;

    @InjectMocks
    private MyServiceImpl myService;

    @BeforeEach
    void setUp() {
        MyTestDataBuilder.resetIdCounter();
        // Additional setup
    }

    @Test
    void myMethod_Success() {
        // Test implementation
    }
}
```

### Test data builder pattern

```java
public class ProductTestDataBuilder {
    private static final AtomicLong ID_COUNTER = new AtomicLong(1);

    private Long id;
    private String sku;
    // ... other fields

    private ProductTestDataBuilder() {
        long currentId = ID_COUNTER.getAndIncrement();
        this.id = currentId;
        this.sku = "SKU-" + String.format("%05d", currentId);
        // ... defaults
    }

    public static ProductTestDataBuilder aProduct() {
        return new ProductTestDataBuilder();
    }

    public ProductTestDataBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public Product build() {
        Product product = new Product();
        product.setId(id);
        product.setSku(sku);
        // ... set all fields
        return product;
    }

    public static void resetIdCounter() {
        ID_COUNTER.set(1);
    }
}
```

### Controller integration test pattern

```java
@WebMvcTest(MyController.class)
class MyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MyService myService;

    @MockBean
    private MessageSource messageSource;

    @Test
    void endpoint_Success() throws Exception {
        when(myService.method(any())).thenReturn(result);

        mockMvc.perform(get("/api/endpoint")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void endpoint_Returns409_WhenBusinessRuleViolation() throws Exception {
        doThrow(new BusinessRuleViolationException("Cannot delete"))
            .when(myService).delete(anyLong());

        mockMvc.perform(delete("/api/endpoint/1"))
            .andExpect(status().isConflict()); // 409 NOT 400
    }
}
```

### DTO validation test pattern

```java
class MyDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void dto_ValidInput() {
        MyRequest request = new MyRequest("valid", "data");
        Set<ConstraintViolation<MyRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void dto_FailsWhenFieldMissing() {
        MyRequest request = new MyRequest(null, "data");
        Set<ConstraintViolation<MyRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("field"));
    }
}
```

## Test categories

### Service unit tests (must cover)

- **Happy path**: All CRUD operations succeed with valid input
- **Validation errors**: Missing required fields, invalid values
- **Entity not found**: Throw `IllegalArgumentException`
- **Business rules**: Deletion constraints, uniqueness checks
- **Tenant validation**: `TenantContext.validateActive()` check
- **Default values**: Auto-set defaults when null (currency, status, visibility)

### Controller integration tests (must cover)

- **HTTP 200**: Successful operations
- **HTTP 400**: Validation errors, `IllegalArgumentException`, `IllegalStateException`
- **HTTP 404**: Entity not found (for GET by ID)
- **HTTP 409**: `BusinessRuleViolationException` (critical distinction from 400)

### DTO validation tests (must cover)

- `@NotNull`, `@NotBlank`, `@NotEmpty` constraints
- `@Size` constraints (min/max)
- `@Pattern` constraints (regex)
- `@Valid` on nested objects

## Running tests

```bash
# All tests
mvn test

# Specific service tests
mvn test -Dtest="ProductServiceImplTest"

# All service tests
mvn test -Dtest="*ServiceImplTest"

# Controller integration tests
mvn test -Dtest="*ControllerIntegrationTest"

# DTO validation tests
mvn test -Dtest="*ValidationTest"

# Coverage report
mvn jacoco:report
# Report: target/site/jacoco/index.html
```

## Gotchas

### BusinessRuleViolationException vs IllegalStateException

- Use `BusinessRuleViolationException` for **domain-specific** business rules that return **409 Conflict**
  - Example: Cannot delete ProductType with assigned products
- Use `IllegalStateException` for **general state violations** that return **400 Bad Request**
  - Example: Cannot delete Category with children

### TenantContext in tests

- `BaseServiceTest` automatically sets up TenantContext in `@BeforeEach`
- Use `simulateInactiveTenantContext()` to test tenant validation
- Always call `resetIdCounter()` on builders to ensure test isolation

### @WebMvcTest scope

- Only loads the specified controller and web layer beans
- Mock all service dependencies with `@MockBean`
- Include `@MockBean MessageSource messageSource` for i18n support

### Avoiding flaky tests

- Use `AtomicLong` ID counters with `resetIdCounter()` in `@BeforeEach`
- Avoid `any()` matchers when specific values matter for verification
- Use `argThat()` for complex argument matching
