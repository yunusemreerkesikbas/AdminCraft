import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule } from '@angular/material/paginator';
import { CrudHttpService, CrudStore } from '@core/crud';
import { BasePaginatedListComponent } from '@core/crud/base-paginated-list.component';
import { TranslocoModule } from '@jsverse/transloco';
import { AdminPageHeaderComponent } from '@shared/components/admin-page-header/admin-page-header.component';
import { GridAction, GridColumn, SpaAdminGridComponent } from '@shared/components/spa-admin-grid';
import { SpaAdminPaginatorComponent } from '@shared/components/spa-admin-paginator/spa-admin-paginator.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { ConfirmationService } from '@shared/services/confirmation.service';
import { take } from 'rxjs';
import { CreateProductTypeRequest, ProductType, UpdateProductTypeRequest } from '../models/product-type.types';
import { ProductTypeService } from '../services/product-type.service';
import { ProductTypeEditDialogComponent } from './product-type-edit-dialog/product-type-edit-dialog.component';

@Component({
    selector: 'spa-product-type-list',
    templateUrl: './product-type-list.component.html',
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        CommonModule,
        TranslocoModule,
        AdminPageHeaderComponent,
        SpaAdminGridComponent,
        SpaAdminPaginatorComponent,
        MatButtonModule,
        MatIconModule,
        MatPaginatorModule,
        FormsModule,
        ReactiveFormsModule
    ]
})
export class ProductTypeListComponent extends BasePaginatedListComponent<ProductType, CreateProductTypeRequest, UpdateProductTypeRequest> {
    protected override service = inject(ProductTypeService) as unknown as CrudHttpService<ProductType, CreateProductTypeRequest, UpdateProductTypeRequest>;
    protected override store = new CrudStore<ProductType>();
    protected override defaultSort = 'createdAt,desc';
    protected override defaultPageSize = 20;
    
    #matDialog = inject(MatDialog);
    #confirmationService = inject(ConfirmationService);
    #notificationService = inject(NotificationService);

    protected columns: GridColumn<ProductType>[] = [
        { key: 'name', label: 'admin.common.grid.name', type: 'text' },
        { key: 'code', label: 'admin.common.grid.code', type: 'text' },
        { key: 'category', label: 'admin.products.types.fields.category', type: 'text' }
    ];

    protected actions: GridAction<ProductType>[] = [
        {
            icon: 'heroicons_outline:pencil-square',
            label: 'admin.common.actions.edit',
            action: 'edit'
        },
        {
            icon: 'heroicons_outline:trash',
            label: 'admin.common.actions.delete',
            action: 'delete',
            color: 'warn'
        }
    ];

    protected override onInit(): void {
        // Base class overrides
    }

    protected onGridAction(event: { action: string; item: ProductType }): void {
        if (event.action === 'edit') {
            this.editType(event.item);
        } else if (event.action === 'delete') {
            this.deleteType(event.item);
        }
    }

    createType(): void {
        this.#openDialog('create');
    }

    editType(item: ProductType): void {
        this.#openDialog('edit', item);
    }

    deleteType(item: ProductType): void {
        this.#confirmationService.confirm('admin.products.types.delete.title', 'admin.products.types.delete.message')
            .pipe(take(1))
            .subscribe(confirmed => {
                if (confirmed) {
                    this.deleteItem(item);
                }
            });
    }

    #openDialog(mode: 'create' | 'edit', item?: ProductType): void {
        this.#matDialog.open(ProductTypeEditDialogComponent, {
            width: '800px',
            disableClose: true,
            data: { mode, item }
        }).afterClosed().pipe(take(1)).subscribe(result => {
            if (result) {
                this.loadItems();
            }
        });
    }

    protected override onDeleteSuccess(item: ProductType): void {
        this.#notificationService.success('admin.common.messages.deleteSuccess');
    }

    protected override onDeleteError(error: any): void {
        this.#notificationService.alert(error?.error?.message || 'admin.common.errors.deleteFailed');
    }
}
