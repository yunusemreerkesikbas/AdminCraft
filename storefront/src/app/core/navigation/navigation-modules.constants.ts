export const NAVIGATION_MODULES = {
    CORE: 'core',
    PRODUCT_CATALOG: 'product',
    COMMERCE: 'commerce',
    MAIL_MARKETING: 'mail_marketing'
} as const;

export type NavigationModuleCode = typeof NAVIGATION_MODULES[keyof typeof NAVIGATION_MODULES];
