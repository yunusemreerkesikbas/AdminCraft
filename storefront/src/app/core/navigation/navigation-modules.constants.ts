export const NAVIGATION_MODULES = {
    CORE: 'core',
    PAGEBUILDER: 'pagebuilder',
    SITE_SETTINGS: 'site_settings',
    MEDIA: 'media',
    COMPONENT_LIBRARY: 'component_library',
    PRODUCT_CATALOG: 'product'
} as const;

export type NavigationModuleCode = typeof NAVIGATION_MODULES[keyof typeof NAVIGATION_MODULES];

