import { Injectable, inject } from '@angular/core';
import { ApiClientService } from '@core/api/api-client.service';
import { ApiResponse } from '@core/crud/api.types';
import { Observable, map } from 'rxjs';
import {
    ConfirmTwoFactorPolicyChangeRequest,
    PatchPlatformSettingsRequest,
    PlatformSettingsResponse,
    RequestTwoFactorPolicyChangeRequest,
    TwoFactorPolicyChangeRequestResponse,
} from './platform-settings.types';

@Injectable({ providedIn: 'root' })
export class PlatformSettingsService {
    readonly #apiClient = inject(ApiClientService);

    getSettings(): Observable<PlatformSettingsResponse> {
        return this.#apiClient
            .get<ApiResponse<PlatformSettingsResponse>>('platformSettings')
            .pipe(map((response) => response.data));
    }

    patchSettings(
        payload: PatchPlatformSettingsRequest
    ): Observable<PlatformSettingsResponse> {
        return this.#apiClient
            .patch<
                ApiResponse<PlatformSettingsResponse>
            >('platformSettings', payload)
            .pipe(map((response) => response.data));
    }

    requestTwoFactorPolicyChange(
        payload: RequestTwoFactorPolicyChangeRequest
    ): Observable<ApiResponse<TwoFactorPolicyChangeRequestResponse>> {
        return this.#apiClient.post<
            ApiResponse<TwoFactorPolicyChangeRequestResponse>
        >('platformSettingsTwoFactorRequestChange', payload);
    }

    confirmTwoFactorPolicyChange(
        payload: ConfirmTwoFactorPolicyChangeRequest
    ): Observable<ApiResponse<PlatformSettingsResponse>> {
        return this.#apiClient.post<ApiResponse<PlatformSettingsResponse>>(
            'platformSettingsTwoFactorConfirmChange',
            payload
        );
    }
}
