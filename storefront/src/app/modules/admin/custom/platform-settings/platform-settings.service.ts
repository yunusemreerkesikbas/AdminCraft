import { Injectable, inject } from '@angular/core';
import { ApiClientService } from '@core/api/api-client.service';
import { Observable, map } from 'rxjs';
import { PatchPlatformSettingsRequest, PlatformSettingsResponse } from './platform-settings.types';

interface ApiResponse<T> {
    result: string;
    message: string;
    data: T;
}

@Injectable({ providedIn: 'root' })
export class PlatformSettingsService {
    readonly #apiClient = inject(ApiClientService);

    getSettings(): Observable<PlatformSettingsResponse> {
        return this.#apiClient
            .get<ApiResponse<PlatformSettingsResponse>>('platformSettings')
            .pipe(map((response) => response.data));
    }

    patchSettings(payload: PatchPlatformSettingsRequest): Observable<PlatformSettingsResponse> {
        return this.#apiClient
            .patch<ApiResponse<PlatformSettingsResponse>>('platformSettings', payload)
            .pipe(map((response) => response.data));
    }
}
