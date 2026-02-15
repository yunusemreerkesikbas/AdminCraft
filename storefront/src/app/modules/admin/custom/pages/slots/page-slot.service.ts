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
        const request: ReorderSlotComponentsRequest = { items: componentIds };
        return this.#api
            .put<ApiResponse<void>>('pageSlotComponentsReorder', request, { id: pageId, slotName })
            .pipe(
                take(1),
                map(() => void 0)
            );
    }

    getSharedSlots(): Observable<PageSlotResponse[]> {
        return this.#api
            .get<ApiResponse<PageSlotResponse[]>>('sharedSlots')
            .pipe(
                take(1),
                map(response => response.data)
            );
    }
}
