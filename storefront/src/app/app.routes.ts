import { Route } from '@angular/router';
import { initialDataResolver } from 'app/app.resolvers';
import { AuthGuard } from 'app/core/auth/guards/auth.guard';
import { moduleGuard } from 'app/core/auth/guards/module.guard';
import { NoAuthGuard } from 'app/core/auth/guards/noAuth.guard';
import { superAdminGuard, tenantAdminGuard } from 'app/core/auth/guards/role.guard';
import { LanguageGuard } from 'app/core/language/language.guard';
import { LayoutComponent } from 'app/layout/layout.component';

export const appRoutes: Route[] = [

    {path: '', pathMatch : 'full', redirectTo: '/tr'},

    {
        path: 'signed-in-redirect',
        pathMatch : 'full',
        redirectTo: 'dashboards/project'
    },

    {
        path: '',
        canActivate: [NoAuthGuard],
        canActivateChild: [NoAuthGuard],
        component: LayoutComponent,
        data: {
            layout: 'empty'
        },
        children: [
            {path: 'confirmation-required', loadChildren: () => import('app/modules/auth/confirmation-required/confirmation-required.routes')},
            {path: 'forgot-password', loadChildren: () => import('app/modules/auth/forgot-password/forgot-password.routes')},
            {path: 'reset-password', loadChildren: () => import('app/modules/auth/reset-password/reset-password.routes')},
            {path: 'sign-in', loadChildren: () => import('app/modules/auth/sign-in/sign-in.routes')},
            {path: 'sign-up', loadChildren: () => import('app/modules/auth/sign-up/sign-up.routes')}
        ]
    },
    {
        path: '',
        canActivate: [AuthGuard],
        canActivateChild: [AuthGuard],
        component: LayoutComponent,
        data: {
            layout: 'empty'
        },
        children: [
            {path: 'sign-out', loadChildren: () => import('app/modules/auth/sign-out/sign-out.routes')},
            {path: 'unlock-session', loadChildren: () => import('app/modules/auth/unlock-session/unlock-session.routes')}
        ]
    },
    {
        path: '',
        component: LayoutComponent,
        data: {
            layout: 'empty'
        },
        children: [
            {path: 'home', loadChildren: () => import('app/modules/landing/home/home.routes')},
        ]
    },
    {
        path: ':lang',
        canActivate: [LanguageGuard, AuthGuard],
        canActivateChild: [AuthGuard],
        component: LayoutComponent,
        resolve: {
            initialData: initialDataResolver
        },
        children: [

            {path: '', redirectTo: 'dashboards/project', pathMatch: 'full'},
            {path: 'dashboards', children: [
                {path: 'project', loadChildren: () => import('app/modules/admin/dashboards/project/project.routes')},
                {path: 'analytics', loadChildren: () => import('app/modules/admin/dashboards/analytics/analytics.routes')},
                {path: 'finance', loadChildren: () => import('app/modules/admin/dashboards/finance/finance.routes')},
                {path: 'crypto', loadChildren: () => import('app/modules/admin/dashboards/crypto/crypto.routes')},
            ]},
            {path: 'apps', children: [
                {path: '**', redirectTo: 'dashboards/project'}
            ]},
            {
                path: 'tenants',
                canActivate: [superAdminGuard],
                loadChildren: () => import('app/modules/admin/custom/tenants/tenants.routes')
            },
            {
                path: 'media',
                canActivate: [tenantAdminGuard, moduleGuard],
                data: { requiredModule: 'media' },
                loadChildren: () => import('app/modules/admin/custom/media/media.routes')
            },
            {
                path: 'users',
                canActivate: [tenantAdminGuard, moduleGuard],
                data: { requiredModule: 'core' },
                loadChildren: () => import('app/modules/admin/custom/users/users.routes')
            },
            {
                path: 'sites',
                canActivate: [tenantAdminGuard, moduleGuard],
                data: { requiredModule: 'core' },
                loadChildren: () => import('app/modules/admin/custom/sites/sites.routes')
            },
            {
                path: 'pages',
                canActivate: [tenantAdminGuard, moduleGuard],
                data: { requiredModule: 'pagebuilder' },
                loadChildren: () => import('app/modules/admin/custom/pages/page-builder.routes')
            },
            {
                path: 'settings',
                canActivate: [tenantAdminGuard, moduleGuard],
                data: { requiredModule: 'site_settings' },
                loadChildren: () => import('app/modules/admin/custom/settings/site-settings.routes')
            },

            // Pages
            {path: 'pages', children: [

                {path: 'activities', loadChildren: () => import('app/modules/admin/pages/activities/activities.routes')},

                {path: 'authentication', loadChildren: () => import('app/modules/admin/pages/authentication/authentication.routes')},

                {path: 'coming-soon', loadChildren: () => import('app/modules/admin/pages/coming-soon/coming-soon.routes')},

                {path: 'error', children: [
                    {path: '404', loadChildren: () => import('app/modules/admin/pages/error/error-404/error-404.routes')},
                    {path: '500', loadChildren: () => import('app/modules/admin/pages/error/error-500/error-500.routes')}
                ]},

                {path: 'invoice', children: [
                    {path: 'printable', children: [
                        {path: 'compact', loadChildren: () => import('app/modules/admin/pages/invoice/printable/compact/compact.routes')},
                        {path: 'modern', loadChildren: () => import('app/modules/admin/pages/invoice/printable/modern/modern.routes')}
                    ]}
                ]},

                {path: 'maintenance', loadChildren: () => import('app/modules/admin/pages/maintenance/maintenance.routes')},

                {path: 'pricing', children: [
                    {path: 'modern', loadChildren: () => import('app/modules/admin/pages/pricing/modern/modern.routes')},
                    {path: 'simple', loadChildren: () => import('app/modules/admin/pages/pricing/simple/simple.routes')},
                    {path: 'single', loadChildren: () => import('app/modules/admin/pages/pricing/single/single.routes')},
                    {path: 'table', loadChildren: () => import('app/modules/admin/pages/pricing/table/table.routes')}
                ]},

                {path: 'profile', loadChildren: () => import('app/modules/admin/pages/profile/profile.routes')},

                {path: 'settings', loadChildren: () => import('app/modules/admin/pages/settings/settings.routes')},
            ]},

            // User Interface
            {path: 'ui', children: [

                {path: 'material-components', loadChildren: () => import('app/modules/admin/ui/material-components/material-components.routes')},

                {path: 'fuse-components', loadChildren: () => import('app/modules/admin/ui/fuse-components/fuse-components.routes')},

                {path: 'other-components', loadChildren: () => import('app/modules/admin/ui/other-components/other-components.routes')},

                {path: 'tailwindcss', loadChildren: () => import('app/modules/admin/ui/tailwindcss/tailwindcss.routes')},

                {path: 'advanced-search', loadChildren: () => import('app/modules/admin/ui/advanced-search/advanced-search.routes')},

                {path: 'animations', loadChildren: () => import('app/modules/admin/ui/animations/animations.routes')},

                {path: 'cards', loadChildren: () => import('app/modules/admin/ui/cards/cards.routes')},

                {path: 'colors', loadChildren: () => import('app/modules/admin/ui/colors/colors.routes')},

                {path: 'confirmation-dialog', loadChildren: () => import('app/modules/admin/ui/confirmation-dialog/confirmation-dialog.routes')},

                {path: 'datatable', loadChildren: () => import('app/modules/admin/ui/datatable/datatable.routes')},

                {path: 'forms', loadChildren: () => import('app/modules/admin/ui/forms/forms.routes')},

                {path: 'icons', loadChildren: () => import('app/modules/admin/ui/icons/icons.routes')},

                {path: 'page-layouts', loadChildren: () => import('app/modules/admin/ui/page-layouts/page-layouts.routes')},

                {path: 'typography', loadChildren: () => import('app/modules/admin/ui/typography/typography.routes')}
            ]},

            // Documentation
            {path: 'docs', children: [

                {path: 'changelog', loadChildren: () => import('app/modules/admin/docs/changelog/changelog.routes')},

                {path: 'guides', loadChildren: () => import('app/modules/admin/docs/guides/guides.routes')}
            ]},

            {path: '404-not-found', pathMatch: 'full', loadChildren: () => import('app/modules/admin/pages/error/error-404/error-404.routes')},
            {path: '**', loadChildren: () => import('app/modules/admin/pages/error/error-404/error-404.routes')}
        ]
    }
];
