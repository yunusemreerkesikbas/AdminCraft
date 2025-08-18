import { inject, Injectable } from '@angular/core';
import { User, UserPagination, CreateUserRequest, UpdateUserRequest, UserRole, ChangePasswordRequest } from './users.types';
import { BehaviorSubject, Observable, tap, switchMap, map } from 'rxjs';
import { ApiClientService } from '@core/api/api-client.service';

@Injectable({ providedIn: 'root' })
export class UsersService {
    private readonly _apiClient = inject(ApiClientService);

    private _users: BehaviorSubject<User[]> = new BehaviorSubject<User[]>([]);
    private _pagination: BehaviorSubject<UserPagination | null> = new BehaviorSubject<UserPagination | null>(null);

    // -----------------------------------------------------------------------------------------------------
    // @ Accessors
    // -----------------------------------------------------------------------------------------------------

    /**
     * Getter for users
     */
    get users$(): Observable<User[]> {
        return this._users.asObservable();
    }

    /**
     * Getter for pagination
     */
    get pagination$(): Observable<UserPagination | null> {
        return this._pagination.asObservable();
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Public methods
    // -----------------------------------------------------------------------------------------------------

    /**
     * Get users
     */
    getUsers(page: number = 0, size: number = 10, sort: string = 'fullName', order: 'asc' | 'desc' = 'asc', search: string = '', role?: UserRole): Observable<{ pagination: UserPagination; users: User[] }> {
        const queryParams: Record<string, any> = {};
        if (role) {
            queryParams.role = role;
        }

        return this._apiClient.get<any>('users', undefined, queryParams).pipe(
            map((response) => {
                let users: User[] = [];
                
                if (response.result === 'SUCCESS' && response.data) {
                    users = response.data.map((item: any) => ({
                        id: item.id,
                        email: item.email,
                        fullName: item.fullName,
                        role: item.role,
                        preferredLanguage: item.preferredLanguage,
                        tenantId: item.tenantId,
                        tenantName: item.tenantName,
                        isActive: item.isActive,
                        lastLoginAt: item.lastLoginAt,
                        createdAt: item.createdAt,
                        updatedAt: item.updatedAt
                    }));

                    // Client-side filtering and search
                    if (search) {
                        const searchLower = search.toLowerCase();
                        users = users.filter(user => 
                            user.fullName.toLowerCase().includes(searchLower) ||
                            user.email.toLowerCase().includes(searchLower) ||
                            (user.tenantName && user.tenantName.toLowerCase().includes(searchLower))
                        );
                    }

                    // Client-side sorting
                    users.sort((a, b) => {
                        const aValue = a[sort] || '';
                        const bValue = b[sort] || '';
                        const comparison = aValue.toString().localeCompare(bValue.toString());
                        return order === 'desc' ? -comparison : comparison;
                    });
                }

                // Client-side pagination
                const totalLength = users.length;
                const startIndex = page * size;
                const endIndex = Math.min(startIndex + size, totalLength);
                const paginatedUsers = users.slice(startIndex, endIndex);

                const pagination: UserPagination = {
                    length: totalLength,
                    size: size,
                    page: page,
                    lastPage: Math.ceil(totalLength / size) - 1,
                    startIndex: startIndex,
                    endIndex: endIndex - 1
                };

                // Update internal state
                this._users.next(paginatedUsers);
                this._pagination.next(pagination);

                return { pagination, users: paginatedUsers };
            })
        );
    }

    /**
     * Get user by id
     */
    getUserById(id: number): Observable<User> {
        return this._apiClient.get<any>('userById', { id }).pipe(
            map((response) => {
                if (response.result === 'SUCCESS' && response.data) {
                    const item = response.data;
                    return {
                        id: item.id,
                        email: item.email,
                        fullName: item.fullName,
                        role: item.role,
                        preferredLanguage: item.preferredLanguage,
                        tenantId: item.tenantId,
                        tenantName: item.tenantName,
                        isActive: item.isActive,
                        lastLoginAt: item.lastLoginAt,
                        createdAt: item.createdAt,
                        updatedAt: item.updatedAt
                    };
                }
                throw new Error('User not found');
            })
        );
    }

    /**
     * Create user
     */
    createUser(user: CreateUserRequest): Observable<User> {
        return this._apiClient.post<any>('users', user).pipe(
            map((response) => {
                if (response.result === 'SUCCESS' && response.data) {
                    return response.data;
                }
                throw new Error('Failed to create user');
            }),
            tap(() => {
                // Refresh the users list
                this.getUsers().subscribe();
            })
        );
    }

    /**
     * Update user
     */
    updateUser(id: number, user: UpdateUserRequest): Observable<User> {
        return this._apiClient.put<any>('userById', user, { id }).pipe(
            map((response) => response.data),
            tap(() => {
                // Refresh the users list
                this.getUsers().subscribe();
            })
        );
    }

    /**
     * Delete user
     */
    deleteUser(id: number): Observable<boolean> {
        return this._apiClient.delete<any>('userById', { id }).pipe(
            map((response) => response.result === 'SUCCESS'),
            tap(() => {
                // Refresh the users list
                this.getUsers().subscribe();
            })
        );
    }

    /**
     * Activate user
     */
    activateUser(id: number): Observable<User> {
        return this._apiClient.post<any>('userActivate', {}, { id }).pipe(
            map((response) => response.data),
            tap(() => {
                // Refresh the users list
                this.getUsers().subscribe();
            })
        );
    }

    /**
     * Deactivate user
     */
    deactivateUser(id: number): Observable<User> {
        return this._apiClient.post<any>('userDeactivate', {}, { id }).pipe(
            map((response) => response.data),
            tap(() => {
                // Refresh the users list
                this.getUsers().subscribe();
            })
        );
    }

    /**
     * Change user password
     */
    changePassword(id: number, passwordRequest: ChangePasswordRequest): Observable<boolean> {
        return this._apiClient.post<any>('userChangePassword', passwordRequest, { id }).pipe(
            map((response) => response.result === 'SUCCESS')
        );
    }

    /**
     * Reset user password
     */
    resetPassword(id: number): Observable<string> {
        return this._apiClient.post<any>('userResetPassword', {}, { id }).pipe(
            map((response) => {
                if (response.result === 'SUCCESS' && response.data) {
                    return response.data.newPassword;
                }
                throw new Error('Failed to reset password');
            })
        );
    }
}