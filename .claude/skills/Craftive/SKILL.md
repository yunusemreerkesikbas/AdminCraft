---
name: craftive-conventions
description: Development conventions and patterns for Craftive. Java project with freeform commits.
---

# Craftive Conventions

> Generated from [yunusemreerkesikbas/Craftive](https://github.com/yunusemreerkesikbas/Craftive) on 2026-03-23

## Overview

This skill teaches Claude the development patterns and conventions used in Craftive.

## Tech Stack

- **Primary Language**: Java
- **Architecture**: hybrid module organization
- **Test Location**: mixed

## When to Use This Skill

Activate this skill when:
- Making changes to this repository
- Adding new features following established patterns
- Writing tests that match project conventions
- Creating commits with proper message format

## Commit Conventions

Follow these commit message conventions based on 8 analyzed commits.

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
Fix health check timing: extend start_period and wait for backend
```

*Commit message example*

```text
Add Spring Boot Actuator for health check endpoints
```

*Commit message example*

```text
Add CLOUDFLARE_DNS_API_TOKEN for Traefik v3.6 ACME DNS challenge
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
- `landing/package.json`
- `landing/tsconfig.json`
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

## Common Workflows

These workflows were detected from analyzing commit patterns.

### Database Migration

Database schema changes with migration files

**Frequency**: ~8 times per month

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

**Frequency**: ~14 times per month

**Steps**:
1. Add feature implementation
2. Add tests for feature
3. Update documentation

**Files typically involved**:
- `landing/app/[locale]/*`
- `landing/app/*`
- `landing/*`
- `**/*.test.*`
- `**/api/**`

**Example commit sequence**:
```
feature/CMS-191  feat: integrate Grafana Cloud Loki with Alloy for centralized logging
chore: update Alloy service configuration in Docker Compose
Merge pull request #234 from yunusemreerkesikbas/feature/CMS-191
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


## Best Practices

Based on analysis of the codebase, follow these practices:

### Do

- Use camelCase for file names
- Prefer mixed exports

### Don't

- Don't use long relative imports (use aliases)
- Don't deviate from established patterns without discussion

---

*This skill was auto-generated by [ECC Tools](https://ecc.tools). Review and customize as needed for your team.*
