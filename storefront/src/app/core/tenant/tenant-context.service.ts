import { NavigationService } from '@/app/core/navigation/navigation.service';
import { inject, Injectable, signal, Signal } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { ApiClientService } from '@core/api/api-client.service';
import { ApiResponse } from '@core/crud';
import { SUPER_ADMIN_ROLE } from '@shared/constants';
import { NotificationService } from '@shared/notifications/notification.service';
import { TenantModule } from 'app/core/tenant/tenant.types';
import { UserService } from 'app/core/user/user.service';
import { User } from 'app/core/user/user.types';
import { Tenant } from 'app/modules/admin/custom/tenants/tenants.types';
import {
    catchError,
    forkJoin,
    map,
    Observable,
    of,
    switchMap,
    take,
    timer,
} from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TenantContextService {
    #apiClient = inject(ApiClientService);
    #navigationService = inject(NavigationService);
    #userService = inject(UserService);
    #notify = inject(NotificationService);

    #STORAGE_KEYS = {
        subdomain: 'currentTenantSubdomain',
        tenantId: 'superAdminSelectedTenantId',
        selectedTenantId: 'superAdminSelectedTenantId',
    } as const;

    #tenantSig = signal<Tenant | null>(null);
    #subdomainSig = signal<string | null>(null);
    #selectedTenantSig = signal<Tenant | null>(null);
    #tenantModulesSig = signal<string[]>([]);

    tenant: Signal<Tenant | null> = this.#tenantSig.asReadonly();
    subdomain: Signal<string | null> = this.#subdomainSig.asReadonly();
    selectedTenant: Signal<Tenant | null> =
        this.#selectedTenantSig.asReadonly();
    tenantModules: Signal<string[]> = this.#tenantModulesSig.asReadonly();

    tenant$ = toObservable(this.#tenantSig);
    subdomain$ = toObservable(this.#subdomainSig);
    selectedTenant$ = toObservable(this.#selectedTenantSig);
    tenantModules$ = toObservable(this.#tenantModulesSig);

    setCurrentTenant(tenant: Tenant): void {
        this.#tenantSig.set(tenant);
        if (tenant?.subdomain) {
            sessionStorage.setItem(
                this.#STORAGE_KEYS.subdomain,
                tenant.subdomain
            );
            this.#subdomainSig.set(tenant.subdomain);
        }
        if (tenant?.id) {
            localStorage.setItem(
                this.#STORAGE_KEYS.tenantId,
                String(tenant.id)
            );
        }
    }

    clear(): void {
        this.#tenantSig.set(null);
        sessionStorage.removeItem(this.#STORAGE_KEYS.subdomain);
        localStorage.removeItem(this.#STORAGE_KEYS.tenantId);
        this.#subdomainSig.set(null);
    }

    getCurrentSubdomain(): string | null {
        const current = this.#tenantSig();
        if (current?.subdomain) {
            return current.subdomain;
        }
        return sessionStorage.getItem(this.#STORAGE_KEYS.subdomain);
    }

    getCurrentTenantId(): number | null {
        const current = this.#tenantSig();
        if (current?.id) {
            return current.id;
        }
        const fromStorage = localStorage.getItem(this.#STORAGE_KEYS.tenantId);
        if (fromStorage) {
            const parsed = Number(fromStorage);
            return Number.isFinite(parsed) ? parsed : null;
        }
        return null;
    }

    setSubdomain(subdomain: string): void {
        if (subdomain) {
            sessionStorage.setItem(this.#STORAGE_KEYS.subdomain, subdomain);
            this.#subdomainSig.set(subdomain);
        }
    }

    selectTenant(tenant: Tenant, reload: boolean = true): void {
        this.setCurrentTenant(tenant);
        this.#selectedTenantSig.set(tenant);
        localStorage.setItem(
            this.#STORAGE_KEYS.selectedTenantId,
            String(tenant.id)
        );
        this.#loadTenantModules(tenant.id, reload);
    }

    #loadTenantModules(tenantId: number, reload: boolean = true): void {
        this.#apiClient
            .get<ApiResponse<TenantModule[]>>('tenantModules', { tenantId })
            .pipe(
                take(1),
                catchError(() => {
                    this.#notify.alert('Failed to load tenant modules');
                    return of({ data: [], result: 'ERROR' } as ApiResponse<
                        TenantModule[]
                    >);
                }),
                switchMap((response) => {
                    const moduleCodes = response.data
                        .filter((m: TenantModule) => m.status === 'enabled')
                        .map((m: TenantModule) => m.moduleCode);
                    this.#tenantModulesSig.set(moduleCodes);
                    this.#userService.setTenantModules(moduleCodes);
                    if (reload) {
                        return timer(0).pipe(
                            map(() => this.#navigationService.reload())
                        );
                    }
                    return of(null);
                })
            )
            .subscribe();
    }

    protected loadCurrentUserTenantModules(): Observable<string[]> {
        return this.#apiClient
            .get<ApiResponse<TenantModule[]>>('tenantCurrentModules')
            .pipe(
                take(1),
                map((response: ApiResponse<TenantModule[]>) => {
                    const moduleCodes = response.data
                        .filter((m: TenantModule) => m.status === 'enabled')
                        .map((m: TenantModule) => m.moduleCode);
                    this.#tenantModulesSig.set(moduleCodes);
                    this.#userService.setTenantModules(moduleCodes);
                    return moduleCodes;
                }),
                switchMap((moduleCodes) =>
                    timer(0).pipe(
                        map(() => {
                            this.#navigationService.reload();
                            return moduleCodes;
                        })
                    )
                ),
                catchError(() => {
                    this.#notify.alert('Failed to load tenant modules');
                    return of([]);
                })
            );
    }

    initializeTenantContext(user: User): Observable<any> {
        if (user.role === SUPER_ADMIN_ROLE) {
            return this.restoreTenantSelection();
        } else if (user.tenantId) {
            return forkJoin([
                this.loadCurrentUserTenantModules(),
                this.#loadTenantDetails(),
            ]);
        }
        return of(null);
    }

    #loadTenantDetails(): Observable<Tenant | null> {
        return this.#apiClient
            .get<ApiResponse<Tenant>>('tenantCurrentDetail')
            .pipe(
                take(1),
                map((response: ApiResponse<Tenant>) => {
                    if (response.data) {
                        this.setCurrentTenant(response.data);
                        return response.data;
                    }
                    return null;
                }),
                catchError(() => {
                    return of(null);
                })
            );
    }

    clearTenantSelection(): void {
        this.#selectedTenantSig.set(null);
        this.#tenantModulesSig.set([]);
        this.#userService.setTenantModules([]);
        localStorage.removeItem(this.#STORAGE_KEYS.selectedTenantId);
        localStorage.removeItem(this.#STORAGE_KEYS.tenantId);
        this.#navigationService.reload();
    }

    getSelectedTenantId(): number | null {
        const savedId = localStorage.getItem(
            this.#STORAGE_KEYS.selectedTenantId
        );
        if (savedId) {
            const parsed = Number(savedId);
            return Number.isFinite(parsed) ? parsed : null;
        }
        return null;
    }

    extractSubdomainFromHost(): string | null {
        const params = new URLSearchParams(window.location.search);
        const querySubdomain = params.get('subdomain');
        if (querySubdomain && this.isValidSubdomain(querySubdomain)) {
            return querySubdomain;
        }

        const hostname = window.location.hostname;
        if (hostname === 'localhost') {
            return 'admin';
        }
        const parts = hostname.split('.');
        const subdomain = parts[0];

        // Platform admin panel hosts:
        //   admin.localhost, app.craftive.io (prod), s1-app.craftive.io (stage)
        if (subdomain === 'admin' || subdomain === 'app' || subdomain === 's1-app') {
            return 'admin';
        }

        if (!this.isValidSubdomain(subdomain)) {
            this.#notify.alert(
                'Invalid tenant subdomain. Please contact your administrator.',
                {
                    durationMs: 5000,
                }
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

initializeFromHostname(): void {
        const subdomain = this.extractSubdomainFromHost();
        if (subdomain) {
            this.setSubdomain(subdomain);

            if (subdomain === 'admin') {
                this.#tenantSig.set(null);
                localStorage.removeItem(this.#STORAGE_KEYS.tenantId);
            }
        }
    }

    restoreTenantSelection(): Observable<Tenant | null> {
        const tenantId = this.getSelectedTenantId();
        if (!tenantId) {
            return of(null);
        }

        return this.#apiClient
            .get<ApiResponse<Tenant>>('tenantById', { id: tenantId })
            .pipe(
                take(1),
                map((response: ApiResponse<Tenant>) => {
                    if (response.data) {
                        this.selectTenant(response.data, false); // Reload false to prevent loop
                        return response.data;
                    }
                    return null;
                }),
                catchError(() => {
                    this.clearTenantSelection();
                    return of(null);
                })
            );
    }
}
