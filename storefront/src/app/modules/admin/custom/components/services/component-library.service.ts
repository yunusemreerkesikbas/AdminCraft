import { Injectable } from '@angular/core';
import { CrudEndpoints, CrudHttpService } from '@core/crud';
import { ApiResponse, PageWithSort } from '@core/crud/api.types';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import {
    BulkDeleteRequest,
    BulkDeleteResult,
    ComponentCompositeResponse,
    ComponentDetailDto,
    ComponentDto,
    ComponentI18nDto,
    ComponentI18nRequest,
    ComponentTypeDto,
    CreateComponentCompositeRequest,
    CreateComponentRequest,
    UpdateComponentCompositeRequest,
    UpdateComponentRequest
} from '../models/component-library.types';

@Injectable({ providedIn: 'root' })
export class ComponentLibraryService extends CrudHttpService<ComponentDto, CreateComponentRequest, UpdateComponentRequest> {
    protected endpoints: CrudEndpoints = {
        list: 'components',
        getById: 'componentById',
        create: 'components',
        update: 'componentById',
        delete: 'componentById'
    };

    listComponents(): Observable<ComponentDto[]> {
        return this.list();
    }

    getComponentById(id: number): Observable<ComponentDto> {
        return this.getById(id);
    }

    getComponentDetail(id: number): Observable<ComponentDetailDto> {
        return this.customGet<ComponentDetailDto>('componentById', { id }, { include: 'translations' });
    }

    getComponentDetailByUid(uid: string): Observable<ComponentDetailDto> {
        return this.customGet<ComponentDetailDto>('componentByUid', { uid }, { include: 'translations' });
    }

    createComponent(req: CreateComponentRequest): Observable<ComponentDto> {
        return this.create(req);
    }

    updateComponent(id: number, req: UpdateComponentRequest): Observable<ComponentDto> {
        return this.update(id, req);
    }

    deleteComponentWithResponse(id: number): Observable<ApiResponse<void>> {
        return this.api.delete<ApiResponse<void>>(this.endpoints.delete, { id });
    }

    bulkDeleteComponentsWithResponse(ids: number[]): Observable<ApiResponse<BulkDeleteResult>> {
        const payload: BulkDeleteRequest = { ids };
        return this.api.post<ApiResponse<BulkDeleteResult>>('componentBulkDelete', payload);
    }

    getComponentI18n(componentId: number, language: string): Observable<ComponentI18nDto> {
        return this.customGet<ComponentI18nDto>('componentI18n', { componentId, language });
    }

    updateComponentI18n(componentId: number, language: string, req: ComponentI18nRequest): Observable<ComponentI18nDto> {
        return this.customPut<ComponentI18nDto>('componentI18n', req, { componentId, language });
    }

    publishComponentI18n(componentId: number, language: string): Observable<ComponentI18nDto> {
        return this.customPost<ComponentI18nDto>('componentI18nPublish', {}, { componentId, language });
    }

    listComponentTypes(): Observable<ComponentTypeDto[]> {
        return this.api.get<ApiResponse<PageWithSort<ComponentTypeDto>>>('componentTypes').pipe(
            map(response => response.data?.content || [])
        );
    }

    getComponentTypeById(id: number): Observable<ComponentTypeDto> {
        return this.customGet<ComponentTypeDto>('componentTypeById', { id });
    }

    createCompositeWithResponse(
        data: CreateComponentCompositeRequest
    ): Observable<ApiResponse<ComponentCompositeResponse>> {
        return this.api.post<ApiResponse<ComponentCompositeResponse>>(
            'componentComposite',
            data
        );
    }

    updateCompositeWithResponse(
        id: number,
        data: UpdateComponentCompositeRequest
    ): Observable<ApiResponse<ComponentCompositeResponse>> {
        return this.api.put<ApiResponse<ComponentCompositeResponse>>(
            'componentCompositeById',
            data,
            { id }
        );
    }

    updateDraftCompositeWithResponse(
        id: number,
        data: UpdateComponentCompositeRequest
    ): Observable<ApiResponse<ComponentCompositeResponse>> {
        return this.api.put<ApiResponse<ComponentCompositeResponse>>(
            'componentCompositeDraftById',
            data,
            { id }
        );
    }

    getComposite(id: number): Observable<ComponentCompositeResponse> {
        return this.customGet<ComponentCompositeResponse>('componentCompositeById', { id });
    }

    assignResponsiveMedia(id: number, responsiveMediaId: number | null): Observable<ComponentDto> {
        const queryParams: Record<string, string | number> = {};
        if (responsiveMediaId) {
            queryParams['responsiveMediaId'] = responsiveMediaId;
        }
        return this.api.patch<ApiResponse<ComponentDto>>('componentResponsiveMedia', {}, { id }, queryParams).pipe(
            map(response => response.data)
        );
    }
}
