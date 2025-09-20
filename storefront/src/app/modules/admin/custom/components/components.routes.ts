import { Routes } from '@angular/router';
import { NavbarFormComponent } from './navbar/form/navbar-form.component';
import { NavbarListComponent } from './navbar/list/navbar-list.component';

export default [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'navbar'
  },
  {
    path: 'navbar',
    children: [
      { path: '', component: NavbarListComponent },
      { path: 'new', component: NavbarFormComponent },
      { path: ':id', component: NavbarFormComponent }
    ]
  }
] as Routes;


