import { Injectable, inject } from '@angular/core';
import { ApiResponse } from '@core/crud';
import { ConfigAuthResponse, ConfigTokenState } from './config-console.types';
import { ConfigConsoleService } from './config-console.service';
import { Observable, Subject, catchError, finalize, map, of, shareReplay, tap } from 'rxjs';

export const CONFIG_CONSOLE_STORAGE_KEY = 'config_console_auth';

@Injectable({ providedIn: 'root' })
export class ConfigSessionService {
    readonly #configConsoleService = inject(ConfigConsoleService);

    private readonly sessionChanged$ = new Subject<ConfigTokenState | null>();
    #refreshInFlight$: Observable<ConfigTokenState | null> | null = null;

    readonly onSessionChanged = this.sessionChanged$.asObservable();

    setStoredSession(session: ConfigTokenState): void {
        try {
            localStorage.setItem(CONFIG_CONSOLE_STORAGE_KEY, JSON.stringify(session));
        } catch {
            return;
        }

        this.sessionChanged$.next(session);
    }

    getStoredSession(): ConfigTokenState | null {
        try {
            const raw = localStorage.getItem(CONFIG_CONSOLE_STORAGE_KEY);
            if (!raw) {
                return null;
            }

            const parsed = JSON.parse(raw) as ConfigTokenState;
            if (!parsed?.accessToken || parsed.issuedAt == null) {
                this.clearStoredSession();
                return null;
            }

            return parsed;
        } catch {
            this.clearStoredSession();
            return null;
        }
    }

    isAccessTokenExpired(session: ConfigTokenState): boolean {
        return Date.now() > session.issuedAt + session.expiresIn * 1000;
    }

    tryRefreshStoredSession(): Observable<ConfigTokenState | null> {
        const storedSession = this.getStoredSession();
        if (!storedSession?.refreshToken) {
            this.clearStoredSession();
            return of(null);
        }

        if (this.#refreshInFlight$) {
            return this.#refreshInFlight$;
        }

        this.#refreshInFlight$ = this.#configConsoleService.refresh(storedSession.refreshToken).pipe(
            map((response: ApiResponse<ConfigAuthResponse>) => {
                if (response.result !== 'SUCCESS' || !response.data) {
                    return null;
                }

                const refreshedSession: ConfigTokenState = {
                    accessToken: response.data.accessToken,
                    refreshToken: response.data.refreshToken,
                    tokenType: response.data.tokenType,
                    expiresIn: response.data.expiresIn,
                    userId: response.data.userId,
                    email: response.data.email,
                    fullName: response.data.fullName,
                    role: response.data.role,
                    tenantId: response.data.tenantId,
                    subdomain: response.data.subdomain,
                    issuedAt: response.data.issuedAt,
                };

                this.setStoredSession(refreshedSession);
                return refreshedSession;
            }),
            tap((session) => {
                if (!session) {
                    this.clearStoredSession();
                }
            }),
            catchError(() => {
                this.clearStoredSession();
                return of(null);
            }),
            finalize(() => {
                this.#refreshInFlight$ = null;
            }),
            shareReplay(1)
        );

        return this.#refreshInFlight$;
    }

    clearStoredSession(): void {
        try {
            localStorage.removeItem(CONFIG_CONSOLE_STORAGE_KEY);
        } catch {
            // ignore
        }

        this.sessionChanged$.next(null);
    }
}
