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
    CommerceLegalTemplate,
    CommerceLegalTemplatePreview,
    CommerceLegalTemplateRequest,
    CommerceOrderResolutionDecisionRequest,
    CommerceOrderResolutionRequestRow,
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

@Injectable({ providedIn: 'root' })
export class CommerceAdminOrderRequestService extends CrudHttpService<
    CommerceOrderResolutionRequestRow,
    Partial<CommerceOrderResolutionRequestRow>,
    Partial<CommerceOrderResolutionRequestRow>
> {
    protected endpoints: CrudEndpoints = {
        list: 'commerceAdminOrderRequests',
        getById: 'commerceAdminOrderRequests',
        create: 'commerceAdminOrderRequests',
        update: 'commerceAdminOrderRequests',
        delete: 'commerceAdminOrderRequests',
    };

    override getById(
        _id: number,
        _params?: Record<string, string | number>
    ): Observable<CommerceOrderResolutionRequestRow> {
        return readOnlyOperation();
    }

    getRequest(requestUid: string): Observable<CommerceOrderResolutionRequestRow> {
        return this.api
            .get<ApiResponse<CommerceOrderResolutionRequestRow>>(
                'commerceAdminOrderRequestByUid',
                { requestUid }
            )
            .pipe(map((response) => response.data));
    }

    decide(
        requestUid: string,
        request: CommerceOrderResolutionDecisionRequest
    ): Observable<CommerceOrderResolutionRequestRow> {
        return this.api
            .patch<ApiResponse<CommerceOrderResolutionRequestRow>>(
                'commerceAdminOrderRequestDecision',
                request,
                { requestUid }
            )
            .pipe(map((response) => response.data));
    }

    override create(
        _dto: Partial<CommerceOrderResolutionRequestRow>
    ): Observable<CommerceOrderResolutionRequestRow> {
        return readOnlyOperation();
    }

    override update(
        _id: number,
        _dto: Partial<CommerceOrderResolutionRequestRow>
    ): Observable<CommerceOrderResolutionRequestRow> {
        return readOnlyOperation();
    }

    override delete(_id: number): Observable<void> {
        return readOnlyOperation();
    }
}

@Injectable({ providedIn: 'root' })
export class CommerceAdminLegalTemplateService {
    readonly #api = inject(ApiClientService);

    list(filters: {
        type?: string;
        language?: string;
        status?: string;
    } = {}): Observable<CommerceLegalTemplate[]> {
        const queryParams = {
            type: filters.type || null,
            language: filters.language || null,
            status: filters.status || null,
        };
        return this.#api
            .get<ApiResponse<CommerceLegalTemplate[]>>(
                'commerceAdminLegalTemplates',
                undefined,
                queryParams
            )
            .pipe(map((response) => response.data));
    }

    create(request: CommerceLegalTemplateRequest): Observable<CommerceLegalTemplate> {
        return this.#api
            .post<ApiResponse<CommerceLegalTemplate>>(
                'commerceAdminLegalTemplates',
                request
            )
            .pipe(map((response) => response.data));
    }

    update(
        templateUid: string,
        request: CommerceLegalTemplateRequest
    ): Observable<CommerceLegalTemplate> {
        return this.#api
            .put<ApiResponse<CommerceLegalTemplate>>(
                'commerceAdminLegalTemplateByUid',
                request,
                { templateUid }
            )
            .pipe(map((response) => response.data));
    }

    publish(templateUid: string): Observable<CommerceLegalTemplate> {
        return this.#api
            .patch<ApiResponse<CommerceLegalTemplate>>(
                'commerceAdminLegalTemplatePublish',
                {},
                { templateUid }
            )
            .pipe(map((response) => response.data));
    }

    archive(templateUid: string): Observable<CommerceLegalTemplate> {
        return this.#api
            .patch<ApiResponse<CommerceLegalTemplate>>(
                'commerceAdminLegalTemplateArchive',
                {},
                { templateUid }
            )
            .pipe(map((response) => response.data));
    }

    preview(templateUid: string): Observable<CommerceLegalTemplatePreview> {
        return this.#api
            .get<ApiResponse<CommerceLegalTemplatePreview>>(
                'commerceAdminLegalTemplatePreview',
                { templateUid }
            )
            .pipe(map((response) => response.data));
    }
}

function readOnlyOperation<T>(): Observable<T> {
    return throwError(() => new Error('Commerce admin operations are read-only'));
}
