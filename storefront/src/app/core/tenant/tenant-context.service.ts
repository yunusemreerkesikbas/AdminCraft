import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { ApiResponse } from '@core/crud';
import { TenantModule } from 'app/core/tenant/tenant.types';
import { Tenant } from 'app/modules/admin/custom/tenants/tenants.types';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TenantContextService {
    private readonly _httpClient = inject(HttpClient);

    private readonly storageKey = 'currentTenantSubdomain';
    private readonly storageKeyId = 'tenantId';
    private readonly storageKeySub = 'tenantSubdomain';
    private readonly sessionStorageKeySelectedTenantId = 'selectedTenantId';

    private _tenant$ = new BehaviorSubject<Tenant | null>(null);
    private _subdomain$ = new BehaviorSubject<string | null>(null);
    private _selectedTenant$ = new BehaviorSubject<Tenant | null>(null);
    private _tenantModules$ = new BehaviorSubject<string[]>([]);

    get tenant$(): Observable<Tenant | null> {
        return this._tenant$.asObservable();
    }

    get subdomain$(): Observable<string | null> {
        return this._subdomain$.asObservable();
    }

    get selectedTenant$(): Observable<Tenant | null> {
        return this._selectedTenant$.asObservable();
    }

    get tenantModules$(): Observable<string[]> {
        return this._tenantModules$.asObservable();
    }

    get currentTenant(): Tenant | null {
        return this._tenant$.getValue();
    }

    setCurrentTenant(tenant: Tenant): void {
        this._tenant$.next(tenant);
        if (tenant?.subdomain) {
            localStorage.setItem(this.storageKey, tenant.subdomain);
            // Keep backward and header compatibility
            localStorage.setItem(this.storageKeySub, tenant.subdomain);
            this._subdomain$.next(tenant.subdomain);
        }
        if (tenant?.id) {
            localStorage.setItem(this.storageKeyId, String(tenant.id));
        }
    }

    clear(): void {
        this._tenant$.next(null);
        localStorage.removeItem(this.storageKey);
        localStorage.removeItem(this.storageKeySub);
        localStorage.removeItem(this.storageKeyId);
        this._subdomain$.next(null);
    }

    getCurrentSubdomain(): string | null {
        const current = this._tenant$.getValue();
        if (current?.subdomain) {
            return current.subdomain;
        }
        return localStorage.getItem(this.storageKey);
    }

    getCurrentTenantId(): number | null {
        const current = this._tenant$.getValue();
        if (current?.id) {
            return current.id;
        }
        const fromStorage = localStorage.getItem(this.storageKeyId);
        if (fromStorage) {
            const parsed = Number(fromStorage);
            return Number.isFinite(parsed) ? parsed : null;
        }
        return null;
    }

    setSubdomain(subdomain: string): void {
        if (subdomain) {
            localStorage.setItem(this.storageKey, subdomain);
            this._subdomain$.next(subdomain);
        }
    }

    selectTenant(tenant: Tenant): void {
        this.setCurrentTenant(tenant);
        this._selectedTenant$.next(tenant);
        sessionStorage.setItem(this.sessionStorageKeySelectedTenantId, String(tenant.id));
        this.loadTenantModules(tenant.id);
    }

    private loadTenantModules(tenantId: number): void {
        this._httpClient
            .get<ApiResponse<TenantModule[]>>(`/api/tenants/${tenantId}/modules`)
            .subscribe({
                next: (response) => {
                    const moduleCodes = response.data
                        .filter((m) => m.status === 'enabled')
                        .map((m) => m.moduleCode);
                    this._tenantModules$.next(moduleCodes);
                },
                error: (error) => {
                    console.error('Failed to load tenant modules:', error);
                    this._tenantModules$.next([]);
                },
            });
    }

    clearTenantSelection(): void {
        this._selectedTenant$.next(null);
        this._tenantModules$.next([]);
        sessionStorage.removeItem(this.sessionStorageKeySelectedTenantId);
        localStorage.removeItem(this.storageKeyId);
    }

    getSelectedTenantId(): number | null {
        const savedId = sessionStorage.getItem(this.sessionStorageKeySelectedTenantId);
        if (savedId) {
            const parsed = Number(savedId);
            return Number.isFinite(parsed) ? parsed : null;
        }
        return null;
    }
}


