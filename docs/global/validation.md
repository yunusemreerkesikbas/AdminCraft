# Validation Framework

AdminCraft uses a synchronized validation framework across frontend and backend to ensure consistent field validation with i18n error messages.

## Overview

| Layer | Location | Purpose |
|-------|----------|---------|
| Backend | `ValidationConstants.java` | Central patterns, limits, message keys |
| Backend | `@Code`, `@CategoryCode`, `@Sku` | Custom validation annotations |
| Frontend | `validation.constants.ts` | Synchronized patterns, limits, messages |
| Frontend | `spa-input`, `spa-textarea`, `spa-select` | Field-level error display |

---

## Backend Validation

### ValidationConstants

Central location for all validation patterns and limits:

**Location:** `backend/src/main/java/com/backend/shared/constants/ValidationConstants.java`

```java
public final class ValidationConstants {
    // Patterns
    public static final String CODE_PATTERN = "^[a-z][a-z0-9_]*$";
    public static final String CATEGORY_CODE_PATTERN = "^[a-z][a-z0-9_-]*$";
    public static final String SKU_PATTERN = "^[A-Za-z0-9_-]+$";

    // Limits
    public static final int SKU_MAX_LENGTH = 100;
    public static final int CODE_MAX_LENGTH = 50;
    public static final int NAME_MAX_LENGTH = 200;

    // Message Keys (i18n)
    public static final String MSG_CODE_PATTERN = "{validation.code.pattern}";
    public static final String MSG_SKU_PATTERN = "{validation.sku.pattern}";
}
```

### Custom Validation Annotations

#### @Code
For product types, attributes, and general code fields.

**Location:** `backend/src/main/java/com/backend/presentation/validation/Code.java`

```java
@Code
private String code;  // Must match: ^[a-z][a-z0-9_]*$
```

**Rules:**
- Must start with lowercase letter
- Only lowercase letters, digits, underscores allowed
- Max 50 characters

#### @CategoryCode
For category codes (allows hyphens).

**Location:** `backend/src/main/java/com/backend/presentation/validation/CategoryCode.java`

```java
@CategoryCode
private String code;  // Must match: ^[a-z][a-z0-9_-]*$
```

**Rules:**
- Must start with lowercase letter
- Lowercase letters, digits, underscores, hyphens allowed
- Max 50 characters

#### @Sku
For product SKU fields.

**Location:** `backend/src/main/java/com/backend/presentation/validation/Sku.java`

```java
@Sku
private String sku;  // Must match: ^[A-Za-z0-9_-]+$
```

**Rules:**
- Alphanumeric with underscores and hyphens
- Max 100 characters

### DTO Usage Example

```java
public class ProductTypeCreateRequest {
    @Code
    private String code;

    @NotBlank
    @Size(max = ValidationConstants.PRODUCT_TYPE_NAME_MAX_LENGTH)
    private String name;
}
```

### i18n Messages

**Location:** `backend/src/main/resources/i18n/messages_tr.properties`

```properties
validation.code.pattern=Kod küçük harfle başlamalı, sadece küçük harf, rakam ve alt çizgi içerebilir
validation.category.code.pattern=Kategori kodu küçük harfle başlamalı, sadece küçük harf, rakam, alt çizgi ve tire içerebilir
validation.sku.pattern=SKU sadece harf, rakam, alt çizgi ve tire içerebilir
```

---

## Frontend Validation

### Validation Constants

**Location:** `storefront/src/app/shared/constants/validation.constants.ts`

```typescript
// Patterns (synchronized with backend)
export const VALIDATION_PATTERNS = {
    CODE: /^[a-z][a-z0-9_]*$/,
    CATEGORY_CODE: /^[a-z][a-z0-9_-]*$/,
    SKU: /^[A-Za-z0-9_-]+$/,
    SLUG: /^[a-z0-9-]+$/,
} as const;

// Size limits
export const VALIDATION_LIMITS = {
    SKU_MAX: 100,
    CODE_MAX: 50,
    NAME_MAX: 200,
} as const;

// Numeric limits
export const VALIDATION_NUMERIC = {
    PRICE_MIN: 0,
} as const;

// i18n message keys (synchronized with backend)
export const VALIDATION_MESSAGES = {
    REQUIRED: 'validation.required',
    MIN_LENGTH: 'validation.min.length',
    MAX_LENGTH: 'validation.max.length',
    MIN: 'validation.min.value',
    MAX: 'validation.max.value',
    CODE_PATTERN: 'validation.code.pattern',
    CATEGORY_CODE_PATTERN: 'validation.category.code.pattern',
    SKU_PATTERN: 'validation.product.sku.pattern',
    SLUG_PATTERN: 'validation.slug.pattern',
} as const;
```

### Form Component Usage

```typescript
import { VALIDATION_PATTERNS, VALIDATION_LIMITS } from '@shared/constants/validation.constants';

this.form = this.fb.group({
    sku: ['', [
        Validators.required,
        Validators.maxLength(VALIDATION_LIMITS.SKU_MAX),
        Validators.pattern(VALIDATION_PATTERNS.SKU)
    ]],
    code: ['', [
        Validators.required,
        Validators.maxLength(VALIDATION_LIMITS.CODE_MAX),
        Validators.pattern(VALIDATION_PATTERNS.CODE)
    ]],
});
```

### spa-input Component

The `spa-input` component automatically displays validation errors.

**Features:**
- Implements `ControlValueAccessor` for reactive forms integration
- Uses Angular 19 `inject(NgControl, { optional: true, self: true })` pattern
- Custom `ErrorStateMatcher` for Angular Material integration
- `patternType` input for explicit pattern error messages
- Automatic i18n error message resolution
- No `CommonModule` import (Angular 19 standalone)

**Template Usage:**
```html
<spa-input
    formControlName="sku"
    [label]="'admin.products.fields.sku' | transloco"
    [placeholder]="'admin.common.placeholders.sku' | transloco"
    patternType="sku"
></spa-input>

<spa-input
    formControlName="code"
    [label]="'admin.products.fields.code' | transloco"
    patternType="code"
></spa-input>
```

**Error Display Logic:**
```typescript
get hasError(): boolean {
    const ctrl = this.activeControl;
    return !!(ctrl && ctrl.invalid && ctrl.touched);
}

get errorMessage(): string {
    const errors = this.activeControl?.errors;
    if (errors['required']) return VALIDATION_MESSAGES.REQUIRED;
    if (errors['pattern']) return this.getPatternErrorMessage();
    // ... other error types
}
```

### Pattern Error Messages

The component uses `patternType` input for explicit pattern selection, with label-based fallback:

| patternType | i18n Key |
|-------------|----------|
| `code` | `validation.code.pattern` |
| `categoryCode` | `validation.category.code.pattern` |
| `sku` | `validation.product.sku.pattern` |
| `slug` | `validation.slug.pattern` |

**Fallback (when patternType not set):**

| Label Contains | i18n Key |
|---------------|----------|
| `sku` | `validation.product.sku.pattern` |
| `category` + `code` | `validation.category.code.pattern` |
| `code`, `kod` | `validation.code.pattern` |
| `slug` | `validation.slug.pattern` |

### i18n Messages

**Location:** `storefront/src/app/modules/admin/i18n/langTR.ts`

```typescript
validation: {
    required: 'Bu alan zorunludur',
    pattern: 'Geçersiz format',
    min: {
        length: 'En az {{count}} karakter olmalıdır',
        value: 'En az {{min}} olmalıdır',
    },
    max: {
        length: 'En fazla {{count}} karakter olabilir',
        value: 'En fazla {{max}} olabilir',
    },
    code: {
        pattern: 'Küçük harf ile başlamalı, sadece küçük harf, rakam ve alt çizgi içerebilir',
    },
    category: {
        code: {
            pattern: 'Küçük harf ile başlamalı, sadece küçük harf, rakam, alt çizgi ve tire içerebilir',
        },
    },
    product: {
        sku: {
            pattern: 'Sadece harf, rakam, alt çizgi ve tire içerebilir',
        },
    },
    slug: {
        pattern: 'Yalnızca küçük harf, sayı ve tire içermelidir',
    },
}
```

---

## Field Validator Framework (Advanced)

For complex validation scenarios beyond simple annotations, use the FieldValidator framework.

### Key Components

| Component | Location | Purpose |
|-----------|----------|---------|
| `FieldValidator<T>` | `infrastructure/validation/FieldValidator.java` | Validator interface |
| `FieldValidatorBuilder<T>` | `infrastructure/validation/FieldValidatorBuilder.java` | Fluent builder API |
| `ValidationRule<T>` | `infrastructure/validation/ValidationRule.java` | Single validation rule |
| `ValidationContext` | `infrastructure/validation/ValidationContext.java` | Runtime metadata |

### Built-in Rules

- `KeyFormatRule` - Regex pattern validation
- `ReservedKeywordRule` - Block reserved keywords
- `LengthRule` - Min/max length validation
- `RangeRule` - Numeric range validation
- `CountLimitRule` - Collection size limits

### Usage Example

```java
@Configuration
public class ValidatorConfig {
    @Bean
    public FieldValidator<String> componentKeyValidator() {
        return FieldValidatorBuilder.<String>forField("key")
            .addRule(new LengthRule<>(1, 50))
            .addRule(new KeyFormatRule("^[a-z][a-z0-9_]*$"))
            .addRule(new ReservedKeywordRule(Set.of("id", "type", "class")))
            .build();
    }
}
```

---

## Best Practices

1. **Synchronize patterns** - Keep backend `ValidationConstants` and frontend `validation.constants.ts` in sync
2. **Use custom annotations** - Prefer `@Code`, `@CategoryCode`, `@Sku` over raw `@Pattern`
3. **i18n all messages** - Never hardcode error messages; use `validation.*` namespace
4. **Trim in DTO layer** - Use record compact constructors to trim whitespace:
   ```java
   public record ProductTypeCreateRequest(String code, String name) {
       public ProductTypeCreateRequest {
           code = code != null ? code.trim() : null;
           name = name != null ? name.trim() : null;
       }
   }
   ```
5. **Use patternType input** - Prefer explicit `patternType` over label-based inference
6. **Test both sides** - Ensure frontend and backend reject the same invalid inputs
