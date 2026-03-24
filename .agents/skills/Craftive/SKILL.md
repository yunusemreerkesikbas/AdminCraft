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
feature/CMS-205 Refactor stage subdomain conventions and update environment configurations
```

*Commit message example*

```text
Merge branch 'master' of github.com:yunusemreerkesikbas/AdminCraft
```

*Commit message example*

```text
Merge pull request #253 from yunusemreerkesikbas/stage
```

*Commit message example*

```text
Fix CORS: add X-Client-Version to allowed headers, update devops docs
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

**Frequency**: ~11 times per month

**Steps**:
1. Create migration file
2. Update schema definitions
3. Generate/update types

**Files typically involved**:
- `**/types.ts`
- `migrations/*`
- `**/schema.*`

**Example commit sequence**:
```
Merge pull request #238 from yunusemreerkesikbas/feature/CMS-196
feature/CMS-198 Enhance SiteSettings and Component responses with media details
Refactor CMS and Component responses to enhance media handling
```

### Feature Development

Standard feature implementation workflow

**Frequency**: ~13 times per month

**Steps**:
1. Add feature implementation
2. Add tests for feature
3. Update documentation

**Files typically involved**:
- `backend/src/main/java/com/backend/application/dto/delivery/*`
- `backend/src/main/java/com/backend/application/dto/request/*`
- `backend/src/main/java/com/backend/application/dto/response/*`
- `**/*.test.*`
- `**/api/**`

**Example commit sequence**:
```
feature/CMS-198 Enhance SiteSettings and Component responses with media details
Refactor CMS and Component responses to enhance media handling
Merge pull request #240 from yunusemreerkesikbas/feature/CMS-198
```

### Refactoring

Code refactoring and cleanup workflow

**Frequency**: ~7 times per month

**Steps**:
1. Ensure tests pass before refactor
2. Refactor code structure
3. Verify tests still pass

**Files typically involved**:
- `src/**/*`

**Example commit sequence**:
```
Merge pull request #238 from yunusemreerkesikbas/feature/CMS-196
feature/CMS-198 Enhance SiteSettings and Component responses with media details
Refactor CMS and Component responses to enhance media handling
```

### Database Schema Migration

Adds or modifies database tables and schema, including baseline scripts and migration files for both platform and tenant databases.

**Frequency**: ~4 times per month

**Steps**:
1. Edit or add SQL migration files in backend/src/main/resources/db/platform/ or backend/src/main/resources/db/tenant/*/
2. Update baseline SQL scripts if needed (V1.0.0__baseline.sql, V1__baseline.sql, etc.)
3. Optionally update impex seed files for initial data
4. Update application configuration files if new config is required
5. Update documentation (docs/global/migrations.md or related module docs)

**Files typically involved**:
- `backend/src/main/resources/db/platform/*.sql`
- `backend/src/main/resources/db/tenant/*/*.sql`
- `backend/src/main/resources/impex/*.sql`
- `backend/src/main/resources/application-*.yml`
- `docs/global/migrations.md`

**Example commit sequence**:
```
Edit or add SQL migration files in backend/src/main/resources/db/platform/ or backend/src/main/resources/db/tenant/*/
Update baseline SQL scripts if needed (V1.0.0__baseline.sql, V1__baseline.sql, etc.)
Optionally update impex seed files for initial data
Update application configuration files if new config is required
Update documentation (docs/global/migrations.md or related module docs)
```

### Environment Config Update

Updates environment-specific configuration files for backend and frontend, often for deployment, credentials, or endpoint changes.

**Frequency**: ~6 times per month

**Steps**:
1. Edit backend/src/main/resources/application-*.yml for backend config
2. Edit .env.example, storefront-nextjs/.env.*, or similar env files for frontend
3. Update docker-compose.*.yml if service config changes
4. Update docs/global/environment-configuration.md and/or docs/global/devops.md to reflect the changes

**Files typically involved**:
- `backend/src/main/resources/application-*.yml`
- `.env.example`
- `storefront-nextjs/.env.*`
- `docker-compose.*.yml`
- `docs/global/environment-configuration.md`
- `docs/global/devops.md`

**Example commit sequence**:
```
Edit backend/src/main/resources/application-*.yml for backend config
Edit .env.example, storefront-nextjs/.env.*, or similar env files for frontend
Update docker-compose.*.yml if service config changes
Update docs/global/environment-configuration.md and/or docs/global/devops.md to reflect the changes
```

### Feature Development With Docs And Tests

Implements a new backend or frontend feature, with corresponding updates to documentation and tests.

**Frequency**: ~5 times per month

**Steps**:
1. Add or refactor backend service/controller/entity/repository files
2. Add or update frontend component/service/types files
3. Add or update integration/unit tests (backend/src/test/java/..., storefront/src/app/...)
4. Update or create relevant documentation in docs/modules/ or docs/global/
5. Optionally update i18n files if user-facing text is added

**Files typically involved**:
- `backend/src/main/java/com/backend/application/service/*.java`
- `backend/src/main/java/com/backend/presentation/controller/*.java`
- `backend/src/main/java/com/backend/domain/entity/*.java`
- `backend/src/main/java/com/backend/domain/repository/*.java`
- `backend/src/test/java/com/backend/**/*.java`
- `storefront/src/app/**/*.ts`
- `storefront/src/app/**/*.html`
- `docs/modules/*.md`
- `docs/global/*.md`
- `backend/src/main/resources/i18n/messages_*.properties`

**Example commit sequence**:
```
Add or refactor backend service/controller/entity/repository files
Add or update frontend component/service/types files
Add or update integration/unit tests (backend/src/test/java/..., storefront/src/app/...)
Update or create relevant documentation in docs/modules/ or docs/global/
Optionally update i18n files if user-facing text is added
```

### Docker And Deployment Pipeline Update

Modifies Dockerfiles, docker-compose files, or CI/CD workflow YAMLs to change build, deployment, or infrastructure setup.

**Frequency**: ~4 times per month

**Steps**:
1. Edit docker-compose.*.yml to add/update/remove services or environment variables
2. Edit Dockerfile(s) for backend/frontend as needed
3. Edit .github/workflows/*.yml for CI/CD changes
4. Update documentation in docs/global/devops.md

**Files typically involved**:
- `docker-compose.*.yml`
- `docker/*/Dockerfile`
- `.github/workflows/*.yml`
- `docs/global/devops.md`

**Example commit sequence**:
```
Edit docker-compose.*.yml to add/update/remove services or environment variables
Edit Dockerfile(s) for backend/frontend as needed
Edit .github/workflows/*.yml for CI/CD changes
Update documentation in docs/global/devops.md
```

### Documentation Enhancement

Improves or extends documentation, often alongside code or config changes, to clarify usage, configuration, or architecture.

**Frequency**: ~6 times per month

**Steps**:
1. Edit or create docs/global/*.md or docs/modules/*.md
2. Update README.md files in root or submodules
3. Optionally update .env.example or config files to match docs

**Files typically involved**:
- `docs/global/*.md`
- `docs/modules/*.md`
- `README.md`
- `.env.example`

**Example commit sequence**:
```
Edit or create docs/global/*.md or docs/modules/*.md
Update README.md files in root or submodules
Optionally update .env.example or config files to match docs
```

### Frontend Landing Or Storefront Structure Update

Adds or restructures major frontend (Next.js or Angular) application sections, layouts, or components, often with config and i18n updates.

**Frequency**: ~3 times per month

**Steps**:
1. Add or refactor files in landing/app/, landing/components/, or storefront-nextjs/app/
2. Update or add TypeScript config, ESLint, or Next.js config files
3. Update i18n files (messages/*.json or content/*.json) if needed
4. Update README.md or docs/storefront-nextjs/README.md

**Files typically involved**:
- `landing/app/**/*.tsx`
- `landing/components/**/*.tsx`
- `landing/content/*.json`
- `landing/tsconfig.json`
- `landing/next.config.ts`
- `storefront-nextjs/app/**/*.tsx`
- `storefront-nextjs/components/**/*.tsx`
- `storefront-nextjs/messages/*.json`
- `storefront-nextjs/tsconfig.json`
- `storefront-nextjs/next.config.ts`
- `docs/storefront-nextjs/README.md`

**Example commit sequence**:
```
Add or refactor files in landing/app/, landing/components/, or storefront-nextjs/app/
Update or add TypeScript config, ESLint, or Next.js config files
Update i18n files (messages/*.json or content/*.json) if needed
Update README.md or docs/storefront-nextjs/README.md
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
