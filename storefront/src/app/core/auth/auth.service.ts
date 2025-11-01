import { inject, Injectable } from '@angular/core';
import { ApiClientService } from '@core/api/api-client.service';
import { AuthUtils } from 'app/core/auth/auth.utils';
import { TenantContextService } from 'app/core/tenant/tenant-context.service';
import { UserService } from 'app/core/user/user.service';
import { catchError, Observable, of, switchMap, throwError } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
    #authenticated: boolean = false;
    #apiClient = inject(ApiClientService);
    #userService = inject(UserService);
    #tenantContext = inject(TenantContextService);

    set accessToken(token: string) {
        localStorage.setItem('accessToken', token);
    }

    get accessToken(): string {
        return localStorage.getItem('accessToken') ?? '';
    }

    forgotPassword(email: string): Observable<any> {
        return this.#apiClient.custom('POST', 'login', {
            body: { email },
            includeAuth: false
        });
    }

    resetPassword(password: string): Observable<any> {
        return this.#apiClient.custom('POST', 'login', {
            body: { password },
            includeAuth: false
        });
    }

    signIn(credentials: { email: string; password: string }): Observable<any> {
        if (this.#authenticated) {
            return throwError('User is already logged in.');
        }
        return this.#apiClient.custom('POST', 'login', {
            body: credentials,
            includeAuth: false
        }).pipe(
            switchMap((response: any) => {
                if (response.result === 'SUCCESS' && response.data) {
                    this.accessToken = response.data.accessToken;
                    this.#authenticated = true;
                    this.storeUserAndTenantInfo(response.data);
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
                        catchError((error) => {
                            console.error('Failed to initialize tenant context:', error);
                            return of(response.data);
                        })
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
            this.#authenticated = true;
            const token = this.accessToken;
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
                    catchError((error) => {
                        console.error('Failed to initialize tenant context from token:', error);
                        return of(true);
                    })
                );
            }
            return of(true);
        } catch (error) {
            return of(false);
        }
    }

    signOut(): Observable<any> {
        localStorage.removeItem('accessToken');
        this.clearUserAndTenantInfo();
        this.#authenticated = false;
        this.#userService.clear();
        return of(true);
    }

    signUp(user: {
        name: string;
        email: string;
        password: string;
        company: string;
    }): Observable<any> {
        return this.#apiClient.custom('POST', 'login', {
            body: user,
            includeAuth: false
        });
    }

    unlockSession(credentials: {
        email: string;
        password: string;
    }): Observable<any> {
        return this.#apiClient.custom('POST', 'login', {
            body: credentials,
            includeAuth: false
        });
    }

    check(): Observable<boolean> {
        if (this.#authenticated) {
            return of(true);
        }
        if (!this.accessToken) {
            return of(false);
        }
        if (AuthUtils.isTokenExpired(this.accessToken)) {
            return of(false);
        }
        return this.signInUsingToken();
    }

    private storeUserAndTenantInfo(data: any): void {
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
            console.warn('Failed to store user/tenant info for headers:', error);
        }
    }

    private clearUserAndTenantInfo(): void {
        try {
            localStorage.removeItem('userId');
            localStorage.removeItem('tenantId');
            localStorage.removeItem('currentTenantSubdomain');
        } catch (error) {
            console.warn('Failed to clear user/tenant info:', error);
        }
    }
}