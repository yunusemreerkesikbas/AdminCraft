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
     * 
     * @example "product_type_1", "color", "size_xl"
     */
    public static final String CODE_PATTERN = "^[a-z][a-z0-9_]*$";

    /**
     * Category Code Pattern
     * - Must start with lowercase letter
     * - Followed by lowercase letters, digits, underscores, or hyphens
     * 
     * @example "electronics-main", "clothing_men", "home-decor"
     */
    public static final String CATEGORY_CODE_PATTERN = "^[a-z][a-z0-9_-]*$";

    /**
     * SKU Pattern
     * - Alphanumeric with underscores and hyphens
     * 
     * @example "SKU-12345", "PROD_001", "ABC123"
     */
    public static final String SKU_PATTERN = "^[A-Za-z0-9_-]+$";

    /**
     * URL Slug Pattern
     * - Lowercase letters, digits, and hyphens only
     * 
     * @example "about-us", "contact-page", "product-detail"
     */
    public static final String SLUG_PATTERN = "^[a-z0-9]+(?:-[a-z0-9]+)*$";

    /**
     * UID Pattern
     * - Start with letter or digit
     * - Letters, digits, underscores, and hyphens allowed
     * 
     * @example "SeedLandingPortfolioGrid", "page_123", "template-main"
     */
    public static final String UID_PATTERN = "^[A-Za-z0-9][A-Za-z0-9_-]*$";

    /**
     * Slot Name Pattern
     * - Start with a letter
     * - Letters, digits, underscores, and hyphens allowed
     * 
     * @example "Header", "Main_Content", "footer-section"
     */
    public static final String SLOT_NAME_PATTERN = "^[A-Za-z][A-Za-z0-9_-]*$";

    /**
     * Media Code Pattern
     * - Must start with lowercase letter
     * - Lowercase letters, digits, underscores, and hyphens allowed
     * 
     * @example "hero_desktop", "banner-1"
     */
    public static final String MEDIA_CODE_PATTERN = "^[a-z][a-z0-9_-]*$";

    /**
     * Global Phone Number Pattern (Loose)
     * - Optional '+' prefix
     * - 7-15 digits
     * 
     * @example "+905551234567", "05551234567", "905551234567"
     */
    public static final String PHONE_GLOBAL_PATTERN = "^\\+?[0-9]{7,15}$";

    /**
     * Optional phone: empty or same rules as {@link #PHONE_GLOBAL_PATTERN}.
     */
    public static final String PHONE_GLOBAL_OPTIONAL_PATTERN = "^(|\\+?[0-9]{7,15})$";

    /**
     * Subdomain Pattern
     * - Start/end with alphanumeric
     * - Middle can contain hyphens
     * - 3-50 characters total
     */
    public static final String SUBDOMAIN_PATTERN = "^[a-z0-9](?:[a-z0-9-]{1,48}[a-z0-9])$";

    /**
     * Password Pattern
     * - At least 8 characters
     * - At least one lowercase letter
     * - At least one uppercase letter
     * - At least one digit
     *
     * @example "Password1", "SecurePass123"
     */
    public static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";

    /**
     * Device Fingerprint Pattern
     * - Alphanumeric with underscore and hyphen
     * - Max 128 characters
     *
     * @example "fp_abc123", "device-fingerprint-123"
     */
    public static final String DEVICE_FINGERPRINT_PATTERN = "^[A-Za-z0-9_-]*$";

    /**
     * IP Address Pattern (IPv4 and IPv6)
     * - Validates both IPv4 (192.168.1.1) and IPv6 addresses
     */
    public static final String IP_ADDRESS_PATTERN =
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|" +
            "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|" +
            "^::$|^::1$|^([0-9a-fA-F]{1,4}:){1,7}:$|^([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}$";

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

    // Gallery
    public static final int GALLERY_MAX_SIZE = 10;

    // Codes
    public static final int CODE_MAX_LENGTH = 50;

    // Slug
    public static final int SLUG_MIN_LENGTH = 3;
    public static final int SLUG_MAX_LENGTH = 200;

    // UID
    public static final int UID_MAX_LENGTH = 100;
    public static final int UID_PAGE_MAX_LENGTH = 36;
    public static final int UID_TEMPLATE_MAX_LENGTH = 50;

    // Slot
    public static final int SLOT_NAME_MAX_LENGTH = 50;

    // Media
    public static final int MEDIA_CODE_MAX_LENGTH = 100;
    public static final int MEDIA_FORMAT_CODE_MAX_LENGTH = 50;
    public static final int MEDIA_FORMAT_NAME_MAX_LENGTH = 100;
    public static final int MEDIA_FORMAT_DIMENSION_MIN = 1;
    public static final int MEDIA_FORMAT_DIMENSION_MAX = 10000;
    public static final int MEDIA_FORMAT_QUALITY_MIN = 1;
    public static final int MEDIA_FORMAT_QUALITY_MAX = 100;
    public static final int MEDIA_TAGS_MAX = 20;
    public static final int MEDIA_I18N_ALT_TEXT_MAX_LENGTH = 500;
    public static final int MEDIA_I18N_TITLE_MAX_LENGTH = 255;
    public static final int MEDIA_I18N_DESCRIPTION_MAX_LENGTH = 5000;

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

    // Components
    public static final int COMPONENT_NAME_MAX_LENGTH = 100;
    public static final int COMPONENT_TYPE_NAME_MAX_LENGTH = 100;
    public static final int COMPONENT_TYPE_CATEGORY_MAX_LENGTH = 50;
    public static final int COMPONENT_STYLE_CLASSES_MAX_LENGTH = 500;
    public static final int COMPONENT_ENTRY_TITLE_MAX_LENGTH = 255;
    public static final int COMPONENT_ENTRY_IMAGE_URL_MAX_LENGTH = 500;
    public static final int COMPONENT_ENTRY_BUTTON_TEXT_MAX_LENGTH = 100;
    public static final int COMPONENT_ENTRY_BUTTON_URL_MAX_LENGTH = 500;

    // Pages
    public static final int PAGE_TITLE_MAX_LENGTH = 200;
    public static final int PAGE_NAME_MAX_LENGTH = 200;
    public static final int PAGE_DESCRIPTION_MAX_LENGTH = 5000;
    public static final int PAGE_CANONICAL_URL_MAX_LENGTH = 255;
    public static final int PAGE_STYLE_CLASSES_MAX_LENGTH = 255;

    // Page Templates
    public static final int PAGE_TEMPLATE_NAME_MAX_LENGTH = 100;
    public static final int PAGE_TEMPLATE_DESCRIPTION_MAX_LENGTH = 500;

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

    public static final String MSG_SLUG_REQUIRED = "validation.slug.required";
    public static final String MSG_SLUG_SIZE = "validation.slug.size";
    public static final String MSG_SLUG_PATTERN = "validation.slug.pattern";

    public static final String MSG_UID_REQUIRED = "validation.uid.required";
    public static final String MSG_UID_SIZE = "validation.uid.size";
    public static final String MSG_UID_PATTERN = "validation.uid.pattern";

    public static final String MSG_SLOT_NAME_REQUIRED = "validation.slot.name.required";
    public static final String MSG_SLOT_NAME_SIZE = "validation.slot.name.size";
    public static final String MSG_SLOT_NAME_PATTERN = "validation.slot.name.pattern";

    public static final String MSG_MEDIA_CODE_REQUIRED = "validation.media.code.required";
    public static final String MSG_MEDIA_CODE_SIZE = "validation.media.code.size";
    public static final String MSG_MEDIA_CODE_PATTERN = "{validation.media.code.pattern}";

    // ============================================================================
    // RECAPTCHA
    // ============================================================================

    /**
     * reCAPTCHA Key Pattern (Site Key and Secret Key)
     * - Exactly 40 alphanumeric characters with optional hyphens and underscores
     * 
     * @example "6LdZU2UqAAAAAG9Y7vX_abcdefghijklmnopqrst"
     */
    public static final String RECAPTCHA_KEY_PATTERN = "^[A-Za-z0-9_-]{40}$";
    public static final int RECAPTCHA_KEY_LENGTH = 40;
    
    /**
     * reCAPTCHA Token Pattern
     * - Alphanumeric with underscores and hyphens
     */
    public static final String RECAPTCHA_TOKEN_PATTERN = "^[A-Za-z0-9_-]+$";
    public static final int RECAPTCHA_TOKEN_MAX_LENGTH = 4096;
    
    /**
     * reCAPTCHA Threshold (Score between 0.0 and 1.0)
     */
    public static final int RECAPTCHA_THRESHOLD_SCALE = 2;

    // Message Keys for reCAPTCHA
    public static final String MSG_RECAPTCHA_KEYS_REQUIRED = "validation.recaptcha.keys.required";
    public static final String MSG_RECAPTCHA_SITE_KEY_INVALID = "validation.recaptcha.siteKey.invalid";
    public static final String MSG_RECAPTCHA_SECRET_KEY_INVALID = "validation.recaptcha.secretKey.invalid";
    public static final String MSG_RECAPTCHA_THRESHOLD_MIN = "validation.recaptcha.threshold.min";
    public static final String MSG_RECAPTCHA_THRESHOLD_MAX = "validation.recaptcha.threshold.max";


    // ============================================================================
    // SITE TECHNICAL LIMITS
    // ============================================================================

    public static final int VERIFICATION_CODE_MAX_LENGTH = 100;
    public static final int ROBOTS_TXT_MAX_LENGTH = 10000;
    public static final int COOKIE_CONSENT_TEXT_MAX_LENGTH = 2000;

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
