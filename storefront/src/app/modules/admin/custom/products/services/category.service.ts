import { Injectable } from '@angular/core';
import { CrudEndpoints, CrudHttpService } from '@core/crud/crud-http.service';
import { Observable } from 'rxjs';
import {
    Category,
    CategoryCompositeRequest,
    CategoryTreeResponse
} from '../models/category.types';

@Injectable({ providedIn: 'root' })
export class CategoryService extends CrudHttpService<Category, CategoryCompositeRequest, CategoryCompositeRequest> {
    protected override endpoints: CrudEndpoints = {
        list: 'productCategories',
        getById: 'productCategoryById',
        create: 'productCategoryComposite',
        update: 'productCategoryCompositeById',
        delete: 'productCategoryById'
    };

    getTree(): Observable<CategoryTreeResponse[]> {
        return this.customGet<CategoryTreeResponse[]>('productCategories');
    }

    getComposite(id: number): Observable<Category> {
        return this.customGet<Category>('productCategoryCompositeById', { id });
    }
}
