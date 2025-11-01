import {
    HttpErrorResponse,
    HttpEvent,
    HttpHandlerFn,
    HttpRequest,
} from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from 'app/core/auth/auth.service';
import { AuthUtils } from 'app/core/auth/auth.utils';
import { Observable, catchError, throwError } from 'rxjs';

export const authInterceptor = (
    req: HttpRequest<unknown>,
    next: HttpHandlerFn
): Observable<HttpEvent<unknown>> => {
    const authService = inject(AuthService);
    let newReq = req.clone();
    const isAuthEndpoint = req.url.includes('/auth/login') ||
                          req.url.includes('/auth/signup') ||
                          req.url.includes('/auth/forgot-password') ||
                          req.url.includes('/auth/reset-password');

    if (!isAuthEndpoint) {
        const token = authService.getAccessToken();
        if (token && !AuthUtils.isTokenExpired(token)) {
            newReq = req.clone({
                headers: req.headers.set('Authorization', 'Bearer ' + token),
            });
        }
    }
    return next(newReq).pipe(
        catchError((error) => {
            if (error instanceof HttpErrorResponse && error.status === 401) {
                authService.signOut();
                location.reload();
            }
            return throwError(error);
        })
    );
};
