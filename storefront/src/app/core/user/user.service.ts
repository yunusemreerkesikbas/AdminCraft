import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal, Signal } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { User } from 'app/core/user/user.types';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class UserService {
    private _httpClient = inject(HttpClient);
    private _userSig = signal<User | null>(null);
    private _tenantModulesSig = signal<string[]>([]);
    readonly user: Signal<User | null> = this._userSig.asReadonly();
    readonly user$: Observable<User | null> = toObservable(this._userSig);

    readonly tenantModules: Signal<string[]> = this._tenantModulesSig.asReadonly();

    hasModule(moduleCode: string): Signal<boolean> {
        return computed(() => this._tenantModulesSig().includes(moduleCode));
    }



    setUser(user: User | null, modules?: string[]): void {
        this._userSig.set(user);
        if (modules !== undefined) {
            this._tenantModulesSig.set(modules);
        }
    }

    setTenantModules(modules: string[]): void {
        this._tenantModulesSig.set(modules);
    }

    clear(): void {
        this._userSig.set(null);
        this._tenantModulesSig.set([]);
    }
}
