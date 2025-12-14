# Module Synchronization Checklist

When adding a new module to AdminCraft, update these files in order:

## Backend

### 1. Database Seed

**File:** `backend/src/main/resources/db/platform/R__seed_modules.sql`

```sql
INSERT INTO modules_catalog (code, name, type, version, deps, enabled_by_default, description) 
VALUES (
    'new_module', 
    'New Module', 
    'core', 
    '1.0.0', 
    '["core"]',
    FALSE,
    'Module description here.'
);
```

### 2. Enum

**File:** `backend/src/main/java/com/backend/domain/enums/ModuleCode.java`

```java
public enum ModuleCode {
    // ... existing
    NEW_MODULE("new_module", "New Module");
}
```

### 3. Migration

**File:** `backend/src/main/resources/db/tenant/new_module/V1__baseline.sql`

```sql
CREATE TABLE new_module_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    -- fields here
) ENGINE=InnoDB CHARSET=utf8mb4;
```

## Frontend

### 4. Navigation Constant

**File:** `storefront/src/app/core/navigation/navigation-modules.constants.ts`

```typescript
export const NAVIGATION_MODULES = {
    // ... existing
    NEW_MODULE: 'new_module'
} as const;
```

### 5. Module Guard (Optional)

**File:** `storefront/src/app/core/auth/guards/module.guard.ts`

Only if you need custom display name in error messages.

### 6. Navigation Items (If Needed)

**File:** `storefront/src/app/shared/navigation/navigation-data.constants.ts`

```typescript
{
    id: 'apps.custom.newModule',
    title: 'New Module',
    type: 'basic',
    icon: 'heroicons_outline:icon-name',
    link: 'new-module',
    requiredModule: NAVIGATION_MODULES.NEW_MODULE,
    excludedRoles: ['SUPER_ADMIN'],
}
```

## Verification

- [ ] Backend compiles: `mvn clean compile`
- [ ] Module in catalog: `curl http://localhost:8080/api/provisioning/modules/catalog`
- [ ] Provision dialog shows new module
- [ ] Navigation appears when module enabled
- [ ] Migration runs successfully

## Notes

- All modules have type: `'core'`
- Core module deps: `NULL`, others: `'["core"]'`
- Module codes: lowercase with underscores
