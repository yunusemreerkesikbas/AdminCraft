# Product/Type/Category Module - Security & Performance Implementation Plan

## 📋 Executive Summary

This plan addresses critical security and performance issues identified in the Product/Type/Category module code review. Implementation is divided into prioritized phases based on risk and impact.

---

## 🎯 Implementation Phases

### ✅ Phase 1: Code Cleanup (IMMEDIATE - This PR)

**Status**: IN PROGRESS  
**Risk**: Low  
**Effort**: 2 hours  

#### Task 1.1: Refactor parseLanguage() Method

**Issue**: Code duplication - `parseLanguage()` method repeated in 7 controllers

**Files Affected**:
- `ProductController.java`
- `CategoryController.java`
- `ProductTypeController.java`
- `PageController.java` (if exists)
- `MediaController.java` (if exists)
- Other controllers with language parsing

**Solution**: Use existing `Language.fromCodeOrDefault(String code)` method

**Implementation**:
```java
// ❌ OLD: Each controller has this
private Language parseLanguage(String lang) {
    try {
        return Language.valueOf(lang.toUpperCase());
    } catch (Exception e) {
        return Language.TR;
    }
}

// ✅ NEW: Use existing enum method
Language language = Language.fromCodeOrDefault(lang);
```

**Benefits**:
- Removes ~35 lines of duplicated code
- Uses existing, tested functionality
- Handles edge cases (null, empty, invalid codes)
- Supports both code ("tr") and uppercase enum name ("TR")

**Testing**:
- [ ] Verify all controllers compile
- [ ] Test with valid language codes (tr, en, es)
- [ ] Test with invalid codes (xxx, null, empty)
- [ ] Verify default language (TR) is returned for invalid input

---

### 🔴 Phase 2: Critical Security Fixes (URGENT - Next Sprint)

**Status**: PLANNED  
**Risk**: HIGH  
**Effort**: 1 week  

#### Task 2.1: Add Multi-Tenant Validation

**Issue**: Missing tenant context validation - risk of cross-tenant data leakage

**Files Affected**:
- `ProductServiceImpl.java` (15+ methods)
- `CategoryServiceImpl.java` (10+ methods)
- `ProductTypeServiceImpl.java` (8+ methods)

**Solution**: Add `TenantContext.validateActive()` at service method entry points

**Implementation Strategy**:

**Option A: Manual Validation (Recommended)**
```java
@Transactional
public Product createComposite(...) {
    TenantContext.validateActive();  // Add this line
    
    // Rest of the method
    Product product = new Product();
    // ...
}
```

**Pros**: Explicit, clear, easy to debug  
**Cons**: Requires change in ~35 methods  

**Option B: AOP Interceptor**
```java
@Aspect
@Component
public class TenantValidationAspect {
    
    @Before("@within(org.springframework.stereotype.Service) && " +
            "execution(public * com.backend.application.service..*(..))")
    public void validateTenant(JoinPoint joinPoint) {
        TenantContext.validateActive();
    }
}
```

**Pros**: Automatic, applies to all services  
**Cons**: Harder to debug, magic behavior, may impact performance  

**Option C: Base Service Class**
```java
public abstract class BaseTenantService {
    
    protected void validateTenant() {
        TenantContext.validateActive();
    }
    
    // Common methods
}

@Service
public class ProductServiceImpl extends BaseTenantService {
    
    public Product create(...) {
        validateTenant();  // Call from base class
        // ...
    }
}
```

**Pros**: Reusable, clear inheritance  
**Cons**: Limits flexibility, still requires manual calls  

**Recommended**: **Option A (Manual)** for explicitness and control

**Testing Plan**:
- [ ] Unit test: Service method without tenant context → should throw exception
- [ ] Unit test: Service method with valid tenant context → should succeed
- [ ] Integration test: Cross-tenant access attempt → should be blocked
- [ ] Performance test: Measure validation overhead (should be <1ms)

**Affected Methods**:

**ProductServiceImpl**:
- `createComposite()` ✓
- `updateComposite()` ✓
- `delete()` ✓
- `findById()` ✓
- `findByIdComposite()` ✓
- `findByUid()` ✓
- `findBySku()` ✓
- `findAll()` ✓
- `findAllPaged()` ✓
- `search()` ✓
- `findByCategoryId()` ✓
- `updateStatus()` ✓
- `updateVisibility()` ✓

**CategoryServiceImpl**:
- `createComposite()` ✓
- `updateComposite()` ✓
- `delete()` ✓
- `findById()` ✓
- `findByIdWithI18n()` ✓
- `findByUid()` ✓
- `findByCode()` ✓
- `findAll()` ✓
- `findRootCategories()` ✓
- `getTree()` ✓

**ProductTypeServiceImpl**:
- `create()` ✓
- `update()` ✓
- `delete()` ✓
- `findById()` ✓
- `findByIdWithAttributes()` ✓
- `findByUid()` ✓
- `findByCode()` ✓
- `findAll()` ✓

---

#### Task 2.2: Audit & Fix SQL Injection Risks

**Issue**: Potential SQL injection in search/filter methods

**Files to Audit**:
- `ProductRepositoryImpl.java`
- `CategoryRepositoryImpl.java`
- `ProductTypeRepositoryImpl.java`

**Verification Checklist**:
- [ ] All `@Query` annotations use `@Param` bindings
- [ ] No string concatenation in JPQL queries
- [ ] No `String.format()` or `+` operators in queries
- [ ] Native queries use named parameters

**Expected Pattern**:
```java
// ✅ SAFE: Parameterized query
@Query("SELECT p FROM Product p WHERE " +
       "(:query IS NULL OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
       "(:status IS NULL OR p.status = :status)")
Page<Product> searchWithFilters(@Param("query") String query, 
                                  @Param("status") ProductStatus status, 
                                  Pageable pageable);

// ❌ UNSAFE: String concatenation (if found, must fix)
String jpql = "SELECT p FROM Product p WHERE p.sku LIKE '%" + search + "%'";
```

**Action Items**:
1. Review all repository implementation files
2. Document findings in audit report
3. Fix any unsafe queries immediately
4. Add code review checklist item for future PRs

---

#### Task 2.3: Implement HTML Sanitization (XSS Prevention)

**Issue**: HTML content stored without sanitization - Stored XSS vulnerability

**Files Affected**:
- `ProductServiceImpl.java` (saveTranslations, updateTranslations)
- `CategoryServiceImpl.java` (createComposite, updateComposite)

**Solution**: Sanitize HTML before persistence using Jsoup

**Dependencies**:
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.jsoup</groupId>
    <artifactId>jsoup</artifactId>
    <version>1.17.2</version>
</dependency>
```

**Implementation**:

**Option A: Utility Class (Recommended)**
```java
// backend/src/main/java/com/backend/shared/common/HtmlSanitizer.java
package com.backend.shared.common;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public class HtmlSanitizer {
    
    private static final Safelist RELAXED_SAFELIST = Safelist.relaxed()
        .addTags("h1", "h2", "h3", "h4", "h5", "h6")
        .addAttributes("a", "target", "rel");
    
    public static String sanitize(String html) {
        if (html == null || html.trim().isEmpty()) {
            return html;
        }
        return Jsoup.clean(html, RELAXED_SAFELIST);
    }
    
    public static String sanitizeBasic(String html) {
        if (html == null || html.trim().isEmpty()) {
            return html;
        }
        return Jsoup.clean(html, Safelist.basic());
    }
    
    public static String stripAll(String html) {
        if (html == null || html.trim().isEmpty()) {
            return html;
        }
        return Jsoup.clean(html, Safelist.none());
    }
}
```

**Usage in Services**:
```java
// ProductServiceImpl.java
private void saveTranslations(Product product, Map<Language, ProductI18nDto> translations, Long createdBy) {
    for (Map.Entry<Language, ProductI18nDto> entry : translations.entrySet()) {
        ProductI18n i18n = new ProductI18n();
        i18n.setProduct(product);
        i18n.setLanguage(entry.getKey());
        i18n.setName(entry.getValue().name());
        i18n.setShortDescription(HtmlSanitizer.sanitize(entry.getValue().shortDescription()));  // ← ADD
        i18n.setDescription(HtmlSanitizer.sanitize(entry.getValue().description()));  // ← ADD
        i18n.setSeoTitle(entry.getValue().seoTitle());
        i18n.setSeoDescription(entry.getValue().seoDescription());
        i18n.setCreatedBy(createdBy);
        i18n.setUpdatedBy(createdBy);
        productI18nRepository.save(i18n);
    }
}
```

**Safelist Configuration**:

| Safelist Type | Allowed Tags | Use Case |
|---------------|--------------|----------|
| `relaxed()` | b, i, u, strong, em, a, img, p, div, span, h1-h6, ul, ol, li | Product descriptions (recommended) |
| `basic()` | b, i, u, strong, em, a, p | Simple formatting |
| `none()` | No HTML tags | Plain text only |

**Testing**:
- [ ] Test with clean HTML → should remain unchanged
- [ ] Test with `<script>` tags → should be removed
- [ ] Test with allowed tags (b, i, a) → should remain
- [ ] Test with null/empty → should handle gracefully
- [ ] Performance test with large HTML (10KB+)

**Fields to Sanitize**:
- `ProductI18n.description` ✓
- `ProductI18n.shortDescription` ✓
- `CategoryI18n.description` ✓

---

### 🟡 Phase 3: Performance Optimizations (NEXT SPRINT)

**Status**: PLANNED  
**Risk**: MEDIUM  
**Effort**: 3-5 days  

#### Task 3.1: Add @EntityGraph for Composite Queries

**Issue**: N+1 query problem when loading entities with relationships

**Files Affected**:
- `ProductRepositoryImpl.java`
- `CategoryRepositoryImpl.java`
- `ProductTypeRepositoryImpl.java`

**Solution**: Use `@EntityGraph` to fetch relationships in single query

**Implementation**:
```java
// ProductRepositoryImpl.java
public interface JpaProductRepository extends JpaRepository<Product, Long> {
    
    @EntityGraph(attributePaths = {
        "i18nContent",
        "attributes",
        "attributes.attributeDefinition",
        "categoryLinks",
        "categoryLinks.category",
        "gallery",
        "gallery.media"
    })
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithAll(@Param("id") Long id);
}
```

**Testing**:
- [ ] Enable SQL logging: `spring.jpa.show-sql=true`
- [ ] Load product without @EntityGraph → count SQL queries
- [ ] Load product with @EntityGraph → count SQL queries
- [ ] Verify query count reduction (should be 1 instead of 10+)
- [ ] Performance benchmark: 100 products load time

**Expected Results**:
- Before: 1 + N (i18n) + N (attributes) + N (categories) + N (gallery) = ~40 queries
- After: 1 query with JOINs
- Performance gain: 80-90% reduction in database round trips

---

#### Task 3.2: Batch Operations for Translations

**Issue**: Individual saves in loops - multiple database round trips

**Files Affected**:
- `ProductServiceImpl.java` (saveTranslations, updateTranslations)
- `CategoryServiceImpl.java` (createComposite, updateComposite)

**Solution**: Use `saveAll()` for batch operations

**Implementation**:
```java
// ❌ BEFORE: Individual saves
private void saveTranslations(Product product, Map<Language, ProductI18nDto> translations, Long createdBy) {
    for (Map.Entry<Language, ProductI18nDto> entry : translations.entrySet()) {
        ProductI18n i18n = new ProductI18n();
        // ... set fields
        productI18nRepository.save(i18n);  // N database calls
    }
}

// ✅ AFTER: Batch save
private void saveTranslations(Product product, Map<Language, ProductI18nDto> translations, Long createdBy) {
    List<ProductI18n> i18nList = translations.entrySet().stream()
        .map(entry -> {
            ProductI18n i18n = new ProductI18n();
            i18n.setProduct(product);
            i18n.setLanguage(entry.getKey());
            i18n.setName(entry.getValue().name());
            i18n.setShortDescription(HtmlSanitizer.sanitize(entry.getValue().shortDescription()));
            i18n.setDescription(HtmlSanitizer.sanitize(entry.getValue().description()));
            i18n.setSeoTitle(entry.getValue().seoTitle());
            i18n.setSeoDescription(entry.getValue().seoDescription());
            i18n.setCreatedBy(createdBy);
            i18n.setUpdatedBy(createdBy);
            return i18n;
        })
        .toList();
    
    productI18nRepository.saveAll(i18nList);  // Single batch operation
}
```

**Configuration** (application.yml):
```yaml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
```

**Testing**:
- [ ] Test with 1 language → should work
- [ ] Test with 5 languages → should batch
- [ ] Enable SQL logging → verify batch INSERT
- [ ] Performance test: 100 products with 5 languages each

**Expected Results**:
- Before: 5 languages = 5 INSERT statements
- After: 5 languages = 1 batch INSERT
- Performance gain: 60-70% faster for multi-language creates

---

#### Task 3.3: Complete DTO Validation

**Issue**: Missing validation annotations on request DTOs

**Files to Audit**:
- `ProductCompositeRequest.java`
- `ProductUpdateRequest.java`
- `CategoryCompositeRequest.java`
- `CategoryUpdateRequest.java`
- `ProductTypeCreateRequest.java`
- `ProductTypeUpdateRequest.java`

**Validation Checklist**:

```java
public record ProductCompositeRequest(
    @NotNull(message = "Product type is required")
    Long productTypeId,
    
    @NotBlank(message = "SKU is required")
    @Size(max = 100, message = "SKU must not exceed 100 characters")
    String sku,
    
    @DecimalMin(value = "0.0", message = "Price must be positive or zero")
    @Digits(integer = 13, fraction = 2, message = "Price format invalid")
    BigDecimal basePrice,
    
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be ISO 4217 code")
    String currency,
    
    @NotNull(message = "Status is required")
    ProductStatus status,
    
    Boolean isVisible,
    
    @Positive(message = "Media ID must be positive")
    Long responsiveMediaId,
    
    @Valid
    @NotEmpty(message = "At least one translation is required")
    Map<Language, ProductI18nRequest> translations,
    
    Map<String, Object> attributes,
    
    List<@Positive Long> categoryIds,
    
    @Positive(message = "Primary category ID must be positive")
    Long primaryCategoryId,
    
    List<@Positive Long> galleryMediaIds
) {}
```

**Testing**:
- [ ] Test with valid data → should succeed
- [ ] Test with null required fields → should return 400 with message
- [ ] Test with invalid format (negative price) → should return 400
- [ ] Test with oversized strings → should return 400
- [ ] Verify error messages are user-friendly

---

### 🟢 Phase 4: Code Quality Improvements (BACKLOG)

**Status**: BACKLOG  
**Risk**: LOW  
**Effort**: 2-3 days  

#### Task 4.1: Add Transaction Isolation Levels

**Files**: All service implementation files

**Implementation**:
```java
@Transactional(isolation = Isolation.READ_COMMITTED)
public Product updateComposite(...) {
    // Prevents dirty reads
}
```

**When to Use**:
- `READ_COMMITTED`: Most operations (default, prevents dirty reads)
- `REPEATABLE_READ`: Critical financial operations
- `SERIALIZABLE`: Inventory management (prevent phantom reads)

---

## 📊 Progress Tracking

### Phase 1: Code Cleanup
- [x] Task 1.1: Refactor parseLanguage() - IN PROGRESS

### Phase 2: Critical Security
- [ ] Task 2.1: Multi-tenant validation - PLANNED
- [ ] Task 2.2: SQL injection audit - PLANNED
- [ ] Task 2.3: HTML sanitization - PLANNED

### Phase 3: Performance
- [ ] Task 3.1: @EntityGraph - PLANNED
- [ ] Task 3.2: Batch operations - PLANNED
- [ ] Task 3.3: DTO validation - PLANNED

### Phase 4: Quality
- [ ] Task 4.1: Transaction isolation - BACKLOG

---

## 🎯 Success Metrics

| Metric | Current | Target | Measurement |
|--------|---------|--------|-------------|
| Code Duplication | 7 instances | 0 | Removed parseLanguage() |
| SQL Queries (composite) | ~40 | 1 | @EntityGraph optimization |
| XSS Vulnerabilities | 2 fields | 0 | HTML sanitization |
| Validation Coverage | ~60% | 100% | All DTOs validated |
| Batch Performance | 5 queries | 1 query | saveAll() usage |

---

## ❓ Open Questions for Discussion

1. **Tenant Validation Approach**: Should we use Manual (Option A), AOP (Option B), or Base Class (Option C)?
   - **Recommendation**: Option A (Manual) for explicitness

2. **HTML Safelist Level**: Which safelist should we use for product descriptions?
   - `relaxed()`: Allows formatting + images (recommended)
   - `basic()`: Only text formatting
   - `none()`: Plain text only
   - **Recommendation**: `relaxed()` for product descriptions

3. **Validation Messages**: Should error messages be i18n-ready?
   - **Recommendation**: Yes, use message codes like "validation.product.sku.required"

4. **Performance Testing**: Should we benchmark before/after for each optimization?
   - **Recommendation**: Yes, for @EntityGraph and batch operations

5. **Migration Strategy**: Should we fix all services at once or incrementally?
   - **Recommendation**: Incremental (ProductService → CategoryService → ProductTypeService)

---

## 📅 Timeline Estimate

| Phase | Duration | Start Date | End Date |
|-------|----------|------------|----------|
| Phase 1 | 2 hours | Sprint N | Sprint N |
| Phase 2 | 1 week | Sprint N+1 | Sprint N+1 |
| Phase 3 | 3-5 days | Sprint N+2 | Sprint N+2 |
| Phase 4 | 2-3 days | Sprint N+3 | Sprint N+3 |

**Total Effort**: ~2 weeks across 3-4 sprints

---

## 🔍 Related Documentation

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Clean Architecture Principles](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Spring Data JPA Best Practices](https://vladmihalcea.com/tutorials/hibernate/)
- [Jsoup Safelist Documentation](https://jsoup.org/apidocs/org/jsoup/safety/Safelist.html)

---

**Document Version**: 1.0  
**Last Updated**: 2026-01-13  
**Author**: @copilot  
**Reviewer**: @yunusemreerkesikbas
