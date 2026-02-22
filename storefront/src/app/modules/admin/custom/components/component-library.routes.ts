import { Routes } from '@angular/router';
import { ComponentListComponent } from './list/component-list.component';

export default [
    {
        path: 'types',
        loadComponent: () =>
            import(
                './types/component-types-list.component'
            ).then((m) => m.ComponentTypesListComponent),
    },
    {
        path: '',
        component: ComponentListComponent
    }
] as Routes;
