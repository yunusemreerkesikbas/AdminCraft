export const NAVIGATION_MODULES = {
    CORE: 'core',
    PRODUCT_CATALOG: 'product'
} as const;

export type NavigationModuleCode = typeof NAVIGATION_MODULES[keyof typeof NAVIGATION_MODULES];
