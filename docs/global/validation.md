# Validation Framework

Craftive uses a synchronized validation framework across frontend and backend to ensure consistent field validation with i18n error messages.

## Overview

| Layer | Location | Purpose |
|-------|----------|---------|
| Backend | `ValidationConstants.java` | Central patterns, limits, message keys |
| Backend | `@Code`, `@CategoryCode`, `@Sku`, `@Slug`, `@Uid`, `@SlotName`, `@MediaCode` | Custom validation annotations |
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
    public static final String SLUG_PATTERN = "^[a-z0-9]+(?:-[a-z0-9]+)*$";
    public static final String UID_PATTERN = "^[a-z0-9][a-z0-9_-]*$";
    public static final String SLOT_NAME_PATTERN = "^[A-Za-z][A-Za-z0-9_-]*$";
    public static final String MEDIA_CODE_PATTERN = "^[a-z][a-z0-9_-]*$";

    // Limits
    public static final int SKU_MAX_LENGTH = 100;
    public static final int CODE_MAX_LENGTH = 50;
    public static final int NAME_MAX_LENGTH = 200;
    public static final int SLUG_MIN_LENGTH = 3;
    public static final int SLUG_MAX_LENGTH = 200;
    public static final int UID_MAX_LENGTH = 100;
    public static final int UID_PAGE_MAX_LENGTH = 36;
    public static final int UID_TEMPLATE_MAX_LENGTH = 50;
    public static final int SLOT_NAME_MAX_LENGTH = 50;
    public static final int MEDIA_CODE_MAX_LENGTH = 100;

    // Message Keys (i18n)
    public static final String MSG_CODE_PATTERN = "{validation.code.pattern}";
    public static final String MSG_SKU_PATTERN = "{validation.sku.pattern}";
    public static final String MSG_SLUG_PATTERN = "{validation.slug.pattern}";
    public static final String MSG_UID_PATTERN = "{validation.uid.pattern}";
    public static final String MSG_SLOT_NAME_PATTERN = "{validation.slot.name.pattern}";
    public static final String MSG_MEDIA_CODE_PATTERN = "{validation.media.code.pattern}";
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

#### @Slug
For page slug fields.

**Location:** `backend/src/main/java/com/backend/presentation/validation/Slug.java`

```java
@Slug
private String slug;  // Must match: ^[a-z0-9]+(?:-[a-z0-9]+)*$
```

**Rules:**
- Lowercase letters, digits, and hyphens only
- Min 3, max 200 characters

#### @Uid
For UID fields (page/template/slot optional).

**Location:** `backend/src/main/java/com/backend/presentation/validation/Uid.java`

```java
@Uid(required = false)
private String uid;  // Must match: ^[a-z0-9][a-z0-9_-]*$
```

**Rules:**
- Start with lowercase letter or digit
- Lowercase letters, digits, underscores, and hyphens allowed
- Max length varies by DTO (e.g., page 36, template 50, default 100)

#### @SlotName
For page/template slot name fields.

**Location:** `backend/src/main/java/com/backend/presentation/validation/SlotName.java`

```java
@SlotName
private String slotName;  // Must match: ^[A-Za-z][A-Za-z0-9_-]*$
```

**Rules:**
- Start with a letter
- Letters, digits, underscores, and hyphens allowed
- Max 50 characters

#### @MediaCode
For media code fields.

**Location:** `backend/src/main/java/com/backend/presentation/validation/MediaCode.java`

```java
@MediaCode
private String code;  // Must match: ^[a-z][a-z0-9_-]*$
```

**Rules:**
- Must start with lowercase letter
- Lowercase letters, digits, underscores, and hyphens allowed
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
    SLUG: /^[a-z0-9]+(?:-[a-z0-9]+)*$/,
    UID: /^[a-z0-9][a-z0-9_-]*$/,
    SLOT_NAME: /^[A-Za-z][A-Za-z0-9_-]*$/,
    MEDIA_CODE: /^[a-z][a-z0-9_-]*$/,
    PASSWORD_COMPLEXITY: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/,
    RECAPTCHA_KEY: /^[A-Za-z0-9_-]{40}$/,
} as const;

// Size limits
export const VALIDATION_LIMITS = {
    SKU_MAX: 100,
    CODE_MAX: 50,
    NAME_MAX: 200,
    SLUG_MIN: 3,
    SLUG_MAX: 200,
    UID_MAX: 100,
    UID_PAGE_MAX: 36,
    UID_TEMPLATE_MAX: 50,
    SLOT_NAME_MAX: 50,
    MEDIA_CODE_MAX: 100,
    USER_PASSWORD_MIN: 8,
    RECAPTCHA_KEY_LENGTH: 40,
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
    UID_PATTERN: 'validation.uid.pattern',
    SLOT_NAME_PATTERN: 'validation.slot.name.pattern',
    MEDIA_CODE_PATTERN: 'validation.media.code.pattern',
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

### Auth & Security Validation

```typescript
this.form = this.fb.group({
    password: [
        '',
        [
            Validators.required,
            Validators.minLength(VALIDATION_LIMITS.USER_PASSWORD_MIN),
            Validators.pattern(VALIDATION_PATTERNS.PASSWORD_COMPLEXITY),
        ],
    ],
    recaptchaSiteKey: [
        '',
        [
            Validators.maxLength(VALIDATION_LIMITS.RECAPTCHA_KEY_LENGTH),
            Validators.pattern(VALIDATION_PATTERNS.RECAPTCHA_KEY),
        ],
    ],
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
| `uid` | `validation.uid.pattern` |
| `slotName` | `validation.slot.name.pattern` |
| `mediaCode` | `validation.media.code.pattern` |

**Fallback (when patternType not set):**

| Label Contains | i18n Key |
|---------------|----------|
| `sku` | `validation.product.sku.pattern` |
| `category` + `code` | `validation.category.code.pattern` |
| `code`, `kod` | `validation.code.pattern` |
| `slug` | `validation.slug.pattern` |
| `uid` | `validation.uid.pattern` |
| `slot` + `name` | `validation.slot.name.pattern` |
| `media` + `code` | `validation.media.code.pattern` |

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

## Nested DTO Validation (`@Valid` Cascade)

When a request DTO contains a nested object with its own `@Size` / `@Pattern` constraints, Bean Validation does **not** recurse into it automatically. You must add `@Valid` on the nested field or record component.

**Example — `SiteSettingsI18nDto`:**
```java
public record SiteSettingsI18nDto(
    @Size(max = 100) String siteName,
    @Size(max = 160) String tagline,
    @Valid SeoDto seo          // ← required; without @Valid the @Size rules inside SeoDto are silently ignored
) {
    public record SeoDto(
        @Size(max = 200) String title,
        @Size(max = 500) String description,
        ...
    ) {}
}
```

This applies to any record or class DTO that embeds another validated type. Always add `@Valid` to the nested field — omitting it is a silent validation gap.

`GlobalExceptionHandler` handles both `FieldError` (field-level) and `ObjectError` (class-level validators such as `@PasswordMatch`) in the `MethodArgumentNotValidException` handler; both produce entries in the `data` map returned to the client.

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
