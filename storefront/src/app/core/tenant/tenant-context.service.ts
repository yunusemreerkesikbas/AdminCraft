import { Injectable } from '@angular/core';
import { Tenant } from 'app/modules/admin/custom/tenants/tenants.types';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TenantContextService {
    private readonly storageKey = 'currentTenantSubdomain';
    private _tenant$ = new BehaviorSubject<Tenant | null>(null);
    private _subdomain$ = new BehaviorSubject<string | null>(null);

    get tenant$(): Observable<Tenant | null> {
        return this._tenant$.asObservable();
    }

    get subdomain$(): Observable<string | null> {
        return this._subdomain$.asObservable();
    }

    get currentTenant(): Tenant | null {
        return this._tenant$.getValue();
    }

    setCurrentTenant(tenant: Tenant): void {
        this._tenant$.next(tenant);
        if (tenant?.subdomain) {
            localStorage.setItem(this.storageKey, tenant.subdomain);
            this._subdomain$.next(tenant.subdomain);
        }
    }

    clear(): void {
        this._tenant$.next(null);
        localStorage.removeItem(this.storageKey);
        this._subdomain$.next(null);
    }

    getCurrentSubdomain(): string | null {
        const current = this._tenant$.getValue();
        if (current?.subdomain) {
            return current.subdomain;
        }
        return localStorage.getItem(this.storageKey);
    }

    setSubdomain(subdomain: string): void {
        if (subdomain) {
            localStorage.setItem(this.storageKey, subdomain);
            this._subdomain$.next(subdomain);
        }
    }
}


