import { Route, UrlMatcher, UrlSegment } from '@angular/router';
import { initialDataResolver, tenantParamResolver } from 'app/app.resolvers';
import { AuthGuard } from 'app/core/auth/guards/auth.guard';
import { NoAuthGuard } from 'app/core/auth/guards/noAuth.guard';
import { LayoutComponent } from 'app/layout/layout.component';

// prettier-ignore
/* eslint-disable max-len */
/* eslint-disable @typescript-eslint/explicit-function-return-type */
const tenantMatcher: UrlMatcher = (segments: UrlSegment[]) => {
    if (!segments.length) { return null; }
    const seg = segments[0].path;
    const valid = /^[a-z0-9-]{1,50}$/.test(seg);
    if (!valid) { return null; }
    return {
        consumed: [segments[0]],
        posParams: { tenant: segments[0] }
    };
};

export const appRoutes: Route[] = [

    // Redirect empty path to 'sign-in'
    {path: '', pathMatch : 'full', redirectTo: 'sign-in'},

    // Redirect signed-in user to the '/dashboards/project'
    //
    // After the user signs in, the sign-in page will redirect the user to the 'signed-in-redirect'
    // path. Below is another redirection for that path to redirect the user to the desired
    // location. This is a small convenience to keep all main routes together here on this file.
    // Note: We no longer use this for dynamic tenant redirect.
    // Kept as a fallback to dashboards.
    {
        path: 'signed-in-redirect',
        pathMatch : 'full',
        redirectTo: 'dashboards/project'
    },

    // Auth routes for guests
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

    // Auth routes for authenticated users
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

    // Landing routes
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

    // Admin routes
    {
        path: '',
        canActivate: [AuthGuard],
        canActivateChild: [AuthGuard],
        component: LayoutComponent,
        resolve: {
            initialData: initialDataResolver
        },
        children: [

            // Dashboards
            {path: 'dashboards', children: [
                {path: 'project', loadChildren: () => import('app/modules/admin/dashboards/project/project.routes')},
                {path: 'analytics', loadChildren: () => import('app/modules/admin/dashboards/analytics/analytics.routes')},
                {path: 'finance', loadChildren: () => import('app/modules/admin/dashboards/finance/finance.routes')},
                {path: 'crypto', loadChildren: () => import('app/modules/admin/dashboards/crypto/crypto.routes')},
            ]},

            // Legacy apps paths redirect to dashboards
            {path: 'apps', children: [
                {path: '**', redirectTo: 'dashboards/project'}
            ]},

            // Tenant-prefixed routes
            {
                matcher: tenantMatcher,
                runGuardsAndResolvers: 'paramsChange',
                resolve: { tenant: tenantParamResolver },
                children: [
                    // Dashboards under tenant
                    {path: 'project', loadChildren: () => import('app/modules/admin/dashboards/project/project.routes')},
                    {path: 'analytics', loadChildren: () => import('app/modules/admin/dashboards/analytics/analytics.routes')},
                    {path: 'finance', loadChildren: () => import('app/modules/admin/dashboards/finance/finance.routes')},
                    {path: 'crypto', loadChildren: () => import('app/modules/admin/dashboards/crypto/crypto.routes')},

                    // AdminCraft features under tenant root
                    {path: 'tenants', loadChildren: () => import('app/modules/admin/custom/tenants/tenants.routes')},
                    // content module removed; replaced by page builder
                    {path: 'media', loadChildren: () => import('app/modules/admin/custom/media/media.routes')},
                    {path: 'users', loadChildren: () => import('app/modules/admin/custom/users/users.routes')},
                    {path: 'sites', loadChildren: () => import('app/modules/admin/custom/sites/sites.routes')},
                    {path: 'pages', loadChildren: () => import('app/modules/admin/custom/pages/page-builder.routes')},

                    // Default under tenant
                    {path: '', pathMatch: 'full', redirectTo: 'project'}
                ]
            },

            // Pages
            {path: 'pages', children: [

                // Activities
                {path: 'activities', loadChildren: () => import('app/modules/admin/pages/activities/activities.routes')},

                // Authentication
                {path: 'authentication', loadChildren: () => import('app/modules/admin/pages/authentication/authentication.routes')},

                // Coming Soon
                {path: 'coming-soon', loadChildren: () => import('app/modules/admin/pages/coming-soon/coming-soon.routes')},

                // Error
                {path: 'error', children: [
                    {path: '404', loadChildren: () => import('app/modules/admin/pages/error/error-404/error-404.routes')},
                    {path: '500', loadChildren: () => import('app/modules/admin/pages/error/error-500/error-500.routes')}
                ]},

                // Invoice
                {path: 'invoice', children: [
                    {path: 'printable', children: [
                        {path: 'compact', loadChildren: () => import('app/modules/admin/pages/invoice/printable/compact/compact.routes')},
                        {path: 'modern', loadChildren: () => import('app/modules/admin/pages/invoice/printable/modern/modern.routes')}
                    ]}
                ]},

                // Maintenance
                {path: 'maintenance', loadChildren: () => import('app/modules/admin/pages/maintenance/maintenance.routes')},

                // Pricing
                {path: 'pricing', children: [
                    {path: 'modern', loadChildren: () => import('app/modules/admin/pages/pricing/modern/modern.routes')},
                    {path: 'simple', loadChildren: () => import('app/modules/admin/pages/pricing/simple/simple.routes')},
                    {path: 'single', loadChildren: () => import('app/modules/admin/pages/pricing/single/single.routes')},
                    {path: 'table', loadChildren: () => import('app/modules/admin/pages/pricing/table/table.routes')}
                ]},

                // Profile
                {path: 'profile', loadChildren: () => import('app/modules/admin/pages/profile/profile.routes')},

                // Settings
                {path: 'settings', loadChildren: () => import('app/modules/admin/pages/settings/settings.routes')},
            ]},

            // User Interface
            {path: 'ui', children: [

                // Material Components
                {path: 'material-components', loadChildren: () => import('app/modules/admin/ui/material-components/material-components.routes')},

                // Fuse Components
                {path: 'fuse-components', loadChildren: () => import('app/modules/admin/ui/fuse-components/fuse-components.routes')},

                // Other Components
                {path: 'other-components', loadChildren: () => import('app/modules/admin/ui/other-components/other-components.routes')},

                // TailwindCSS
                {path: 'tailwindcss', loadChildren: () => import('app/modules/admin/ui/tailwindcss/tailwindcss.routes')},

                // Advanced Search
                {path: 'advanced-search', loadChildren: () => import('app/modules/admin/ui/advanced-search/advanced-search.routes')},

                // Animations
                {path: 'animations', loadChildren: () => import('app/modules/admin/ui/animations/animations.routes')},

                 // Cards
                {path: 'cards', loadChildren: () => import('app/modules/admin/ui/cards/cards.routes')},

                // Colors
                {path: 'colors', loadChildren: () => import('app/modules/admin/ui/colors/colors.routes')},

                // Confirmation Dialog
                {path: 'confirmation-dialog', loadChildren: () => import('app/modules/admin/ui/confirmation-dialog/confirmation-dialog.routes')},

                // Datatable
                {path: 'datatable', loadChildren: () => import('app/modules/admin/ui/datatable/datatable.routes')},

                // Forms
                {path: 'forms', loadChildren: () => import('app/modules/admin/ui/forms/forms.routes')},

                // Icons
                {path: 'icons', loadChildren: () => import('app/modules/admin/ui/icons/icons.routes')},

                // Page Layouts
                {path: 'page-layouts', loadChildren: () => import('app/modules/admin/ui/page-layouts/page-layouts.routes')},

                // Typography
                {path: 'typography', loadChildren: () => import('app/modules/admin/ui/typography/typography.routes')}
            ]},

            // Documentation
            {path: 'docs', children: [

                // Changelog
                {path: 'changelog', loadChildren: () => import('app/modules/admin/docs/changelog/changelog.routes')},

                // Guides
                {path: 'guides', loadChildren: () => import('app/modules/admin/docs/guides/guides.routes')}
            ]},

            // 404 & Catch all
            {path: '404-not-found', pathMatch: 'full', loadChildren: () => import('app/modules/admin/pages/error/error-404/error-404.routes')},
            {path: '**', loadChildren: () => import('app/modules/admin/pages/error/error-404/error-404.routes')}
        ]
    }
];
