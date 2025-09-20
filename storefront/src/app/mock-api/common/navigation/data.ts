/* eslint-disable */
import { FuseNavigationItem } from '@fuse/components/navigation';

export const defaultNavigation: FuseNavigationItem[] = [
    {
        id: 'dashboards',
        title: 'Dashboards',
        subtitle: 'Unique dashboard designs',
        type: 'group',
        icon: 'heroicons_outline:home',
        children: [
            {
                id: 'dashboards.project',
                title: 'Project',
                type: 'basic',
                icon: 'heroicons_outline:clipboard-document-check',
                link: '/__TENANT__/project',
            },
            {
                id: 'dashboards.analytics',
                title: 'Analytics',
                type: 'basic',
                icon: 'heroicons_outline:chart-pie',
                link: '/__TENANT__/analytics',
            },
            {
                id: 'dashboards.finance',
                title: 'Finance',
                type: 'basic',
                icon: 'heroicons_outline:banknotes',
                link: '/__TENANT__/finance',
            },
        ],
    },
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
                link: '/__TENANT__/tenants',
            },
            // Content management removed; use Page Builder instead
            {
                id: 'apps.custom.media',
                title: 'Medya Yönetimi',
                type: 'basic',
                icon: 'heroicons_outline:photo',
                link: '/__TENANT__/media',
            },
            {
                id: 'apps.custom.users',
                title: 'Kullanıcı Yönetimi',
                type: 'basic',
                icon: 'heroicons_outline:user-group',
                link: '/__TENANT__/users',
            },
            
            {
                id: 'apps.custom.sites',
                title: 'Site Yönetimi',
                type: 'basic',
                icon: 'heroicons_outline:globe-alt',
                link: '/__TENANT__/sites',
            },
            {
                id: 'apps.custom.pagebuilder',
                title: 'Page Builder',
                type: 'basic',
                icon: 'heroicons_outline:rectangle-stack',
                link: '/__TENANT__/pages',
            },
            {
                id: 'apps.custom.pagebuilder.categories',
                title: 'Page Categories',
                type: 'basic',
                icon: 'heroicons_outline:folder',
                link: '/__TENANT__/pages/categories',
            },
            {
                id: 'apps.custom.components',
                title: 'UI Component Yönetimi',
                type: 'collapsable',
                icon: 'heroicons_outline:squares-2x2',
                children: [
                    {
                        id: 'apps.custom.components.navbar',
                        title: 'Navbar',
                        type: 'basic',
                        icon: 'heroicons_outline:bars-3',
                        link: '/__TENANT__/components/navbar',
                    },
                    {
                        id: 'apps.custom.components.logo',
                        title: 'Logo',
                        type: 'basic',
                        icon: 'heroicons_outline:photo',
                        link: '/__TENANT__/components/logo',
                    },
                    {
                        id: 'apps.custom.components.cta',
                        title: 'CTA',
                        type: 'basic',
                        icon: 'heroicons_outline:bolt',
                        link: '/__TENANT__/components/cta',
                    },
                    {
                        id: 'apps.custom.components.brands',
                        title: 'Brands',
                        type: 'basic',
                        icon: 'heroicons_outline:building-storefront',
                        link: '/__TENANT__/components/brands',
                    },
                    {
                        id: 'apps.custom.components.faq',
                        title: 'FAQ',
                        type: 'basic',
                        icon: 'heroicons_outline:question-mark-circle',
                        link: '/__TENANT__/components/faq',
                    },
                    {
                        id: 'apps.custom.components.breadcrumb',
                        title: 'Breadcrumb',
                        type: 'basic',
                        icon: 'heroicons_outline:ellipsis-horizontal',
                        link: '/__TENANT__/components/breadcrumb',
                    },
                ],
            },
            {
                id: 'apps.custom.settings',
                title: 'Site Ayarları',
                type: 'basic',
                icon: 'heroicons_outline:cog-6-tooth',
                link: '/__TENANT__/settings',
            },
        ],
    },
];
export const compactNavigation: FuseNavigationItem[] = [
    {
        id: 'dashboards',
        title: 'Dashboards',
        tooltip: 'Dashboards',
        type: 'aside',
        icon: 'heroicons_outline:home',
        children: [], // This will be filled from defaultNavigation so we don't have to manage multiple sets of the same navigation
    },
    {
        id: 'apps',
        title: 'Apps',
        tooltip: 'Apps',
        type: 'aside',
        icon: 'heroicons_outline:squares-2x2',
        children: [], // This will be filled from defaultNavigation so we don't have to manage multiple sets of the same navigation
    },
    {
        id: 'pages',
        title: 'Pages',
        tooltip: 'Pages',
        type: 'aside',
        icon: 'heroicons_outline:document-duplicate',
        children: [], // This will be filled from defaultNavigation so we don't have to manage multiple sets of the same navigation
    },
    {
        id: 'user-interface',
        title: 'UI',
        tooltip: 'UI',
        type: 'aside',
        icon: 'heroicons_outline:rectangle-stack',
        children: [], // This will be filled from defaultNavigation so we don't have to manage multiple sets of the same navigation
    },
    {
        id: 'navigation-features',
        title: 'Navigation',
        tooltip: 'Navigation',
        type: 'aside',
        icon: 'heroicons_outline:bars-3',
        children: [], // This will be filled from defaultNavigation so we don't have to manage multiple sets of the same navigation
    },
];
export const futuristicNavigation: FuseNavigationItem[] = [
    {
        id: 'dashboards',
        title: 'DASHBOARDS',
        type: 'group',
        children: [], // This will be filled from defaultNavigation so we don't have to manage multiple sets of the same navigation
    },
    {
        id: 'apps',
        title: 'APPS',
        type: 'group',
        children: [], // This will be filled from defaultNavigation so we don't have to manage multiple sets of the same navigation
    },
    {
        id: 'others',
        title: 'OTHERS',
        type: 'group',
    },
    {
        id: 'pages',
        title: 'Pages',
        type: 'aside',
        icon: 'heroicons_outline:document-duplicate',
        children: [], // This will be filled from defaultNavigation so we don't have to manage multiple sets of the same navigation
    },
    {
        id: 'user-interface',
        title: 'User Interface',
        type: 'aside',
        icon: 'heroicons_outline:rectangle-stack',
        children: [], // This will be filled from defaultNavigation so we don't have to manage multiple sets of the same navigation
    },
    {
        id: 'navigation-features',
        title: 'Navigation Features',
        type: 'aside',
        icon: 'heroicons_outline:bars-3',
        children: [], // This will be filled from defaultNavigation so we don't have to manage multiple sets of the same navigation
    },
];
export const horizontalNavigation: FuseNavigationItem[] = [
    {
        id: 'dashboards',
        title: 'Dashboards',
        type: 'group',
        icon: 'heroicons_outline:home',
        children: [], // This will be filled from defaultNavigation so we don't have to manage multiple sets of the same navigation
    },
    {
        id: 'apps',
        title: 'Apps',
        type: 'group',
        icon: 'heroicons_outline:squares-2x2',
        children: [], // This will be filled from defaultNavigation so we don't have to manage multiple sets of the same navigation
    },
    {
        id: 'pages',
        title: 'Pages',
        type: 'group',
        icon: 'heroicons_outline:document-duplicate',
        children: [], // This will be filled from defaultNavigation so we don't have to manage multiple sets of the same navigation
    },
    {
        id: 'user-interface',
        title: 'UI',
        type: 'group',
        icon: 'heroicons_outline:rectangle-stack',
        children: [], // This will be filled from defaultNavigation so we don't have to manage multiple sets of the same navigation
    },
    {
        id: 'navigation-features',
        title: 'Misc',
        type: 'group',
        icon: 'heroicons_outline:bars-3',
        children: [], // This will be filled from defaultNavigation so we don't have to manage multiple sets of the same navigation
    },
];
