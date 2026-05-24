import { Injectable, inject, signal } from '@angular/core';
import { ApiClientService } from '@core/api/api-client.service';
import { PageRequest } from '@core/crud/api.types';
import { TenantModule } from '@core/tenant/tenant.types';
import { ApiResponse } from '@modules/admin/custom/pages/page-builder.types';
import { Observable, finalize, map, switchMap, take, tap } from 'rxjs';
import { TenantDetailResponse } from '../tenants/tenants.types';
import {
    ConfirmTwoFactorPolicyChangeRequest,
    RequestTwoFactorPolicyChangeRequest,
    SecuritySettingsResponse,
    Site,
    SiteActivityFeedResponse,
    SiteActivityTrendResponse,
    SiteAnalyticsSummaryResponse,
    SiteInsightsSummaryResponse,
    SiteOverviewResponse,
    SiteSettingsPatchRequest,
    SiteSettingsResponseDto,
    SiteTechnicalPatchRequest,
    SiteTechnicalResponse,
    TwoFactorPolicyChangeRequestResponse,
    UpdateSecuritySettingsRequest,
} from './site.types';

@Injectable({ providedIn: 'root' })
export class SiteService {
    readonly #apiClient = inject(ApiClientService);

    readonly overviewSig = signal<SiteOverviewResponse | null>(null);
    readonly technicalSig = signal<SiteTechnicalResponse | null>(null);
    readonly settingsSig = signal<SiteSettingsResponseDto | null>(null);
    readonly securitySig = signal<SecuritySettingsResponse | null>(null);
    readonly tenantSig = signal<TenantDetailResponse | null>(null);
    readonly modulesSig = signal<TenantModule[]>([]);
    readonly loadingSig = signal<boolean>(false);

    getOverview(): Observable<SiteOverviewResponse> {
        this.loadingSig.set(true);
        return this.#apiClient
            .get<ApiResponse<SiteOverviewResponse>>('siteOverview')
            .pipe(
                map((response) => response.data),
                tap((data) => {
                    this.overviewSig.set(data);
                }),
                finalize(() => this.loadingSig.set(false))
            );
    }

    getAnalyticsSummary(): Observable<SiteAnalyticsSummaryResponse> {
        return this.#apiClient
            .get<
                ApiResponse<SiteAnalyticsSummaryResponse>
            >('siteAnalyticsSummary')
            .pipe(map((response) => response.data));
    }

    getInsightsSummary(): Observable<SiteInsightsSummaryResponse> {
        return this.#apiClient
            .get<
                ApiResponse<SiteInsightsSummaryResponse>
            >('siteInsightsSummary')
            .pipe(map((response) => response.data));
    }

    getActivityTrend(
        pageRequest: PageRequest = {}
    ): Observable<SiteActivityTrendResponse> {
        return this.#apiClient
            .get<ApiResponse<SiteActivityTrendResponse>>(
                'siteActivityTrend',
                undefined,
                {
                    page: pageRequest.page ?? 0,
                    size: pageRequest.size ?? 7,
                    sort: pageRequest.sort ?? 'date,desc',
                }
            )
            .pipe(map((response) => response.data));
    }

    getActivityFeed(
        pageRequest: PageRequest = {}
    ): Observable<SiteActivityFeedResponse> {
        return this.#apiClient
            .get<ApiResponse<SiteActivityFeedResponse>>(
                'siteActivity',
                undefined,
                {
                    page: pageRequest.page ?? 0,
                    size: pageRequest.size ?? 10,
                    sort: pageRequest.sort ?? 'createdAt,desc',
                }
            )
            .pipe(map((response) => response.data));
    }

    getTechnicalSettings(): Observable<SiteTechnicalResponse> {
        this.loadingSig.set(true);
        return this.#apiClient
            .get<ApiResponse<SiteTechnicalResponse>>('siteTechnical')
            .pipe(
                map((response) => response.data),
                tap((data) => {
                    this.technicalSig.set(data);
                }),
                finalize(() => this.loadingSig.set(false))
            );
    }

    patchTechnicalSettings(
        payload: SiteTechnicalPatchRequest
    ): Observable<SiteTechnicalResponse> {
        this.loadingSig.set(true);
        return this.#apiClient
            .patch<ApiResponse<SiteTechnicalResponse>>('siteTechnical', payload)
            .pipe(
                map((response) => response.data),
                tap((data) => {
                    this.technicalSig.set(data);
                }),
                finalize(() => this.loadingSig.set(false))
            );
    }

    getSecuritySettings(): Observable<SecuritySettingsResponse> {
        this.loadingSig.set(true);
        return this.#apiClient
            .get<ApiResponse<SecuritySettingsResponse>>('siteSecuritySettings')
            .pipe(
                map((response) => response.data),
                tap((data) => {
                    this.securitySig.set(data);
                }),
                finalize(() => this.loadingSig.set(false))
            );
    }

    patchSecuritySettings(
        payload: UpdateSecuritySettingsRequest
    ): Observable<SecuritySettingsResponse> {
        return this.patchSecuritySettingsWithResponse(payload).pipe(
            map((response) => response.data)
        );
    }

    patchSecuritySettingsWithResponse(
        payload: UpdateSecuritySettingsRequest
    ): Observable<ApiResponse<SecuritySettingsResponse>> {
        this.loadingSig.set(true);
        return this.#apiClient
            .patch<
                ApiResponse<SecuritySettingsResponse>
            >('siteSecuritySettings', payload)
            .pipe(
                tap((response) => {
                    this.securitySig.set(response.data);
                }),
                finalize(() => this.loadingSig.set(false))
            );
    }

    requestTwoFactorPolicyChange(
        payload: RequestTwoFactorPolicyChangeRequest
    ): Observable<ApiResponse<TwoFactorPolicyChangeRequestResponse>> {
        return this.#apiClient.post<
            ApiResponse<TwoFactorPolicyChangeRequestResponse>
        >('siteSecurityTwoFactorRequestChange', payload);
    }

    confirmTwoFactorPolicyChange(
        payload: ConfirmTwoFactorPolicyChangeRequest
    ): Observable<ApiResponse<SecuritySettingsResponse>> {
        return this.#apiClient.post<ApiResponse<SecuritySettingsResponse>>(
            'siteSecurityTwoFactorConfirmChange',
            payload
        );
    }

    getTenantDetail(): Observable<TenantDetailResponse> {
        return this.#apiClient
            .get<ApiResponse<TenantDetailResponse>>('tenantCurrentDetail')
            .pipe(
                map((response) => response.data),
                tap((data) => this.tenantSig.set(data))
            );
    }

    getTenantModules(): Observable<TenantModule[]> {
        return this.#apiClient
            .get<ApiResponse<TenantModule[]>>('tenantCurrentModules')
            .pipe(
                map((response) => response.data || []),
                tap((data) => this.modulesSig.set(data))
            );
    }

    getSiteSettings(): Observable<SiteSettingsResponseDto> {
        this.loadingSig.set(true);
        return this.#apiClient
            .get<ApiResponse<SiteSettingsResponseDto>>('siteSettings')
            .pipe(
                map((response) => response.data),
                tap((data) => {
                    this.settingsSig.set(data);
                }),
                finalize(() => this.loadingSig.set(false))
            );
    }

    patchSiteSettings(
        payload: SiteSettingsPatchRequest
    ): Observable<SiteSettingsResponseDto> {
        this.loadingSig.set(true);
        return this.#apiClient
            .custom<ApiResponse<SiteSettingsResponseDto>>(
                'PATCH',
                'siteSettings',
                {
                    body: payload,
                }
            )
            .pipe(
                map((response) => response.data),
                tap((data) => {
                    this.settingsSig.set(data);
                }),
                finalize(() => this.loadingSig.set(false))
            );
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Site CRUD (delegated from sites service)
    // -----------------------------------------------------------------------------------------------------

    /**
     * Get sites list
     */
    getSites(): Observable<Site[]> {
        return this.#apiClient
            .get<ApiResponse<Site[]>>('sites')
            .pipe(map((response) => response.data || []));
    }

    /**
     * Get site by ID
     */
    getSiteById(id: number): Observable<Site> {
        return this.#apiClient
            .get<ApiResponse<Site>>('siteById', { id })
            .pipe(map((response) => response.data));
    }

    /**
     * Enable maintenance mode
     */
    enableMaintenanceMode(
        id: number,
        message?: string
    ): Observable<ApiResponse<Site>> {
        return this.#apiClient
            .post<ApiResponse<Site>>('siteMaintenance', {}, { id }, { message })
            .pipe(
                switchMap((response) =>
                    this.getOverview().pipe(
                        take(1),
                        map(() => response)
                    )
                )
            );
    }

    /**
     * Disable maintenance mode
     */
    disableMaintenanceMode(id: number): Observable<ApiResponse<Site>> {
        return this.#apiClient
            .delete<ApiResponse<Site>>('siteMaintenance', { id })
            .pipe(
                switchMap((response) =>
                    this.getOverview().pipe(
                        take(1),
                        map(() => response)
                    )
                )
            );
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Reset State
    // -----------------------------------------------------------------------------------------------------

    /**
     * Reset all cached state
     */
    resetState(): void {
        this.overviewSig.set(null);
        this.technicalSig.set(null);
        this.settingsSig.set(null);
        this.securitySig.set(null);
        this.tenantSig.set(null);
        this.modulesSig.set([]);
        this.loadingSig.set(false);
    }
}
