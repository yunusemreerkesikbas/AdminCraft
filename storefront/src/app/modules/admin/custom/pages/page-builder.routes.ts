import { Routes } from '@angular/router';
import { PageListComponent } from './list/page-list.component';
import { PageBuilderComponent } from './page-builder.component';

export default [
  {
    path: '',
    component: PageBuilderComponent,
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'list' },
      { path: 'list', component: PageListComponent },
    ],
  },
] as Routes;


