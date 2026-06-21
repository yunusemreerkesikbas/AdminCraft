import { Injectable, inject } from '@angular/core';
import { ApiClientService } from '@core/api/api-client.service';
import { ApiResponse, Page, SearchRequest } from '@core/crud/api.types';
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
    CommerceNotificationOutboxRow,
    CommerceNotificationTemplate,
    CommerceNotificationTemplatePreview,
    CommerceNotificationTemplateRequest,
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
export class CommerceAdminNotificationOutboxService extends CrudHttpService<
    CommerceNotificationOutboxRow,
    Partial<CommerceNotificationOutboxRow>,
    Partial<CommerceNotificationOutboxRow>
> {
    protected endpoints: CrudEndpoints = {
        list: 'commerceAdminNotificationOutbox',
        getById: 'commerceAdminNotificationOutbox',
        create: 'commerceAdminNotificationOutbox',
        update: 'commerceAdminNotificationOutbox',
        delete: 'commerceAdminNotificationOutbox',
    };

    override listPaged(
        request: SearchRequest & {
            status?: string | null;
            eventType?: string | null;
            aggregateUid?: string | null;
        }
    ): Observable<Page<CommerceNotificationOutboxRow>> {
        const queryParams: Record<string, string | number> = {
            page: request.page ?? 0,
            size: request.size ?? 20,
            sort: request.sort ?? 'createdAt,desc',
        };
        if (request.search) {
            queryParams['search'] = request.search;
        }
        if (request.status) {
            queryParams['status'] = request.status;
        }
        if (request.eventType) {
            queryParams['eventType'] = request.eventType;
        }
        if (request.aggregateUid) {
            queryParams['aggregateUid'] = request.aggregateUid;
        }
        return this.api
            .get<ApiResponse<Page<CommerceNotificationOutboxRow>>>(
                'commerceAdminNotificationOutbox',
                undefined,
                queryParams
            )
            .pipe(map((response) => response.data));
    }

    override getById(
        _id: number,
        _params?: Record<string, string | number>
    ): Observable<CommerceNotificationOutboxRow> {
        return readOnlyOperation();
    }

    getOutbox(outboxUid: string): Observable<CommerceNotificationOutboxRow> {
        return this.api
            .get<ApiResponse<CommerceNotificationOutboxRow>>(
                'commerceAdminNotificationOutboxByUid',
                { outboxUid }
            )
            .pipe(map((response) => response.data));
    }

    retry(outboxUid: string): Observable<CommerceNotificationOutboxRow> {
        return this.api
            .post<ApiResponse<CommerceNotificationOutboxRow>>(
                'commerceAdminNotificationOutboxRetry',
                {},
                { outboxUid }
            )
            .pipe(map((response) => response.data));
    }

    override create(
        _dto: Partial<CommerceNotificationOutboxRow>
    ): Observable<CommerceNotificationOutboxRow> {
        return readOnlyOperation();
    }

    override update(
        _id: number,
        _dto: Partial<CommerceNotificationOutboxRow>
    ): Observable<CommerceNotificationOutboxRow> {
        return readOnlyOperation();
    }

    override delete(_id: number): Observable<void> {
        return readOnlyOperation();
    }
}

@Injectable({ providedIn: 'root' })
export class CommerceAdminNotificationTemplateService {
    readonly #api = inject(ApiClientService);

    list(filters: {
        eventType?: string;
        channel?: string;
        language?: string;
        active?: boolean | null;
    } = {}): Observable<CommerceNotificationTemplate[]> {
        const queryParams: Record<string, string | boolean | null> = {
            eventType: filters.eventType || null,
            channel: filters.channel || null,
            language: filters.language || null,
            active: filters.active ?? null,
        };
        return this.#api
            .get<ApiResponse<CommerceNotificationTemplate[]>>(
                'commerceAdminNotificationTemplates',
                undefined,
                queryParams
            )
            .pipe(map((response) => response.data));
    }

    getTemplate(templateUid: string): Observable<CommerceNotificationTemplate> {
        return this.#api
            .get<ApiResponse<CommerceNotificationTemplate>>(
                'commerceAdminNotificationTemplateByUid',
                { templateUid }
            )
            .pipe(map((response) => response.data));
    }

    update(
        templateUid: string,
        request: CommerceNotificationTemplateRequest
    ): Observable<CommerceNotificationTemplate> {
        return this.#api
            .put<ApiResponse<CommerceNotificationTemplate>>(
                'commerceAdminNotificationTemplateByUid',
                request,
                { templateUid }
            )
            .pipe(map((response) => response.data));
    }

    preview(templateUid: string): Observable<CommerceNotificationTemplatePreview> {
        return this.#api
            .get<ApiResponse<CommerceNotificationTemplatePreview>>(
                'commerceAdminNotificationTemplatePreview',
                { templateUid }
            )
            .pipe(map((response) => response.data));
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
