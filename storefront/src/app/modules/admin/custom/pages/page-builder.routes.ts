import { Routes } from '@angular/router';
import { PageCategoriesComponent } from './categories/page-categories.component';
import { PageListComponent } from './list/page-list.component';
import { PageBuilderComponent } from './page-builder.component';

export default [
  {
    path: '',
    component: PageBuilderComponent,
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'list' },
      { path: 'list', component: PageListComponent },
      { path: 'categories', component: PageCategoriesComponent },
    ],
  },
] as Routes;


