import { Injectable, inject } from '@angular/core';
import { ApiClientService } from '@core/api/api-client.service';
import { ApiResponse } from '@core/crud/api.types';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import {
    PreviewTicketIssueRequest,
    PreviewTicketResponse,
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
}
