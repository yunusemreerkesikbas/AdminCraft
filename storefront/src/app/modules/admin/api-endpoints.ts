/**
 * API endpoint configuration
 * Simple structure with string templates
 */
export const SPA_ENDPOINTS_CONFIG = {
    // ----- AUTH -----
    login: 'auth/login',
    refresh: 'auth/refresh',
    logout: 'auth/logout',
    verifyOtp: 'auth/verify-otp',
    forgotPassword: 'auth/forgot-password',
    resetPassword: 'auth/reset-password',
    verifyResetToken: 'auth/verify-reset-token',
    verifyEmailToken: 'auth/verify-email-token',
    setInitialPassword: 'auth/set-initial-password',

    // ----- SITE SECURITY -----
    siteSecuritySettings: 'sites/security',

    // ----- USERS -----
    users: 'users',
    userById: 'users/${id}',
    userActivate: 'users/${id}/activate',
    userDeactivate: 'users/${id}/deactivate',
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
    mediaBulkDelete: 'media/bulk-delete',
    mediaById: 'media/${id}',
    mediaDetail: 'media/${id}/detail',
    mediaUpload: 'media/upload',
    mediaComposite: 'media/composite',
    mediaCompositeById: 'media/${id}/composite',
    mediaI18n: 'media/${mediaId}/i18n/${language}',
    mediaBind: 'media/${mediaId}/bind',
    mediaLink: 'media/${mediaId}/links',

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
    siteMaintenance: 'sites/${id}/maintenance',

    // ----- SITE SETTINGS -----
    siteSettings: 'site-settings',

    // ----- MAIL MARKETING (TENANT) -----
    mailTemplateTypes: 'mail/templates/types',
    mailTemplateTypeDetail: 'mail/templates/types/${templateType}',
    mailTemplateTypeTranslation: 'mail/templates/types/${templateType}/translations/${language}',
    mailTemplates: 'mail/templates',
    mailTemplateById: 'mail/templates/${id}',
    mailSubscribers: 'mail/subscribers',
    mailSubscribersAdmin: 'mail/subscribers/admin',
    mailSubscriberAdminById: 'mail/subscribers/admin/${id}',
    mailProviderConfig: 'mail/provider-config',
    mailCampaignSend: 'mail/campaigns/send',
    mailCampaignList: 'mail/campaigns',
    mailCampaignById: 'mail/campaigns/${id}',
    mailCampaignOutbox: 'mail/campaigns/${id}/outbox',
    mailSubscribersExport: 'mail/subscribers/admin/export',
    publicNewsletterSubscribe: 'public/newsletter/subscribe',
    publicNewsletterConfirm: 'public/newsletter/confirm',
    publicNewsletterUnsubscribe: 'public/newsletter/unsubscribe',

    // ----- SITE DASHBOARD -----
    siteOverview: 'sites/overview',
    siteAnalyticsSummary: 'sites/analytics/summary',
    siteInsightsSummary: 'sites/insights/summary',
    siteActivity: 'sites/activity',
    siteActivityTrend: 'sites/activity/trend',
    siteTechnical: 'sites/technical',
    siteRobotsTxt: 'sites/robots.txt',

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
    pageSlotComponent:
        'pages/${id}/slots/${slotName}/components/${componentId}',
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
    componentBulkDelete: 'components/bulk-delete',

    // ----- COMPONENT LIBRARY: ENTRY FIELDS -----
    componentTypeEntryFields: 'components/types/${typeId}/entry-fields',
    componentTypeEntryFieldById: 'components/types/${typeId}/entry-fields/${id}',

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

    // ----- PLATFORM -----
    platformDashboard: 'platform/dashboard',
    platformSettings: 'platform/settings',
    platformDemoRequests: 'platform/demo-requests',
    platformDemoRequestById: 'platform/demo-requests/${id}',
    platformMailTemplateTypes: 'platform/mail/templates/types',
    platformMailTemplateTypeDetail: 'platform/mail/templates/types/${templateType}',
    platformMailTemplateTypeTranslation: 'platform/mail/templates/types/${templateType}/translations/${language}',
    platformMailTemplates: 'platform/mail/templates',
    platformMailTemplateById: 'platform/mail/templates/${id}',
    platformMailSubscribers: 'platform/mail/subscribers',
    platformMailSubscribersAdmin: 'platform/mail/subscribers/admin',
    platformMailSubscriberAdminById: 'platform/mail/subscribers/admin/${id}',
    platformMailCampaignSend: 'platform/mail/campaigns/send',
    platformMailCampaignList: 'platform/mail/campaigns',
    platformMailCampaignById: 'platform/mail/campaigns/${id}',
    platformMailCampaignOutbox: 'platform/mail/campaigns/${id}/outbox',
    platformMailSubscribersExport: 'platform/mail/subscribers/admin/export',
    platformPublicNewsletterSubscribe: 'platform/public/newsletter/subscribe',
    platformPublicNewsletterConfirm: 'platform/public/newsletter/confirm',
    platformPublicNewsletterUnsubscribe: 'platform/public/newsletter/unsubscribe',
    tenantProvisioningJobs: 'tenants/${tenantId}/provisioning-jobs',

    // ----- PROVISIONING -----
    provisioningModulesCatalog: 'provisioning/modules/catalog',
    provisioningTenantProvision: 'provisioning/tenants/${tenantId}/provision',
    provisioningSyncMigrations:
        'provisioning/tenants/${tenantId}/sync-migrations',
    provisioningJob: 'provisioning/jobs/${jobId}',
    provisioningJobByUuid: 'provisioning/jobs/${jobUuid}',

    // ----- CMS DELIVERY API (PUBLIC) -----
    cmsComponent: 'cms/components/${uid}',
    cmsComponentsBatch: 'cms/components',
    cmsPage: 'cms/pages',
    cmsSite: 'cms/site',
    cmsConfig: 'cms/config',

    // ----- PAGE TEMPLATES -----
    pageTemplates: 'page-templates',
    pageTemplateBulkDelete: 'page-templates/bulk-delete',
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
    productTypeProductCount: 'products/types/${id}/product-count',

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

    // ----- PRODUCT CATALOG: GLOBAL PRODUCT FIELDS -----
    productFields: 'products/fields',
    productFieldsVisible: 'products/fields/visible',
    productFieldById: 'products/fields/${id}',

    // ----- IMPEX -----
    impexExecute: 'impex/execute',
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
