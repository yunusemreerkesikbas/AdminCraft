import { Route } from '@angular/router';
import { TenantsListComponent } from './list/tenants-list.component';

export default [
    {
        path: '',
        component: TenantsListComponent,
    },
] as Route[];