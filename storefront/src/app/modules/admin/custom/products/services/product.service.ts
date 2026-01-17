import { ApiResponse } from '@/app/core/crud';
import { Injectable } from '@angular/core';
import { CrudEndpoints, CrudHttpService } from '@core/crud/crud-http.service';
import { map, Observable } from 'rxjs';
import {
    Product,
    ProductCompositeRequest,
    ProductMediaResponse
} from '../models/product.types';

@Injectable({ providedIn: 'root' })
export class ProductService extends CrudHttpService<Product, ProductCompositeRequest, ProductCompositeRequest> {
    protected override endpoints: CrudEndpoints = {
        list: 'products',
        getById: 'productById',
        create: 'productComposite',
        update: 'productCompositeById',
        delete: 'productById'
    };

    getComposite(id: number): Observable<Product> {
        return this.customGet<Product>('productById', { id }, { include: 'translations' });
    }

    getGallery(id: number): Observable<ProductMediaResponse[]> {
        return this.customGet<ProductMediaResponse[]>('productMedia', { id });
    }

    addMedia(id: number, mediaId: number): Observable<ProductMediaResponse> {
        return this.api.post<ApiResponse<ProductMediaResponse>>('productMedia', { mediaId }, { id }).pipe(
            map(res => res.data)
        );
    }

    removeMedia(id: number, mediaId: number): Observable<void> {
        return this.api.delete<ApiResponse<void>>('productMediaById', { id, mediaId }).pipe(
            map(() => void 0)
        );
    }
}
