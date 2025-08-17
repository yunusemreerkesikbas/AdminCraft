import { inject, Injectable } from '@angular/core';
import { AuthUtils } from 'app/core/auth/auth.utils';
import { UserService } from 'app/core/user/user.service';
import { TenantContextService } from 'app/core/tenant/tenant-context.service';
import { ApiClientService } from '@core/api/api-client.service';
import { catchError, Observable, of, switchMap, throwError } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
    private _authenticated: boolean = false;
    private readonly _apiClient = inject(ApiClientService);
    private _userService = inject(UserService);
    private _tenantContext = inject(TenantContextService);

    // -----------------------------------------------------------------------------------------------------
    // @ Accessors
    // -----------------------------------------------------------------------------------------------------

    /**
     * Setter & getter for access token
     */
    set accessToken(token: string) {
        localStorage.setItem('accessToken', token);
    }

    get accessToken(): string {
        return localStorage.getItem('accessToken') ?? '';
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Public methods
    // -----------------------------------------------------------------------------------------------------

    /**
     * Forgot password
     *
     * @param email
     */
    forgotPassword(email: string): Observable<any> {
        // TODO: Add endpoint to api-endpoints.ts when backend implements this
        return this._apiClient.custom('POST', 'login', {
            body: { email },
            includeAuth: false
        });
    }

    /**
     * Reset password
     *
     * @param password
     */
    resetPassword(password: string): Observable<any> {
        // TODO: Add endpoint to api-endpoints.ts when backend implements this
        return this._apiClient.custom('POST', 'login', {
            body: { password },
            includeAuth: false
        });
    }

    /**
     * Sign in
     *
     * @param credentials
     */
    signIn(credentials: { email: string; password: string }): Observable<any> {
        // Throw error, if the user is already logged in
        if (this._authenticated) {
            return throwError('User is already logged in.');
        }

        return this._apiClient.custom('POST', 'login', {
            body: credentials,
            includeAuth: false
        }).pipe(
            switchMap((response: any) => {
                // Check if the response has the expected structure
                if (response.result === 'SUCCESS' && response.data) {
                    // Store the access token in the local storage
                    this.accessToken = response.data.accessToken;

                    // Set the authenticated flag to true
                    this._authenticated = true;

                    // Store user and tenant information for headers
                    this.storeUserAndTenantInfo(response.data);

                    // Store the user on the user service (create user object from response data)
                    const user = {
                        id: response.data.userId,
                        email: response.data.email,
                        name: response.data.fullName,
                        role: response.data.role,
                        tenantId: response.data.tenantId,
                        preferredLanguage: response.data.preferredLanguage
                    };
                    
                    this._userService.user = user;

                    // Use subdomain from login response (backend should provide)
                    const subFromLogin: string | undefined =
                        response.data.subdomain ||
                        response.data.tenantSubdomain;
                    if (subFromLogin) {
                        this._tenantContext.setSubdomain(subFromLogin);
                    }
                    return of(response.data);
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

    /**
     * Sign in using the access token
     */
    signInUsingToken(): Observable<any> {
        // Simply validate the existing token and set authenticated state
        // No need to call backend since JWT is stateless
        
        try {
            // Set the authenticated flag to true
            this._authenticated = true;

            // Try to decode JWT payload to get user info (for basic validation)
            const token = this.accessToken;
            if (token) {
                const payload = JSON.parse(atob(token.split('.')[1]));
                
                // Create user object from JWT payload
                const user = {
                    id: 0, // We don't have user ID in JWT, will be loaded separately if needed
                    email: payload.sub,
                    name: payload.sub, // Use email as name temporarily
                    role: payload.role,
                    tenantId: 0,
                    preferredLanguage: 'tr'
                };

                // Store the user on the user service
                this._userService.user = user;

            }

            // Return true
            return of(true);
        } catch (error) {
            return of(false);
        }
    }

    /**
     * Sign out
     */
    signOut(): Observable<any> {
        // Remove the access token from the local storage
        localStorage.removeItem('accessToken');

        // Clear user and tenant information
        this.clearUserAndTenantInfo();

        // Set the authenticated flag to false
        this._authenticated = false;

        // Return the observable
        return of(true);
    }

    /**
     * Sign up
     *
     * @param user
     */
    signUp(user: {
        name: string;
        email: string;
        password: string;
        company: string;
    }): Observable<any> {
        // TODO: Add endpoint to api-endpoints.ts when backend implements this
        return this._apiClient.custom('POST', 'login', {
            body: user,
            includeAuth: false
        });
    }

    /**
     * Unlock session
     *
     * @param credentials
     */
    unlockSession(credentials: {
        email: string;
        password: string;
    }): Observable<any> {
        // TODO: Add endpoint to api-endpoints.ts when backend implements this
        return this._apiClient.custom('POST', 'login', {
            body: credentials,
            includeAuth: false
        });
    }

    /**
     * Check the authentication status
     */
    check(): Observable<boolean> {
        // Check if the user is logged in
        if (this._authenticated) {
            return of(true);
        }

        // Check the access token availability
        if (!this.accessToken) {
            return of(false);
        }

        // Check the access token expire date
        if (AuthUtils.isTokenExpired(this.accessToken)) {
            return of(false);
        }

        // If the access token exists, and it didn't expire, sign in using it
        return this.signInUsingToken();
    }

    /**
     * Store user and tenant information for HTTP headers
     * Based on SecurityConfig allowed headers: X-Tenant-ID, X-Tenant-Subdomain, X-User-ID
     */
    private storeUserAndTenantInfo(data: any): void {
        try {
            // Store user ID for X-User-ID header
            if (data.userId) {
                localStorage.setItem('userId', data.userId.toString());
            }

            // Store tenant ID for X-Tenant-ID header
            if (data.tenantId) {
                localStorage.setItem('tenantId', data.tenantId.toString());
            }

            // Store tenant subdomain for X-Tenant-Subdomain header
            if (data.subdomain || data.tenantSubdomain) {
                const subdomain = data.subdomain || data.tenantSubdomain;
                localStorage.setItem('tenantSubdomain', subdomain);
            }
        } catch (error) {
            console.warn('Failed to store user/tenant info for headers:', error);
        }
    }

    /**
     * Clear stored user and tenant information on logout
     */
    private clearUserAndTenantInfo(): void {
        try {
            localStorage.removeItem('userId');
            localStorage.removeItem('tenantId');
            localStorage.removeItem('tenantSubdomain');
        } catch (error) {
            console.warn('Failed to clear user/tenant info:', error);
        }
    }
}
