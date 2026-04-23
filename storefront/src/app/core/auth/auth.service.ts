import { inject, Injectable, signal, Signal } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { ApiClientService } from '@core/api/api-client.service';
import {
    LoginResponse,
    LoginResponseData,
    TwoFactorPendingState,
    VerifyOtpRequest,
} from 'app/core/auth/auth.types';
import { AuthUtils } from 'app/core/auth/auth.utils';
import { TenantContextService } from 'app/core/tenant/tenant-context.service';
import { UserService } from 'app/core/user/user.service';
import { User } from 'app/core/user/user.types';
import { NotificationService } from 'app/shared/notifications/notification.service';
import { catchError, finalize, Observable, of, shareReplay, switchMap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
    #authenticatedSig = signal<boolean>(false);
    readonly authenticatedSig: Signal<boolean> =
        this.#authenticatedSig.asReadonly();
    readonly authenticated$: Observable<boolean> = toObservable(
        this.#authenticatedSig
    );

    #requires2FASig = signal<boolean>(false);
    readonly requires2FASig: Signal<boolean> =
        this.#requires2FASig.asReadonly();

    #twoFactorPendingSig = signal<TwoFactorPendingState | null>(null);
    readonly twoFactorPendingSig: Signal<TwoFactorPendingState | null> =
        this.#twoFactorPendingSig.asReadonly();

    readonly #apiClient = inject(ApiClientService);
    readonly #userService = inject(UserService);
    readonly #tenantContext = inject(TenantContextService);
    readonly #notificationService = inject(NotificationService);

    #accessTokenSig = signal<string>('');
    #refreshInProgress: Observable<boolean> | null = null;

    #setAccessToken(token: string): void {
        this.#accessTokenSig.set(token);
    }

    #getAccessToken(): string {
        return this.#accessTokenSig();
    }

    getAccessToken(): string {
        return this.#getAccessToken();
    }

    forgotPassword(
        email: string,
        subdomain?: string,
        recaptchaToken?: string
    ): Observable<any> {
        return this.#apiClient.custom('POST', 'forgotPassword', {
            body: { email, recaptchaToken },
            customHeaders: subdomain
                ? { 'X-Tenant-Subdomain': subdomain }
                : undefined,
        });
    }

    verifyResetToken(token: string, subdomain?: string): Observable<any> {
        return this.#apiClient.custom('GET', 'verifyResetToken', {
            queryParams: { token },
            customHeaders: subdomain
                ? { 'X-Tenant-Subdomain': subdomain }
                : undefined,
        });
    }

    resetPassword(
        token: string,
        password: string,
        confirmPassword: string,
        subdomain?: string,
        recaptchaToken?: string
    ): Observable<any> {
        return this.#apiClient.custom('POST', 'resetPassword', {
            body: { token, password, confirmPassword, recaptchaToken },
            customHeaders: subdomain
                ? { 'X-Tenant-Subdomain': subdomain }
                : undefined,
        });
    }

    verifyEmailToken(token: string, subdomain?: string): Observable<any> {
        return this.#apiClient.custom('GET', 'verifyEmailToken', {
            queryParams: { token },
            customHeaders: subdomain
                ? { 'X-Tenant-Subdomain': subdomain }
                : undefined,
        });
    }

    setInitialPassword(
        token: string,
        password: string,
        confirmPassword: string,
        deviceFingerprint?: string,
        trustDevice?: boolean,
        deviceName?: string,
        subdomain?: string,
        recaptchaToken?: string
    ): Observable<LoginResponse> {
        return this.#apiClient.custom<LoginResponse>(
            'POST',
            'setInitialPassword',
            {
                body: {
                    token,
                    password,
                    confirmPassword,
                    deviceFingerprint,
                    trustDevice,
                    deviceName,
                    recaptchaToken,
                },
                customHeaders: subdomain
                    ? { 'X-Tenant-Subdomain': subdomain }
                    : undefined,
            }
        );
    }

    completeSignInWithResponse(response: LoginResponseData): void {
        this.#setAccessToken(response.accessToken);
        this.#authenticatedSig.set(true);

        this.#storeUserAndTenantInfo(response);

        const user: User = {
            id: response.userId,
            email: response.email,
            name: response.fullName ?? response.email,
            role: response.role,
            tenantId: response.tenantId,
            preferredLanguage: response.preferredLanguage,
        };
        this.#userService.setUser(user);

        if (response.subdomain) {
            this.#tenantContext.setSubdomain(response.subdomain);
        }
    }

    signIn(credentials: {
        email: string;
        password: string;
        deviceFingerprint?: string;
        recaptchaToken?: string;
    }): Observable<boolean | 'requires2FA'> {
        return this.#apiClient.post<LoginResponse>('login', credentials).pipe(
            switchMap((response) => {
                if (response.result === 'SUCCESS' && response.data) {
                    if (response.data.requires2FA) {
                        this.#requires2FASig.set(true);
                        this.#twoFactorPendingSig.set({
                            pendingToken: response.data.pendingToken,
                            email: response.data.email,
                            tenantId: response.data.tenantId,
                            subdomain: response.data.subdomain,
                        });
                        this.#notificationService.info(response.message ?? '');
                        return of('requires2FA' as const);
                    }
                    return this.#completeSignIn(
                        response.data,
                        response.message
                    );
                } else {
                    this.#notificationService.alert(response.message);
                    return of(false);
                }
            }),
            catchError((error) => {
                const message = error?.error?.message || error?.message;
                const errorCode = error?.error?.data?.errorCode;

                if (errorCode === 'ACCOUNT_LOCKED') {
                    this.#notificationService.warning(message, {
                        durationMs: 10000,
                    });
                } else {
                    this.#notificationService.alert(message);
                }
                return of(false);
            })
        );
    }

    verifyOtp(request: VerifyOtpRequest): Observable<boolean> {
        return this.#apiClient.post<LoginResponse>('verifyOtp', request).pipe(
            switchMap((response) => {
                if (response.result === 'SUCCESS' && response.data) {
                    this.#requires2FASig.set(false);
                    this.#twoFactorPendingSig.set(null);
                    return this.#completeSignIn(
                        response.data,
                        response.message
                    );
                } else {
                    this.#notificationService.alert(response.message ?? '');
                    return of(false);
                }
            }),
            catchError((error) => {
                const message = error?.error?.message || error?.message;
                this.#notificationService.alert(message);
                return of(false);
            })
        );
    }

    cancel2FA(): void {
        this.#requires2FASig.set(false);
        this.#twoFactorPendingSig.set(null);
    }

    #completeSignIn(
        data: LoginResponseData,
        message: string | undefined
    ): Observable<boolean> {
        this.#setAccessToken(data.accessToken);
        this.#authenticatedSig.set(true);
        this.#storeUserAndTenantInfo(data);
        const user: User = {
            id: data.userId,
            email: data.email,
            name: data.fullName ?? data.email,
            role: data.role,
            tenantId: data.tenantId,
            preferredLanguage: data.preferredLanguage,
        };
        this.#userService.setUser(user);
        const subFromLogin: string | undefined = data.subdomain;
        if (subFromLogin) {
            this.#tenantContext.setSubdomain(subFromLogin);
        }

        return this.#tenantContext.initializeTenantContext(user).pipe(
            switchMap(() => {
                this.#notificationService.success(message);
                return of(true);
            })
        );
    }

    signInUsingToken(): Observable<boolean> {
        try {
            const token = this.#getAccessToken();
            if (token && !AuthUtils.isTokenExpired(token)) {
                const decoded = AuthUtils.decodeToken(token);
                if (decoded) {
                    this.#authenticatedSig.set(true);
                    const storedName = (() => {
                        try {
                            return localStorage.getItem('userFullName');
                        } catch {
                            return null;
                        }
                    })();
                    const user: User = {
                        id: decoded.userId || 0,
                        email: decoded.sub,
                        name: storedName ?? decoded.sub,
                        role: decoded.role,
                        tenantId: decoded.tenantId || 0,
                    };
                    this.#userService.setUser(user);
                    return this.#tenantContext
                        .initializeTenantContext(user)
                        .pipe(
                            switchMap(() => of(true)),
                            catchError(() => of(false))
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
        this.#setAccessToken('');
        this.#clearUserAndTenantInfo();
        this.#authenticatedSig.set(false);
        this.#userService.clear();
        return this.#apiClient.custom('POST', 'logout', {});
    }

    refresh(): Observable<boolean> {
        if (!this.#refreshInProgress) {
            this.#refreshInProgress = this.#apiClient
                .custom<LoginResponse>('POST', 'refresh', {})
                .pipe(
                    switchMap((response) => {
                        if (response.result === 'SUCCESS' && response.data?.accessToken) {
                            this.#setAccessToken(response.data.accessToken);
                            this.#authenticatedSig.set(true);
                            return of(true);
                        }
                        return of(false);
                    }),
                    catchError(() => of(false)),
                    finalize(() => { setTimeout(() => { this.#refreshInProgress = null; }, 2000); }),
                    shareReplay(1)
                );
        }
        return this.#refreshInProgress;
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
        if (token && !AuthUtils.isTokenExpired(token)) {
            return this.signInUsingToken();
        }
        return this.refresh().pipe(
            switchMap((refreshed) => (refreshed ? this.signInUsingToken() : of(false)))
        );
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
            if (data.fullName) {
                localStorage.setItem('userFullName', data.fullName);
            }
        } catch (error) {}
    }

    #clearUserAndTenantInfo(): void {
        try {
            localStorage.removeItem('userId');
            localStorage.removeItem('tenantId');
            localStorage.removeItem('currentTenantSubdomain');
            localStorage.removeItem('userFullName');
        } catch (error) {}
    }
}
