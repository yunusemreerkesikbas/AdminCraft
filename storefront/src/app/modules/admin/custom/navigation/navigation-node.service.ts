import { Injectable } from '@angular/core';
import { CrudEndpoints, CrudHttpService } from '@core/crud';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import {
    CreateEntryCompositeRequest,
    CreateEntryRequest,
    CreateNodeCompositeRequest,
    CreateNodeRequest,
    EntryCompositeResponse,
    EntryI18nRequest,
    NavigationEntry,
    NavigationEntryI18n,
    NavigationNode,
    NavigationNodeI18n,
    NodeCompositeResponse,
    NodeI18nRequest,
    ReorderRequest,
    UpdateEntryCompositeRequest,
    UpdateEntryRequest,
    UpdateNodeCompositeRequest,
    UpdateNodeRequest
} from './navigation-node.types';

@Injectable({ providedIn: 'root' })
export class NavigationNodeService extends CrudHttpService<
    NavigationNode,
    CreateNodeRequest,
    UpdateNodeRequest
> {
    protected endpoints: CrudEndpoints = {
        list: 'navigationNodes',
        getById: 'navigationNodeById',
        create: 'navigationNodes',
        update: 'navigationNodeById',
        delete: 'navigationNodeById'
    };

    getAllRoots(): Observable<NavigationNode[]> {
        return this.list();
    }

    getTree(id: number): Observable<NavigationNode> {
        return this.customGet<NavigationNode>('navigationNodeTree', { id });
    }

    override create(data: CreateNodeRequest): Observable<NavigationNode> {
        if (data.parentId) {
            return this.customPost<NavigationNode>(
                'navigationNodeChildren',
                data,
                { id: data.parentId }
            );
        }
        return super.create(data);
    }
    
    createNode(data: CreateNodeRequest): Observable<NavigationNode> {
        return this.create(data);
    }
    updateNode(id: number, data: UpdateNodeRequest): Observable<NavigationNode> {
        return this.update(id, data);
    }

    deleteNode(id: number): Observable<void> {
        return this.delete(id);
    }

    reorderNodes(parentId: number, itemIds: number[]): Observable<void> {
        return this.customPut<void>(
            'navigationNodesReorder',
            { items: itemIds } as ReorderRequest,
            { id: parentId }
        ).pipe(map(() => undefined));
    }

    getNodeI18n(nodeId: number, language: string): Observable<NavigationNodeI18n> {
        return this.customGet<NavigationNodeI18n>(
            'navigationNodeI18n',
            { id: nodeId, language }
        );
    }

    upsertNodeI18n(nodeId: number, language: string, data: NodeI18nRequest): Observable<NavigationNodeI18n> {
        return this.customPut<NavigationNodeI18n>(
            'navigationNodeI18n',
            data,
            { id: nodeId, language }
        );
    }

    createEntry(data: CreateEntryRequest): Observable<NavigationEntry> {
        return this.customPost<NavigationEntry>('navigationEntries', data);
    }

    updateEntry(id: number, data: UpdateEntryRequest): Observable<NavigationEntry> {
        return this.customPut<NavigationEntry>('navigationEntryById', data, { id });
    }

    deleteEntry(id: number): Observable<void> {
        return this.customDelete<void>('navigationEntryById', { id });
    }

    reorderEntries(nodeId: number, itemIds: number[]): Observable<void> {
        return this.customPut<void>(
            'navigationEntryReorder',
            { items: itemIds } as ReorderRequest,
            { nodeId }
        ).pipe(map(() => undefined));
    }

    getEntryI18n(entryId: number, language: string): Observable<NavigationEntryI18n> {
        return this.customGet<NavigationEntryI18n>(
            'navigationEntryI18n',
            { id: entryId, language }
        );
    }

    upsertEntryI18n(entryId: number, language: string, data: EntryI18nRequest): Observable<NavigationEntryI18n> {
        return this.customPut<NavigationEntryI18n>(
            'navigationEntryI18n',
            data,
            { id: entryId, language }
        );
    }

    createNodeComposite(data: CreateNodeCompositeRequest): Observable<NodeCompositeResponse> {
        return this.customPost<NodeCompositeResponse>('navigationNodeComposite', data);
    }

    updateNodeComposite(id: number, data: UpdateNodeCompositeRequest): Observable<NodeCompositeResponse> {
        return this.customPut<NodeCompositeResponse>('navigationNodeCompositeById', data, { id });
    }

    createEntryComposite(data: CreateEntryCompositeRequest): Observable<EntryCompositeResponse> {
        return this.customPost<EntryCompositeResponse>('navigationEntryComposite', data);
    }

    updateEntryComposite(id: number, data: UpdateEntryCompositeRequest): Observable<EntryCompositeResponse> {
        return this.customPut<EntryCompositeResponse>('navigationEntryCompositeById', data, { id });
    }

    getNodeComposite(id: number): Observable<NodeCompositeResponse> {
        return this.customGet<NodeCompositeResponse>('navigationNodeCompositeById', { id });
    }

    getEntryComposite(id: number): Observable<EntryCompositeResponse> {
        return this.customGet<EntryCompositeResponse>('navigationEntryCompositeById', { id });
    }
}
