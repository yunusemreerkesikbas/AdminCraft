import { inject, Injectable } from '@angular/core';
import {
  Tenant,
  TenantPagination,
  CreateTenantRequest,
  UpdateTenantRequest,
  TenantStatus,
  TenantLanguagesDto,
  UpdateTenantLanguagesRequest,
  ProvisionLanguagesRequest,
  ProvisioningJobDto
} from './tenants.types';
import { BehaviorSubject, Observable, tap, switchMap, map, interval, takeWhile } from 'rxjs';
import { ApiClientService } from '@core/api/api-client.service';
import { EndpointKey } from '@modules/admin/api-endpoints';

@Injectable({ providedIn: 'root' })
export class TenantsService {
    private readonly _apiClient = inject(ApiClientService);

    private _tenants: BehaviorSubject<Tenant[]> = new BehaviorSubject<Tenant[]>([]);
    private _pagination: BehaviorSubject<TenantPagination | null> = new BehaviorSubject<TenantPagination | null>(null);

    // -----------------------------------------------------------------------------------------------------
    // @ Accessors
    // -----------------------------------------------------------------------------------------------------

    /**
     * Getter for tenants
     */
    get tenants$(): Observable<Tenant[]> {
        return this._tenants.asObservable();
    }

    /**
     * Getter for pagination
     */
    get pagination$(): Observable<TenantPagination | null> {
        return this._pagination.asObservable();
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Public methods
    // -----------------------------------------------------------------------------------------------------

    /**
     * Get tenants (no pagination from backend yet, we'll simulate it client-side)
     */
    getTenants(page: number = 0, size: number = 10, sort: string = 'companyName', order: 'asc' | 'desc' = 'asc', search: string = ''): Observable<{ pagination: TenantPagination; tenants: Tenant[] }> {
        return this._apiClient.get<any>('tenants').pipe(
            map((response) => {
                let tenants: Tenant[] = [];
                
                if (response.result === 'SUCCESS' && response.data) {
                    tenants = response.data.map((item: any) => ({
                        id: item.id,
                        subdomain: item.subdomain,
                        companyName: item.companyName,
                        databaseName: item.databaseName,
                        status: item.status,
                        defaultLanguage: item.defaultLanguage,
                        supportedLanguages: item.supportedLanguages || [item.defaultLanguage],
                        adminEmail: item.adminEmail,
                        adminName: item.adminName,
                        phone: item.phone,
                        customDomain: item.customDomain,
                        timezone: item.timezone,
                        currency: item.currency,
                        sslEnabled: item.sslEnabled,
                        notes: item.notes,
                        createdAt: item.createdAt,
                        updatedAt: item.updatedAt,
                        activatedAt: item.activatedAt,
                        suspendedAt: null
                    }));

                    // Client-side filtering and search
                    if (search) {
                        const searchLower = search.toLowerCase();
                        tenants = tenants.filter(tenant => 
                            tenant.companyName.toLowerCase().includes(searchLower) ||
                            tenant.subdomain.toLowerCase().includes(searchLower) ||
                            tenant.adminEmail.toLowerCase().includes(searchLower)
                        );
                    }

                    // Client-side sorting
                    tenants.sort((a, b) => {
                        const aValue = a[sort] || '';
                        const bValue = b[sort] || '';
                        const comparison = aValue.toString().localeCompare(bValue.toString());
                        return order === 'desc' ? -comparison : comparison;
                    });
                }

                // Client-side pagination
                const totalLength = tenants.length;
                const startIndex = page * size;
                const endIndex = Math.min(startIndex + size, totalLength);
                const paginatedTenants = tenants.slice(startIndex, endIndex);

                const pagination: TenantPagination = {
                    length: totalLength,
                    size: size,
                    page: page,
                    lastPage: Math.ceil(totalLength / size) - 1,
                    startIndex: startIndex,
                    endIndex: endIndex - 1
                };

                // Update internal state
                this._tenants.next(paginatedTenants);
                this._pagination.next(pagination);

                return { pagination, tenants: paginatedTenants };
            })
        );
    }

    /**
     * Get tenant by id
     */
    getTenantById(id: number): Observable<Tenant> {
        return this._apiClient.get<any>('tenantById', { id }).pipe(
            map((response) => {
                if (response.result === 'SUCCESS' && response.data) {
                    const item = response.data;
                    return {
                        id: item.id,
                        subdomain: item.subdomain,
                        companyName: item.companyName,
                        databaseName: item.databaseName,
                        status: item.status,
                        defaultLanguage: item.defaultLanguage,
                        supportedLanguages: item.supportedLanguages || [item.defaultLanguage],
                        adminEmail: item.adminEmail,
                        adminName: item.adminName,
                        phone: item.phone,
                        customDomain: item.customDomain,
                        timezone: item.timezone,
                        currency: item.currency,
                        sslEnabled: item.sslEnabled,
                        notes: item.notes,
                        createdAt: item.createdAt,
                        updatedAt: item.updatedAt,
                        activatedAt: item.activatedAt,
                        suspendedAt: null
                    };
                }
                throw new Error('Tenant not found');
            })
        );
    }

    /**
     * Create tenant
     */
    createTenant(tenant: CreateTenantRequest): Observable<Tenant> {
        return this._apiClient.post<any>('tenants', tenant).pipe(
            map((response) => {
                if (response.result === 'SUCCESS' && response.data) {
                    const item = response.data;
                    return {
                        id: item.id,
                        subdomain: item.subdomain,
                        companyName: item.companyName,
                        databaseName: item.databaseName,
                        status: item.status,
                        defaultLanguage: item.defaultLanguage,
                        supportedLanguages: item.supportedLanguages || [item.defaultLanguage],
                        adminEmail: item.adminEmail,
                        adminName: item.adminName,
                        phone: item.phone,
                        customDomain: item.customDomain,
                        timezone: item.timezone,
                        currency: item.currency,
                        sslEnabled: item.sslEnabled,
                        notes: item.notes,
                        createdAt: item.createdAt,
                        updatedAt: item.updatedAt,
                        activatedAt: item.activatedAt,
                        suspendedAt: null
                    };
                }
                throw new Error('Failed to create tenant');
            }),
            tap(() => {
                // Refresh the tenants list
                this.getTenants().subscribe();
            })
        );
    }

    /**
     * Update tenant
     */
    updateTenant(id: number, tenant: UpdateTenantRequest): Observable<Tenant> {
        return this._apiClient.put<any>('tenantById', tenant, { id }).pipe(
            map((response) => response.data),
            tap(() => {
                // Refresh the tenants list
                this.getTenants().subscribe();
            })
        );
    }

    /**
     * Delete tenant
     */
    deleteTenant(id: number): Observable<boolean> {
        return this._apiClient.delete<any>('tenantById', { id }).pipe(
            map((response) => response.result === 'SUCCESS'),
            tap(() => {
                // Refresh the tenants list
                this.getTenants().subscribe();
            })
        );
    }

    /**
     * Activate tenant
     */
    activateTenant(id: number): Observable<Tenant> {
        return this._apiClient.post<any>('tenantActivate', {}, { id }).pipe(
            map((response) => response.data),
            tap(() => {
                // Refresh the tenants list
                this.getTenants().subscribe();
            })
        );
    }

    /**
     * Suspend tenant
     */
    suspendTenant(id: number): Observable<Tenant> {
        return this._apiClient.post<any>('tenantSuspend', {}, { id }).pipe(
            map((response) => response.data),
            tap(() => {
                // Refresh the tenants list
                this.getTenants().subscribe();
            })
        );
    }

    /**
     * Set maintenance mode for tenant
     */
    setMaintenanceMode(id: number): Observable<Tenant> {
        return this._apiClient.post<any>('tenantMaintenance', {}, { id }).pipe(
            map((response) => response.data),
            tap(() => {
                // Refresh the tenants list
                this.getTenants().subscribe();
            })
        );
    }

    /**
     * Check subdomain availability
     */
    checkSubdomainAvailability(subdomain: string): Observable<boolean> {
        return this._apiClient.get<any>('tenantCheckSubdomain', { subdomain }).pipe(
            map((response) => response.result === 'SUCCESS' && response.data === true)
        );
    }

    getTenantLanguages(tenantId: number): Observable<TenantLanguagesDto> {
        return this._apiClient.get<any>('tenantLanguages', { tenantId }).pipe(
            map((response) => response.data)
        );
    }

    updateTenantLanguages(tenantId: number, req: UpdateTenantLanguagesRequest): Observable<TenantLanguagesDto> {
        return this._apiClient.put<any>('tenantLanguages', req, { tenantId }).pipe(
            map((response) => response.data)
        );
    }

    provisionLanguages(tenantId: number, req: ProvisionLanguagesRequest): Observable<ProvisioningJobDto> {
        return this._apiClient.post<any>('tenantLanguagesProvision', req, { tenantId }).pipe(
            map((response) => response.data)
        );
    }

    getProvisioningJob(jobUuid: string): Observable<ProvisioningJobDto> {
        return this._apiClient.get<any>('provisioningJob', { jobUuid }).pipe(
            map((response) => response.data)
        );
    }

    pollProvisioningJob(jobUuid: string, intervalMs: number = 2000): Observable<ProvisioningJobDto> {
        return interval(intervalMs).pipe(
            switchMap(() => this.getProvisioningJob(jobUuid)),
            takeWhile((job) => job.status === 'PENDING' || job.status === 'RUNNING', true)
        );
    }
}