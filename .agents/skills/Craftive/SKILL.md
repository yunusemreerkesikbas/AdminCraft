```markdown
# Craftive Development Patterns

> Auto-generated skill from repository analysis

## Overview

This skill teaches the core development patterns, coding conventions, and workflows used in the Craftive codebase. The repository is primarily Java-based (with some TypeScript for the frontend), and follows a modular, layered architecture for backend and admin UI development. It covers backend feature development, database migrations, full-stack feature delivery (including admin UI), environment configuration, and feature removal. The skill also documents testing practices and provides command suggestions for common tasks.

---

## Coding Conventions

### File Naming

- **Java/Backend:** Uses `camelCase` for files and classes.
  - Example: `userProfileService.java`, `orderController.java`
- **Frontend (TypeScript/Angular):** Also uses `camelCase`.
  - Example: `adminDashboard.component.ts`

### Import Style

- **Java:** Uses import aliases for clarity.
  ```java
  import com.backend.application.dto.UserDto;
  import com.backend.domain.entity.OrderEntity as Order;
  ```
- **TypeScript:** Uses path aliases for modules.
  ```typescript
  import { AdminService } from '@admin/services/adminService';
  ```

### Export Style

- **Java:** Mixed usage of public classes and interfaces.
  ```java
  public class UserServiceImpl implements UserService { ... }
  ```
- **TypeScript:** Both default and named exports.
  ```typescript
  export default class AdminDashboard { ... }
  export const NAVIGATION = [ ... ];
  ```

---

## Workflows

### Add or Refactor Backend Feature with API and Tests

**Trigger:** When adding or refactoring a backend feature or API endpoint  
**Command:** `/new-backend-feature`

1. **Create or update DTOs**  
   - Location: `backend/src/main/java/com/backend/application/dto/`
   - Example:
     ```java
     public class UserRequestDto { ... }
     ```
2. **Implement or update service classes**  
   - Location: `backend/src/main/java/com/backend/application/service/`
3. **Add or update controllers**  
   - Location: `backend/src/main/java/com/backend/presentation/controller/`
4. **Modify domain/repository interfaces and implementations**  
   - Location: `backend/src/main/java/com/backend/domain/`
5. **Update configuration/properties if needed**  
   - Location: `backend/src/main/resources/application*.yml`
6. **Add or update tests**  
   - Location: `backend/src/test/java/com/backend/`
   - Example:
     ```java
     @Test
     public void testUserService() { ... }
     ```
7. **Update documentation**  
   - Location: `docs/`

---

### Database Schema Change with Migration

**Trigger:** When adding/modifying database schema  
**Command:** `/new-migration`

1. **Create or update SQL migration file**  
   - Location: `backend/src/main/resources/db/platform/` or `db/tenant/`
   - Example: `V20240601__add_user_table.sql`
2. **Update Java entity/model classes**  
   - Location: `backend/src/main/java/com/backend/domain/entity/`
3. **Update configuration if necessary**  
   - Location: `backend/src/main/resources/application*.yml`
4. **Update/add tests if logic is affected**  
   - Location: `backend/src/test/java/com/backend/`
5. **Update documentation**  
   - Location: `docs/`

---

### Feature Development Full-Stack with Admin UI

**Trigger:** When building a new feature for the admin interface with backend support  
**Command:** `/new-admin-feature`

1. **Backend:**
   - Create/update DTOs, services, controllers, migrations (see above locations)
2. **Frontend:**
   - Implement/update Angular components
     - Location: `storefront/src/app/modules/admin/custom/`
     - Example: `user-management.component.ts`, `user-management.component.html`
   - Update/add i18n translation files
     - Location: `storefront/src/app/modules/admin/i18n/langEN.ts`, `langTR.ts`
     - Example:
       ```typescript
       export const langEN = { 'USER_MANAGEMENT': 'User Management' };
       ```
   - Update navigation constants if needed
     - Location: `storefront/src/app/shared/navigation/navigation-data.constants.ts`
3. **Update documentation**  
   - Location: `docs/`

---

### Environment and Deployment Configuration Update

**Trigger:** When changing environment variables, deployment logic, or infrastructure  
**Command:** `/update-env`

1. **Edit Docker Compose files**  
   - Location: `docker-compose*.yml`
2. **Update .env and environment files**  
   - Location: `storefront-nextjs/.env*`, `storefront/src/environments/environment*.ts`
3. **Modify deployment scripts**  
   - Location: `scripts/server/*.sh`
4. **Update documentation**  
   - Location: `docs/global/environment-configuration.md`, `docs/global/devops.md`

---

### Feature Removal or Cleanup

**Trigger:** When removing deprecated/unused features or files  
**Command:** `/remove-feature`

1. **Delete obsolete SQL seed/migration files**  
   - Location: `backend/src/main/resources/impex/**/*.sql`
2. **Remove related backend code/configuration**  
   - Location: `backend/src/main/java/com/backend/`
3. **Remove related frontend code/components**  
   - Location: `storefront-nextjs/`, `storefront/src/app/`
4. **Update documentation**  
   - Location: `docs/`

---

## Testing Patterns

- **Backend Java Tests:**  
  - Located in `backend/src/test/java/com/backend/`
  - Use standard JUnit patterns:
    ```java
    @Test
    public void testOrderCreation() { ... }
    ```
- **Frontend Tests:**  
  - Use Jest for TypeScript files
  - Test files follow the pattern: `*.spec.ts`
    ```typescript
    describe('AdminDashboard', () => {
      it('should render', () => { ... });
    });
    ```

---

## Commands

| Command              | Purpose                                                        |
|----------------------|----------------------------------------------------------------|
| /new-backend-feature | Add or refactor backend features, APIs, and corresponding tests|
| /new-migration       | Add or modify database schema with migration files             |
| /new-admin-feature   | Implement full-stack features including admin UI               |
| /update-env          | Update environment variables and deployment configuration      |
| /remove-feature      | Remove deprecated features and clean up codebase               |
```
