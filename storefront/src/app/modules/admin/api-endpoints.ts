/**
 * API endpoint configuration
 * Simple structure with string templates
 */
export const SPA_ENDPOINTS_CONFIG = {
    // ----- AUTH -----
    login: 'auth/login',

    // ----- USERS -----
    users: 'users',
    userById: 'users/${id}',
    userActivate: 'users/${id}/activate',
    userDeactivate: 'users/${id}/deactivate',
    userChangePassword: 'users/${id}/change-password',
    userResetPassword: 'users/${id}/reset-password',

    // ----- TENANTS -----
    tenants: 'tenants',
    tenantById: 'tenants/${id}',
    tenantModules: 'tenants/${tenantId}/modules',
    tenantCurrentModules: 'tenants/current/modules',
    tenantCurrentDetail: 'tenants/current/detail',
    tenantLanguages: 'tenants/${tenantId}/languages',
    tenantLanguagesProvision: 'tenants/${tenantId}/languages/provision',
    generateAdminUser: 'tenants/${tenantId}/generate-admin',

    // ----- CONTENT (removed, replaced by Page Builder) -----

    // ----- MEDIA -----
    media: 'media',
    mediaById: 'media/${id}',
    mediaDetail: 'media/${id}/detail',
    mediaByUid: '/media/uid/${uid}',
    mediaUpload: '/media/upload',
    mediaComposite: '/media/composite',
    mediaCompositeById: '/media/${id}/composite',
    mediaI18n: '/media/${mediaId}/i18n/${language}',

    // ----- MEDIA CONTAINERS -----
    mediaContainers: 'media/containers',
    mediaContainerById: 'media/containers/${id}',
    mediaContainerGenerate: 'media/containers/${id}/generate',
    mediaGenerateFormat: 'media/${id}/generate-format',
    mediaGenerateFormats: 'media/${id}/generate-formats',
    mediaVariantDelete: 'media/${mediaId}/variants/${variantId}',
    mediaFocalPoint: 'media/${id}/focal-point',
    
    // ----- RESPONSIVE MEDIA -----
    responsiveMedia: 'responsive-media',
    responsiveMediaById: 'responsive-media/${id}',
    responsiveMediaByCode: 'responsive-media/code/${code}',
    componentResponsiveMedia: 'components/${id}/responsive-media',
    linkedComponents: 'responsive-media/media/${mediaId}/linked-components',

    // ----- MEDIA FORMATS -----
    mediaFormats: 'media/formats',
    mediaFormatById: 'media/formats/${id}',

    // ----- CMS MEDIA DELIVERY -----
    cmsMedia: 'cms/media/${uid}',
    cmsMediaBatch: 'cms/media',

    // ----- SITES -----
    sites: 'sites',
    siteById: 'sites/${id}',
    sitePublish: 'sites/${id}/publish',
    siteActivate: 'sites/${id}/activate',
    siteDeactivate: 'sites/${id}/deactivate',
    siteMenus: 'sites/${siteId}/menus',

    // ----- SITE SETTINGS -----
    siteSettings: 'site-settings',

    // ----- PAGE BUILDER: PAGES -----
    pages: 'pages',
    pageById: 'pages/${id}',
    pageWithI18n: 'pages/${id}/with-i18n',
    pageSetHome: 'pages/${id}/set-home',
    pageBySlug: 'pages/slug/${language}/${slug}',
    pagePublish: 'pages/${id}/publish',
    pageUnpublish: 'pages/${id}/unpublish',
    pageSchedule: 'pages/${id}/schedule',

    // ----- PAGE BUILDER: PAGE I18N -----
    pageI18n: 'pages/${pageId}/i18n/${language}',
    pageI18nPublish: 'pages/${pageId}/publish/${language}',

    // ----- PAGE BUILDER: PAGE COMPOSITE -----
    pagesComposite: 'pages/composite',
    pageComposite: 'pages/${id}/composite',

    // ----- PAGE BUILDER: SLOTS -----
    pageSlots: 'pages/${id}/slots',
    pageSlot: 'pages/${id}/slots/${slotName}',
    pageSlotComponents: 'pages/${id}/slots/${slotName}/components',
    pageSlotComponent: 'pages/${id}/slots/${slotName}/components/${componentId}',
    pageSlotComponentsReorder: 'pages/${id}/slots/${slotName}/reorder',
    sharedSlots: 'pages/shared/slots',

    // ----- COMPONENT LIBRARY: TYPES -----
    componentTypes: 'components/types',
    componentTypeById: 'components/types/${id}',
    componentTypeValidateSchema: 'components/types/validate-schema',

    // ----- COMPONENT LIBRARY: COMPONENTS -----
    components: 'components',
    componentById: 'components/${id}',
    componentWithI18n: 'components/${id}?include=translations',
    componentI18n: 'components/${componentId}/i18n/${language}',
    componentI18nPublish: 'components/${componentId}/publish/${language}',
    componentComposite: 'components/composite',
    componentCompositeById: 'components/${id}/composite',

    // ----- COMPONENT LIBRARY: ENTRY FIELDS -----
    componentTypeEntryFields: 'components/types/${typeId}/entry-fields',
    componentTypeEntryFieldById: 'components/types/${typeId}/entry-fields/${id}',
    componentTypeEntryFieldsImport: 'components/types/${typeId}/entry-fields/import',
    componentTypeEntryFieldsExport: 'components/types/${typeId}/entry-fields/export',
    componentTypeEntryFieldValidate: 'components/types/${typeId}/entry-fields/validate',

    // ----- COMPONENT LIBRARY: ENTRIES -----
    componentEntriesList: 'components/${componentId}/entries',
    componentEntriesByComponent: 'components/${componentId}/entries',
    componentEntriesCreate: 'components/${componentId}/entries',
    componentEntryById: 'components/entries/${entryId}',
    componentEntryUpdate: 'components/entries/${entryId}',
    componentEntryDelete: 'components/entries/${entryId}',
    componentEntryI18n: 'components/entries/${entryId}/i18n/${language}',
    componentEntryI18nUpsert: 'components/entries/${entryId}/i18n/${language}',
    componentEntryPublish: 'components/entries/${entryId}/publish/${language}',
    componentEntryComposite: 'components/entries/composite',
    componentEntryCompositeById: 'components/entries/${id}/composite',

    // ----- PROVISIONING -----
    provisioningModulesCatalog: 'provisioning/modules/catalog',
    provisioningTenantProvision: 'provisioning/tenants/${tenantId}/provision',
    provisioningSyncMigrations: 'provisioning/tenants/${tenantId}/sync-migrations',
    provisioningJob: 'provisioning/jobs/${jobId}',
    provisioningJobByUuid: 'provisioning/jobs/${jobUuid}',

    // ----- CMS DELIVERY API (PUBLIC) -----
    cmsComponent: 'cms/components/${uid}',
    cmsComponentsBatch: 'cms/components',
    cmsPage: 'cms/pages/${uid}',
    cmsPagesBatch: 'cms/pages',

    // ----- PAGE TEMPLATES -----
    pageTemplates: 'page-templates',
    pageTemplatesActive: 'page-templates/active',
    pageTemplateById: 'page-templates/${id}',
    pageTemplateSlots: 'page-templates/${id}/slots',
    pageTemplateSlot: 'page-templates/${id}/slots/${slotName}',
    pageTemplateSlotsReorder: 'page-templates/${id}/slots/reorder',
    pageTemplateAssign: 'page-templates/${id}/assign/${pageId}',

    // ----- NAVIGATION -----
    navigationNodes: 'navigation/nodes',
    navigationNodeById: 'navigation/nodes/${id}',
    navigationNodeTree: 'navigation/nodes/${id}',
    navigationNodeChildren: 'navigation/nodes/${id}/children',
    navigationNodesReorder: 'navigation/nodes/${id}/reorder',
    navigationNodeI18n: 'navigation/nodes/${id}/i18n/${language}',
    navigationNodeComposite: 'navigation/nodes/composite',
    navigationNodeCompositeById: 'navigation/nodes/${id}/composite',
    navigationEntries: 'navigation/entries',
    navigationEntryById: 'navigation/entries/${id}',
    navigationEntryReorder: 'navigation/nodes/${nodeId}/entries/reorder',
    navigationEntryI18n: 'navigation/entries/${id}/i18n/${language}',
    navigationEntryComposite: 'navigation/entries/composite',
    navigationEntryCompositeById: 'navigation/entries/${id}/composite',

    // ----- PAGE TEMPLATE I18N -----
    pageTemplateI18n: 'page-templates/${id}/i18n/${language}',

    // ----- PRODUCT CATALOG: PRODUCT TYPES -----
    productTypes: 'products/types',
    productTypeById: 'products/types/${id}',

    // ----- PRODUCT CATALOG: ATTRIBUTE DEFINITIONS -----
    productAttributeDefinitions: 'products/types/${typeId}/attributes',
    productAttributeDefinitionById: 'products/types/${typeId}/attributes/${id}',

    // ----- PRODUCT CATALOG: CATEGORIES -----
    productCategories: 'products/categories',
    productCategoryComposite: 'products/categories/composite',
    productCategoryById: 'products/categories/${id}',
    productCategoryCompositeById: 'products/categories/${id}/composite',

    // ----- PRODUCT CATALOG: PRODUCTS -----
    products: 'products',
    productComposite: 'products/composite',
    productById: 'products/${id}',
    productCompositeById: 'products/${id}/composite',
    productMedia: 'products/${id}/media',
    productMediaById: 'products/${id}/media/${mediaId}',

    // ----- PRODUCT CATALOG: CMS DELIVERY (PUBLIC) -----
    cmsProducts: 'cms/products',
    cmsProductByUid: 'cms/products/${uid}',
    cmsCategories: 'cms/categories',
    cmsCategoryByUid: 'cms/categories/${uid}',
    cmsCategoryProducts: 'cms/categories/${uid}/products',
} as const;

export type EndpointKey = keyof typeof SPA_ENDPOINTS_CONFIG;

/**
 * Simple endpoint resolver with parameter substitution
 * @param endpointKey - Key from SPA_ENDPOINTS_CONFIG
 * @param params - Parameters to substitute in the template
 * @returns Resolved endpoint URL
 */
export function resolveEndpoint(
    endpointKey: EndpointKey,
    params: Record<string, string | number> = {}
): string {
    const template = SPA_ENDPOINTS_CONFIG[endpointKey];
    if (!template) {
        throw new Error(`Unknown endpoint key: ${endpointKey}`);
    }

    // Replace parameters in template
    return template.replace(/\$\{(\w+)\}/g, (match, key) => {
        const value = params[key];
        if (value === undefined || value === null) {
            throw new Error(`Parameter '${key}' is required but not provided`);
        }
        return encodeURIComponent(String(value));
    });
}


