import { inject, Injectable } from '@angular/core';
import { ApiClientService } from '@core/api/api-client.service';
import { ApiResponse } from '@core/crud';
import { map, Observable, take } from 'rxjs';
import { AddComponentToSlotRequest, CreatePageSlotRequest, PageSlotResponse, ReorderSlotComponentsRequest, UpdatePageSlotRequest } from './page-slot.types';

@Injectable({ providedIn: 'root' })
export class PageSlotService {
    readonly #api = inject(ApiClientService);

    getSlots(pageId: number): Observable<PageSlotResponse[]> {
        return this.#api
            .get<ApiResponse<PageSlotResponse[]>>('pageSlots', { id: pageId })
            .pipe(
                take(1),
                map(response => response.data)
            );
    }

    createSlot(pageId: number, request: CreatePageSlotRequest): Observable<PageSlotResponse> {
        return this.#api
            .post<ApiResponse<PageSlotResponse>>('pageSlots', request, { id: pageId })
            .pipe(
                take(1),
                map(response => response.data)
            );
    }

    updateSlot(pageId: number, slotName: string, request: UpdatePageSlotRequest): Observable<PageSlotResponse> {
        return this.#api
            .put<ApiResponse<PageSlotResponse>>('pageSlot', request, { id: pageId, slotName })
            .pipe(
                take(1),
                map(response => response.data)
            );
    }

    deleteSlot(pageId: number, slotName: string): Observable<void> {
        return this.#api
            .delete<ApiResponse<void>>('pageSlot', { id: pageId, slotName })
            .pipe(
                take(1),
                map(() => void 0)
            );
    }

    addComponent(pageId: number, slotName: string, request: AddComponentToSlotRequest): Observable<void> {
        return this.#api
            .post<ApiResponse<void>>('pageSlotComponents', request, { id: pageId, slotName })
            .pipe(
                take(1),
                map(() => void 0)
            );
    }

    removeComponent(pageId: number, slotName: string, componentId: number): Observable<void> {
        return this.#api
            .delete<ApiResponse<void>>('pageSlotComponent', { 
                id: pageId, 
                slotName, 
                componentId 
            })
            .pipe(
                take(1),
                map(() => void 0)
            );
    }

    reorderComponents(pageId: number, slotName: string, componentIds: number[]): Observable<void> {
        const request: ReorderSlotComponentsRequest = { componentIds };
        return this.#api
            .put<ApiResponse<void>>('pageSlotComponentsReorder', request, { id: pageId, slotName })
            .pipe(
                take(1),
                map(() => void 0)
            );
    }

    getSharedSlots(): Observable<PageSlotResponse[]> {
        // Shared slots endpoints might differ, assuming they are filtered query or specific endpoint
        // For now, let's assume we use a specific pageId 'shared' or a query param. 
        // Based on backend walkthrough, it seems shared slots are just slots with page_id=NULL.
        // But the API might need a specific endpoint to list them specifically if not attached to a page.
        // Let's assume there is an endpoint or we filter. The sprint plan mentions:
        // /api/pages/{id}/slots 
        // Logic: Shared slots logic is handled in backend service. 
        // getSlots(pageId) should return both page-specific AND shared slots (merged).
        return new Observable(sub => sub.next([])); // Placeholder if needed explicitly
    }
}
