import { Route } from '@angular/router';

export default [
    { path: '', pathMatch: 'full', redirectTo: '/dashboards/project' },
    {
        path: 'pages',
        loadChildren: () => import('./pages/page-builder.routes').then(m => m.default)
    },
    {
        path: 'components',
        loadChildren: () => import('./components/components.routes').then(m => m.default)
    },
    {
        path: 'settings',
        loadChildren: () => import('./settings/site-settings.routes').then(m => m.default)
    }
] as Route[];