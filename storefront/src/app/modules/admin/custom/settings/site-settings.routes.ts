import { Routes } from '@angular/router';

export default [
  {
    path: '',
    loadComponent: () =>
      import('./site-settings.component').then((m) => m.SiteSettingsPageComponent),
  },
] as Routes;


