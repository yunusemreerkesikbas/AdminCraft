import { Injectable } from '@angular/core';
import { ApiResponse } from '@core/crud/api.types';
import { CrudEndpoints, CrudHttpService } from '@core/crud/crud-http.service';
import { Observable } from 'rxjs';
import {
    ChangePasswordRequest,
    CreateUserRequest,
    ResetPasswordResponse,
    UpdateUserRequest,
    User,
} from './users.types';

@Injectable({ providedIn: 'root' })
export class UsersService extends CrudHttpService<
    User,
    CreateUserRequest,
    UpdateUserRequest
> {
    protected endpoints: CrudEndpoints = {
        list: 'users',
        getById: 'userById',
        create: 'users',
        update: 'userById',
        delete: 'userById',
    };

    activateUser(id: number): Observable<User> {
        return this.customPost<User>('userActivate', {}, { id });
    }

    deactivateUser(id: number): Observable<User> {
        return this.customPost<User>('userDeactivate', {}, { id });
    }

    changePassword(
        id: number,
        request: ChangePasswordRequest
    ): Observable<void> {
        return this.customPost<void>('userChangePassword', request, { id });
    }

    resetPassword(id: number): Observable<ResetPasswordResponse> {
        return this.customPost<ResetPasswordResponse>(
            'userResetPassword',
            {},
            { id }
        );
    }
    createWithResponse(
        request: CreateUserRequest
    ): Observable<ApiResponse<User>> {
        return this.api.post<ApiResponse<User>>(this.endpoints.create, request);
    }

    updateWithResponse(
        id: number,
        request: UpdateUserRequest
    ): Observable<ApiResponse<User>> {
        return this.api.put<ApiResponse<User>>(this.endpoints.update, request, {
            id,
        });
    }

    deleteWithResponse(id: number): Observable<ApiResponse<void>> {
        return this.api.delete<ApiResponse<void>>(this.endpoints.delete, {
            id,
        });
    }
}
