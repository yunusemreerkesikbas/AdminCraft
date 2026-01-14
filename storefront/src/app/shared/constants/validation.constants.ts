/**
 * Validation Constants - Synchronized with Backend
 * These patterns and limits match backend Jakarta Validation annotations
 *
 * @see backend/src/main/java/com/backend/presentation/dto/request/*.java
 */

// ============================================================================
// REGEX PATTERNS (Backend @Pattern ile uyumlu)
// ============================================================================
export const VALIDATION_PATTERNS = {
    /**
     * Product Type & Attribute Code Pattern
     * - Must start with lowercase letter
     * - Followed by lowercase letters, digits, or underscores
     * @example "product_type_1", "color", "size_xl"
     * @backend ^[a-z][a-z0-9_]*$
     */
    CODE: /^[a-z][a-z0-9_]*$/,

    /**
     * Category Code Pattern
     * - Must start with lowercase letter
     * - Followed by lowercase letters, digits, underscores, or hyphens
     * @example "electronics-main", "clothing_men", "home-decor"
     * @backend ^[a-z][a-z0-9_-]*$
     */
    CATEGORY_CODE: /^[a-z][a-z0-9_-]*$/,

    /**
     * SKU Pattern
     * - Alphanumeric with underscores and hyphens
     * @example "SKU-12345", "PROD_001", "ABC123"
     */
    SKU: /^[A-Za-z0-9_-]+$/,

    /**
     * URL Slug Pattern
     * - Lowercase letters, digits, and hyphens only
     * @example "about-us", "contact-page", "product-detail"
     * @backend [a-z0-9-]+
     */
    SLUG: /^[a-z0-9]+(?:-[a-z0-9]+)*$/,

    /**
     * UID Pattern
     * - Start with lowercase letter or digit
     * - Lowercase letters, digits, underscores, and hyphens allowed
     * @backend ^[a-z0-9][a-z0-9_-]*$
     */
    UID: /^[a-z0-9][a-z0-9_-]*$/,

    /**
     * Slot Name Pattern
     * - Start with a letter
     * - Letters, digits, underscores, and hyphens allowed
     * @backend ^[A-Za-z][A-Za-z0-9_-]*$
     */
    SLOT_NAME: /^[A-Za-z][A-Za-z0-9_-]*$/,

    /**
     * Media Code Pattern
     * - Must start with lowercase letter
     * - Lowercase letters, digits, underscores, and hyphens allowed
     * @backend ^[a-z][a-z0-9_-]*$
     */
    MEDIA_CODE: /^[a-z][a-z0-9_-]*$/,

    /**
     * E.164 Phone Number Pattern
     * @example "+905551234567", "+12025551234"
     * @backend ^\\+[1-9]\\d{1,14}$
     */
    PHONE_E164: /^\+[1-9]\d{1,14}$/,

    /**
     * Subdomain Pattern
     * - Start/end with alphanumeric
     * - Middle can contain hyphens
     * - 3-50 characters total
     * @backend ^[a-z0-9](?:[a-z0-9-]{1,48}[a-z0-9])$
     */
    SUBDOMAIN: /^[a-z0-9](?:[a-z0-9-]{1,48}[a-z0-9])$/,
} as const;

// ============================================================================
// SIZE LIMITS (Backend @Size ile uyumlu)
// ============================================================================
export const VALIDATION_LIMITS = {
    // Product
    SKU_MAX: 100,
    NAME_MAX: 200,
    SHORT_DESCRIPTION_MAX: 500,
    SEO_TITLE_MAX: 200,
    SEO_DESCRIPTION_MAX: 500,
    CURRENCY_MAX: 3,

    // Codes
    CODE_MAX: 50,

    // Slug
    SLUG_MIN: 3,
    SLUG_MAX: 200,

    // UID
    UID_MAX: 100,
    UID_PAGE_MAX: 36,
    UID_TEMPLATE_MAX: 50,

    // Slot
    SLOT_NAME_MAX: 50,

    // Media
    MEDIA_CODE_MAX: 100,
    MEDIA_FORMAT_CODE_MAX: 50,
    MEDIA_FORMAT_NAME_MAX: 100,
    MEDIA_FORMAT_DIMENSION_MIN: 1,
    MEDIA_FORMAT_DIMENSION_MAX: 10000,
    MEDIA_FORMAT_QUALITY_MIN: 1,
    MEDIA_FORMAT_QUALITY_MAX: 100,
    MEDIA_TAGS_MAX: 20,
    MEDIA_ALT_TEXT_MAX: 500,
    MEDIA_TITLE_MAX: 255,
    MEDIA_DESCRIPTION_MAX: 5000,

    // Category
    CATEGORY_CODE_MAX: 50,
    CATEGORY_NAME_MAX: 200,

    // Product Type
    PRODUCT_TYPE_CODE_MAX: 50,
    PRODUCT_TYPE_NAME_MAX: 100,
    PRODUCT_TYPE_CATEGORY_MAX: 50,

    // Attribute
    ATTRIBUTE_CODE_MAX: 50,
    ATTRIBUTE_NAME_MAX: 100,

    // Components
    COMPONENT_NAME_MAX: 100,
    COMPONENT_TYPE_NAME_MAX: 100,
    COMPONENT_TYPE_CATEGORY_MAX: 50,
    COMPONENT_STYLE_CLASSES_MAX: 500,
    COMPONENT_ENTRY_TITLE_MAX: 255,
    COMPONENT_ENTRY_IMAGE_URL_MAX: 500,
    COMPONENT_ENTRY_BUTTON_TEXT_MAX: 100,
    COMPONENT_ENTRY_BUTTON_URL_MAX: 500,

    // Pages
    PAGE_TITLE_MAX: 200,
    PAGE_NAME_MAX: 200,
    PAGE_DESCRIPTION_MAX: 5000,
    PAGE_CANONICAL_URL_MAX: 255,
    PAGE_STYLE_CLASSES_MAX: 255,

    // Page Templates
    PAGE_TEMPLATE_NAME_MAX: 100,
    PAGE_TEMPLATE_DESCRIPTION_MAX: 500,

    // Slots
    SLOT_POSITION_MAX: 20,
    SLOT_ALLOWED_TYPES_MAX: 50,
    SLOT_ALLOWED_TYPE_MAX: 100,
} as const;

// ============================================================================
// NUMERIC LIMITS (Backend @Min/@Max/@DecimalMin ile uyumlu)
// ============================================================================
export const VALIDATION_NUMERIC = {
    PRICE_MIN: 0,
    SORT_ORDER_MIN: 0,
    SORT_ORDER_DEFAULT: 0,
} as const;

// ============================================================================
// ERROR MESSAGE KEYS (i18n keys - synchronized with backend)
// ============================================================================
export const VALIDATION_MESSAGES = {
    REQUIRED: 'validation.required',
    REQUIRED_FIELD: 'validation.required',
    MIN_LENGTH: 'validation.min.length',
    MAX_LENGTH: 'validation.max.length',
    MIN: 'validation.min.value',
    MAX: 'validation.max.value',
    PATTERN: 'validation.pattern',
    EMAIL: 'validation.email',
    URL: 'validation.url.invalid',

    CODE_PATTERN: 'validation.code.pattern',
    CATEGORY_CODE_PATTERN: 'validation.category.code.pattern',
    SKU_PATTERN: 'validation.product.sku.pattern',
    SLUG_PATTERN: 'validation.slug.pattern',
    UID_PATTERN: 'validation.uid.pattern',
    SLOT_NAME_PATTERN: 'validation.slot.name.pattern',
    MEDIA_CODE_PATTERN: 'validation.media.code.pattern',
} as const;

// ============================================================================
// HELPER TYPE
// ============================================================================
export type ValidationPatternKey = keyof typeof VALIDATION_PATTERNS;
export type ValidationLimitKey = keyof typeof VALIDATION_LIMITS;
