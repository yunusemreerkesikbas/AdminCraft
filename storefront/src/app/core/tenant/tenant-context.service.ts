import { inject, Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiClientService } from '@core/api/api-client.service';
import { ApiResponse } from '@core/crud';
import { NavigationService } from 'app/core/navigation/navigation.service';
import { TenantModule } from 'app/core/tenant/tenant.types';
import { UserService } from 'app/core/user/user.service';
import { User } from 'app/core/user/user.types';
import { Tenant } from 'app/modules/admin/custom/tenants/tenants.types';
import { BehaviorSubject, catchError, map, Observable, of, take } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TenantContextService {
    private readonly _apiClient = inject(ApiClientService);
    private readonly _navigationService = inject(NavigationService);
    private readonly _userService = inject(UserService);
    #snackBar = inject(MatSnackBar);

    private readonly STORAGE_KEYS = {
        subdomain: 'currentTenantSubdomain',
        tenantId: 'superAdminSelectedTenantId',
        selectedTenantId: 'superAdminSelectedTenantId'
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
        const fromStorage = localStorage.getItem(this.STORAGE_KEYS.tenantId); // Changed from sessionStorage
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
        localStorage.setItem(this.STORAGE_KEYS.selectedTenantId, String(tenant.id));
        this.#loadTenantModules(tenant.id);
    }

    #loadTenantModules(tenantId: number): void {
        this._apiClient
            .get<ApiResponse<TenantModule[]>>('tenantModules', { tenantId })
            .pipe(
                take(1),
                catchError(() => {
                    this.#snackBar.open('Failed to load tenant modules', 'Close', { duration: 3000 });
                    return of({ data: [], result: 'ERROR' } as ApiResponse<TenantModule[]>);
                })
            )
            .subscribe((response: ApiResponse<TenantModule[]>) => {
                const moduleCodes = response.data
                    .filter((m: TenantModule) => m.status === 'enabled')
                    .map((m: TenantModule) => m.moduleCode);
                this._tenantModules$.next(moduleCodes);
                this._userService.setTenantModules(moduleCodes); 
                setTimeout(() => this._navigationService.reload(), 0);
            });
    }

    protected loadCurrentUserTenantModules(): Observable<string[]> {
        return this._apiClient
            .get<ApiResponse<TenantModule[]>>('tenantCurrentModules')
            .pipe(
                take(1),
                map((response: ApiResponse<TenantModule[]>) => {
                    const moduleCodes = response.data
                        .filter((m: TenantModule) => m.status === 'enabled')
                        .map((m: TenantModule) => m.moduleCode);
                    this._tenantModules$.next(moduleCodes);
                    this._userService.setTenantModules(moduleCodes);
                    setTimeout(() => this._navigationService.reload(), 0);
                    return moduleCodes;
                }),
                catchError((error) => {
                    console.error('Failed to load current user tenant modules:', error);
                    this.#snackBar.open('Failed to load tenant modules', 'Close', { duration: 3000 });
                    return of([]);
                })
            );
    }

    initializeTenantContext(user: User): Observable<any> {
        if (user.role === 'SUPER_ADMIN') {
            return this.restoreTenantSelection();
        } else if (user.role === 'TENANT_ADMIN' && user.tenantId) {
            return this.loadCurrentUserTenantModules();
        }
        return of(null);
    }

    clearTenantSelection(): void {
        this._selectedTenant$.next(null);
        this._tenantModules$.next([]);
        this._userService.setTenantModules([]); // Sync with UserService
        localStorage.removeItem(this.STORAGE_KEYS.selectedTenantId);
        localStorage.removeItem(this.STORAGE_KEYS.tenantId);
        this._navigationService.reload();
    }

    getSelectedTenantId(): number | null {
        const savedId = localStorage.getItem(this.STORAGE_KEYS.selectedTenantId); // Changed from sessionStorage
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
            this.setSubdomain(subdomain);

            if (subdomain === 'admin') {
                this._tenant$.next(null);
                localStorage.removeItem(this.STORAGE_KEYS.tenantId); // Changed from sessionStorage
            }
        }
    }

    restoreTenantSelection(): Observable<Tenant | null> {
        const tenantId = this.getSelectedTenantId();
        if (!tenantId) {
            return of(null);
        }

        return this._apiClient.get<ApiResponse<Tenant>>('tenantById', { id: tenantId }).pipe(
            take(1),
            map((response: ApiResponse<Tenant>) => {
                if (response.data) {
                    this.selectTenant(response.data);
                    return response.data;
                }
                return null;
            }),
            catchError((error) => {
                console.error('Failed to restore tenant selection:', error);
                this.clearTenantSelection();
                return of(null);
            })
        );
    }
}


