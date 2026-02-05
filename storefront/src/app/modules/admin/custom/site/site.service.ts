import { Injectable, inject, signal } from '@angular/core';
import { ApiClientService } from '@core/api/api-client.service';
import { TenantModule } from '@core/tenant/tenant.types';
import { ApiResponse } from '@modules/admin/custom/pages/page-builder.types';
import { Observable, map, switchMap, take, tap } from 'rxjs';
import { TenantDetailResponse } from '../tenants/tenants.types';
import {
    SecuritySettingsResponse,
    Site,
    SiteOverviewResponse,
    SiteSettingsPatchRequest,
    SiteSettingsResponseDto,
    SiteTechnicalPatchRequest,
    SiteTechnicalResponse,
    TwoFactorPolicy,
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

    // -----------------------------------------------------------------------------------------------------
    // @ Site Overview
    // -----------------------------------------------------------------------------------------------------

    /**
     * Get site overview for dashboard
     */
    getOverview(): Observable<SiteOverviewResponse> {
        this.loadingSig.set(true);
        return this.#apiClient
            .get<ApiResponse<SiteOverviewResponse>>('siteOverview')
            .pipe(
                map((response) => response.data),
                tap((data) => {
                    this.overviewSig.set(data);
                    this.loadingSig.set(false);
                })
            );
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Site Technical Settings
    // -----------------------------------------------------------------------------------------------------

    /**
     * Get technical settings
     */
    getTechnicalSettings(): Observable<SiteTechnicalResponse> {
        this.loadingSig.set(true);
        return this.#apiClient
            .get<ApiResponse<SiteTechnicalResponse>>('siteTechnical')
            .pipe(
                map((response) => response.data),
                tap((data) => {
                    this.technicalSig.set(data);
                    this.loadingSig.set(false);
                })
            );
    }

    /**
     * Update technical settings (PATCH)
     */
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
                    this.loadingSig.set(false);
                })
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
                    this.loadingSig.set(false);
                })
            );
    }

    patchSecuritySettings(
        twoFactorPolicy: TwoFactorPolicy
    ): Observable<SecuritySettingsResponse> {
        this.loadingSig.set(true);
        return this.#apiClient
            .patch<ApiResponse<SecuritySettingsResponse>>('siteSecuritySettings', {
                twoFactorPolicy,
            })
            .pipe(
                map((response) => response.data),
                tap((data) => {
                    this.securitySig.set(data);
                    this.loadingSig.set(false);
                })
            );
    }

    getTenantInfo(): Observable<TenantInfoResponse> {

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

    // -----------------------------------------------------------------------------------------------------
    // @ Site Settings (from existing service)
    // -----------------------------------------------------------------------------------------------------

    /**
     * Get site settings (general, address, social, seo)
     */
    getSiteSettings(): Observable<SiteSettingsResponseDto> {
        this.loadingSig.set(true);
        return this.#apiClient
            .get<ApiResponse<SiteSettingsResponseDto>>('siteSettings')
            .pipe(
                map((response) => response.data),
                tap((data) => {
                    this.settingsSig.set(data);
                    this.loadingSig.set(false);
                })
            );
    }

    /**
     * Update site settings (PATCH)
     */
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
                    this.loadingSig.set(false);
                })
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
    enableMaintenanceMode(id: number, message?: string): Observable<Site> {
        return this.#apiClient
            .post<ApiResponse<Site>>('siteMaintenance', {}, { id }, { message })
            .pipe(
                switchMap((response) =>
                    this.getOverview().pipe(
                        take(1),
                        map(() => response.data)
                    )
                )
            );
    }

    /**
     * Disable maintenance mode
     */
    disableMaintenanceMode(id: number): Observable<Site> {
        return this.#apiClient
            .delete<ApiResponse<Site>>('siteMaintenance', { id })
            .pipe(
                switchMap((response) =>
                    this.getOverview().pipe(
                        take(1),
                        map(() => response.data)
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
