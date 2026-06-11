# Product Catalog

## Purpose

The Product Catalog module provides tenant-scoped management and public delivery for:

- Product types with dynamic attribute definitions (EAV)
- Global custom fields for all products (tenant-wide)
- Hierarchical categories with i18n content
- Products with composite create/update (translations + attributes + categories + custom fields + responsive gallery)
- Responsive media support for both main product image and gallery items (Desktop + Mobile)
- Commerce-ready product variants with reusable tenant-wide options, variant SKU, gross price, VAT rate, stock, and active state

## Database

Tenant migrations:

- `backend/src/main/resources/db/tenant/product/`
  - `V1.0.0__baseline.sql`
  - `V1.0.1__product_variants.sql`
  - `R__seed_product_types.sql`

Platform registration (module catalog):

- Module code: [`backend/src/main/java/com/backend/domain/enums/ModuleCode.java`](../../backend/src/main/java/com/backend/domain/enums/ModuleCode.java)
- Seed: [`backend/src/main/resources/db/platform/R__seed_modules.sql`](../../backend/src/main/resources/db/platform/R__seed_modules.sql)

## Admin API (tenant-scoped, authenticated)

All admin endpoints in this module require `TENANT_ADMIN` and are tenant-scoped (tenant must be resolved).

### Product types + attribute definitions

Controller: [`backend/src/main/java/com/backend/presentation/controller/ProductTypeController.java`](../../backend/src/main/java/com/backend/presentation/controller/ProductTypeController.java)

Base path: `/api/products/types`

- `GET /api/products/types` (paginated + sort + search)
- `POST /api/products/types`
- `GET /api/products/types/{id}` (includes attribute definitions)
- `PUT /api/products/types/{id}`
- `DELETE /api/products/types/{id}`

Attributes:

- `GET /api/products/types/{typeId}/attributes`
- `POST /api/products/types/{typeId}/attributes` (creates attribute with auto-generated `code` from `name` using `SlugGenerator`)
- `PUT /api/products/types/{typeId}/attributes/{attrId}` (updates `name` and `fieldType` only)
- `DELETE /api/products/types/{typeId}/attributes/{attrId}`

**Attribute Definition Model**:

- Fields: `id`, `uuid`, `uid`, `code` (auto-generated), `name`, `fieldType`
- Removed fields (as of V33): `isRequired`, `isSearchable`, `sortOrder`, `validationConfig`
- Code generation: Automatically generated from `name` using `SlugGenerator.generateUniqueCode()` to ensure uniqueness per product type

### Global Product Fields

Controller: [`backend/src/main/java/com/backend/presentation/controller/ProductFieldController.java`](../../backend/src/main/java/com/backend/presentation/controller/ProductFieldController.java)

Base path: `/api/products/fields`

- `GET /api/products/fields` (list all definitions)
- `POST /api/products/fields` (create definition with auto-generated `code` from `name` using `SlugGenerator`)
- `PUT /api/products/fields/{id}` (update definition: `name` and `fieldType` only)
- `DELETE /api/products/fields/{id}` (delete definition)

**Custom Field Definition Model**:

- Fields: `id`, `uuid`, `uid`, `code` (auto-generated), `name`, `fieldType`
- Removed fields (as of V32): `isRequired`, `isVisibleInList`, `sortOrder`, `defaultValue`, `validationConfig`
- Code generation: Automatically generated from `name` using `SlugGenerator.generateUniqueCode()` to ensure uniqueness

### Variant options and values

Controller: [`backend/src/main/java/com/backend/presentation/controller/ProductVariantOptionController.java`](../../backend/src/main/java/com/backend/presentation/controller/ProductVariantOptionController.java)

Base path: `/api/products/variant-options`

- `GET /api/products/variant-options`
- `GET /api/products/variant-options/{id}`
- `POST /api/products/variant-options`
- `PUT /api/products/variant-options/{id}`
- `DELETE /api/products/variant-options/{id}`

Variant options are tenant-wide reusable definitions such as color or size. Supported display types are `TEXT` and `COLOR`. Option and value codes are generated from names/labels using `SlugGenerator`.

### Categories (hierarchical, i18n, composite)

Controller: [`backend/src/main/java/com/backend/presentation/controller/CategoryController.java`](../../backend/src/main/java/com/backend/presentation/controller/CategoryController.java)

Base path: `/api/products/categories`

- `GET /api/products/categories` (tree)
- `GET /api/products/categories/{id}`
- `GET /api/products/categories/{id}/composite` (includes all translations)
- `POST /api/products/categories/composite` (atomic create: base + translations)
- `PUT /api/products/categories/{id}/composite` (atomic update: base + translations)
- `DELETE /api/products/categories/{id}`

### Products (composite, EAV attributes, categories, gallery)

Controller: [`backend/src/main/java/com/backend/presentation/controller/ProductController.java`](../../backend/src/main/java/com/backend/presentation/controller/ProductController.java)

Base path: `/api/products`

- `GET /api/products` (paginated + sort + search; optional filters: `status`, `categoryId`)
- `GET /api/products/{id}?include=translations` (composite view with translations)
- `POST /api/products/composite` (atomic create)
- `PUT /api/products/{id}/composite` (atomic update)
- `DELETE /api/products/{id}`
- `PATCH /api/products/{id}/status?status=DRAFT|PUBLISHED`
- `PATCH /api/products/{id}/visibility?isVisible=true|false`

Composite product requests and responses include optional `variants`.

Variant fields:

- `sku`
- `price` (gross price, tenant currency)
- `firstPrice` (optional original/list price)
- `vatRate`
- `stockQuantity`
- `active`
- `responsiveMediaId` (optional)
- `optionValueIds`

### Custom Fields Structure

Global product fields are returned in a **nested structure** within product responses:

```json
{
  "id": 10012,
  "sku": "PROD-001",
  "status": "PUBLISHED",
  "customFields": {
    "isbanner": false,
    "featured": true,
    "discount_percent": 15
  }
}
```

**Design Rationale:**

- **Namespace collision prevention**: Custom field names (e.g., `status`, `isVisible`, `name`) cannot conflict with fixed product fields
- **Type safety**: TypeScript can properly type `product.customFields` as `Record<string, unknown>`
- **API versioning**: The nested structure allows future changes to custom fields without breaking existing clients
- **Clear separation**: Makes it explicit which fields are custom vs. fixed product properties

**Response DTOs:**

- `ProductCompositeResponse.customFields`: `Map<String, Object>` (nested)
- `ProductCompositeResponse.variants`: product variant rows with selected option values
- `ProductListItemResponse`: Does not include customFields (list view optimization)
- `ProductDeliveryResponse`: includes active variants, but does not include customFields (public delivery optimization)

**Request DTOs:**

- `ProductCompositeRequest.customFields`: `Map<String, Object>` (nested)
- `ProductUpdateRequest.customFields`: `Map<String, Object>` (nested)
- `ProductCompositeRequest.variants` / `ProductUpdateRequest.variants`: optional variant rows. If omitted/empty on create, the backend creates one default variant from product SKU and base price.

## Public delivery APIs

Controller: [`backend/src/main/java/com/backend/presentation/controller/ProductCmsDeliveryController.java`](../../backend/src/main/java/com/backend/presentation/controller/ProductCmsDeliveryController.java)

Base path: `/api/cms/products`

Products:

- `GET /api/cms/products/{uid}?lang=TR`
- `GET /api/cms/products?uids=uid1&uids=uid2&lang=TR` (max 50)
- `GET /api/cms/products/category/{categoryUid}?page=0&size=20&lang=TR`
- `GET /api/cms/products/search?q=query&page=0&size=20&lang=TR`

Categories:

- `GET /api/cms/products/categories/{uid}?lang=TR`
- `GET /api/cms/products/categories?lang=TR` (visible category tree)

Language resolution:

- `lang` query param wins when provided (`Language` enum values like `TR`, `EN`)
- otherwise `Accept-Language` is mapped to `Language` (fallback: `TR`)

Rate limit:

- **100 req/min per tenant** (enforced using `TenantContext.tenantId`)

## Frontend integration (Admin)

Location: `storefront/src/app/modules/admin/custom/products/`

Key parts:

- Models: `models/`
  - `product.types.ts` (Product, ProductCompositeRequest, ProductListItemResponse, etc.)
  - `product-type.types.ts` (ProductType, AttributeDefinition, ProductFieldType, etc.)
  - `product-field.types.ts` (Custom field definitions: code, name, fieldType)
  - `product-variant-option.types.ts` (tenant-wide option/value definitions)
  - `category.types.ts` (Category, CategoryTreeResponse, etc.)
- Services:
  - `services/product.service.ts` (CRUD service extending `CrudHttpService`)
  - `services/product-type.service.ts` (CRUD service with attribute management)
  - `services/product-field.service.ts` (custom field definition management)
  - `services/product-variant-option.service.ts` (variant option/value management)
  - `services/category.service.ts` (tree operations and composite CRUD)
- Components:
  - `list/product-list.component.ts` (paginated list with search, extends `BaseCrudListComponent`; displays: name+SKU, productTypeName, status, actions)
  - `product-edit-dialog/` (composite product create/edit with tabs: general, i18n, attributes, custom fields, variants, categories, media)
  - `fields/product-field-dialog/` (simplified custom field definition create/edit: only `name` and `fieldType`; `code` auto-generated)
  - `variant-options/product-variant-options-dialog.component.ts` (minimal option/value management dialog)
  - `types/product-type-list.component.ts` (paginated list)
  - `types/product-type-edit-dialog/` (type management with attributes tab)
  - `types/product-attribute-dialog/` (simplified attribute definition create/edit: only `name` and `fieldType`; `code` auto-generated)
  - `categories/category-tree.component.ts` (hierarchical tree using Angular Material `MatTree`)
  - `categories/category-edit-dialog/` (composite category create/edit)
- Routes: `products.routes.ts`
- Shared component integration:
  - `storefront/src/app/shared/components/custom-ui/spa-dynamic-form/` (dynamic attribute rendering based on `DynamicFieldConfig`)

## Security & tenant isolation

### Multi-Tenant Validation

- **Tenant isolation** is enforced by database-per-tenant design (no `tenant_id` columns in tenant tables)
- **Request categorization** and tenant resolution is enforced by [`backend/src/main/java/com/backend/infrastructure/tenant/TenantFilter.java`](../../backend/src/main/java/com/backend/infrastructure/tenant/TenantFilter.java)
- **Service-level validation**: All 40 service methods across ProductService, CategoryService, and ProductTypeService include `TenantContext.validateActive()` at entry points to prevent cross-tenant data leakage
- **Admin endpoints** are authenticated and role-protected (`TENANT_ADMIN`)
- **Public delivery endpoints** are unauthenticated but still tenant-scoped (tenant must be resolvable)

### XSS Protection

HTML content fields are sanitized before persistence using Jsoup with `Safelist.relaxed()`:

- `ProductI18n.shortDescription` - Sanitized on create/update
- `ProductI18n.description` - Sanitized on create/update
- `CategoryI18n.description` - Sanitized on create/update

Allowed HTML tags: `<b>`, `<i>`, `<u>`, `<strong>`, `<em>`, `<a>`, `<img>`, `<p>`, `<div>`, `<h1-h6>`, lists  
Blocked tags: `<script>`, `<iframe>`, `<object>`, event handlers

### Input Validation

All request DTOs have comprehensive Bean Validation annotations:

- **Required fields**: `@NotNull`, `@NotBlank`, `@NotEmpty`
- **Size constraints**: `@Size(max=...)` on all string fields
- **Numeric validation**: `@DecimalMin("0.0")` on price fields
- **Pattern validation**: `@Pattern` for code fields (lowercase alphanumeric + underscore/hyphen)
- **Nested validation**: `@Valid` on complex objects (e.g., `Map<Language, *I18nRequest>`)
- **Controller validation**: All controllers use `@Validated` and `@Valid` on request bodies

## Performance optimizations

### N+1 Query Prevention

Repository methods use `@EntityGraph` to load related entities in single queries:

- `ProductRepository.findByIdComposite()` - Loads product with all relationships (i18n, attributes, categories, gallery) in 1 query instead of 40+
- `CategoryRepository.findByIdWithI18n()` - Loads category with translations in 1 query
- **Performance gain**: 80-90% reduction in database round trips

### Batch Operations

Translation save operations use batch processing for improved performance:

- `ProductServiceImpl.saveTranslations()` - Uses `saveAll()` for batch INSERT
- `ProductServiceImpl.updateTranslations()` - Uses `saveAll()` for batch UPDATE
- `CategoryServiceImpl` - Batch saves for category translations
- **Performance impact**: 60-70% faster for multi-language operations (5 languages = 1 batch query instead of 5 individual queries)

## Business rules & validation

### ProductType deletion

- ProductType cannot be deleted if any products are using it
- Returns **409 CONFLICT** with product count in error message
- Implemented via `BusinessRuleViolationException`

### Input validation rules

**Price validation**:

- `basePrice` field on product creation/update must be ≥ 0
- Variant `price`, `firstPrice`, and `vatRate` must be ≥ 0
- `firstPrice` cannot be less than `price`
- Enforced via `@DecimalMin("0.0")` on DTOs:
  - `ProductCompositeRequest`
  - `ProductUpdateRequest`
  - `ProductVariantRequest`

**String length validation**:

- `sku`: max 100 characters (`@Size(max=100)`)
- variant `sku`: max 100 characters and unique in `product_variants`
- `name`: max 200 characters (in i18n DTOs)
- `code`: max 100 characters with pattern validation (auto-generated from `name`)

**Required field validation**:

- All DTOs enforce required fields via `@NotNull`, `@NotBlank`, or `@NotEmpty`
- Translations map must contain at least one entry (`@NotEmpty`)
- Controller methods validate all request bodies using `@Valid`

**Code auto-generation**:

- `ProductFieldDefinition`, `ProductAttributeDefinition`, and variant options automatically generate `code` from `name` using `SlugGenerator.generateUniqueCode()`
- Code generation handles Turkish characters (ı, ğ, ü, ş, ö, ç) and ensures uniqueness by appending numeric suffixes if needed
- Location: `backend/src/main/java/com/backend/shared/util/SlugGenerator.java`

### Variant rules

- A product can use at most 2 distinct variant options.
- `PUBLISHED` products require at least one active valid variant.
- Active variants require SKU, price, VAT rate, and non-negative stock.
- Stock `0` does not block publish. Commerce cart add/update blocks requested quantities that exceed current stock, but does not reserve stock.
- Product `basePrice` is kept for backward compatibility and syncs to the minimum active variant price.
- Commerce cart stores variant gross price and VAT snapshots, then compares them with live variant values on cart read.

**Note**: Attribute validation rules (`validationConfig`) and required field checks (`isRequired`) have been removed as of migrations V32 and V33. Products now only validate basic field types (TEXT, NUMBER, BOOLEAN, DATE, etc.) without custom validation rules.

### Entity type safety

- `ProductMedia.mediaType` uses `ProductMediaType` enum (`PRIMARY`, `GALLERY`, `THUMBNAIL`)
- Stored as `VARCHAR` via `@Enumerated(EnumType.STRING)`

## Testing

The Product Catalog module has comprehensive backend test coverage using JUnit 5 + Mockito + AssertJ. For general testing patterns and conventions, see [`../global/testing.md`](../global/testing.md).

### Test structure

```
backend/src/test/java/com/backend/
├── testutil/
│   ├── BaseServiceTest.java                    # Base class with TenantContext setup
│   └── builders/                               # Test data builders (Builder Pattern)
│       ├── ProductTestDataBuilder.java
│       ├── ProductTypeTestDataBuilder.java
│       ├── CategoryTestDataBuilder.java
│       ├── CategoryI18nTestDataBuilder.java
│       ├── ProductI18nTestDataBuilder.java
│       └── ProductAttributeDefinitionTestDataBuilder.java
├── application/service/impl/
│   ├── ProductServiceImplTest.java             # ~35 unit tests
│   ├── CategoryServiceImplTest.java            # ~30 unit tests
│   └── ProductTypeServiceImplTest.java         # ~25 unit tests
├── presentation/controller/
│   ├── ProductControllerIntegrationTest.java   # @WebMvcTest integration tests
│   ├── CategoryControllerIntegrationTest.java
│   └── ProductTypeControllerIntegrationTest.java
└── presentation/dto/
    └── ProductCatalogDtoValidationTest.java    # Bean Validation tests
```

### Test categories

**Service Unit Tests** - Mock-based tests for business logic:

- CRUD operations (create, update, delete, find)
- Validation logic (translations, attributes, categories)
- Business rule enforcement (deletion constraints)
- TenantContext validation
- Exception handling (IllegalArgumentException, IllegalStateException, BusinessRuleViolationException)

**Controller Integration Tests** - `@WebMvcTest` for HTTP layer:

- Request/response mapping
- HTTP status codes (200, 400, 404, 409)
- Input validation via `@Valid`
- Error response formatting

**DTO Validation Tests** - Bean Validation annotation tests:

- `@NotNull`, `@NotBlank`, `@Size`, `@Pattern` constraints
- Nested validation (`@Valid` on maps)

### Critical test cases

| Test Case                           | Exception Type                   | HTTP Status      |
| ----------------------------------- | -------------------------------- | ---------------- |
| ProductType delete with products    | `BusinessRuleViolationException` | **409 Conflict** |
| Category delete with children       | `IllegalStateException`          | 400              |
| Category delete with products       | `IllegalStateException`          | 400              |
| Product create without translations | `IllegalArgumentException`       | 400              |
| Duplicate SKU prevention            | `IllegalArgumentException`       | 400              |
| Duplicate variant SKU prevention    | `IllegalArgumentException`       | 400              |
| More than two variant options       | `IllegalArgumentException`       | 400              |
| Publish without active valid variant | `IllegalArgumentException`      | 400              |
| Duplicate code prevention           | `IllegalArgumentException`       | 400              |
| TenantContext not active            | `IllegalStateException`          | 500              |
| Circular category parent            | `IllegalArgumentException`       | 400              |

### Running tests

```bash
# All Product Catalog tests
mvn test -Dtest="*ServiceImplTest,*ControllerIntegrationTest,*DtoValidationTest"

# Service unit tests only
mvn test -Dtest="ProductServiceImplTest,CategoryServiceImplTest,ProductTypeServiceImplTest"

# Controller integration tests only
mvn test -Dtest="*ControllerIntegrationTest"

# Coverage report
mvn jacoco:report
# Report: target/site/jacoco/index.html
```

### Test data builders

All builders follow the fluent Builder Pattern for readable test setup:

```java
// Example usage
ProductType productType = ProductTypeTestDataBuilder.aProductType()
    .withId(1L)
    .withCode("electronics")
    .withName("Electronics")
    .build();

Product product = ProductTestDataBuilder.aProduct()
    .withProductType(productType)
    .withSku("SKU-001")
    .withStatus(ProductStatus.PUBLISHED)
    .build();
```

## Implementation guide

### Minimal working flow (admin → delivery)

1. Create or seed a product type and its attribute definitions:
   - Admin API: `POST /api/products/types` then `POST /api/products/types/{typeId}/attributes`
   - Seed: `backend/src/main/resources/db/tenant/product/R__seed_product_types.sql`
2. Create categories (with translations):
   - `POST /api/products/categories/composite`
3. Create a product (atomic composite):
   - `POST /api/products/composite`
4. Optionally create reusable variant options:
   - `POST /api/products/variant-options`
5. Add variants on the product composite payload or let create generate the default variant.
6. Publish the product:
   - `PATCH /api/products/{id}/status?status=PUBLISHED`
7. Fetch the product publicly:
   - `GET /api/cms/products/{uid}?lang=TR`

### Add a new attribute type

- Attribute field types are defined in:
  - `backend/src/main/java/com/backend/domain/enums/ProductFieldType.java`
- Supported types: `TEXT`, `RICHTEXT`, `NUMBER`, `BOOLEAN`, `DATE`, `MEDIA`
- **Note**: Custom validation rules (`validationConfig`) have been removed. Products now only validate basic field types without custom min/max length, pattern, or range validations.

### Code generation

- Both global product fields and product attribute definitions use automatic code generation from the `name` field
- Implementation: `backend/src/main/java/com/backend/shared/util/SlugGenerator.java`
- The `code` field is generated using `SlugGenerator.generateCodeFromName()` and made unique using `SlugGenerator.generateUniqueCode()`
- Code generation handles Turkish character transliteration and ensures uniqueness per product type (for attributes) or globally (for custom fields)
