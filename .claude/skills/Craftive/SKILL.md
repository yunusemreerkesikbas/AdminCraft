---
name: craftive-conventions
description: Development conventions and patterns for Craftive. Java project with freeform commits.
---

# Craftive Conventions

> Generated from [yunusemreerkesikbas/Craftive](https://github.com/yunusemreerkesikbas/Craftive) on 2026-03-24

## Overview

This skill teaches Claude the development patterns and conventions used in Craftive.

## Tech Stack

- **Primary Language**: Java
- **Architecture**: hybrid module organization
- **Test Location**: mixed
- **Test Framework**: jest

## When to Use This Skill

Activate this skill when:
- Making changes to this repository
- Adding new features following established patterns
- Writing tests that match project conventions
- Creating commits with proper message format

## Commit Conventions

Follow these commit message conventions based on 200 analyzed commits.

### Commit Style: Free-form Messages

### Prefixes Used

- `fix`

### Message Guidelines

- Average message length: ~60 characters
- Keep first line concise and descriptive
- Use imperative mood ("Add feature" not "Added feature")


*Commit message example*

```text
chore: update Alloy service configuration in Docker Compose
```

*Commit message example*

```text
refactor: update email service and configuration management
```

*Commit message example*

```text
feat: add mail marketing & impex UI (storefront)
```

*Commit message example*

```text
fix: use NULL for created_by in seed_pages_and_slots ImpEx
```

*Commit message example*

```text
docs: update environment configuration with MySQL port conflict resolution and troubleshooting steps
```

*Commit message example*

```text
Fix CORS: add X-Client-Version to allowed headers, update devops docs
```

*Commit message example*

```text
Fix API base URL: add /api context-path to stage and prod environments
```

*Commit message example*

```text
Fix deploy: force-recreate containers and SSH-based health check
```

## Architecture

### Project Structure: Single Package

This project uses **hybrid** module organization.

### Configuration Files

- `.github/workflows/ci.yml`
- `.github/workflows/deploy-prod.yml`
- `.github/workflows/deploy-stage.yml`
- `.github/workflows/migration-guardrails.yml`
- `Dockerfile`
- `docker-compose.yml`
- `docker/backend/Dockerfile`
- `docker/frontend/Dockerfile`
- `docker/storefront/Dockerfile`
- `landing/next.config.ts`
- `landing/package.json`
- `landing/tsconfig.json`
- `storefront-nextjs/next.config.ts`
- `storefront-nextjs/package.json`
- `storefront-nextjs/tsconfig.json`
- `storefront/.prettierrc`
- `storefront/package.json`
- `storefront/tailwind.config.js`
- `storefront/tsconfig.json`

### Guidelines

- This project uses a hybrid organization
- Follow existing patterns when adding new code

## Code Style

### Language: Java

### Naming Conventions

| Element | Convention |
|---------|------------|
| Files | camelCase |
| Functions | camelCase |
| Classes | PascalCase |
| Constants | SCREAMING_SNAKE_CASE |

### Import Style: Path Aliases (@/, ~/)

### Export Style: Mixed Style


*Preferred import style*

```typescript
// Use path aliases for imports
import { Button } from '@/components/Button'
import { useAuth } from '@/hooks/useAuth'
import { api } from '@/lib/api'
```

## Testing

### Test Framework: jest

### File Pattern: `*.spec.ts`

### Test Types

- **Unit tests**: Test individual functions and components in isolation
- **Integration tests**: Test interactions between multiple components/services


*Test file structure*

```typescript
import { describe, it, expect } from 'jest'

describe('MyFunction', () => {
  it('should return expected result', () => {
    const result = myFunction(input)
    expect(result).toBe(expected)
  })
})
```

## Common Workflows

These workflows were detected from analyzing commit patterns.

### Database Migration

Database schema changes with migration files

**Frequency**: ~14 times per month

**Steps**:
1. Create migration file
2. Update schema definitions
3. Generate/update types

**Files typically involved**:
- `migrations/*`
- `**/types.ts`
- `**/schema.*`

**Example commit sequence**:
```
feature/CMS-194 Enhance media handling and site settings
feature/CMS-195 Apply code review fixes
Merge pull request #237 from yunusemreerkesikbas/feature/CMS-195
```

### Feature Development

Standard feature implementation workflow

**Frequency**: ~15 times per month

**Steps**:
1. Add feature implementation
2. Add tests for feature
3. Update documentation

**Files typically involved**:
- `landing/app/[locale]/*`
- `landing/app/*`
- `landing/components/*`
- `**/*.test.*`
- `**/api/**`

**Example commit sequence**:
```
feature/CMS-192 chore: update landing project configurations and components
Merge pull request #235 from yunusemreerkesikbas/feature/CMS-192
feature/CMS-193 chore: update configuration files and add observability setup
```

### Test Driven Development

Test-first development workflow (TDD)

**Frequency**: ~4 times per month

**Steps**:
1. Write failing test
2. Implement code to pass test
3. Refactor if needed

**Files typically involved**:
- `**/*.test.*`
- `**/*.spec.*`
- `src/**/*`

**Example commit sequence**:
```
test: add tests for user validation
feat: implement user validation
```

### Refactoring

Code refactoring and cleanup workflow

**Frequency**: ~8 times per month

**Steps**:
1. Ensure tests pass before refactor
2. Refactor code structure
3. Verify tests still pass

**Files typically involved**:
- `src/**/*`

**Example commit sequence**:
```
feature/CMS-193 chore: update configuration files and add observability setup
Merge pull request #236 from yunusemreerkesikbas/feature/CMS-193
feature/CMS-194 Enhance media handling and site settings
```

### Backend Database Schema Change

Add or modify backend database tables, columns, or constraints for platform or tenant modules.

**Frequency**: ~3 times per month

**Steps**:
1. Edit or add SQL migration files under backend/src/main/resources/db/platform/ or backend/src/main/resources/db/tenant/*/
2. Optionally update baseline SQLs (V1.0.0__baseline.sql) for new modules or consolidated schemas
3. Update related Java entity/repository/service files if needed
4. Update impex seed files if initial data is needed
5. Update documentation (docs/global/migrations.md or module docs)

**Files typically involved**:
- `backend/src/main/resources/db/platform/*.sql`
- `backend/src/main/resources/db/tenant/*/*.sql`
- `backend/src/main/resources/impex/*.sql`
- `backend/src/main/java/com/backend/domain/entity/*.java`
- `docs/global/migrations.md`
- `docs/modules/*.md`

**Example commit sequence**:
```
Edit or add SQL migration files under backend/src/main/resources/db/platform/ or backend/src/main/resources/db/tenant/*/
Optionally update baseline SQLs (V1.0.0__baseline.sql) for new modules or consolidated schemas
Update related Java entity/repository/service files if needed
Update impex seed files if initial data is needed
Update documentation (docs/global/migrations.md or module docs)
```

### Backend Service Feature Or Refactor

Add, refactor, or enhance backend service logic (Java), often with new or updated tests and documentation.

**Frequency**: ~4 times per month

**Steps**:
1. Edit or add Java files in backend/src/main/java/com/backend/application/service/ or related domain/infrastructure layers
2. Update or add integration/unit test files in backend/src/test/java/com/backend/application/service/ or integration/
3. Update or add controller files if API surface changes
4. Update or add DTOs if request/response shapes change
5. Update documentation (docs/modules/*.md or docs/README.md)

**Files typically involved**:
- `backend/src/main/java/com/backend/application/service/*.java`
- `backend/src/main/java/com/backend/domain/entity/*.java`
- `backend/src/main/java/com/backend/presentation/controller/*.java`
- `backend/src/main/java/com/backend/application/dto/**/*.java`
- `backend/src/test/java/com/backend/application/service/*.java`
- `docs/modules/*.md`
- `docs/README.md`

**Example commit sequence**:
```
Edit or add Java files in backend/src/main/java/com/backend/application/service/ or related domain/infrastructure layers
Update or add integration/unit test files in backend/src/test/java/com/backend/application/service/ or integration/
Update or add controller files if API surface changes
Update or add DTOs if request/response shapes change
Update documentation (docs/modules/*.md or docs/README.md)
```

### Backend Media Handling Enhancement

Refactor or enhance backend media handling, including media binding, responsive media, and error handling.

**Frequency**: ~2 times per month

**Steps**:
1. Edit MediaServiceImpl, MediaController, and related service/controller files
2. Update DTOs for media (e.g., EntryDeliveryResponse, MediaBindRequest)
3. Update or add migration/seed files for media-related tables
4. Update localization files for new error messages
5. Update or add frontend files for media dialogs/components if needed
6. Update documentation for media modules

**Files typically involved**:
- `backend/src/main/java/com/backend/application/service/MediaServiceImpl.java`
- `backend/src/main/java/com/backend/presentation/controller/MediaController.java`
- `backend/src/main/java/com/backend/application/dto/**/*.java`
- `backend/src/main/resources/impex/*.sql`
- `backend/src/main/resources/i18n/messages_en.properties`
- `backend/src/main/resources/i18n/messages_tr.properties`
- `storefront/src/app/modules/admin/custom/media/**/*.ts`
- `docs/modules/media.md`

**Example commit sequence**:
```
Edit MediaServiceImpl, MediaController, and related service/controller files
Update DTOs for media (e.g., EntryDeliveryResponse, MediaBindRequest)
Update or add migration/seed files for media-related tables
Update localization files for new error messages
Update or add frontend files for media dialogs/components if needed
Update documentation for media modules
```

### Frontend Admin Angular Component Or Dialog

Add or refactor Angular admin UI components/dialogs, often with i18n and service updates.

**Frequency**: ~2 times per month

**Steps**:
1. Edit or add component/dialog files under storefront/src/app/modules/admin/custom/
2. Update or add related service/types files
3. Update i18n files (langEN.ts, langTR.ts) for new UI strings
4. Update shared components if needed
5. Update documentation for the affected module

**Files typically involved**:
- `storefront/src/app/modules/admin/custom/**/*.ts`
- `storefront/src/app/modules/admin/custom/**/*.html`
- `storefront/src/app/modules/admin/custom/**/*.scss`
- `storefront/src/app/modules/admin/i18n/langEN.ts`
- `storefront/src/app/modules/admin/i18n/langTR.ts`
- `docs/modules/*.md`

**Example commit sequence**:
```
Edit or add component/dialog files under storefront/src/app/modules/admin/custom/
Update or add related service/types files
Update i18n files (langEN.ts, langTR.ts) for new UI strings
Update shared components if needed
Update documentation for the affected module
```

### Devops Docker Compose And Deployment Workflow

Update Docker Compose files, GitHub Actions workflows, and deployment scripts for infrastructure or deployment improvements.

**Frequency**: ~3 times per month

**Steps**:
1. Edit docker-compose.*.yml files for service/image/env changes
2. Edit .github/workflows/*.yml for CI/CD pipeline changes
3. Edit scripts/server/*.sh or similar for provisioning
4. Update docs/global/devops.md or related documentation
5. Optionally update backend configuration files for environment changes

**Files typically involved**:
- `docker-compose*.yml`
- `.github/workflows/*.yml`
- `scripts/server/*.sh`
- `docs/global/devops.md`
- `backend/src/main/resources/application-*.yml`

**Example commit sequence**:
```
Edit docker-compose.*.yml files for service/image/env changes
Edit .github/workflows/*.yml for CI/CD pipeline changes
Edit scripts/server/*.sh or similar for provisioning
Update docs/global/devops.md or related documentation
Optionally update backend configuration files for environment changes
```

### Frontend Nextjs Project Initialization Or Major Update

Initialize or perform major updates to a Next.js frontend project (landing or storefront-nextjs), including config, env, and core components.

**Frequency**: ~1 times per month

**Steps**:
1. Add or update .env, .gitignore, package.json, tsconfig.json, and config files
2. Add or update core app/layout/pages/components
3. Add or update public assets and i18n files
4. Update README.md and documentation

**Files typically involved**:
- `storefront-nextjs/*`
- `landing/*`
- `storefront-nextjs/app/**/*.tsx`
- `storefront-nextjs/components/**/*.tsx`
- `landing/app/**/*.tsx`
- `landing/components/**/*.tsx`
- `storefront-nextjs/package.json`
- `landing/package.json`
- `storefront-nextjs/tsconfig.json`
- `landing/tsconfig.json`
- `storefront-nextjs/.env*`
- `landing/.env*`
- `storefront-nextjs/README.md`
- `landing/README.md`

**Example commit sequence**:
```
Add or update .env, .gitignore, package.json, tsconfig.json, and config files
Add or update core app/layout/pages/components
Add or update public assets and i18n files
Update README.md and documentation
```

### Documentation And Config Update

Update documentation and configuration files for clarity, new features, or environment changes.

**Frequency**: ~2 times per month

**Steps**:
1. Edit docs/README.md, docs/global/*.md, or docs/modules/*.md
2. Edit backend/src/main/resources/application*.yml for config changes
3. Edit .gitignore or other root config files
4. Optionally update code comments or settings files

**Files typically involved**:
- `docs/README.md`
- `docs/global/*.md`
- `docs/modules/*.md`
- `backend/src/main/resources/application*.yml`
- `.gitignore`

**Example commit sequence**:
```
Edit docs/README.md, docs/global/*.md, or docs/modules/*.md
Edit backend/src/main/resources/application*.yml for config changes
Edit .gitignore or other root config files
Optionally update code comments or settings files
```


## Best Practices

Based on analysis of the codebase, follow these practices:

### Do

- Write tests using jest
- Follow *.spec.ts naming pattern
- Use camelCase for file names
- Prefer mixed exports

### Don't

- Don't use long relative imports (use aliases)
- Don't skip tests for new features
- Don't deviate from established patterns without discussion

---

*This skill was auto-generated by [ECC Tools](https://ecc.tools). Review and customize as needed for your team.*
