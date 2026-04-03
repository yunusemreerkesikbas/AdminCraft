```markdown
# Craftive Development Patterns

> Auto-generated skill from repository analysis

## Overview

This skill teaches you the core development patterns, coding conventions, and common workflows found in the Craftive repository. The codebase is primarily written in TypeScript, with no specific frontend framework detected, though Angular-like patterns are present in the admin modules. You'll learn how to structure files, manage imports/exports, and follow established workflows for configuration management and admin component enhancement.

## Coding Conventions

### File Naming

- Use **kebab-case** for file names.
  - Example: `runtime-env.ts`, `site-management.component.ts`

### Import Style

- Mixed import styles are used. Both default and named imports may appear.
  - Example:
    ```typescript
    import { ConfigService } from './config-service';
    import runtimeEnv from './runtime-env';
    ```

### Export Style

- Prefer **named exports**.
  - Example:
    ```typescript
    export function getSiteConfig() { ... }
    export const DEFAULT_TIMEOUT = 5000;
    ```

### Component Structure

- For admin site/media components, keep related files together:
  - `.component.ts` (logic)
  - `.component.html` (template)
  - `.component.scss` (styles)
  - `.types.ts` (types/interfaces)
  - `.service.ts` (services)

### Example Directory Layout

```
storefront/src/app/modules/admin/custom/site/
  ├── site-management.component.ts
  ├── site-management.component.html
  ├── site-management.component.scss
  ├── site-management.types.ts
  └── site-management.service.ts
```

## Workflows

### Update Docker Compose and Environment for New Config

**Trigger:** When introducing a new environment variable or updating configuration for deployment environments.  
**Command:** `/update-config-env`

1. **Add or update** the environment variable in `.env.example` and/or `storefront-nextjs/.env.local.example`.
2. **Update** `docker-compose.prod.yml` and `docker-compose.stage.yml` to include the new variable or adjust relevant settings.
3. **Optionally update** related runtime config files, such as `storefront-nextjs/lib/core/config/runtime-env.ts`.
4. **Commit** your changes with a clear message describing the config update.

**Example:**

_Adding a new SEO API key:_

- `.env.example`:
  ```
  SEO_API_KEY=your-key-here
  ```
- `docker-compose.prod.yml`:
  ```yaml
  environment:
    - SEO_API_KEY=${SEO_API_KEY}
  ```
- `runtime-env.ts`:
  ```typescript
  export const SEO_API_KEY = process.env.SEO_API_KEY;
  ```

---

### Enhance Admin Site or Media Components

**Trigger:** When adding new features, refactoring, or enhancing the admin dashboard's site/media management UI.  
**Command:** `/enhance-admin-component`

1. **Update or create** component TypeScript files for new logic or features.
2. **Update corresponding** HTML and SCSS files for UI changes.
3. **Modify or add** related types in `.types.ts` files and services as needed.
4. **Update i18n files** (`langEN.ts`, `langTR.ts`) if new text is introduced.
5. **Test** your changes and ensure all affected components work as expected.
6. **Commit** your changes with a descriptive message.

**Example:**

_Adding a new media upload feature:_

- `media-upload.component.ts`:
  ```typescript
  export class MediaUploadComponent { ... }
  ```
- `media-upload.component.html`:
  ```html
  <input type="file" (change)="onFileSelected($event)" />
  ```
- `media-upload.component.scss`:
  ```scss
  .upload-btn { ... }
  ```
- `media-upload.types.ts`:
  ```typescript
  export interface MediaFile { ... }
  ```
- `langEN.ts`:
  ```typescript
  export const EN = {
    uploadSuccess: "Upload successful!",
    ...
  };
  ```

---

## Testing Patterns

- Test files follow the `*.test.*` pattern (e.g., `user.service.test.ts`).
- The specific testing framework is unknown, but tests are likely colocated with the code they test.
- Example test file:
  ```typescript
  // user.service.test.ts
  import { getUser } from './user.service';

  test('should fetch user by ID', () => {
    expect(getUser(1)).toEqual({ id: 1, name: 'Alice' });
  });
  ```

## Commands

| Command               | Purpose                                                                 |
|-----------------------|-------------------------------------------------------------------------|
| /update-config-env    | Synchronize environment variables and Docker Compose for new config      |
| /enhance-admin-component | Add or improve features in admin site or media components             |
```
