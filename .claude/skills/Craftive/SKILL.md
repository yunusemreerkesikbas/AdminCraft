```markdown
# Craftive Development Patterns

> Auto-generated skill from repository analysis

## Overview
This skill teaches the core development patterns and conventions used in the Craftive TypeScript codebase. It covers file naming, import/export styles, commit message patterns, and testing approaches. By following these guidelines, contributors can maintain consistency and quality across the project.

## Coding Conventions

### File Naming
- Use **kebab-case** for all file names.
  - Example:  
    ```
    user-profile.ts
    api-client.test.ts
    ```

### Import Style
- Mixed import styles are used, including both default and named imports.
  - Example:
    ```typescript
    import fs from 'fs';
    import { parseUser } from './utils/parse-user';
    ```

### Export Style
- Prefer **named exports** for modules.
  - Example:
    ```typescript
    // Good
    export function fetchData() { ... }
    export const API_URL = '...';

    // Avoid default exports
    // export default function fetchData() { ... }
    ```

### Commit Messages
- Freeform commit messages with no enforced prefix.
- Average commit message length: ~59 characters.
  - Example:
    ```
    Add user authentication middleware
    Fix bug in data parsing logic
    ```

## Workflows

### Add a New Feature
**Trigger:** When implementing a new feature or module  
**Command:** `/add-feature`

1. Create a new file using kebab-case naming.
2. Write TypeScript code using named exports.
3. Import dependencies using mixed import styles as needed.
4. Add or update corresponding test files (`*.test.ts`).
5. Commit changes with a clear, descriptive message.

### Fix a Bug
**Trigger:** When resolving a bug or issue  
**Command:** `/fix-bug`

1. Locate the relevant file(s) using kebab-case convention.
2. Apply the fix, maintaining import/export styles.
3. Update or add tests to cover the fix.
4. Commit with a descriptive message explaining the fix.

### Write and Run Tests
**Trigger:** When adding or updating tests  
**Command:** `/run-tests`

1. Create or edit test files matching the pattern `*.test.ts`.
2. Write tests for each exported function or module.
3. Use the project's test runner (framework unknown; check documentation or package.json).
4. Run tests and ensure all pass before committing.

## Testing Patterns

- Test files are named using the pattern `*.test.ts`.
- Each test file should correspond to a source file and cover its named exports.
- Testing framework is not specified; review project documentation for specifics.
- Example test file:
  ```typescript
  // user-profile.test.ts
  import { getUserProfile } from './user-profile';

  describe('getUserProfile', () => {
    it('returns correct user data', () => {
      // test implementation
    });
  });
  ```

## Commands
| Command      | Purpose                                      |
|--------------|----------------------------------------------|
| /add-feature | Start the workflow for adding a new feature  |
| /fix-bug     | Begin the bugfix workflow                    |
| /run-tests   | Run all tests in the codebase                |
```
