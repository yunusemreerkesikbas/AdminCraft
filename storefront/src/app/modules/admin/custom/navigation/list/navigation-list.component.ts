import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterModule } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { AdminPageHeaderComponent } from '@shared/components/admin-page-header/admin-page-header.component';
import { SpaEmptyStateComponent } from '@shared/components/custom-ui/spa-empty-state/spa-empty-state.component';
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
        MatProgressSpinnerModule,
        MatTooltipModule,
        TranslocoModule,
        AdminPageHeaderComponent,
        SpaEmptyStateComponent
    ],
    changeDetection: ChangeDetectionStrategy.OnPush,
    styles: [
        `
            .inventory-grid {
                grid-template-columns: auto 80px 100px;

                @screen md {
                    grid-template-columns: auto 100px 150px;
                }

                @screen lg {
                    grid-template-columns: auto 120px 200px;
                }
            }
        `
    ]
})
export class NavigationListComponent implements OnInit {
    #navigationNodeService = inject(NavigationNodeService);
    #matDialog = inject(MatDialog);

    protected rootNodesSig = signal<NavigationNode[]>([]);
    protected isLoadingSig = signal<boolean>(true);

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
            // Only refresh if a node was created
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
            // Only refresh if changes were made inside the manager
            if (result) {
                this.#loadRoots();
            }
        });
    }
}


