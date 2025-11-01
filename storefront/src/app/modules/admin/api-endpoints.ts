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
    tenantCurrentModules: 'tenants/current/modules', // Sprint 21: TENANT_ADMIN's own modules
    tenantLanguages: 'tenants/${tenantId}/languages',
    tenantLanguagesProvision: 'tenants/${tenantId}/languages/provision',
    generateAdminUser: 'tenants/${tenantId}/generate-admin',

    // ----- CONTENT (removed, replaced by Page Builder) -----

    // ----- MEDIA -----
    media: 'media',
    mediaById: 'media/${id}',
    mediaUpload: 'media/upload',

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

    // ----- PAGE BUILDER: CATEGORIES -----
    pageCategories: 'page-categories',
    pageCategoryById: 'page-categories/${id}',
    pageCategoryWithTranslations: 'page-categories/${id}?include=translations',
    pageCategoryI18n: 'page-categories/${categoryId}/i18n/${language}',

    // ----- PROVISIONING -----
    provisioningModulesCatalog: 'provisioning/modules/catalog',
    provisioningTenantProvision: 'provisioning/tenants/${tenantId}/provision',
    provisioningJob: 'provisioning/jobs/${jobId}',
    provisioningJobByUuid: 'provisioning/jobs/${jobUuid}'
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


