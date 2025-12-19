import { inject, Injectable } from '@angular/core';
import { ApiClientService } from '@core/api/api-client.service';
import { ApiResponse } from '@core/crud';
import { map, Observable, take } from 'rxjs';
import { CreateEntryRequest, CreateNodeRequest, NavigationEntry, NavigationNode, ReorderRequest, UpdateEntryRequest, UpdateNodeRequest } from './navigation-node.types';

@Injectable({ providedIn: 'root' })
export class NavigationNodeService {
    readonly #api = inject(ApiClientService);

    getAllRoots(): Observable<NavigationNode[]> {
        return this.#api
            .get<ApiResponse<NavigationNode[]>>('navigationNodes')
            .pipe(
                take(1),
                map(response => response.data)
            );
    }

    getTree(id: number): Observable<NavigationNode> {
        return this.#api
            .get<ApiResponse<NavigationNode>>('navigationNodeTree', { id })
            .pipe(
                take(1),
                map(response => response.data)
            );
    }

    createNode(data: CreateNodeRequest): Observable<NavigationNode> {
        if (data.parentId) {
            return this.#api
                .post<ApiResponse<NavigationNode>>('navigationNodeChildren', data, { id: data.parentId })
                .pipe(
                    take(1),
                    map(response => response.data)
                );
        }

        return this.#api
            .post<ApiResponse<NavigationNode>>('navigationNodes', data)
            .pipe(
                take(1),
                map(response => response.data)
            );
    }

    updateNode(id: number, data: UpdateNodeRequest): Observable<NavigationNode> {
        return this.#api
            .put<ApiResponse<NavigationNode>>('navigationNodeById', data, { id })
            .pipe(
                take(1),
                map(response => response.data)
            );
    }

    deleteNode(id: number): Observable<void> {
        return this.#api
            .delete<ApiResponse<void>>('navigationNodeById', { id })
            .pipe(
                take(1),
                map(() => void 0)
            );
    }

    reorderNodes(parentId: number, itemIds: number[]): Observable<void> {
        return this.#api
            .put<ApiResponse<void>>('navigationNodesReorder', { items: itemIds } as ReorderRequest, { id: parentId })
            .pipe(
                take(1),
                map(() => void 0)
            );
    }

    // --- Entries ---

    createEntry(data: CreateEntryRequest): Observable<NavigationEntry> {
        return this.#api
            .post<ApiResponse<NavigationEntry>>('navigationEntries', data)
            .pipe(
                take(1),
                map(response => response.data)
            );
    }

    updateEntry(id: number, data: UpdateEntryRequest): Observable<NavigationEntry> {
        return this.#api
            .put<ApiResponse<NavigationEntry>>('navigationEntryById', data, { id })
            .pipe(
                take(1),
                map(response => response.data)
            );
    }

    deleteEntry(id: number): Observable<void> {
        return this.#api
            .delete<ApiResponse<void>>('navigationEntryById', { id })
            .pipe(
                take(1),
                map(() => void 0)
            );
    }

    reorderEntries(nodeId: number, itemIds: number[]): Observable<void> {
        return this.#api
            .put<ApiResponse<void>>('navigationEntryReorder', { items: itemIds } as ReorderRequest, { nodeId })
            .pipe(
                take(1),
                map(() => void 0)
            );
    }
}

