import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiResponse } from '@core/crud';
import { NavigationService } from 'app/core/navigation/navigation.service';
import { TenantModule } from 'app/core/tenant/tenant.types';
import { Tenant } from 'app/modules/admin/custom/tenants/tenants.types';
import { BehaviorSubject, catchError, Observable, of, take } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TenantContextService {
    private readonly _httpClient = inject(HttpClient);
    private readonly _navigationService = inject(NavigationService);
    #snackBar = inject(MatSnackBar);

    private readonly STORAGE_KEYS = {
        subdomain: 'currentTenantSubdomain',
        tenantId: 'tenantId',
        selectedTenantId: 'selectedTenantId'
    } as const;

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
            localStorage.setItem(this.STORAGE_KEYS.subdomain, tenant.subdomain);
            this._subdomain$.next(tenant.subdomain);
        }
        if (tenant?.id) {
            localStorage.setItem(this.STORAGE_KEYS.tenantId, String(tenant.id));
        }
    }

    clear(): void {
        this._tenant$.next(null);
        localStorage.removeItem(this.STORAGE_KEYS.subdomain);
        localStorage.removeItem(this.STORAGE_KEYS.tenantId);
        this._subdomain$.next(null);
    }

    getCurrentSubdomain(): string | null {
        const current = this._tenant$.getValue();
        if (current?.subdomain) {
            return current.subdomain;
        }
        return localStorage.getItem(this.STORAGE_KEYS.subdomain);
    }

    getCurrentTenantId(): number | null {
        const current = this._tenant$.getValue();
        if (current?.id) {
            return current.id;
        }
        const fromStorage = localStorage.getItem(this.STORAGE_KEYS.tenantId);
        if (fromStorage) {
            const parsed = Number(fromStorage);
            return Number.isFinite(parsed) ? parsed : null;
        }
        return null;
    }

    setSubdomain(subdomain: string): void {
        if (subdomain) {
            localStorage.setItem(this.STORAGE_KEYS.subdomain, subdomain);
            this._subdomain$.next(subdomain);
        }
    }

    selectTenant(tenant: Tenant): void {
        this.setCurrentTenant(tenant);
        this._selectedTenant$.next(tenant);
        sessionStorage.setItem(this.STORAGE_KEYS.selectedTenantId, String(tenant.id));
        this.loadTenantModules(tenant.id);
    }

    private loadTenantModules(tenantId: number): void {
        this._httpClient
            .get<ApiResponse<TenantModule[]>>(`/api/tenants/${tenantId}/modules`)
            .pipe(
                take(1),
                catchError(() => {
                    this.#snackBar.open('Failed to load tenant modules', 'Close', { duration: 3000 });
                    return of({ data: [], result: 'ERROR' } as ApiResponse<TenantModule[]>);
                })
            )
            .subscribe((response) => {
                const moduleCodes = response.data
                    .filter((m) => m.status === 'enabled')
                    .map((m) => m.moduleCode);
                this._tenantModules$.next(moduleCodes);

                // Reload navigation to apply module-based filtering
                this._navigationService.reload();
            });
    }

    clearTenantSelection(): void {
        this._selectedTenant$.next(null);
        this._tenantModules$.next([]);
        sessionStorage.removeItem(this.STORAGE_KEYS.selectedTenantId);
        localStorage.removeItem(this.STORAGE_KEYS.tenantId);

        // Reload navigation to show all modules
        this._navigationService.reload();
    }

    getSelectedTenantId(): number | null {
        const savedId = sessionStorage.getItem(this.STORAGE_KEYS.selectedTenantId);
        if (savedId) {
            const parsed = Number(savedId);
            return Number.isFinite(parsed) ? parsed : null;
        }
        return null;
    }

    extractSubdomainFromHost(): string | null {
        const hostname = window.location.hostname;
        if (hostname === 'localhost') {
            this.redirectToAdminLocalhost();
            return 'admin';
        }
        const parts = hostname.split('.');
        const subdomain = parts[0];
        if (subdomain === 'admin') {
            return 'admin';
        }
        if (!this.isValidSubdomain(subdomain)) {
            this.#snackBar.open(
                'Invalid tenant subdomain. Please contact your administrator.',
                'Close',
                { duration: 5000 }
            );
            return null;
        }

        return subdomain;
    }

    isValidSubdomain(subdomain: string): boolean {
        if (!subdomain) return false;
        const pattern = /^[a-z0-9-]{1,50}$/;
        return pattern.test(subdomain);
    }

    private redirectToAdminLocalhost(): void {
        const currentUrl = window.location.href;
        const newUrl = currentUrl.replace('localhost', 'admin.localhost');
        if (currentUrl !== newUrl) {
            window.location.replace(newUrl);
        }
    }

    initializeFromHostname(): void {
        const subdomain = this.extractSubdomainFromHost();
        if (subdomain) {
            // Store subdomain even for 'admin' for Sprint 17 authentication
            this.setSubdomain(subdomain);

            // Clear tenant selection for admin subdomain (platform access)
            if (subdomain === 'admin') {
                // Keep subdomain but clear tenant-specific data
                this._tenant$.next(null);
                localStorage.removeItem(this.STORAGE_KEYS.tenantId);
            }
        }
    }
}


