import { Routes } from '@angular/router';
import { CategoryTreeComponent } from './categories/category-tree.component';
import { ProductListComponent } from './list/product-list.component';
import { ProductTypeListComponent } from './types/product-type-list.component';

export default [
    {
        path: '',
        component: ProductListComponent
    },
    {
        path: 'types',
        component: ProductTypeListComponent
    },
    {
        path: 'categories',
        component: CategoryTreeComponent
    }
] as Routes;
