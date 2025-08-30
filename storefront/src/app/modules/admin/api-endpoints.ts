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
    tenantCheckSubdomain: 'tenants/check/subdomain/${subdomain}',
    tenantActivate: 'tenants/${id}/activate',
    tenantSuspend: 'tenants/${id}/suspend',
    tenantMaintenance: 'tenants/${id}/maintenance',
    tenantStatsCount: 'tenants/stats/count',
    tenantBySubdomain: 'tenants/subdomain/${subdomain}',

    // ----- CONTENT -----
    contents: 'contents',
    contentById: 'contents/${id}',
    contentPublish: 'contents/${id}/publish',
    contentArchive: 'contents/${id}/archive',
    contentTypes: 'content-types',

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

    // ----- PAGE BUILDER: PAGES -----
    pages: 'pages',
    pageById: 'pages/${id}',
    pageBySlug: 'pages/slug/${language}/${slug}',
    pagePublish: 'pages/${id}/publish',
    pageUnpublish: 'pages/${id}/unpublish',
    pageSchedule: 'pages/${id}/schedule',

    // ----- PAGE BUILDER: CATEGORIES -----
    pageCategories: 'page-categories',
    pageCategoryById: 'page-categories/${id}',
    pageCategoryTree: 'page-categories/tree',
    pageCategoryChildren: 'page-categories/children',
    pageCategoryMove: 'page-categories/${id}/move',
    pageCategoryReorder: 'page-categories/reorder',

    // ----- PAGE BUILDER: SECTIONS & BLOCKS -----
    pageBuilderSections: 'page-builder/sections',
    pageBuilderSectionById: 'page-builder/sections/${id}',
    pageBuilderBlocks: 'page-builder/blocks',
    pageBuilderBlockById: 'page-builder/blocks/${id}'
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


