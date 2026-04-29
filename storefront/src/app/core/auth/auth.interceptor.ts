import {
    HttpErrorResponse,
    HttpEvent,
    HttpHandlerFn,
    HttpRequest,
} from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from 'app/core/auth/auth.service';
import { AuthUtils } from 'app/core/auth/auth.utils';
import { ConfigSessionService } from 'app/modules/config/console/config-session.service';
import { Observable, catchError, switchMap, throwError } from 'rxjs';

export const authInterceptor = (
    req: HttpRequest<unknown>,
    next: HttpHandlerFn
): Observable<HttpEvent<unknown>> => {
    const authService = inject(AuthService);
    const configSession = inject(ConfigSessionService);
    let newReq = req.clone({ withCredentials: true });
    const isAuthEndpoint = req.url.includes('/auth/login') ||
        req.url.includes('/auth/signup') ||
        req.url.includes('/auth/forgot-password') ||
        req.url.includes('/auth/reset-password') ||
        req.url.includes('/auth/refresh');
    const isConfigEndpoint = req.url.includes('/config/auth') || req.url.includes('/config/admin');
    const isConfigAdmin = req.url.includes('/config/admin');

    if (!isAuthEndpoint && !isConfigEndpoint && !req.headers.has('Authorization')) {
        const token = authService.getAccessToken();
        if (token && !AuthUtils.isTokenExpired(token)) {
            newReq = newReq.clone({
                headers: newReq.headers.set('Authorization', 'Bearer ' + token),
            });
        }
    }
    return next(newReq).pipe(
        catchError((error) => {
            if (error instanceof HttpErrorResponse) {
                if (isConfigAdmin && (error.status === 401 || error.status === 403)) {
                    if (req.headers.has('X-Config-Refresh-Retry')) {
                        configSession.clearStoredSession();
                        return throwError(() => error);
                    }

                    return configSession.tryRefreshStoredSession().pipe(
                        switchMap((session) => {
                            if (!session) {
                                return throwError(() => error);
                            }

                            return next(
                                req.clone({
                                    headers: req.headers
                                        .set('Authorization', `Bearer ${session.accessToken}`)
                                        .set('X-Config-Refresh-Retry', '1'),
                                })
                            );
                        }),
                        catchError(() => {
                            configSession.clearStoredSession();
                            return throwError(() => error);
                        })
                    );
                }
            }
            return throwError(() => error);
        })
    );
};
