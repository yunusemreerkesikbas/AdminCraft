import { Route } from '@angular/router';

export default [
    // Deprecated: AdminCraft under /apps. Routes are re-exposed under '/:tenant'.
    // Keep a fallback redirect to root dashboards for legacy links.
    { path: '', pathMatch: 'full', redirectTo: '/dashboards/project' }
] as Route[];