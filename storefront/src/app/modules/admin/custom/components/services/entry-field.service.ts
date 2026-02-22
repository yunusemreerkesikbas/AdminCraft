import { Injectable, inject } from '@angular/core';
import { ApiClientService } from '@core/api/api-client.service';
import { ApiResponse } from '@core/crud';
import { Observable, map } from 'rxjs';
import {
    CreateEntryFieldRequest,
    EntryFieldDefinitionResponse,
} from '../models/component-entry.types';

@Injectable({ providedIn: 'root' })
export class EntryFieldService {
    #api = inject(ApiClientService);

    addField(typeId: number, field: CreateEntryFieldRequest): Observable<EntryFieldDefinitionResponse> {
        return this.#api.post<ApiResponse<EntryFieldDefinitionResponse>>('componentTypeEntryFields', field, { typeId })
            .pipe(map(response => response.data));
    }

    getFields(typeId: number): Observable<EntryFieldDefinitionResponse[]> {
        return this.#api.get<ApiResponse<EntryFieldDefinitionResponse[]>>('componentTypeEntryFields', { typeId })
            .pipe(map(response => response.data || []));
    }
}
