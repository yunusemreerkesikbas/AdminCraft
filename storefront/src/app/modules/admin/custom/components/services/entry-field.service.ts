import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { ApiClientService } from '@core/api/api-client.service';
import { ApiResponse } from '@core/crud';
import { HttpHeadersService } from '@core/api/http-headers.service';
import { environment } from '@environments/environment';
import { resolveEndpoint } from '@modules/admin/api-endpoints';
import { Observable, map } from 'rxjs';
import { CreateEntryFieldRequest, EntryFieldDefinitionResponse, ImportResultResponse, ImportSchemaRequest, ValidationResult } from '../models/component-entry.types';

@Injectable({ providedIn: 'root' })
export class EntryFieldService {
    #api = inject(ApiClientService);
    #http = inject(HttpClient);
    #httpHeaders = inject(HttpHeadersService);
    #baseUrl = environment.apiBaseUrl;

    addField(typeId: number, field: CreateEntryFieldRequest): Observable<EntryFieldDefinitionResponse> {
        return this.#api.post<ApiResponse<EntryFieldDefinitionResponse>>('componentTypeEntryFields', field, { typeId })
            .pipe(map(response => response.data));
    }

    getFields(typeId: number): Observable<EntryFieldDefinitionResponse[]> {
        return this.#api.get<ApiResponse<EntryFieldDefinitionResponse[]>>('componentTypeEntryFields', { typeId })
            .pipe(map(response => response.data || []));
    }

    importSchema(typeId: number, schema: ImportSchemaRequest): Observable<ImportResultResponse> {
        return this.#api.post<ApiResponse<ImportResultResponse>>('componentTypeEntryFieldsImport', schema, { typeId })
            .pipe(map(response => response.data));
    }

    exportSchema(typeId: number): Observable<Blob> {
        const endpoint = resolveEndpoint('componentTypeEntryFieldsExport', { typeId });
        const url = `${this.#baseUrl}/${endpoint}`;
        
        return this.#http.get(url, {
            headers: this.#httpHeaders.getStandardHeaders(),
            responseType: 'blob'
        });
    }

    validateField(typeId: number, field: CreateEntryFieldRequest): Observable<ValidationResult> {
        return this.#api.post<ApiResponse<ValidationResult>>('componentTypeEntryFieldValidate', field, { typeId })
            .pipe(map(response => response.data));
    }
}

