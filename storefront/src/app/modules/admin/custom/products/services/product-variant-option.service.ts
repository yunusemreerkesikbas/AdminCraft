import { Injectable } from '@angular/core';
import { CrudEndpoints, CrudHttpService } from '@core/crud/crud-http.service';
import { Observable } from 'rxjs';
import {
    CreateProductVariantOptionRequest,
    ProductVariantOption,
    UpdateProductVariantOptionRequest,
} from '../models/product-variant-option.types';

@Injectable({ providedIn: 'root' })
export class ProductVariantOptionService extends CrudHttpService<
    ProductVariantOption,
    CreateProductVariantOptionRequest,
    UpdateProductVariantOptionRequest
> {
    protected override endpoints: CrudEndpoints = {
        list: 'productVariantOptions',
        getById: 'productVariantOptionById',
        create: 'productVariantOptions',
        update: 'productVariantOptionById',
        delete: 'productVariantOptionById',
    };

    getAll(): Observable<ProductVariantOption[]> {
        return this.customGet<ProductVariantOption[]>(
            'productVariantOptions',
            {}
        );
    }
}
