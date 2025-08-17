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
    siteMenus: 'sites/${siteId}/menus'
} as const;

export function resolveEndpoint(
    template: string,
    params: Record<string, string | number>
): string {
    return template.replace(/\$\{(\w+)\}/g, (_match, key) =>
        encodeURIComponent(String(params[key] ?? ''))
    );
}


