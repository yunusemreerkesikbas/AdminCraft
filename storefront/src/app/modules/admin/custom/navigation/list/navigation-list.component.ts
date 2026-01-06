import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { AdminPageHeaderComponent } from '@shared/components/admin-page-header/admin-page-header.component';
import { GridAction, GridColumn, SpaAdminGridComponent } from '@shared/components/spa-admin-grid';
import { SpaGenericModalComponent } from '@shared/components/spa-generic-modal/spa-generic-modal.component';
import { NavigationNodeDialogComponent } from '../dialogs/node-dialog/node-dialog.component';
import { NavigationNodeManagerDialogComponent } from '../manager/navigation-node-manager-dialog.component';
import { NavigationNodeService } from '../navigation-node.service';
import { NavigationNode } from '../navigation-node.types';

@Component({
    selector: 'app-navigation-list',
    templateUrl: './navigation-list.component.html',
    standalone: true,
    imports: [
        CommonModule,
        RouterModule,
        MatButtonModule,
        MatIconModule,
        TranslocoModule,
        AdminPageHeaderComponent,
        SpaAdminGridComponent
    ],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class NavigationListComponent implements OnInit {
    #navigationNodeService = inject(NavigationNodeService);
    #matDialog = inject(MatDialog);

    protected rootNodesSig = signal<NavigationNode[]>([]);
    protected isLoadingSig = signal<boolean>(true);

    protected columns: GridColumn<NavigationNode>[] = [
        {
            key: 'title',
            label: 'admin.common.grid.name',
            type: 'text',
            width: 'auto',
            getSecondaryValue: (node) => {
                const childCount = node.children?.length;
                return childCount ? `${node.uid} (${childCount} submenu)` : node.uid;
            }
        },
        {
            key: 'status',
            label: 'admin.common.grid.status',
            type: 'status',
            getValue: (node) => node.isVisible ? 'ACTIVE' : 'INACTIVE',
            width: '120px',
            hideOn: 'sm'
        }
    ];

    protected actions: GridAction<NavigationNode>[] = [
        {
            icon: 'heroicons_outline:pencil-square',
            label: 'admin.common.manage',
            action: 'manage'
        },
        {
            icon: 'heroicons_outline:trash',
            label: 'admin.common.delete',
            action: 'delete',
            color: 'warn'
        }
    ];

    ngOnInit(): void {
        this.#loadRoots();
    }

    #loadRoots(): void {
        this.isLoadingSig.set(true);
        this.#navigationNodeService.getAllRoots().subscribe({
            next: (nodes) => {
                this.rootNodesSig.set(nodes);
                this.isLoadingSig.set(false);
            },
            error: () => this.isLoadingSig.set(false)
        });
    }

    protected onGridAction(event: { action: string; item: NavigationNode }): void {
        switch (event.action) {
            case 'manage':
                this.openNodeManager(event.item);
                break;
            case 'delete':
                this.deleteNode(event.item);
                break;
        }
    }



    deleteNode(node: NavigationNode): void {
        const dialogRef = this.#matDialog.open(SpaGenericModalComponent, {
            data: {
                title: 'admin.navigation.actions.deleteNode',
                message: 'admin.navigation.messages.confirmDeleteNode',
                variant: 'confirmation',
                type: 'error',
                actions: [
                    { label: 'admin.common.cancel', value: false },
                    { label: 'admin.common.delete', value: true, color: 'warn' }
                ]
            } as any
        });

        dialogRef.afterClosed().subscribe((result) => {
            if (result) {
                this.isLoadingSig.set(true);
                this.#navigationNodeService.deleteNode(node.id).subscribe({
                    next: () => {
                        this.#loadRoots();
                    },
                    error: () => this.isLoadingSig.set(false)
                });
            }
        });
    }

    openCreateDialog(): void {
        this.#matDialog.open(NavigationNodeDialogComponent, {
            width: '700px',
            data: {
                mode: 'create',
                parentId: null
            }
        }).afterClosed().subscribe((result) => {
            if (result) {
                this.#loadRoots();
            }
        });
    }

    openNodeManager(node: NavigationNode): void {
        this.#matDialog.open(NavigationNodeManagerDialogComponent, {
            width: '900px',
            height: '80vh',
            data: { nodeId: node.id }
        }).afterClosed().subscribe((result) => {
            if (result) {
                this.#loadRoots();
            }
        });
    }
}
