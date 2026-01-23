import { Injectable } from '@angular/core';
import { CrudEndpoints, CrudHttpService } from '@core/crud/crud-http.service';
import { Observable } from 'rxjs';
import {
    CreateProductFieldRequest,
    ProductFieldDefinition,
    UpdateProductFieldRequest,
} from '../models/product-field.types';

/**
 * Service for managing global product field definitions.
 */
@Injectable({ providedIn: 'root' })
export class ProductFieldService extends CrudHttpService<
    ProductFieldDefinition,
    CreateProductFieldRequest,
    UpdateProductFieldRequest
> {
    protected override endpoints: CrudEndpoints = {
        list: 'productFields',
        getById: 'productFieldById',
        create: 'productFields',
        update: 'productFieldById',
        delete: 'productFieldById',
    };

    /**
     * Get field definitions visible in list view.
     */
    getVisible(): Observable<ProductFieldDefinition[]> {
        return this.customGet<ProductFieldDefinition[]>(
            'productFieldsVisible',
            {}
        );
    }

    /**
     * Get all field definitions.
     */
    getAllDefinitions(): Observable<ProductFieldDefinition[]> {
        return this.customGet<ProductFieldDefinition[]>('productFields', {});
    }
}
