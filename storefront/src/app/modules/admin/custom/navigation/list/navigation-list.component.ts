
import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, OnInit } from '@angular/core';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule } from '@angular/material/paginator';
import { BasePaginatedListComponent } from '@core/crud/base-paginated-list.component';
import { TranslocoModule } from '@jsverse/transloco';
import { AdminPageHeaderComponent } from '@shared/components/admin-page-header/admin-page-header.component';
import { SpaSearchInputComponent } from '@shared/components/custom-ui/spa-search-input/spa-search-input.component';
import { GridAction, GridColumn, SpaAdminGridComponent } from '@shared/components/spa-admin-grid';
import { SpaAdminPaginatorComponent } from '@shared/components/spa-admin-paginator/spa-admin-paginator.component';
import { SpaAdminSortDropdownComponent } from '@shared/components/spa-admin-sort-dropdown/spa-admin-sort-dropdown.component';
import { SpaGenericModalComponent } from '@shared/components/spa-generic-modal/spa-generic-modal.component';
import { takeUntil } from 'rxjs';
import { NavigationNodeDialogComponent } from '../dialogs/node-dialog/node-dialog.component';
import { NavigationNodeManagerDialogComponent } from '../manager/navigation-node-manager-dialog.component';
import { NavigationNodeService } from '../navigation-node.service';
import { CreateNodeRequest, NavigationNode, UpdateNodeRequest } from '../navigation-node.types';
import { NavigationStore } from '../navigation.store';

@Component({
    selector: 'app-navigation-list',
    templateUrl: './navigation-list.component.html',
    standalone: true,
    imports: [
        CommonModule,
        TranslocoModule,
        AdminPageHeaderComponent,
        SpaAdminGridComponent,
        SpaAdminPaginatorComponent,
        SpaAdminSortDropdownComponent,
        SpaSearchInputComponent,
        MatPaginatorModule,
        MatButtonModule,
        MatIconModule,
        FormsModule,
        ReactiveFormsModule
    ],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class NavigationListComponent extends BasePaginatedListComponent<
    NavigationNode,
    CreateNodeRequest,
    UpdateNodeRequest
> implements OnInit {
    protected override service = inject(NavigationNodeService);
    protected override store = inject(NavigationStore);
    protected override readonly defaultSort = 'createdAt,desc';
    protected override readonly defaultPageSize = 20;

    #matDialog = inject(MatDialog);

    protected searchInputControl = new FormControl('');
    protected paginatedItemsSig = computed(() => this.store.items());

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

    protected override onInit(): void {
        this.#setupSearchDebounce();
    }

    #setupSearchDebounce(): void {
        this.searchInputControl.valueChanges.pipe(
            takeUntil(this.destroy$)
        ).subscribe(query => {
            this.onSearchInput(query || '');

        });
    }

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

    protected onGridAction(event: { action: string; item: NavigationNode }): void {
        switch (event.action) {
            case 'manage':
                this.openNodeManager(event.item);
                break;
            case 'delete':
                this.#confirmDelete(event.item);
                break;
        }
    }

    #confirmDelete(node: NavigationNode): void {
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
                this.deleteItem(node);
            }
        });
    }

    protected override onDeleteSuccess(item: NavigationNode): void {
        // Notification handled by base class, just refresh list
        this.loadItems();
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
                this.loadItems();
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
                this.loadItems();
            }
        });
    }
}
