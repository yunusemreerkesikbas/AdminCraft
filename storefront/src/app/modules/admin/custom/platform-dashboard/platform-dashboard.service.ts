import { Injectable, inject } from '@angular/core';
import { ApiClientService } from '@core/api/api-client.service';
import { Observable, map } from 'rxjs';
import { PlatformDashboardResponse } from './platform-dashboard.types';

interface ApiResponse<T> {
    result: string;
    message: string;
    data: T;
}

@Injectable({ providedIn: 'root' })
export class PlatformDashboardService {
    readonly #apiClient = inject(ApiClientService);

    getDashboard(): Observable<PlatformDashboardResponse> {
        return this.#apiClient
            .get<ApiResponse<PlatformDashboardResponse>>('platformDashboard')
            .pipe(map((response) => response.data));
    }
}
