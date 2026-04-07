```markdown
# Craftive Development Patterns

> Auto-generated skill from repository analysis

## Overview
This skill teaches the core development patterns and conventions used in the Craftive TypeScript codebase. You'll learn how to structure files, write imports and exports, follow commit message patterns, and write tests in alignment with the project's standards. This guide ensures consistency and clarity for contributors working without a framework.

## Coding Conventions

### File Naming
- Use **camelCase** for all file names.
  - Example: `userProfile.ts`, `dataFetcher.ts`

### Import Style
- Use **relative imports** for all modules.
  - Example:
    ```typescript
    import { fetchData } from './dataFetcher';
    ```

### Export Style
- Use **named exports** exclusively.
  - Example:
    ```typescript
    // In dataFetcher.ts
    export function fetchData() { /* ... */ }
    ```
    ```typescript
    // In another file
    import { fetchData } from './dataFetcher';
    ```

### Commit Messages
- Freeform style, no enforced prefixes.
- Average commit message length: ~90 characters.
  - Example:
    ```
    Add initial implementation of user authentication logic
    ```

## Workflows

### Adding a New Module
**Trigger:** When you need to add a new feature or utility.
**Command:** `/add-module`

1. Create a new file using camelCase naming (e.g., `newFeature.ts`).
2. Implement your logic using TypeScript.
3. Export functions or constants using named exports.
4. Import dependencies using relative paths.
5. Write corresponding test file as `newFeature.test.ts`.
6. Commit your changes with a clear, descriptive message.

### Writing Tests
**Trigger:** When you implement new code or fix a bug.
**Command:** `/write-test`

1. Create a test file named `moduleName.test.ts` in the same directory as the module.
2. Use the project's preferred (unknown) testing framework.
3. Write tests that cover all exported functions.
4. Run the tests to ensure correctness.
5. Commit with a message describing the test coverage or bug fix.

## Testing Patterns

- Test files follow the pattern: `*.test.*` (e.g., `userProfile.test.ts`).
- Place test files alongside the module they test.
- Testing framework is not specified; follow existing patterns in the codebase.
- Example test file:
  ```typescript
  // userProfile.test.ts
  import { getUserProfile } from './userProfile';

  test('should fetch user profile by ID', () => {
    // ...test implementation
  });
  ```

## Commands
| Command        | Purpose                                      |
|----------------|----------------------------------------------|
| /add-module    | Scaffold and add a new TypeScript module     |
| /write-test    | Create and run tests for a module            |
```
