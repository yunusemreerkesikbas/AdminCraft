/* eslint-disable */
import { FuseNavigationItem } from '@fuse/components/navigation';
import { NAVIGATION_MODULES } from 'app/core/navigation/navigation-modules.constants';

export const defaultNavigation: FuseNavigationItem[] = [

    {
        id: 'apps',
        title: 'Yönetim',
        subtitle: 'Custom made application designs',
        type: 'group',
        icon: 'heroicons_outline:home',
        children: [

            {
                id: 'apps.custom.tenants',
                title: 'Tenant Yönetimi',
                type: 'basic',
                icon: 'heroicons_outline:users',
                link: 'tenants',
                requiredModule: null, // Platform module - always visible
                requiredRole: 'SUPER_ADMIN', // Only SUPER_ADMIN can access tenant management
            },
            {
                id: 'apps.custom.media',
                title: 'Medya Yönetimi',
                type: 'basic',
                icon: 'heroicons_outline:photo',
                link: 'media',
                requiredModule: NAVIGATION_MODULES.MEDIA,
                excludedRoles: ['SUPER_ADMIN'], // Hide from SUPER_ADMIN
            },
            {
                id: 'apps.custom.users',
                title: 'Kullanıcı Yönetimi',
                type: 'basic',
                icon: 'heroicons_outline:user-group',
                link: 'users',
                requiredModule: NAVIGATION_MODULES.CORE,
                excludedRoles: ['SUPER_ADMIN'], // Hide from SUPER_ADMIN
            },

            {
                id: 'apps.custom.sites',
                title: 'Site Yönetimi',
                type: 'basic',
                icon: 'heroicons_outline:globe-alt',
                link: 'sites',
                requiredModule: NAVIGATION_MODULES.CORE,
                excludedRoles: ['SUPER_ADMIN'], // Hide from SUPER_ADMIN
            },
            {
                id: 'apps.custom.pagebuilder',
                title: 'Page Builder',
                type: 'basic',
                icon: 'heroicons_outline:rectangle-stack',
                link: 'pages',
                requiredModule: NAVIGATION_MODULES.PAGEBUILDER,
                excludedRoles: ['SUPER_ADMIN'], // Hide from SUPER_ADMIN
            },
            {
                id: 'apps.custom.pagebuilder.categories',
                title: 'Page Categories',
                type: 'basic',
                icon: 'heroicons_outline:folder',
                link: 'pages/categories',
                requiredModule: NAVIGATION_MODULES.PAGE_CATEGORIES,
                excludedRoles: ['SUPER_ADMIN'], // Hide from SUPER_ADMIN
            },
            {
                id: 'apps.custom.settings',
                title: 'Site Ayarları',
                type: 'basic',
                icon: 'heroicons_outline:cog-6-tooth',
                link: 'settings',
                requiredModule: NAVIGATION_MODULES.SITE_SETTINGS,
                excludedRoles: ['SUPER_ADMIN'], // Hide from SUPER_ADMIN
            },
        ],
    },
];
