import { Injectable, inject } from '@angular/core';
import { ApiClientService } from '@core/api/api-client.service';
import { ApiResponse } from '@core/crud/api.types';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import {
    PreviewTicketIssueRequest,
    PreviewTicketResponse,
    SmartEditDraftOverview,
} from '../smartedit.types';

@Injectable({ providedIn: 'root' })
export class SmartEditPreviewService {
    readonly #api = inject(ApiClientService);

    issueTicket(pageId?: number): Observable<PreviewTicketResponse> {
        const body: PreviewTicketIssueRequest = pageId !== undefined ? { pageId } : {};
        return this.#api
            .post<ApiResponse<PreviewTicketResponse>>('cmsPreviewTickets', body)
            .pipe(
                map((response) => {
                    if (!response.data) throw new Error(response.message);
                    return response.data;
                }),
            );
    }

    listPageDrafts(pageId: number, language: string): Observable<SmartEditDraftOverview> {
        return this.#api
            .get<ApiResponse<SmartEditDraftOverview>>('cmsPreviewPageDrafts', { pageId }, { language })
            .pipe(map((response) => this.#requireData(response)));
    }

    getPublishReview(pageId: number, language: string): Observable<SmartEditDraftOverview> {
        return this.#api
            .get<ApiResponse<SmartEditDraftOverview>>('cmsPreviewPublishReview', { pageId }, { language })
            .pipe(map((response) => this.#requireData(response)));
    }

    discardDraft(draftId: number): Observable<ApiResponse<void>> {
        return this.#api.delete<ApiResponse<void>>('cmsPreviewDraftById', { draftId });
    }

    discardPageDrafts(pageId: number, language: string): Observable<ApiResponse<{ deletedCount: number }>> {
        return this.#api.delete<ApiResponse<{ deletedCount: number }>>(
            'cmsPreviewPageDrafts',
            { pageId },
            { language },
        );
    }

    #requireData<T>(response: ApiResponse<T>): T {
        if (!response.data) {
            throw new Error(response.message);
        }
        return response.data;
    }
}
