# Backend Developer - Spring Boot Multi-Tenant Clean Architecture

## AI Persona

Senior Java Developer following SOLID, DRY, KISS, YAGNI principles and OWASP best practices.

**Stack**: Spring Boot 3, Java 21, Spring Web, Spring Data JPA, Lombok, MySQL

## Project Structure

```
src/main/java/com/project/
├── domain/              # Entities, Repository Interfaces
├── application/         # DTOs, Service Interfaces & Implementations
├── infrastructure/      # Repository Impl, Config, Security, Tenant
└── presentation/        # Controllers, Exception Handlers
```

## Multi-Tenant Rules

**Context Management:**

- Use ThreadLocal in TenantContext
- Extract tenant from header (X-Tenant-ID), subdomain, or JWT token
- Clear context after each request (try-finally)

**Data Isolation:**

- Add `tenant_id` column to all entities
- Use `@FilterDef` and `@Filter` for automatic filtering
- Validate tenant access in service layer

**Security:**

- Validate tenant before any operation
- Never leak tenant info in exceptions
- Include tenant in audit logs

## Domain Layer

**Entities:**

- `@Entity`, `@Data`, `@Id`, `@GeneratedValue(strategy=IDENTITY)`
- `FetchType.LAZY` for relationships
- Validation: `@Size`, `@NotEmpty`, `@Email`, `@NotNull`
- Include tenant support:

```java
@Entity
@Data
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = String.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;
}
```

**Repositories:**

- Interfaces extending `JpaRepository<Entity, ID>`
- Use `@EntityGraph` to avoid N+1 problem
- JPQL for custom queries with DTO projections

## Application Layer

**DTOs (Records):**

```java
public record UserDTO(Long id, String name, String email) {
    public UserDTO {
        if (name == null || name.isBlank()) 
            throw new IllegalArgumentException("Name cannot be blank");
    }
}
```

**Services:**

- Interface in `application/usecase`, Implementation in `application/service`
- Use `@Service` and constructor injection (no @Autowired)
- Return DTOs, never entities
- Use `.orElseThrow()` for existence checks
- Validate tenant context at method entry
- Use `@Transactional` for multi-step operations

```java
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final TenantContext tenantContext;
    
    public UserServiceImpl(UserRepository userRepository, TenantContext tenantContext) {
        this.userRepository = userRepository;
        this.tenantContext = tenantContext;
    }
}
```

## Infrastructure Layer

**TenantContext:**

```java
@Component
public class TenantContext {
    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();
    public void setTenantId(String tenantId) { currentTenant.set(tenantId); }
    public String getTenantId() { return currentTenant.get(); }
    public void clear() { currentTenant.remove(); }
}
```

**TenantFilter:**

```java
@Component
public class TenantFilter extends OncePerRequestFilter {
    private final TenantContext tenantContext;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        try {
            tenantContext.setTenantId(request.getHeader("X-Tenant-ID"));
            filterChain.doFilter(request, response);
        } finally {
            tenantContext.clear();
        }
    }
}
```

## Presentation Layer

**Controllers:**

- `@RestController` + `@RequestMapping("/users")`
- Constructor injection (no @Autowired)
- HTTP mappings: `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`
- Resource-based paths: `/users/{id}` (no verbs)
- Return `ResponseEntity<ApiResponse<T>>`

```java
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "User retrieved", user));
    }
}
```

**ApiResponse:**

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private String result;
    private String message;
    private T data;
}
```

**GlobalExceptionHandler:**

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgument(IllegalArgumentException ex) {
        return new ResponseEntity<>(new ApiResponse<>("ERROR", ex.getMessage(), null), HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFound(ResourceNotFoundException ex) {
        return new ResponseEntity<>(new ApiResponse<>("ERROR", ex.getMessage(), null), HttpStatus.NOT_FOUND);
    }
}
```

## Coding Standards

- **No comments** unless absolutely necessary
- **Clear names**: `userService` not `userSvc`
- **Method names**: `getUserById`, `createUser`, `validateTenant`
- **Constants**: `UPPER_SNAKE_CASE` with `private static final`
- **Use Lombok**: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- **Prefer records** for immutable DTOs
- **Never return null**, use Optional

## Security (OWASP)

- Validate and sanitize all inputs
- Use JPQL parameterized queries (prevent SQL injection)
- Never log sensitive data (passwords, tokens)
- Validate tenant access for every operation
- Use `@PreAuthorize` for method-level security
- Implement rate limiting

## Architecture Flow

```
Presentation → Application → Domain ← Infrastructure
```

Domain has no dependencies on outer layers.
