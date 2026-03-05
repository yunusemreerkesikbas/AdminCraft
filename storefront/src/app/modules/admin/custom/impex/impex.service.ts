import { Injectable, inject } from '@angular/core';
import { ApiClientService } from '@core/api/api-client.service';
import { ApiResponse } from '@core/crud/api.types';
import { Observable, map } from 'rxjs';
import { ImpExResult } from './impex.types';

export interface ImpExResponse {
    message: string;
    result: ImpExResult;
}

@Injectable({ providedIn: 'root' })
export class ImpExService {
    readonly #apiClient = inject(ApiClientService);

    execute(sqlContent: string): Observable<ImpExResponse> {
        return this.#apiClient
            .post<ApiResponse<ImpExResult>>('impexExecute', { sqlContent })
            .pipe(
                map((response) => ({
                    message: response.message!,
                    result: response.data,
                }))
            );
    }
}
