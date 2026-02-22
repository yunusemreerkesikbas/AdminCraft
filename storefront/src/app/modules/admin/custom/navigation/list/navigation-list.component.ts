import { NestedTreeControl } from '@angular/cdk/tree';
import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    DestroyRef,
    inject,
    OnInit
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatTreeModule, MatTreeNestedDataSource } from '@angular/material/tree';
import { TranslocoModule } from '@jsverse/transloco';
import { AdminPageHeaderComponent } from '@shared/components/admin-page-header/admin-page-header.component';
import { SpaEmptyStateComponent } from '@shared/components/custom-ui/spa-empty-state/spa-empty-state.component';
import { SpaGenericModalComponent } from '@shared/components/spa-generic-modal/spa-generic-modal.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { take } from 'rxjs';
import { NavigationNodeDialogComponent } from '../dialogs/node-dialog/node-dialog.component';
import { NavigationNodeManagerDialogComponent } from '../manager/navigation-node-manager-dialog.component';
import { NavigationNodeService } from '../navigation-node.service';
import { NavigationNode } from '../navigation-node.types';

@Component({
    selector: 'spa-navigation-list',
    templateUrl: './navigation-list.component.html',
    styleUrls: ['./navigation-list.component.scss'],
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        TranslocoModule,
        MatTreeModule,
        MatButtonModule,
        MatIconModule,
        MatTooltipModule,
        AdminPageHeaderComponent,
        SpaEmptyStateComponent,
        SpaGenericModalComponent
    ]
})
export class SpaNavigationListComponent implements OnInit {
    #navigationService = inject(NavigationNodeService);
    #matDialog = inject(MatDialog);
    #notificationService = inject(NotificationService);
    #cdr = inject(ChangeDetectorRef);
    #destroyRef = inject(DestroyRef);

    treeControl = new NestedTreeControl<NavigationNode>(node => node.children);
    dataSource = new MatTreeNestedDataSource<NavigationNode>();

    hasChild = (_: number, node: NavigationNode) =>
        node.children === undefined ? true : node.children.length > 0;

    ngOnInit(): void {
        this.loadRoots();
        this.treeControl.expansionModel.changed
            .pipe(takeUntilDestroyed(this.#destroyRef))
            .subscribe(() => {
                this.treeControl.expansionModel.selected.forEach((node) => {
                    if (node.children === undefined) {
                        this.loadChildren(node);
                    }
                });
                this.#cdr.markForCheck();
            });
    }

    loadRoots(): void {
        this.#navigationService
            .searchPaged({ page: 0, size: 100, sort: 'createdAt,desc' })
            .pipe(take(1))
            .subscribe({
                next: (page) => {
                    const content = page?.content ?? [];
                    this.dataSource.data = content;
                    this.treeControl.dataNodes = content;
                    this.#cdr.markForCheck();
                },
                error: () =>
                    this.#notificationService.alert('admin.navigation.errors.loadFailed')
            });
    }

    loadChildren(node: NavigationNode): void {
        if (node.children !== undefined) {
            return;
        }
        this.#navigationService.getTree(node.id).pipe(take(1)).subscribe({
            next: (fullNode) => {
                node.children = fullNode.children ?? [];
                this.#cdr.markForCheck();
            },
            error: () =>
                this.#notificationService.alert('admin.navigation.errors.loadFailed')
        });
    }

    openCreateDialog(parentId?: number | null): void {
        this.#matDialog
            .open(NavigationNodeDialogComponent, {
                width: '700px',
                data: {
                    mode: 'create',
                    parentId: parentId ?? null
                }
            })
            .afterClosed()
            .pipe(take(1))
            .subscribe((result) => {
                if (result) {
                    this.loadRoots();
                }
            });
    }

    openNodeManager(node: NavigationNode): void {
        this.#matDialog
            .open(NavigationNodeManagerDialogComponent, {
                width: '900px',
                height: '80vh',
                data: { nodeId: node.id }
            })
            .afterClosed()
            .pipe(take(1))
            .subscribe((result) => {
                if (result) {
                    this.loadRoots();
                }
            });
    }

    confirmDelete(node: NavigationNode): void {
        const dialogRef = this.#matDialog.open(SpaGenericModalComponent, {
            data: {
                title: 'admin.navigation.actions.deleteNode',
                message: 'admin.navigation.nodes.confirmDelete',
                variant: 'confirmation',
                type: 'error',
                actions: [
                    { label: 'admin.common.cancel', value: false },
                    { label: 'admin.common.delete', value: true, color: 'warn' }
                ]
            } as never
        });

        dialogRef
            .afterClosed()
            .pipe(take(1))
            .subscribe((result) => {
                if (result) {
                    this.#navigationService.deleteNode(node.id).pipe(take(1)).subscribe({
                        next: () => {
                            this.#notificationService.success(
                                'admin.common.messages.deleteSuccess'
                            );
                            this.loadRoots();
                        },
                        error: (err) =>
                            this.#notificationService.alert(
                                err?.error?.message ?? 'admin.common.errors.deleteFailed'
                            )
                    });
                }
            });
    }
}
