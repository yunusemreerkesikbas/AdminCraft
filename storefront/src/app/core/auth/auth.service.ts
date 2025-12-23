import { inject, Injectable, signal, Signal } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { ApiClientService } from '@core/api/api-client.service';
import { LoginResponse, LoginResponseData } from 'app/core/auth/auth.types';
import { AuthUtils } from 'app/core/auth/auth.utils';
import { TenantContextService } from 'app/core/tenant/tenant-context.service';
import { UserService } from 'app/core/user/user.service';
import { User } from 'app/core/user/user.types';
import { NotificationService } from 'app/shared/notifications/notification.service';
import { catchError, Observable, of, switchMap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
    #authenticatedSig = signal<boolean>(false);
    readonly authenticatedSig: Signal<boolean> = this.#authenticatedSig.asReadonly();
    readonly authenticated$: Observable<boolean> = toObservable(this.#authenticatedSig);

    readonly #apiClient = inject(ApiClientService);
    readonly #userService = inject(UserService);
    readonly #tenantContext = inject(TenantContextService);
    readonly #notificationService = inject(NotificationService);

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

    signIn(credentials: { email: string; password: string }): Observable<boolean> {
        if (this.#authenticatedSig()) {
            this.#notificationService.alert('User is already logged in.');
            return of(false);
        }
        return this.#apiClient.post<LoginResponse>('login', credentials).pipe(
            switchMap((response) => {
                if (response.result === 'SUCCESS' && response.data) {
                    this.#setAccessToken(response.data.accessToken);
                    this.#authenticatedSig.set(true);
                    this.#storeUserAndTenantInfo(response.data);
                    const user: User = {
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
                        response.data.subdomain;
                    if (subFromLogin) {
                        this.#tenantContext.setSubdomain(subFromLogin);
                    }

                    return this.#tenantContext.initializeTenantContext(user).pipe(
                        switchMap(() => {
                            this.#notificationService.success(response.message || 'Login successful');
                            return of(true);
                        }),
                        catchError(() => {
                            this.#notificationService.success(response.message || 'Login successful'); // Still success auth-wise
                            return of(true);
                        })
                    );
                } else {
                    this.#notificationService.alert(response.message || 'Authentication failed');
                    return of(false);
                }
            }),
            catchError((error) => {
                const message = error?.error?.message || error?.message || 'Authentication failed';
                this.#notificationService.alert(message);
                return of(false);
            })
        );
    }

    signInUsingToken(): Observable<boolean> {
        try {
            const token = this.#getAccessToken();
            if (token) {
                const decoded = AuthUtils.decodeToken(token);
                if (decoded) {
                    this.#authenticatedSig.set(true);
                    const user: User = {
                        id: decoded.userId || 0,
                        email: decoded.sub,
                        name: decoded.sub,
                        role: decoded.role,
                        tenantId: decoded.tenantId || 0,
                        preferredLanguage: (decoded['preferredLanguage'] as string) || localStorage.getItem('translocoLang') || 'tr'
                    };
                    this.#userService.setUser(user);
                    return this.#tenantContext.initializeTenantContext(user).pipe(
                        switchMap(() => of(true)),
                        catchError(() => of(true))
                    );
                }
            }
            this.#authenticatedSig.set(false);
            return of(false);
        } catch (error) {
            this.#authenticatedSig.set(false);
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

    #storeUserAndTenantInfo(data: LoginResponseData): void {
        try {
            if (data.userId) {
                localStorage.setItem('userId', data.userId.toString());
            }
            if (data.tenantId) {
                localStorage.setItem('tenantId', data.tenantId.toString());
            }
            if (data.subdomain) {
                const subdomain = data.subdomain;
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