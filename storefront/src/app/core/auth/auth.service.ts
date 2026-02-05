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
import { catchError, Observable, of, switchMap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
    #authenticatedSig = signal<boolean>(false);
    readonly authenticatedSig: Signal<boolean> =
        this.#authenticatedSig.asReadonly();
    readonly authenticated$: Observable<boolean> = toObservable(
        this.#authenticatedSig
    );

    #requires2FASig = signal<boolean>(false);
    readonly requires2FASig: Signal<boolean> = this.#requires2FASig.asReadonly();

    #twoFactorPendingSig = signal<TwoFactorPendingState | null>(null);
    readonly twoFactorPendingSig: Signal<TwoFactorPendingState | null> =
        this.#twoFactorPendingSig.asReadonly();

    readonly #apiClient = inject(ApiClientService);
    readonly #userService = inject(UserService);
    readonly #tenantContext = inject(TenantContextService);
    readonly #notificationService = inject(NotificationService);
    readonly #LOCK_STORAGE_PREFIX = 'accountLockUntil:';

    #setAccessToken(token: string): void {
        localStorage.setItem('accessToken', token);
    }

    #getAccessToken(): string {
        return localStorage.getItem('accessToken') ?? '';
    }

    getAccessToken(): string {
        return this.#getAccessToken();
    }

    forgotPassword(email: string, subdomain?: string): Observable<any> {
        return this.#apiClient.custom('POST', 'forgotPassword', {
            body: { email },
            customHeaders: subdomain ? { 'X-Tenant-Subdomain': subdomain } : undefined
        });
    }

    verifyResetToken(token: string, subdomain?: string): Observable<any> {
        return this.#apiClient.custom('GET', 'verifyResetToken', {
            queryParams: { token },
            customHeaders: subdomain ? { 'X-Tenant-Subdomain': subdomain } : undefined
        });
    }

    resetPassword(
        token: string,
        password: string,
        confirmPassword: string,
        subdomain?: string
    ): Observable<any> {
        return this.#apiClient.custom('POST', 'resetPassword', {
            body: { token, password, confirmPassword },
            customHeaders: subdomain ? { 'X-Tenant-Subdomain': subdomain } : undefined
        });
    }

    verifyEmailToken(token: string, subdomain?: string): Observable<any> {
        return this.#apiClient.custom('GET', 'verifyEmailToken', {
            queryParams: { token },
            customHeaders: subdomain ? { 'X-Tenant-Subdomain': subdomain } : undefined
        });
    }

    setInitialPassword(
        token: string,
        password: string,
        confirmPassword: string,
        deviceFingerprint?: string,
        trustDevice?: boolean,
        deviceName?: string,
        subdomain?: string
    ): Observable<LoginResponse> {
        return this.#apiClient.custom<LoginResponse>('POST', 'setInitialPassword', {
            body: {
                token,
                password,
                confirmPassword,
                deviceFingerprint,
                trustDevice,
                deviceName,
            },
            customHeaders: subdomain ? { 'X-Tenant-Subdomain': subdomain } : undefined
        });
    }

    /**
     * Complete sign-in process after receiving auth response
     * Stores tokens and updates user state
     */
    completeSignInWithResponse(response: LoginResponseData): void {
        // Store tokens
        this.#setAccessToken(response.accessToken);
        this.#authenticatedSig.set(true);

        // Store user and tenant info
        this.#storeUserAndTenantInfo(response);

        // Set user data
        const user: User = {
            id: response.userId,
            email: response.email,
            name: response.email,
            role: response.role,
            tenantId: response.tenantId,
            preferredLanguage: response.preferredLanguage,
        };
        this.#userService.setUser(user);

        // Set subdomain
        if (response.subdomain) {
            this.#tenantContext.setSubdomain(response.subdomain);
        }
    }

    signIn(credentials: {
        email: string;
        password: string;
        deviceFingerprint?: string;
    }): Observable<boolean | 'requires2FA'> {
        if (this.#authenticatedSig()) {
            this.#notificationService.alert('User is already logged in.');
            return of(false);
        }
        const remainingMinutes = this.#getLocalLockRemainingMinutes(
            credentials.email
        );
        if (remainingMinutes > 0) {
            this.#notificationService.warning(
                `Account is locked. Try again in ${remainingMinutes} minutes.`,
                { durationMs: 10000 }
            );
            return of(false);
        }
        return this.#apiClient.post<LoginResponse>('login', credentials).pipe(
            switchMap((response) => {
                if (response.result === 'SUCCESS' && response.data) {
                    // Check if 2FA is required
                    if (response.data.requires2FA) {
                        this.#requires2FASig.set(true);
                        this.#twoFactorPendingSig.set({
                            pendingToken: response.data.pendingToken,
                            email: response.data.email,
                            tenantId: response.data.tenantId,
                            subdomain: response.data.subdomain,
                        });
                        this.#notificationService.info(
                            'Two-factor authentication required. Please check your email for the verification code.'
                        );
                        return of('requires2FA' as const);
                    }

                    // Normal login success
                    return this.#completeSignIn(response.data, response.message);
                } else {
                    this.#notificationService.alert(
                        response.message || 'Authentication failed'
                    );
                    return of(false);
                }
            }),
            catchError((error) => {
                const message =
                    error?.error?.message ||
                    error?.message ||
                    'Authentication failed';
                const errorCode = error?.error?.data?.errorCode;

                if (errorCode === 'ACCOUNT_LOCKED') {
                    const remainingFromServer = Number(
                        error?.error?.data?.remainingMinutes
                    );
                    if (
                        Number.isFinite(remainingFromServer) &&
                        remainingFromServer > 0
                    ) {
                        this.#setLocalLock(
                            credentials.email,
                            remainingFromServer
                        );
                    }
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
                    // Clear 2FA state
                    this.#requires2FASig.set(false);
                    this.#twoFactorPendingSig.set(null);

                    // Complete login
                    return this.#completeSignIn(response.data, response.message);
                } else {
                    this.#notificationService.alert(
                        response.message || 'OTP verification failed'
                    );
                    return of(false);
                }
            }),
            catchError((error) => {
                const message =
                    error?.error?.message ||
                    error?.message ||
                    'OTP verification failed';
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
        this.#clearLocalLock(data.email);
        this.#setAccessToken(data.accessToken);
        this.#authenticatedSig.set(true);
        this.#storeUserAndTenantInfo(data);
        const user: User = {
            id: data.userId,
            email: data.email,
            name: data.email,
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
                this.#notificationService.success(
                    message || 'Login successful'
                );
                return of(true);
            }),
            catchError(() => {
                this.#notificationService.success(
                    message || 'Login successful'
                ); // Still success auth-wise
                return of(true);
            })
        );
    }

    #getLocalLockRemainingMinutes(email: string): number {
        const lockUntilRaw = localStorage.getItem(
            this.#getLockStorageKey(email)
        );
        if (!lockUntilRaw) {
            return 0;
        }
        const lockUntil = Number(lockUntilRaw);
        if (!Number.isFinite(lockUntil)) {
            localStorage.removeItem(this.#getLockStorageKey(email));
            return 0;
        }
        const remainingMs = lockUntil - Date.now();
        if (remainingMs <= 0) {
            localStorage.removeItem(this.#getLockStorageKey(email));
            return 0;
        }
        return Math.ceil(remainingMs / 60000);
    }

    #setLocalLock(email: string, remainingMinutes: number): void {
        if (!Number.isFinite(remainingMinutes) || remainingMinutes <= 0) {
            return;
        }
        const lockUntil = Date.now() + remainingMinutes * 60 * 1000;
        localStorage.setItem(
            this.#getLockStorageKey(email),
            String(lockUntil)
        );
    }

    #clearLocalLock(email: string): void {
        localStorage.removeItem(this.#getLockStorageKey(email));
    }

    #getLockStorageKey(email: string): string {
        const normalizedEmail = email.trim().toLowerCase();
        const host = window.location.host || 'default';
        return `${this.#LOCK_STORAGE_PREFIX}${host}:${normalizedEmail}`;
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
                    };
                    this.#userService.setUser(user);
                    return this.#tenantContext
                        .initializeTenantContext(user)
                        .pipe(
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
        } catch (error) {}
    }

    #clearUserAndTenantInfo(): void {
        try {
            localStorage.removeItem('userId');
            localStorage.removeItem('tenantId');
            localStorage.removeItem('currentTenantSubdomain');
        } catch (error) {}
    }
}
