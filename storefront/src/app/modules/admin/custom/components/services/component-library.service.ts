import { Injectable } from '@angular/core';
import { CrudEndpoints, CrudHttpService } from '@core/crud';
import { Observable } from 'rxjs';
import {
    ComponentCompositeResponse,
    ComponentDetailDto,
    ComponentDto,
    ComponentI18nDto,
    ComponentI18nRequest,
    ComponentTypeDto,
    CreateComponentCompositeRequest,
    CreateComponentRequest,
    CreateComponentTypeRequest,
    UpdateComponentCompositeRequest,
    UpdateComponentRequest,
    UpdateComponentTypeRequest
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

    createComponent(req: CreateComponentRequest): Observable<ComponentDto> {
        return this.create(req);
    }

    updateComponent(id: number, req: UpdateComponentRequest): Observable<ComponentDto> {
        return this.update(id, req);
    }

    deleteComponent(id: number): Observable<void> {
        return this.delete(id);
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
        return this.customGet<ComponentTypeDto[]>('componentTypes');
    }

    getComponentTypeById(id: number): Observable<ComponentTypeDto> {
        return this.customGet<ComponentTypeDto>('componentTypeById', { id });
    }

    createComponentType(req: CreateComponentTypeRequest): Observable<ComponentTypeDto> {
        return this.customPost<ComponentTypeDto>('componentTypes', req);
    }

    updateComponentType(id: number, req: UpdateComponentTypeRequest): Observable<ComponentTypeDto> {
        return this.customPut<ComponentTypeDto>('componentTypeById', req, { id });
    }

    deleteComponentType(id: number): Observable<void> {
        return this.customDelete<void>('componentTypeById', { id });
    }

    createComposite(data: CreateComponentCompositeRequest): Observable<ComponentCompositeResponse> {
        return this.customPost<ComponentCompositeResponse>('componentComposite', data);
    }

    updateComposite(id: number, data: UpdateComponentCompositeRequest): Observable<ComponentCompositeResponse> {
        return this.customPut<ComponentCompositeResponse>('componentCompositeById', data, { id });
    }

    getComposite(id: number): Observable<ComponentCompositeResponse> {
        return this.customGet<ComponentCompositeResponse>('componentCompositeById', { id });
    }
}


