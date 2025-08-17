import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Tenant } from 'app/modules/admin/apps/admincraft/tenants/tenants.types';

@Injectable({ providedIn: 'root' })
export class TenantContextService {
    private readonly storageKey = 'currentTenantSubdomain';
    private _tenant$ = new BehaviorSubject<Tenant | null>(null);

    get tenant$(): Observable<Tenant | null> {
        return this._tenant$.asObservable();
    }

    setCurrentTenant(tenant: Tenant): void {
        this._tenant$.next(tenant);
        if (tenant?.subdomain) {
            localStorage.setItem(this.storageKey, tenant.subdomain);
        }
    }

    clear(): void {
        this._tenant$.next(null);
        localStorage.removeItem(this.storageKey);
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
        }
    }
}


