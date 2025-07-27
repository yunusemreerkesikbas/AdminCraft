import { Route } from '@angular/router';

export default [
    {
        path: 'tenants',
        loadChildren: () => import('app/modules/admin/apps/admincraft/tenants/tenants.routes'),
    },
    {
        path: 'content',
        loadChildren: () => import('app/modules/admin/apps/admincraft/content/content.routes'),
    },
    {
        path: 'media',
        loadChildren: () => import('app/modules/admin/apps/admincraft/media/media.routes'),
    },
    {
        path: 'users',
        loadChildren: () => import('app/modules/admin/apps/admincraft/users/users.routes'),
    },
    {
        path: 'sites',
        loadChildren: () => import('app/modules/admin/apps/admincraft/sites/sites.routes'),
    },
] as Route[];