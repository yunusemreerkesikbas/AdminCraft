# Extended Fields Validation Rules

## Overview
This document defines the validation rules for dynamic extended fields in the Component Library module. Frontend implementations must follow these rules to ensure schema compatibility with backend validation.

## Field Schema Structure

```json
{
  "i18n": [
    {
      "key": "fieldName",
      "type": "text|textarea|number|boolean|select",
      "label": "Display Label",
      "required": false,
      "...constraints"
    }
  ]
}
```

## General Rules

| Rule | Value | Description |
|------|-------|-------------|
| Max Fields | 20 | Maximum number of fields per schema |
| Max Schema Size | 100KB | Maximum JSON schema size |
| Field Key Pattern | `^[a-zA-Z][a-zA-Z0-9_]*$` | Must start with letter, alphanumeric + underscore |
| Max Key Length | 50 | Maximum characters for field key |
| Max Label Length | 100 | Maximum characters for field label |
| Valid Types | text, textarea, number, boolean, select | Allowed field types |

## Field Types & Constraints

### 1. Text

```json
{
  "key": "authorName",
  "type": "text",
  "label": "Author Name",
  "required": true,
  "minLength": 2,
  "maxLength": 100,
  "pattern": "^[A-Za-z\\s]+$"
}
```

**Constraints:**
- `required`: boolean (optional)
- `minLength`: integer >= 0 (optional)
- `maxLength`: integer >= 1 (optional)
- `pattern`: valid regex string (optional)

**Validation:**
- If both minLength and maxLength provided: minLength <= maxLength
- Pattern must be valid regex

### 2. Textarea

```json
{
  "key": "description",
  "type": "textarea",
  "label": "Description",
  "required": false,
  "minLength": 10,
  "maxLength": 500
}
```

**Constraints:** Same as text type

### 3. Number

```json
{
  "key": "rating",
  "type": "number",
  "label": "Rating",
  "required": true,
  "min": 1,
  "max": 5
}
```

**Constraints:**
- `required`: boolean (optional)
- `min`: number (optional)
- `max`: number (optional)

**Validation:**
- If both min and max provided: min <= max
- Supports decimal numbers

### 4. Boolean

```json
{
  "key": "featured",
  "type": "boolean",
  "label": "Featured Item",
  "required": false
}
```

**Constraints:**
- `required`: boolean (optional)

**Validation:**
- No type-specific constraints

### 5. Select

```json
{
  "key": "category",
  "type": "select",
  "label": "Category",
  "required": true,
  "options": ["tech", "business", "lifestyle"]
}
```

**Constraints:**
- `required`: boolean (optional)
- `options`: array of strings (REQUIRED)

**Validation:**
- options array cannot be empty
- All options must be non-empty strings
- Duplicate options allowed

## XSS Sanitization

Backend automatically sanitizes:
- Field labels
- Select options
- String values in extended data

Frontend should NOT pre-sanitize values.

## Validation Endpoint

### POST /api/components/types/validate-schema

**Request:**
```json
{
  "i18n": [
    {"key": "author", "type": "text", "label": "Author"}
  ]
}
```

**Success Response:**
```json
{
  "success": true,
  "message": "Schema validated successfully",
  "data": {
    "i18n": [
      {"key": "author", "type": "text", "label": "Author"}
    ]
  }
}
```

**Error Response:**
```json
{
  "success": false,
  "message": "Invalid extended fields schema: Field key 'author-name' must start with a letter..."
}
```

## Frontend Implementation Guide

### Schema Builder Form

```typescript
interface FieldDefinition {
  key: string;
  type: 'text' | 'textarea' | 'number' | 'boolean' | 'select';
  label: string;
  required?: boolean;
  minLength?: number;
  maxLength?: number;
  min?: number;
  max?: number;
  pattern?: string;
  options?: string[];
}

interface ExtendedFieldsSchema {
  i18n: FieldDefinition[];
}
```

### Validation Rules (Angular Validators)

```typescript
const KEY_PATTERN = /^[a-zA-Z][a-zA-Z0-9_]*$/;
const MAX_KEY_LENGTH = 50;
const MAX_LABEL_LENGTH = 100;
const MAX_FIELDS = 20;

const keyValidators = [
  Validators.required,
  Validators.maxLength(MAX_KEY_LENGTH),
  Validators.pattern(KEY_PATTERN)
];

const labelValidators = [
  Validators.required,
  Validators.maxLength(MAX_LABEL_LENGTH)
];
```

### Dynamic Form Rendering

```typescript
function renderField(fieldDef: FieldDefinition, control: FormControl) {
  switch (fieldDef.type) {
    case 'text':
      return `<input type="text" [formControl]="control"
              [maxLength]="fieldDef.maxLength" />`;
    case 'textarea':
      return `<textarea [formControl]="control"
              [maxLength]="fieldDef.maxLength"></textarea>`;
    case 'number':
      return `<input type="number" [formControl]="control"
              [min]="fieldDef.min" [max]="fieldDef.max" />`;
    case 'boolean':
      return `<input type="checkbox" [formControl]="control" />`;
    case 'select':
      return `<select [formControl]="control">
              <option *ngFor="let opt of fieldDef.options">{{opt}}</option>
            </select>`;
  }
}
```

### Client-Side Validation

```typescript
function buildValidators(fieldDef: FieldDefinition): ValidatorFn[] {
  const validators: ValidatorFn[] = [];

  if (fieldDef.required) {
    validators.push(Validators.required);
  }

  if (fieldDef.type === 'text' || fieldDef.type === 'textarea') {
    if (fieldDef.minLength != null) {
      validators.push(Validators.minLength(fieldDef.minLength));
    }
    if (fieldDef.maxLength != null) {
      validators.push(Validators.maxLength(fieldDef.maxLength));
    }
    if (fieldDef.pattern) {
      validators.push(Validators.pattern(fieldDef.pattern));
    }
  }

  if (fieldDef.type === 'number') {
    if (fieldDef.min != null) {
      validators.push(Validators.min(fieldDef.min));
    }
    if (fieldDef.max != null) {
      validators.push(Validators.max(fieldDef.max));
    }
  }

  return validators;
}
```

## Error Messages

Backend error messages follow i18n pattern:
- `component.type.schema.validate.success`
- `component.type.schema.validate.error`

Frontend should display these messages from API responses.

## Example Schemas

### Testimonial Component

```json
{
  "i18n": [
    {
      "key": "customerName",
      "type": "text",
      "label": "Customer Name",
      "required": true,
      "maxLength": 100
    },
    {
      "key": "testimonial",
      "type": "textarea",
      "label": "Testimonial Text",
      "required": true,
      "minLength": 20,
      "maxLength": 500
    },
    {
      "key": "rating",
      "type": "number",
      "label": "Rating (1-5)",
      "required": true,
      "min": 1,
      "max": 5
    },
    {
      "key": "verified",
      "type": "boolean",
      "label": "Verified Purchase"
    },
    {
      "key": "industry",
      "type": "select",
      "label": "Industry",
      "options": ["Technology", "Healthcare", "Finance", "Retail"]
    }
  ]
}
```

### CTA Component

```json
{
  "i18n": [
    {
      "key": "ctaText",
      "type": "text",
      "label": "CTA Text",
      "required": true,
      "maxLength": 50
    },
    {
      "key": "description",
      "type": "textarea",
      "label": "Description",
      "maxLength": 200
    },
    {
      "key": "priority",
      "type": "number",
      "label": "Priority",
      "min": 1,
      "max": 10
    },
    {
      "key": "style",
      "type": "select",
      "label": "Button Style",
      "options": ["primary", "secondary", "outline"]
    }
  ]
}
```

## Notes

- All extended fields are i18n fields (language-specific)
- No general (language-agnostic) fields in Sprint 2
- Backend validates on create/update component type
- Backend validates extended localized data against schema when saving component i18n
- XSS sanitization happens server-side
- Field keys must be unique within schema
- Frontend should call validate endpoint before form submission
