# Validation (Reusable Field Validators)

AdminCraft contains a reusable, rule-based validation framework that can be used across modules to validate “keys” and other fields consistently.

## Key building blocks

- `FieldValidator<T>`: validator interface (`backend/src/main/java/com/backend/infrastructure/validation/FieldValidator.java`)
- `FieldValidatorBuilder<T>`: fluent builder API (`backend/src/main/java/com/backend/infrastructure/validation/FieldValidatorBuilder.java`)
- `ValidationRule<T>`: single rule (`backend/src/main/java/com/backend/infrastructure/validation/ValidationRule.java`)
- `ValidationContext`: runtime metadata (counts, ids, etc.) (`backend/src/main/java/com/backend/infrastructure/validation/ValidationContext.java`)
- Default implementation: `DefaultFieldValidator` (`backend/src/main/java/com/backend/infrastructure/validation/DefaultFieldValidator.java`)
- Built-in rules: `backend/src/main/java/com/backend/infrastructure/validation/rules/`
  - `KeyFormatRule`, `ReservedKeywordRule`, `LengthRule`, `RangeRule`, `CountLimitRule`

## Usage pattern

### 1) Configure as Spring beans

Create validators as `@Bean`s in a `@Configuration` class and inject them where needed.

Example (component entry field validation config):

- `backend/src/main/java/com/backend/application/service/ComponentFieldValidatorConfig.java`

### 2) Use in application services

- Validate early in the application layer.
- Pass `ValidationContext` when rules require runtime metadata (e.g., “existing count” limits).
- Prefer throwing via a `throwIfInvalid()` style API to keep controller code clean.

## When to use

Use this framework when you want:

- shared key constraints (regex, reserved keywords, length)
- consistent error messages
- module-independent validation logic

