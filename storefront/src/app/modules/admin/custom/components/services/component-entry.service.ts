import { Injectable } from '@angular/core';
import { CrudEndpoints, CrudHttpService } from '@core/crud/crud-http.service';
import { Observable } from 'rxjs';
import {
    ComponentEntry,
    ComponentEntryCompositeResponse,
    ComponentEntryDetailDto,
    CreateComponentEntryCompositeRequest,
    CreateEntryRequest,
    EntryI18nDto,
    EntryI18nRequest,
    UpdateComponentEntryCompositeRequest,
    UpdateEntryRequest
} from '../models/component-entry.types';

interface ApiResponse<T> {
    success: boolean;
    data: T;
    message?: string;
}


@Injectable({ providedIn: 'root' })
export class ComponentEntryService extends CrudHttpService<ComponentEntry, CreateEntryRequest, UpdateEntryRequest> {
    protected endpoints: CrudEndpoints = {
        list: 'componentEntriesList',
        getById: 'componentEntryById',
        create: 'componentEntriesCreate',
        update: 'componentEntryUpdate',
        delete: 'componentEntryDelete'
    };

    override create(data: CreateEntryRequest): Observable<ComponentEntry> {
        return this.customPost<ComponentEntry>('componentEntriesCreate', data, { componentId: data.componentId });
    }

    override update(entryId: number, data: UpdateEntryRequest): Observable<ComponentEntry> {
        return this.customPut<ComponentEntry>('componentEntryUpdate', data, { entryId });
    }

    override delete(entryId: number): Observable<void> {
        return this.customDelete<void>('componentEntryDelete', { entryId });
    }

    listByComponentId(componentId: number): Observable<ComponentEntry[]> {
        return this.customGet<ComponentEntry[]>('componentEntriesByComponent', { componentId });
    }

    getEntryDetail(entryId: number): Observable<ComponentEntryDetailDto> {
        return this.customGet<ComponentEntryDetailDto>('componentEntryById', { entryId }, { include: 'translations' });
    }

    getI18n(entryId: number, language: string): Observable<EntryI18nDto> {
        return this.customGet<EntryI18nDto>('componentEntryI18n', { entryId, language });
    }

    upsertI18n(entryId: number, language: string, data: EntryI18nRequest): Observable<EntryI18nDto> {
        return this.customPut<EntryI18nDto>('componentEntryI18nUpsert', data, { entryId, language });
    }

    publish(entryId: number, language: string): Observable<EntryI18nDto> {
        return this.customPost<EntryI18nDto>('componentEntryPublish', {}, { entryId, language });
    }

    createComposite(data: CreateComponentEntryCompositeRequest): Observable<ComponentEntryCompositeResponse> {
        return this.customPost<ComponentEntryCompositeResponse>('componentEntryComposite', data);
    }

    updateComposite(id: number, data: UpdateComponentEntryCompositeRequest): Observable<ComponentEntryCompositeResponse> {
        return this.customPut<ComponentEntryCompositeResponse>('componentEntryCompositeById', data, { id });
    }
}


