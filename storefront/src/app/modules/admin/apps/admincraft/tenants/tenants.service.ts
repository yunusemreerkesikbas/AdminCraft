import { HttpClient, HttpHeaders } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Tenant, TenantPagination, CreateTenantRequest, UpdateTenantRequest, TenantStatus } from './tenants.types';
import { BehaviorSubject, Observable, tap, switchMap, map } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TenantsService {
    private _httpClient = inject(HttpClient);
    private readonly apiUrl = 'http://localhost:8080/api';

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
        const headers = new HttpHeaders({
            'Content-Type': 'application/json',
            'Accept-Language': 'tr',
            'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        });

        return this._httpClient.get<any>(`${this.apiUrl}/tenants`, { headers }).pipe(
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
        const headers = new HttpHeaders({
            'Content-Type': 'application/json',
            'Accept-Language': 'tr',
            'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        });

        return this._httpClient.get<any>(`${this.apiUrl}/tenants/${id}`, { headers }).pipe(
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
        const headers = new HttpHeaders({
            'Content-Type': 'application/json',
            'Accept-Language': 'tr',
            'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        });

        return this._httpClient.post<any>(`${this.apiUrl}/tenants`, tenant, { headers }).pipe(
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
        const headers = new HttpHeaders({
            'Content-Type': 'application/json',
            'Accept-Language': 'tr'
        });

        return this._httpClient.put<any>(`${this.apiUrl}/tenants/${id}`, tenant, { headers }).pipe(
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
        const headers = new HttpHeaders({
            'Content-Type': 'application/json',
            'Accept-Language': 'tr',
            'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        });

        return this._httpClient.delete<any>(`${this.apiUrl}/tenants/${id}`, { headers }).pipe(
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
        const headers = new HttpHeaders({
            'Content-Type': 'application/json',
            'Accept-Language': 'tr'
        });

        return this._httpClient.post<any>(`${this.apiUrl}/tenants/${id}/activate`, {}, { headers }).pipe(
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
        const headers = new HttpHeaders({
            'Content-Type': 'application/json',
            'Accept-Language': 'tr'
        });

        return this._httpClient.post<any>(`${this.apiUrl}/tenants/${id}/suspend`, {}, { headers }).pipe(
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
        const headers = new HttpHeaders({
            'Content-Type': 'application/json',
            'Accept-Language': 'tr'
        });

        return this._httpClient.post<any>(`${this.apiUrl}/tenants/${id}/maintenance`, {}, { headers }).pipe(
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
        const headers = new HttpHeaders({
            'Content-Type': 'application/json',
            'Accept-Language': 'tr',
            'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        });

        return this._httpClient.get<any>(`${this.apiUrl}/tenants/check/subdomain/${subdomain}`, { headers }).pipe(
            map((response) => response.result === 'SUCCESS' && response.data === true)
        );
    }
}