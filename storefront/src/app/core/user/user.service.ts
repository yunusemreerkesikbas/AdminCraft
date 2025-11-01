import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal, Signal } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { User } from 'app/core/user/user.types';
import { map, Observable, tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class UserService {
    private _httpClient = inject(HttpClient);
    private _user = signal<User | null>(null);
    private _tenantModules = signal<string[]>([]);
    readonly user: Signal<User | null> = this._user.asReadonly();
    readonly user$: Observable<User | null> = toObservable(this._user);

    readonly tenantModules: Signal<string[]> = this._tenantModules.asReadonly();

    hasModule(moduleCode: string): Signal<boolean> {
        return computed(() => this._tenantModules().includes(moduleCode));
    }

    get(): Observable<User> {
        return this._httpClient.get<User>('api/common/user').pipe(
            tap((user) => {
                this._user.set(user);
            })
        );
    }

    update(user: User): Observable<any> {
        return this._httpClient.patch<User>('api/common/user', { user }).pipe(
            map((response) => {
                this._user.set(response);
                return response;
            })
        );
    }

    setUser(user: User | null, modules?: string[]): void {
        this._user.set(user);
        if (modules !== undefined) {
            this._tenantModules.set(modules);
        }
    }

    setTenantModules(modules: string[]): void {
        this._tenantModules.set(modules);
    }

    clear(): void {
        this._user.set(null);
        this._tenantModules.set([]);
    }
}
