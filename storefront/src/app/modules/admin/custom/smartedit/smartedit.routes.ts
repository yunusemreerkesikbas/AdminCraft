import { Routes } from '@angular/router';

export default [
    {
        path: ':pageId',
        loadComponent: () =>
            import('./smartedit-shell.component').then((m) => m.SmartEditShellComponent),
    },
] as Routes;
