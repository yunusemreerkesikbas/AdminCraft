import { Injectable } from '@angular/core';
import { ApiResponse } from '@core/crud/api.types';
import { CrudEndpoints, CrudHttpService } from '@core/crud/crud-http.service';
import { Observable } from 'rxjs';
import { ComponentTypeDto, CreateComponentTypeRequest, UpdateComponentTypeRequest } from '../models/component-library.types';

@Injectable({ providedIn: 'root' })
export class ComponentTypeService extends CrudHttpService<ComponentTypeDto, CreateComponentTypeRequest, UpdateComponentTypeRequest> {
    protected endpoints: CrudEndpoints = {
        list: 'componentTypes',
        getById: 'componentTypeById',
        create: 'componentTypes',
        update: 'componentTypeById',
        delete: 'componentTypeById'
    };

    createWithResponse(
        request: CreateComponentTypeRequest
    ): Observable<ApiResponse<ComponentTypeDto>> {
        return this.api.post<ApiResponse<ComponentTypeDto>>(
            this.endpoints.create,
            request
        );
    }

    updateWithResponse(
        id: number,
        request: UpdateComponentTypeRequest
    ): Observable<ApiResponse<ComponentTypeDto>> {
        return this.api.put<ApiResponse<ComponentTypeDto>>(
            this.endpoints.update,
            request,
            { id }
        );
    }

    deleteWithResponse(id: number): Observable<ApiResponse<void>> {
        return this.api.delete<ApiResponse<void>>(this.endpoints.delete, {
            id,
        });
    }
}
