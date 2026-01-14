package com.backend.shared.constants;

import java.util.Set;

/**
 * Centralized validation constants synchronized with frontend.
 * These patterns and limits match frontend validation.constants.ts
 *
 * @see storefront/src/app/shared/constants/validation.constants.ts
 */
public final class ValidationConstants {

    private ValidationConstants() {
        // Prevent instantiation
    }

    // ============================================================================
    // REGEX PATTERNS (Frontend VALIDATION_PATTERNS ile senkronize)
    // ============================================================================

    /**
     * Product Type & Attribute Code Pattern
     * - Must start with lowercase letter
     * - Followed by lowercase letters, digits, or underscores
     * @example "product_type_1", "color", "size_xl"
     */
    public static final String CODE_PATTERN = "^[a-z][a-z0-9_]*$";

    /**
     * Category Code Pattern
     * - Must start with lowercase letter
     * - Followed by lowercase letters, digits, underscores, or hyphens
     * @example "electronics-main", "clothing_men", "home-decor"
     */
    public static final String CATEGORY_CODE_PATTERN = "^[a-z][a-z0-9_-]*$";

    /**
     * SKU Pattern
     * - Alphanumeric with underscores and hyphens
     * @example "SKU-12345", "PROD_001", "ABC123"
     */
    public static final String SKU_PATTERN = "^[A-Za-z0-9_-]+$";

    /**
     * URL Slug Pattern
     * - Lowercase letters, digits, and hyphens only
     * @example "about-us", "contact-page", "product-detail"
     */
    public static final String SLUG_PATTERN = "^[a-z0-9-]+$";

    /**
     * E.164 Phone Number Pattern
     * @example "+905551234567", "+12025551234"
     */
    public static final String PHONE_E164_PATTERN = "^\\+[1-9]\\d{1,14}$";

    /**
     * Subdomain Pattern
     * - Start/end with alphanumeric
     * - Middle can contain hyphens
     * - 3-50 characters total
     */
    public static final String SUBDOMAIN_PATTERN = "^[a-z0-9](?:[a-z0-9-]{1,48}[a-z0-9])$";

    // ============================================================================
    // SIZE LIMITS (Frontend VALIDATION_LIMITS ile senkronize)
    // ============================================================================

    // Product
    public static final int SKU_MAX_LENGTH = 100;
    public static final int NAME_MAX_LENGTH = 200;
    public static final int SHORT_DESCRIPTION_MAX_LENGTH = 500;
    public static final int SEO_TITLE_MAX_LENGTH = 200;
    public static final int SEO_DESCRIPTION_MAX_LENGTH = 500;
    public static final int CURRENCY_MAX_LENGTH = 3;

    // Codes
    public static final int CODE_MAX_LENGTH = 50;

    // Category
    public static final int CATEGORY_CODE_MAX_LENGTH = 50;
    public static final int CATEGORY_NAME_MAX_LENGTH = 200;

    // Product Type
    public static final int PRODUCT_TYPE_CODE_MAX_LENGTH = 50;
    public static final int PRODUCT_TYPE_NAME_MAX_LENGTH = 100;
    public static final int PRODUCT_TYPE_CATEGORY_MAX_LENGTH = 50;

    // Attribute
    public static final int ATTRIBUTE_CODE_MAX_LENGTH = 50;
    public static final int ATTRIBUTE_NAME_MAX_LENGTH = 100;

    // ============================================================================
    // NUMERIC LIMITS (Frontend VALIDATION_NUMERIC ile senkronize)
    // ============================================================================

    public static final String PRICE_MIN = "0.0";
    public static final int SORT_ORDER_MIN = 0;

    // ============================================================================
    // RESERVED VALUES
    // ============================================================================

    public static final Set<String> RESERVED_SUBDOMAINS = Set.of(
            "admin",
            "www",
            "api",
            "app",
            "platform",
            "mail",
            "support",
            "cdn",
            "static",
            "assets",
            "auth",
            "login",
            "dashboard",
            "console",
            "portal");

    // ============================================================================
    // VALIDATION MESSAGE KEYS (i18n keys)
    // ============================================================================

    public static final String MSG_CODE_REQUIRED = "validation.code.required";
    public static final String MSG_CODE_SIZE = "validation.code.size";
    public static final String MSG_CODE_PATTERN = "validation.code.pattern";

    public static final String MSG_CATEGORY_CODE_REQUIRED = "validation.category.code.required";
    public static final String MSG_CATEGORY_CODE_SIZE = "validation.category.code.size";
    public static final String MSG_CATEGORY_CODE_PATTERN = "validation.category.code.pattern";

    public static final String MSG_SKU_REQUIRED = "validation.product.sku.required";
    public static final String MSG_SKU_SIZE = "validation.product.sku.size";
    public static final String MSG_SKU_PATTERN = "validation.product.sku.pattern";

    // ============================================================================
    // HELPER METHODS
    // ============================================================================

    public static boolean isReservedSubdomain(String subdomain) {
        if (subdomain == null || subdomain.trim().isEmpty()) {
            return false;
        }
        return RESERVED_SUBDOMAINS.contains(subdomain.toLowerCase().trim());
    }
}
