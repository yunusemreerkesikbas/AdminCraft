import { inject, Injectable, signal, Signal } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { ApiClientService } from '@core/api/api-client.service';
import { AuthUtils } from 'app/core/auth/auth.utils';
import { TenantContextService } from 'app/core/tenant/tenant-context.service';
import { UserService } from 'app/core/user/user.service';
import { catchError, Observable, of, switchMap, throwError } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
    #authenticatedSig = signal<boolean>(false);
    readonly authenticated: Signal<boolean> = this.#authenticatedSig.asReadonly();
    readonly authenticated$ = toObservable(this.#authenticatedSig);

    readonly #apiClient = inject(ApiClientService);
    readonly #userService = inject(UserService);
    readonly #tenantContext = inject(TenantContextService);

    #setAccessToken(token: string): void {
        localStorage.setItem('accessToken', token);
    }

    #getAccessToken(): string {
        return localStorage.getItem('accessToken') ?? '';
    }

    getAccessToken(): string {
        return this.#getAccessToken();
    }

    forgotPassword(email: string): Observable<any> {
        return this.#apiClient.post('login', { email });
    }

    resetPassword(password: string): Observable<any> {
        return this.#apiClient.post('login', { password });
    }

    signIn(credentials: { email: string; password: string }): Observable<any> {
        if (this.#authenticatedSig()) {
            return throwError('User is already logged in.');
        }
        return this.#apiClient.post('login', credentials).pipe(
            switchMap((response: any) => {
                if (response.result === 'SUCCESS' && response.data) {
                    this.#setAccessToken(response.data.accessToken);
                    this.#authenticatedSig.set(true);
                    this.#storeUserAndTenantInfo(response.data);
                    const user = {
                        id: response.data.userId,
                        email: response.data.email,
                        name: response.data.fullName,
                        role: response.data.role,
                        tenantId: response.data.tenantId,
                        preferredLanguage: response.data.preferredLanguage
                    };
                    this.#userService.setUser(user);
                    const subFromLogin: string | undefined =
                        response.data.subdomain ||
                        response.data.tenantSubdomain;
                    if (subFromLogin) {
                        this.#tenantContext.setSubdomain(subFromLogin);
                    }

                    return this.#tenantContext.initializeTenantContext(user).pipe(
                        switchMap(() => of(response.data)),
                        catchError(() => of(response.data))
                    );
                } else {
                    return throwError(response.message || 'Authentication failed');
                }
            }),
            catchError((error) => {
                return throwError(
                    error?.error?.message ||
                    error?.message ||
                    'Authentication failed'
                );
            })
        );
    }

    signInUsingToken(): Observable<any> {
        try {
            this.#authenticatedSig.set(true);
            const token = this.#getAccessToken();
            if (token) {
                const payload = JSON.parse(atob(token.split('.')[1]));
                const user = {
                    id: payload.userId || 0,
                    email: payload.sub,
                    name: payload.sub,
                    role: payload.role,
                    tenantId: payload.tenantId || 0,
                    preferredLanguage: 'tr'
                };
                this.#userService.setUser(user);
                return this.#tenantContext.initializeTenantContext(user).pipe(
                    switchMap(() => of(true)),
                    catchError(() => of(true))
                );
            }
            return of(true);
        } catch (error) {
            return of(false);
        }
    }

    signOut(): Observable<any> {
        localStorage.removeItem('accessToken');
        this.#clearUserAndTenantInfo();
        this.#authenticatedSig.set(false);
        this.#userService.clear();
        return of(true);
    }

    signUp(user: {
        name: string;
        email: string;
        password: string;
        company: string;
    }): Observable<any> {
        return this.#apiClient.post('login', user);
    }

    unlockSession(credentials: {
        email: string;
        password: string;
    }): Observable<any> {
        return this.#apiClient.post('login', credentials);
    }

    check(): Observable<boolean> {
        if (this.#authenticatedSig()) {
            return of(true);
        }
        const token = this.#getAccessToken();
        if (!token) {
            return of(false);
        }
        if (AuthUtils.isTokenExpired(token)) {
            return of(false);
        }
        return this.signInUsingToken();
    }

    #storeUserAndTenantInfo(data: any): void {
        try {
            if (data.userId) {
                localStorage.setItem('userId', data.userId.toString());
            }
            if (data.tenantId) {
                localStorage.setItem('tenantId', data.tenantId.toString());
            }
            if (data.subdomain || data.tenantSubdomain) {
                const subdomain = data.subdomain || data.tenantSubdomain;
                localStorage.setItem('currentTenantSubdomain', subdomain);
            }
        } catch (error) {
        }
    }

    #clearUserAndTenantInfo(): void {
        try {
            localStorage.removeItem('userId');
            localStorage.removeItem('tenantId');
            localStorage.removeItem('currentTenantSubdomain');
        } catch (error) {
        }
    }
}