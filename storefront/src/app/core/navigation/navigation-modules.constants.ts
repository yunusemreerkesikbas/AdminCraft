export const NAVIGATION_MODULES = {
    CORE: 'core',
    PRODUCT_CATALOG: 'product',
    MAIL_MARKETING: 'mail_marketing'
} as const;

export type NavigationModuleCode = typeof NAVIGATION_MODULES[keyof typeof NAVIGATION_MODULES];
