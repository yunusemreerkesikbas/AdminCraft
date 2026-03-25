import { Route } from '@angular/router';
import { initialDataResolver } from 'app/app.resolvers';
import { AuthGuard } from 'app/core/auth/guards/auth.guard';
import { moduleGuard } from 'app/core/auth/guards/module.guard';
import { NoAuthGuard } from 'app/core/auth/guards/noAuth.guard';
import {
    superAdminGuard,
    tenantAdminGuard,
    tenantUserGuard,
    tenantAdminOrSuperAdminGuard,
} from 'app/core/auth/guards/role.guard';
import { rootRedirectGuard } from 'app/core/auth/guards/root-redirect.guard';
import { LanguageGuard } from 'app/core/language/language.guard';
import { LayoutComponent } from 'app/layout/layout.component';

export const appRoutes: Route[] = [
    { 
        path: '', 
        pathMatch: 'full', 
        canActivate: [rootRedirectGuard],
        children: []
    },

    {
        path: 'signed-in-redirect',
        pathMatch: 'full',
        redirectTo: 'pages',
    },
    {
        path: 'config',
        component: LayoutComponent,
        data: {
            layout: 'empty',
        },
        children: [
            {
                path: '',
                loadComponent: () =>
                    import(
                        'app/modules/config/console/config-console.component'
                    ).then((m) => m.ConfigConsoleComponent),
            },
        ],
    },

    {
        path: '',
        canActivate: [NoAuthGuard],
        canActivateChild: [NoAuthGuard],
        component: LayoutComponent,
        data: {
            layout: 'empty',
        },
        children: [
            {
                path: 'confirmation-required',
                loadChildren: () =>
                    import(
                        'app/modules/auth/confirmation-required/confirmation-required.routes'
                    ),
            },
            {
                path: 'forgot-password',
                loadChildren: () =>
                    import(
                        'app/modules/auth/forgot-password/forgot-password.routes'
                    ),
            },
            {
                path: 'reset-password',
                loadChildren: () =>
                    import(
                        'app/modules/auth/reset-password/reset-password.routes'
                    ),
            },
            {
                path: 'set-password',
                loadChildren: () =>
                    import(
                        'app/modules/auth/set-password/set-password.routes'
                    ),
            },
            {
                path: 'sign-in',
                loadChildren: () =>
                    import('app/modules/auth/sign-in/sign-in.routes'),
            },
            {
                path: 'sign-up',
                loadChildren: () =>
                    import('app/modules/auth/sign-up/sign-up.routes'),
            },
        ],
    },
    {
        path: ':lang',
        canActivate: [LanguageGuard],
        component: LayoutComponent,
        data: {
            layout: 'empty',
        },
        children: [
            {
                path: 'pages/error',
                children: [
                    {
                        path: '403',
                        loadChildren: () =>
                            import(
                                'app/modules/admin/pages/error/error-403/error-403.routes'
                            ),
                    },
                    {
                        path: '404',
                        loadChildren: () =>
                            import(
                                'app/modules/admin/pages/error/error-404/error-404.routes'
                            ),
                    },
                    {
                        path: '500',
                        loadChildren: () =>
                            import(
                                'app/modules/admin/pages/error/error-500/error-500.routes'
                            ),
                    },
                ],
            },
        ],
    },
    {
        path: '',
        canActivate: [AuthGuard],
        canActivateChild: [AuthGuard],
        component: LayoutComponent,
        data: {
            layout: 'empty',
        },
        children: [
            {
                path: 'sign-out',
                loadChildren: () =>
                    import('app/modules/auth/sign-out/sign-out.routes'),
            },
            {
                path: 'unlock-session',
                loadChildren: () =>
                    import(
                        'app/modules/auth/unlock-session/unlock-session.routes'
                    ),
            },
        ],
    },
    {
        path: '',
        component: LayoutComponent,
        data: {
            layout: 'empty',
        },
        children: [
            {
                path: 'home',
                loadChildren: () =>
                    import('app/modules/landing/home/home.routes'),
            },
        ],
    },
    {
        path: ':lang',
        canActivate: [LanguageGuard, AuthGuard],
        canActivateChild: [AuthGuard],
        component: LayoutComponent,
        resolve: {
            initialData: initialDataResolver,
        },
        children: [
            { path: '', redirectTo: 'pages', pathMatch: 'full' },
            {
                path: 'dashboards',
                children: [
                    {
                        path: 'project',
                        loadChildren: () =>
                            import(
                                'app/modules/admin/dashboards/project/project.routes'
                            ),
                    },
                    {
                        path: 'analytics',
                        loadChildren: () =>
                            import(
                                'app/modules/admin/dashboards/analytics/analytics.routes'
                            ),
                    },
                    {
                        path: 'finance',
                        loadChildren: () =>
                            import(
                                'app/modules/admin/dashboards/finance/finance.routes'
                            ),
                    },
                    {
                        path: 'crypto',
                        loadChildren: () =>
                            import(
                                'app/modules/admin/dashboards/crypto/crypto.routes'
                            ),
                    },
                ],
            },
            { path: 'apps', children: [{ path: '**', redirectTo: 'pages' }] },
            {
                path: 'platform-dashboard',
                canActivate: [superAdminGuard],
                loadChildren: () =>
                    import(
                        'app/modules/admin/custom/platform-dashboard/platform-dashboard.routes'
                    ),
            },
            {
                path: 'tenants',
                canActivate: [superAdminGuard],
                children: [
                    {
                        path: '',
                        loadComponent: () =>
                            import(
                                'app/modules/admin/custom/tenants/list/tenants-list.component'
                            ).then((m) => m.TenantsListComponent),
                    },
                    {
                        path: ':id',
                        loadComponent: () =>
                            import(
                                'app/modules/admin/custom/tenants/detail/tenant-detail.component'
                            ).then((m) => m.SpaTenantDetailComponent),
                    },
                ],
            },
            {
                path: 'platform-settings',
                canActivate: [superAdminGuard],
                loadChildren: () =>
                    import(
                        'app/modules/admin/custom/platform-settings/platform-settings.routes'
                    ),
            },
            {
                path: 'demo-requests',
                canActivate: [superAdminGuard],
                loadComponent: () =>
                    import(
                        'app/modules/admin/custom/demo-requests/demo-requests-list.component'
                    ).then((m) => m.DemoRequestsListComponent),
            },
            {
                path: 'platform-mail',
                canActivate: [superAdminGuard],
                children: [
                    {
                        path: '',
                        loadComponent: () =>
                            import(
                                'app/modules/admin/custom/mail-marketing/platform-mail-template-list.component'
                            ).then((m) => m.SpaPlatformMailTemplateListComponent),
                    },
                    {
                        path: 'subscribers',
                        data: { scope: 'platform' },
                        loadComponent: () =>
                            import(
                                'app/modules/admin/custom/mail-marketing/subscribers/mail-subscriber-list.component'
                            ).then((m) => m.MailSubscriberListComponent),
                    },
                    {
                        path: ':templateType',
                        loadComponent: () =>
                            import(
                                'app/modules/admin/custom/mail-marketing/platform-mail-marketing.component'
                            ).then((m) => m.SpaPlatformMailMarketingComponent),
                    },
                ],
            },
            {
                path: 'media',
                canActivate: [tenantUserGuard, moduleGuard],
                data: { requiredModule: 'core' },
                loadChildren: () =>
                    import('app/modules/admin/custom/media/media.routes'),
            },
            {
                path: 'users',
                canActivate: [tenantUserGuard, moduleGuard],
                data: { requiredModule: 'core' },
                loadChildren: () =>
                    import('app/modules/admin/custom/users/users.routes'),
            },
            {
                path: 'sites',
                canActivate: [tenantUserGuard, moduleGuard],
                data: { requiredModule: 'core' },
                loadChildren: () =>
                    import('app/modules/admin/custom/sites/sites.routes'),
            },
            {
                path: 'pages',
                canActivate: [tenantUserGuard, moduleGuard],
                data: { requiredModule: 'core' },
                loadChildren: () =>
                    import(
                        'app/modules/admin/custom/pages/page-builder.routes'
                    ),
            },
            {
                path: 'settings',
                canActivate: [tenantUserGuard, moduleGuard],
                data: { requiredModule: 'core' },
                loadChildren: () =>
                    import(
                        'app/modules/admin/custom/settings/site-settings.routes'
                    ),
            },
            {
                path: 'site',
                canActivate: [tenantUserGuard, moduleGuard],
                data: { requiredModule: 'core' },
                loadComponent: () =>
                    import(
                        'app/modules/admin/custom/site/site-dashboard.component'
                    ).then((m) => m.SpaSiteDashboardComponent),
            },
            {
                path: 'components',
                canActivate: [tenantUserGuard, moduleGuard],
                data: { requiredModule: 'core' },
                loadChildren: () =>
                    import(
                        'app/modules/admin/custom/components/component-library.routes'
                    ),
            },
            {
                path: 'products',
                canActivate: [tenantUserGuard, moduleGuard],
                data: { requiredModule: 'product' },
                loadChildren: () =>
                    import('app/modules/admin/custom/products/products.routes'),
            },
            {
                path: 'page-templates',
                canActivate: [tenantUserGuard, moduleGuard],
                data: { requiredModule: 'core' },
                loadComponent: () =>
                    import(
                        'app/modules/admin/custom/templates/list/page-template-list.component'
                    ).then((m) => m.PageTemplateListComponent),
            },
            {
                path: 'navigation',
                canActivate: [tenantUserGuard, moduleGuard],
                data: { requiredModule: 'core' },
                loadComponent: () =>
                    import(
                        'app/modules/admin/custom/navigation/list/navigation-list.component'
                    ).then((m) => m.SpaNavigationListComponent),
            },
            {
                path: 'impex',
                canActivate: [tenantAdminOrSuperAdminGuard],
                loadComponent: () =>
                    import('app/modules/admin/custom/impex/impex.component').then(
                        (m) => m.SpaImpExComponent
                    ),
            },
            {
                path: 'mail-marketing',
                canActivate: [tenantAdminGuard, moduleGuard],
                data: { requiredModule: 'mail_marketing' },
                children: [
                    {
                        path: '',
                        loadComponent: () =>
                            import(
                                'app/modules/admin/custom/mail-marketing/tenant-mail-template-list.component'
                            ).then((m) => m.SpaTenantMailTemplateListComponent),
                    },
                    {
                        path: 'subscribers',
                        data: { scope: 'tenant' },
                        loadComponent: () =>
                            import(
                                'app/modules/admin/custom/mail-marketing/subscribers/mail-subscriber-list.component'
                            ).then((m) => m.MailSubscriberListComponent),
                    },
                    {
                        path: ':templateType',
                        loadComponent: () =>
                            import(
                                'app/modules/admin/custom/mail-marketing/tenant-mail-marketing.component'
                            ).then((m) => m.SpaTenantMailMarketingComponent),
                    },
                ],
            },

            // Pages
            {
                path: 'pages',
                children: [
                    {
                        path: 'activities',
                        loadChildren: () =>
                            import(
                                'app/modules/admin/pages/activities/activities.routes'
                            ),
                    },

                    {
                        path: 'authentication',
                        loadChildren: () =>
                            import(
                                'app/modules/admin/pages/authentication/authentication.routes'
                            ),
                    },

                    {
                        path: 'coming-soon',
                        loadChildren: () =>
                            import(
                                'app/modules/admin/pages/coming-soon/coming-soon.routes'
                            ),
                    },

                    {
                        path: 'invoice',
                        children: [
                            {
                                path: 'printable',
                                children: [
                                    {
                                        path: 'compact',
                                        loadChildren: () =>
                                            import(
                                                'app/modules/admin/pages/invoice/printable/compact/compact.routes'
                                            ),
                                    },
                                    {
                                        path: 'modern',
                                        loadChildren: () =>
                                            import(
                                                'app/modules/admin/pages/invoice/printable/modern/modern.routes'
                                            ),
                                    },
                                ],
                            },
                        ],
                    },

                    {
                        path: 'maintenance',
                        loadChildren: () =>
                            import(
                                'app/modules/admin/pages/maintenance/maintenance.routes'
                            ),
                    },

                    {
                        path: 'pricing',
                        children: [
                            {
                                path: 'modern',
                                loadChildren: () =>
                                    import(
                                        'app/modules/admin/pages/pricing/modern/modern.routes'
                                    ),
                            },
                            {
                                path: 'simple',
                                loadChildren: () =>
                                    import(
                                        'app/modules/admin/pages/pricing/simple/simple.routes'
                                    ),
                            },
                            {
                                path: 'single',
                                loadChildren: () =>
                                    import(
                                        'app/modules/admin/pages/pricing/single/single.routes'
                                    ),
                            },
                            {
                                path: 'table',
                                loadChildren: () =>
                                    import(
                                        'app/modules/admin/pages/pricing/table/table.routes'
                                    ),
                            },
                        ],
                    },

                    {
                        path: 'profile',
                        loadChildren: () =>
                            import(
                                'app/modules/admin/pages/profile/profile.routes'
                            ),
                    },

                    {
                        path: 'settings',
                        loadChildren: () =>
                            import(
                                'app/modules/admin/pages/settings/settings.routes'
                            ),
                    },
                ],
            },

            // User Interface
            {
                path: 'ui',
                children: [
                    {
                        path: 'material-components',
                        loadChildren: () =>
                            import(
                                'app/modules/admin/ui/material-components/material-components.routes'
                            ),
                    },

                    {
                        path: 'fuse-components',
                        loadChildren: () =>
                            import(
                                'app/modules/admin/ui/fuse-components/fuse-components.routes'
                            ),
                    },

                    {
                        path: 'other-components',
                        loadChildren: () =>
                            import(
                                'app/modules/admin/ui/other-components/other-components.routes'
                            ),
                    },

                    {
                        path: 'tailwindcss',
                        loadChildren: () =>
                            import(
                                'app/modules/admin/ui/tailwindcss/tailwindcss.routes'
                            ),
                    },

                    {
                        path: 'advanced-search',
                        loadChildren: () =>
                            import(
                                'app/modules/admin/ui/advanced-search/advanced-search.routes'
                            ),
                    },

                    {
                        path: 'animations',
                        loadChildren: () =>
                            import(
                                'app/modules/admin/ui/animations/animations.routes'
                            ),
                    },

                    {
                        path: 'cards',
                        loadChildren: () =>
                            import('app/modules/admin/ui/cards/cards.routes'),
                    },

                    {
                        path: 'colors',
                        loadChildren: () =>
                            import('app/modules/admin/ui/colors/colors.routes'),
                    },

                    {
                        path: 'confirmation-dialog',
                        loadChildren: () =>
                            import(
                                'app/modules/admin/ui/confirmation-dialog/confirmation-dialog.routes'
                            ),
                    },

                    {
                        path: 'datatable',
                        loadChildren: () =>
                            import(
                                'app/modules/admin/ui/datatable/datatable.routes'
                            ),
                    },

                    {
                        path: 'forms',
                        loadChildren: () =>
                            import('app/modules/admin/ui/forms/forms.routes'),
                    },

                    {
                        path: 'icons',
                        loadChildren: () =>
                            import('app/modules/admin/ui/icons/icons.routes'),
                    },

                    {
                        path: 'page-layouts',
                        loadChildren: () =>
                            import(
                                'app/modules/admin/ui/page-layouts/page-layouts.routes'
                            ),
                    },

                    {
                        path: 'typography',
                        loadChildren: () =>
                            import(
                                'app/modules/admin/ui/typography/typography.routes'
                            ),
                    },
                ],
            },

            {
                path: '404-not-found',
                pathMatch: 'full',
                data: {
                    layout: 'empty',
                },
                loadChildren: () =>
                    import(
                        'app/modules/admin/pages/error/error-404/error-404.routes'
                    ),
            },
            {
                path: '**',
                data: {
                    layout: 'empty',
                },
                loadChildren: () =>
                    import(
                        'app/modules/admin/pages/error/error-404/error-404.routes'
                    ),
            },
        ],
    },
];
