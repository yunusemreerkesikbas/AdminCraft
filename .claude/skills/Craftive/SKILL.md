```markdown
# Craftive Development Patterns

> Auto-generated skill from repository analysis

## Overview
This skill provides a comprehensive guide to the development patterns, workflows, and conventions used in the Craftive Java backend repository. It covers file organization, code style, typical commit practices, and step-by-step instructions for common workflows such as analytics integration and service/DTO refactoring. This guide is designed to help contributors quickly understand and apply the established patterns in the codebase.

## Coding Conventions

### File Naming
- **Convention:** PascalCase
- **Example:** `SiteAnalyticsServiceImpl.java`, `SiteController.java`

### Imports
- **Style:** Relative imports within the Java package structure.
- **Example:**
  ```java
  import com.backend.application.service.SiteAnalyticsService;
  ```

### Exports
- **Style:** Named exports (Java's standard public class definitions).
- **Example:**
  ```java
  public class SiteAnalyticsServiceImpl implements SiteAnalyticsService {
      // Implementation
  }
  ```

### Commit Patterns
- **Type:** Mixed
- **Prefix:** `feat` (feature additions are common)
- **Average Length:** 84 characters

## Workflows

### Add or Update Analytics/Insights Integration
**Trigger:** When someone wants to add or improve analytics or insights features (such as Google Analytics, Search Console, Crux) in the backend.  
**Command:** `/add-analytics-integration`

1. **Create or update DTOs** for analytics/insights data transfer.
   - Example: `SiteAnalyticsSummaryAppDto.java`, `SiteInsightsSummaryAppDto.java`
2. **Implement or modify service and service implementation classes** for analytics/insights.
   - Example: `SiteAnalyticsService.java`, `SiteAnalyticsServiceImpl.java`
3. **Add or update domain ports and infrastructure adapters** for external integrations.
   - Example: `SiteAnalyticsPort.java`, `GoogleAnalyticsPortAdapter.java`
4. **Update configuration files and properties** related to analytics/insights.
   - Example: `GoogleAnalyticsProperties.java`, `CruxHistoryProperties.java`
5. **Modify controllers** to expose new or updated endpoints.
   - Example: `SiteController.java`
6. **Update or add tests** for service implementations and controllers.
   - Example: `SiteAnalyticsServiceImplTest.java`
7. **Update documentation** to reflect new integrations or changes.
   - Example: `docs/3rd-party/google-analytics-ga4.md`

**Sample Service Implementation:**
```java
public class SiteAnalyticsServiceImpl implements SiteAnalyticsService {
    @Override
    public SiteAnalyticsSummaryAppDto getAnalyticsSummary(String siteId) {
        // Integration logic here
    }
}
```

---

### Refactor and Standardize Service DTOs and Validation
**Trigger:** When someone wants to improve code quality, validation, and consistency in service and DTO layers.  
**Command:** `/refactor-service-dto`

1. **Deprecate or update DTOs**, removing or adjusting validation annotations.
   - Example: Update `ImpExRequest.java` to use new validation rules.
2. **Introduce new classes** (e.g., status enums or constants) to standardize logic.
   - Example: `SiteDataStatus.java`
3. **Refactor service implementations** to use new standardized structures.
   - Example: Update `SiteAnalyticsServiceImpl.java` to use new DTOs and enums.
4. **Update related controllers and helper classes** for new logic.
   - Example: `ImpExController.java`, `SecurityHelper.java`
5. **Update or add tests** for affected services.
   - Example: `SiteAnalyticsServiceImplTest.java`
6. **Update documentation** to reflect refactored logic and structures.
   - Example: `docs/global/environment-configuration.md`

**Sample Enum Introduction:**
```java
public enum SiteDataStatus {
    ACTIVE,
    INACTIVE,
    PENDING
}
```

---

## Testing Patterns

- **Framework:** Unknown (Java tests detected, but framework not specified)
- **File Pattern:** Test files are named with the suffix `*Test.java`
- **Example:**
  ```
  backend/src/test/java/com/backend/application/service/SiteAnalyticsServiceImplTest.java
  ```
- **Typical Test Structure:**
  ```java
  public class SiteAnalyticsServiceImplTest {
      @Test
      public void testGetAnalyticsSummary() {
          // Arrange, Act, Assert
      }
  }
  ```

## Commands

| Command                    | Purpose                                                         |
|----------------------------|-----------------------------------------------------------------|
| /add-analytics-integration | Add or update analytics/insights integrations in the backend    |
| /refactor-service-dto      | Refactor and standardize service DTOs and validation logic      |
```