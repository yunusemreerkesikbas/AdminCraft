import { Injectable, inject } from '@angular/core';
import { ApiClientService } from '@core/api/api-client.service';
import { ApiResponse } from '@core/crud/api.types';
import { CrudEndpoints, CrudHttpService } from '@core/crud';
import { map, Observable, throwError } from 'rxjs';
import {
    COMMERCE_MODULE_CODE,
    ChangeCommerceOrderStatusRequest,
    CommerceAdminDashboard,
    CommerceAdminOrderDetail,
    CommerceAdminOrderRow,
    CommerceAdminPaymentAttemptRow,
} from '../models/commerce.types';

@Injectable({ providedIn: 'root' })
export class CommerceAdminService {
    readonly #api = inject(ApiClientService);
    readonly moduleCode = COMMERCE_MODULE_CODE;

    getDashboard(): Observable<CommerceAdminDashboard> {
        return this.#api
            .get<ApiResponse<CommerceAdminDashboard>>('commerceAdminDashboard')
            .pipe(map((response) => response.data));
    }
}

@Injectable({ providedIn: 'root' })
export class CommerceAdminOrderService extends CrudHttpService<
    CommerceAdminOrderRow,
    Partial<CommerceAdminOrderRow>,
    Partial<CommerceAdminOrderRow>
> {
    protected endpoints: CrudEndpoints = {
        list: 'commerceAdminOrders',
        getById: 'commerceAdminOrders',
        create: 'commerceAdminOrders',
        update: 'commerceAdminOrders',
        delete: 'commerceAdminOrders',
    };

    override getById(
        _id: number,
        _params?: Record<string, string | number>
    ): Observable<CommerceAdminOrderRow> {
        return readOnlyOperation();
    }

    getOrder(orderUid: string): Observable<CommerceAdminOrderDetail> {
        return this.api
            .get<ApiResponse<CommerceAdminOrderDetail>>(
                'commerceAdminOrderByUid',
                { orderUid }
            )
            .pipe(map((response) => response.data));
    }

    updateOrderStatus(
        orderUid: string,
        request: ChangeCommerceOrderStatusRequest
    ): Observable<CommerceAdminOrderDetail> {
        return this.api
            .patch<ApiResponse<CommerceAdminOrderDetail>>(
                'commerceAdminOrderStatus',
                request,
                { orderUid }
            )
            .pipe(map((response) => response.data));
    }

    override create(_dto: Partial<CommerceAdminOrderRow>): Observable<CommerceAdminOrderRow> {
        return readOnlyOperation();
    }

    override update(
        _id: number,
        _dto: Partial<CommerceAdminOrderRow>
    ): Observable<CommerceAdminOrderRow> {
        return readOnlyOperation();
    }

    override delete(_id: number): Observable<void> {
        return readOnlyOperation();
    }
}

@Injectable({ providedIn: 'root' })
export class CommerceAdminPaymentAttemptService extends CrudHttpService<
    CommerceAdminPaymentAttemptRow,
    Partial<CommerceAdminPaymentAttemptRow>,
    Partial<CommerceAdminPaymentAttemptRow>
> {
    protected endpoints: CrudEndpoints = {
        list: 'commerceAdminPaymentAttempts',
        getById: 'commerceAdminPaymentAttempts',
        create: 'commerceAdminPaymentAttempts',
        update: 'commerceAdminPaymentAttempts',
        delete: 'commerceAdminPaymentAttempts',
    };

    override getById(
        _id: number,
        _params?: Record<string, string | number>
    ): Observable<CommerceAdminPaymentAttemptRow> {
        return readOnlyOperation();
    }

    override create(
        _dto: Partial<CommerceAdminPaymentAttemptRow>
    ): Observable<CommerceAdminPaymentAttemptRow> {
        return readOnlyOperation();
    }

    override update(
        _id: number,
        _dto: Partial<CommerceAdminPaymentAttemptRow>
    ): Observable<CommerceAdminPaymentAttemptRow> {
        return readOnlyOperation();
    }

    override delete(_id: number): Observable<void> {
        return readOnlyOperation();
    }
}

function readOnlyOperation<T>(): Observable<T> {
    return throwError(() => new Error('Commerce admin operations are read-only'));
}
