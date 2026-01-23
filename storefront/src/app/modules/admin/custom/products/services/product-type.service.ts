import { Injectable } from '@angular/core';
import { ApiResponse } from '@core/crud/api.types';
import { CrudEndpoints, CrudHttpService } from '@core/crud/crud-http.service';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import {
    AttributeDefinition,
    CreateAttributeDefinitionRequest,
    CreateProductTypeRequest,
    ProductType,
    UpdateAttributeDefinitionRequest,
    UpdateProductTypeRequest
} from '../models/product-type.types';

@Injectable({ providedIn: 'root' })
export class ProductTypeService extends CrudHttpService<ProductType, CreateProductTypeRequest, UpdateProductTypeRequest> {
    protected override endpoints: CrudEndpoints = {
        list: 'productTypes',
        getById: 'productTypeById',
        create: 'productTypes',
        update: 'productTypeById',
        delete: 'productTypeById'
    };

    getAttributes(typeId: number): Observable<AttributeDefinition[]> {
        return this.customGet<AttributeDefinition[]>('productAttributeDefinitions', { typeId });
    }

    createAttribute(typeId: number, request: CreateAttributeDefinitionRequest): Observable<AttributeDefinition> {
        return this.api.post<ApiResponse<AttributeDefinition>>('productAttributeDefinitions', request, { typeId }).pipe(
            map(res => res.data)
        );
    }

    updateAttribute(typeId: number, attrId: number, request: UpdateAttributeDefinitionRequest): Observable<AttributeDefinition> {
        return this.api.put<ApiResponse<AttributeDefinition>>('productAttributeDefinitionById', request, { typeId, id: attrId }).pipe(
            map(res => res.data)
        );
    }

    deleteAttribute(typeId: number, attrId: number): Observable<void> {
        return this.api.delete<ApiResponse<void>>('productAttributeDefinitionById', { typeId, id: attrId }).pipe(
            map(() => void 0)
        );
    }

    getProductCount(typeId: number): Observable<number> {
        return this.customGet<number>('productTypeProductCount', { id: typeId });
    }
}
