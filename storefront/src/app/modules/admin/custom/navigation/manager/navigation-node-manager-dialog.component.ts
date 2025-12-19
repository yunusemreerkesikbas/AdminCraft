import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaEmptyStateComponent } from '@shared/components/custom-ui/spa-empty-state/spa-empty-state.component';
import { SpaReorderListComponent } from '@shared/components/custom-ui/spa-reorder-list/spa-reorder-list.component';
import { SpaDialogBase } from '@shared/components/spa-dialog-base/spa-dialog-base.directive';
import { SpaDialogData } from '@shared/components/spa-dialog-base/spa-dialog-base.types';
import { SpaDialogComponent } from '@shared/components/spa-dialog/spa-dialog.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { NavigationEntryDialogComponent } from '../dialogs/entry-dialog/entry-dialog.component';
import { NavigationNodeDialogComponent } from '../dialogs/node-dialog/node-dialog.component';
import { NavigationNodeService } from '../navigation-node.service';
import { NavigationNode } from '../navigation-node.types';

export interface NavigationNodeManagerData extends SpaDialogData {
    nodeId: number;
}

@Component({
    selector: 'app-navigation-node-manager-dialog',
    standalone: true,
    imports: [
        CommonModule,
        TranslocoModule,
        MatDialogModule,
        MatButtonModule,
        MatIconModule,
        MatTooltipModule,
        MatTabsModule,
        SpaDialogComponent,
        SpaEmptyStateComponent,
        SpaReorderListComponent
    ],
    templateUrl: './navigation-node-manager-dialog.component.html'
})
export class NavigationNodeManagerDialogComponent extends SpaDialogBase<void, NavigationNodeManagerData> implements OnInit {
    readonly #navigationNodeService = inject(NavigationNodeService);
    readonly #matDialog = inject(MatDialog);
    readonly #notificationService = inject(NotificationService);

    currentNodeSig = signal<NavigationNode | null>(null);
    breadcrumbsSig = signal<NavigationNode[]>([]);
    isLoadingSig = signal<boolean>(true);
    rootNodeId: number = this.data?.nodeId || 0;

    ngOnInit(): void {
        if (this.rootNodeId) {
            this.loadNode(this.rootNodeId);
        }
    }

    loadNode(id: number, addToBreadcrumbs = false): void {
        this.isLoadingSig.set(true);
        this.#navigationNodeService.getTree(id).subscribe({
            next: (node) => {
                this.currentNodeSig.set(node);
                
                if (addToBreadcrumbs) {
                    this.breadcrumbsSig.update(prev => [...prev, node]);
                } else if (id === this.rootNodeId) {
                    this.breadcrumbsSig.set([node]);
                }
                
                this.isLoadingSig.set(false);
            },
            error: () => this.isLoadingSig.set(false)
        });
    }

    navigateUp(node: NavigationNode): void {
        const index = this.breadcrumbsSig().findIndex(b => b.id === node.id);
        if (index !== -1) {
            this.breadcrumbsSig.update(prev => prev.slice(0, index + 1));
            this.loadNode(node.id);
        }
    }

    drillDown(childNode: NavigationNode): void {
        // Optimistically add to breadcrumb for UI snap, although loadNode handles real data
        // Actually loadNode(addToBreadcrumbs=true) does the fetch then update.
        // But for drillDown we strictly want to fetch the new tree.
        // Important: getTree(id) returns the node and ITS children.
        // The childNode passed here is just the summary from the list.
        this.loadNode(childNode.id, true);
    }

    openCreateChildDialog(): void {
        const parentId = this.currentNodeSig()?.id;
        if (!parentId) return;

        this.#matDialog.open(NavigationNodeDialogComponent, {
            width: '600px',
            data: { mode: 'create', parentId }
        }).afterClosed().subscribe(result => {
             if (result) this.refreshCurrent();
        });
    }

    openEditNodeDialog(node: NavigationNode): void {
        this.#matDialog.open(NavigationNodeDialogComponent, {
            width: '600px',
            data: { mode: 'edit', node, parentId: this.currentNodeSig()?.id }
        }).afterClosed().subscribe(result => {
             if (result) this.refreshCurrent();
        });
    }

    deleteNode(node: NavigationNode): void {
        this.#navigationNodeService.deleteNode(node.id).subscribe({
            next: () => {
                this.#notificationService.success('admin.navigation.messages.successDeleteNode');
                this.refreshCurrent();
            },
            error: (err) => {
                this.#notificationService.alert(err.error?.message || 'admin.common.error');
            }
        });
    }

    onReorderNodes(newOrder: NavigationNode[]): void {
        const parentId = this.currentNodeSig()?.id;
        if (!parentId) return;
        
        const ids = newOrder.map(n => n.id);
        this.#navigationNodeService.reorderNodes(parentId, ids).subscribe();
    }

    openCreateEntryDialog(): void {
        const nodeId = this.currentNodeSig()?.id;
        if (!nodeId) return;

        this.#matDialog.open(NavigationEntryDialogComponent, {
            width: '600px',
            data: { mode: 'create', nodeId }
        }).afterClosed().subscribe(result => {
             if (result) this.refreshCurrent();
        });
    }

    openEditEntryDialog(entry: any): void {
        this.#matDialog.open(NavigationEntryDialogComponent, {
            width: '600px',
            data: { mode: 'edit', entry, nodeId: this.currentNodeSig()?.id }
        }).afterClosed().subscribe(result => {
             if (result) this.refreshCurrent();
        });
    }

    deleteEntry(entry: any): void {
        this.#navigationNodeService.deleteEntry(entry.id).subscribe({
             next: () => {
                 this.#notificationService.success('admin.navigation.messages.successDeleteEntry');
                 this.refreshCurrent();
             },
             error: (err) => {
                 this.#notificationService.alert(err.error?.message || 'admin.common.error');
             }
         });
    }

    onReorderEntries(newOrder: any[]): void {
        const nodeId = this.currentNodeSig()?.id;
        if (!nodeId) return;

        const ids = newOrder.map(e => e.id);
        this.#navigationNodeService.reorderEntries(nodeId, ids).subscribe();
    }

    refreshCurrent(): void {
        const current = this.currentNodeSig();
        if (current) {
            this.isLoadingSig.set(true);
            this.#navigationNodeService.getTree(current.id).subscribe(updatedNode => {
                 this.currentNodeSig.set(updatedNode);
                 this.isLoadingSig.set(false);
            });
        }
    }

    closeDialog(): void {
        this.close();
    }
}
