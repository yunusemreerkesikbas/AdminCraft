import { HttpClient, HttpHeaders } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Site, SitePagination, CreateSiteRequest, UpdateSiteRequest, Menu, Language } from './sites.types';
import { BehaviorSubject, Observable, tap, switchMap, map } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SitesService {
    private _httpClient = inject(HttpClient);
    private readonly apiUrl = 'http://localhost:8080/api';

    private _sites: BehaviorSubject<Site[]> = new BehaviorSubject<Site[]>([]);
    private _pagination: BehaviorSubject<SitePagination | null> = new BehaviorSubject<SitePagination | null>(null);
    private _menus: BehaviorSubject<Menu[]> = new BehaviorSubject<Menu[]>([]);

    // -----------------------------------------------------------------------------------------------------
    // @ Accessors
    // -----------------------------------------------------------------------------------------------------

    /**
     * Getter for sites
     */
    get sites$(): Observable<Site[]> {
        return this._sites.asObservable();
    }

    /**
     * Getter for pagination
     */
    get pagination$(): Observable<SitePagination | null> {
        return this._pagination.asObservable();
    }

    /**
     * Getter for menus
     */
    get menus$(): Observable<Menu[]> {
        return this._menus.asObservable();
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Public methods
    // -----------------------------------------------------------------------------------------------------

    /**
     * Get sites
     */
    getSites(page: number = 0, size: number = 10, sort: string = 'siteName', order: 'asc' | 'desc' = 'asc', search: string = ''): Observable<{ pagination: SitePagination; sites: Site[] }> {
        const headers = new HttpHeaders({
            'Content-Type': 'application/json',
            'Accept-Language': 'tr',
            'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        });

        return this._httpClient.get<any>(`${this.apiUrl}/sites`, { headers }).pipe(
            map((response) => {
                let sites: Site[] = [];
                
                if (response.result === 'SUCCESS' && response.data) {
                    sites = response.data.map((item: any) => ({
                        id: item.id,
                        siteName: item.siteName,
                        description: item.description,
                        enabledLanguages: item.enabledLanguages || [item.defaultLanguage],
                        defaultLanguage: item.defaultLanguage,
                        tenantId: item.tenantId,
                        tenantName: item.tenantName,
                        domain: item.domain,
                        isActive: item.isActive,
                        theme: item.theme,
                        logoUrl: item.logoUrl,
                        faviconUrl: item.faviconUrl,
                        createdAt: item.createdAt,
                        updatedAt: item.updatedAt,
                        publishedAt: item.publishedAt
                    }));

                    // Client-side filtering and search
                    if (search) {
                        const searchLower = search.toLowerCase();
                        sites = sites.filter(site => 
                            site.siteName.toLowerCase().includes(searchLower) ||
                            (site.domain && site.domain.toLowerCase().includes(searchLower)) ||
                            (site.tenantName && site.tenantName.toLowerCase().includes(searchLower))
                        );
                    }

                    // Client-side sorting
                    sites.sort((a, b) => {
                        const aValue = a[sort] || '';
                        const bValue = b[sort] || '';
                        const comparison = aValue.toString().localeCompare(bValue.toString());
                        return order === 'desc' ? -comparison : comparison;
                    });
                }

                // Client-side pagination
                const totalLength = sites.length;
                const startIndex = page * size;
                const endIndex = Math.min(startIndex + size, totalLength);
                const paginatedSites = sites.slice(startIndex, endIndex);

                const pagination: SitePagination = {
                    length: totalLength,
                    size: size,
                    page: page,
                    lastPage: Math.ceil(totalLength / size) - 1,
                    startIndex: startIndex,
                    endIndex: endIndex - 1
                };

                // Update internal state
                this._sites.next(paginatedSites);
                this._pagination.next(pagination);

                return { pagination, sites: paginatedSites };
            })
        );
    }

    /**
     * Get site by id
     */
    getSiteById(id: number): Observable<Site> {
        const headers = new HttpHeaders({
            'Content-Type': 'application/json',
            'Accept-Language': 'tr',
            'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        });

        return this._httpClient.get<any>(`${this.apiUrl}/sites/${id}`, { headers }).pipe(
            map((response) => {
                if (response.result === 'SUCCESS' && response.data) {
                    const item = response.data;
                    return {
                        id: item.id,
                        siteName: item.siteName,
                        description: item.description,
                        enabledLanguages: item.enabledLanguages || [item.defaultLanguage],
                        defaultLanguage: item.defaultLanguage,
                        tenantId: item.tenantId,
                        tenantName: item.tenantName,
                        domain: item.domain,
                        isActive: item.isActive,
                        theme: item.theme,
                        logoUrl: item.logoUrl,
                        faviconUrl: item.faviconUrl,
                        createdAt: item.createdAt,
                        updatedAt: item.updatedAt,
                        publishedAt: item.publishedAt
                    };
                }
                throw new Error('Site not found');
            })
        );
    }

    /**
     * Create site
     */
    createSite(site: CreateSiteRequest): Observable<Site> {
        const headers = new HttpHeaders({
            'Content-Type': 'application/json',
            'Accept-Language': 'tr',
            'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        });

        return this._httpClient.post<any>(`${this.apiUrl}/sites`, site, { headers }).pipe(
            map((response) => {
                if (response.result === 'SUCCESS' && response.data) {
                    return response.data;
                }
                throw new Error('Failed to create site');
            }),
            tap(() => {
                // Refresh the sites list
                this.getSites().subscribe();
            })
        );
    }

    /**
     * Update site
     */
    updateSite(id: number, site: UpdateSiteRequest): Observable<Site> {
        const headers = new HttpHeaders({
            'Content-Type': 'application/json',
            'Accept-Language': 'tr',
            'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        });

        return this._httpClient.put<any>(`${this.apiUrl}/sites/${id}`, site, { headers }).pipe(
            map((response) => response.data),
            tap(() => {
                // Refresh the sites list
                this.getSites().subscribe();
            })
        );
    }

    /**
     * Delete site
     */
    deleteSite(id: number): Observable<boolean> {
        const headers = new HttpHeaders({
            'Content-Type': 'application/json',
            'Accept-Language': 'tr',
            'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        });

        return this._httpClient.delete<any>(`${this.apiUrl}/sites/${id}`, { headers }).pipe(
            map((response) => response.result === 'SUCCESS'),
            tap(() => {
                // Refresh the sites list
                this.getSites().subscribe();
            })
        );
    }

    /**
     * Publish site
     */
    publishSite(id: number): Observable<Site> {
        const headers = new HttpHeaders({
            'Content-Type': 'application/json',
            'Accept-Language': 'tr',
            'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        });

        return this._httpClient.post<any>(`${this.apiUrl}/sites/${id}/publish`, {}, { headers }).pipe(
            map((response) => response.data),
            tap(() => {
                // Refresh the sites list
                this.getSites().subscribe();
            })
        );
    }

    /**
     * Activate site
     */
    activateSite(id: number): Observable<Site> {
        const headers = new HttpHeaders({
            'Content-Type': 'application/json',
            'Accept-Language': 'tr',
            'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        });

        return this._httpClient.post<any>(`${this.apiUrl}/sites/${id}/activate`, {}, { headers }).pipe(
            map((response) => response.data),
            tap(() => {
                // Refresh the sites list
                this.getSites().subscribe();
            })
        );
    }

    /**
     * Deactivate site
     */
    deactivateSite(id: number): Observable<Site> {
        const headers = new HttpHeaders({
            'Content-Type': 'application/json',
            'Accept-Language': 'tr',
            'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        });

        return this._httpClient.post<any>(`${this.apiUrl}/sites/${id}/deactivate`, {}, { headers }).pipe(
            map((response) => response.data),
            tap(() => {
                // Refresh the sites list
                this.getSites().subscribe();
            })
        );
    }

    /**
     * Get menus for site
     */
    getMenusBySite(siteId: number): Observable<Menu[]> {
        const headers = new HttpHeaders({
            'Content-Type': 'application/json',
            'Accept-Language': 'tr',
            'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        });

        return this._httpClient.get<any>(`${this.apiUrl}/sites/${siteId}/menus`, { headers }).pipe(
            map((response) => {
                if (response.result === 'SUCCESS' && response.data) {
                    const menus = response.data.map((item: any) => ({
                        id: item.id,
                        name: item.name,
                        language: item.language,
                        tenantId: item.tenantId,
                        siteId: item.siteId,
                        items: item.items || [],
                        createdAt: item.createdAt,
                        updatedAt: item.updatedAt
                    }));
                    
                    this._menus.next(menus);
                    return menus;
                }
                return [];
            })
        );
    }
}