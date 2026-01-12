import { NestedTreeControl } from '@angular/cdk/tree';
import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTreeModule, MatTreeNestedDataSource } from '@angular/material/tree';
import { TranslocoModule } from '@jsverse/transloco';
import { AdminPageHeaderComponent } from '@shared/components/admin-page-header/admin-page-header.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { ConfirmationService } from '@shared/services/confirmation.service';
import { SpaEmptyStateComponent } from 'app/shared/components/custom-ui/spa-empty-state/spa-empty-state.component';
import { take } from 'rxjs';
import { Category, CategoryTreeResponse } from '../models/category.types';
import { CategoryService } from '../services/category.service';
import { CategoryEditDialogComponent } from './category-edit-dialog/category-edit-dialog.component';

@Component({
    selector: 'spa-category-tree',
    templateUrl: './category-tree.component.html',
    styleUrls: ['./category-tree.component.scss'],
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        CommonModule,
        TranslocoModule,
        MatTreeModule,
        MatButtonModule,
        MatIconModule,
        AdminPageHeaderComponent,
        SpaEmptyStateComponent
    ]
})
export class CategoryTreeComponent implements OnInit {
    #categoryService = inject(CategoryService);
    #matDialog = inject(MatDialog);
    #confirmationService = inject(ConfirmationService);
    #notificationService = inject(NotificationService);

    treeControl = new NestedTreeControl<CategoryTreeResponse>(node => node.children);
    dataSource = new MatTreeNestedDataSource<CategoryTreeResponse>();
    
    flatCategories: Category[] = [];

    hasChild = (_: number, node: CategoryTreeResponse) => !!node.children && node.children.length > 0;

    ngOnInit(): void {
        this.loadTree();
    }

    loadTree(): void {
        this.#categoryService.getTree().pipe(take(1)).subscribe({
            next: (tree) => {
                this.dataSource.data = tree;
                this.flatCategories = this.#flattenTree(tree);
            },
            error: () => this.#notificationService.alert('admin.products.categories.errors.loadFailed')
        });
    }

    #flattenTree(tree: CategoryTreeResponse[]): Category[] {
        const result: Category[] = [];
        const stack = [...tree];
        while (stack.length) {
            const node = stack.pop()!;
            result.push({
                id: node.id,
                uid: node.uid,
                code: node.code,
                name: node.name,
                parentId: node.parentId,
                sortOrder: node.sortOrder,
                isVisible: node.isVisible
            });
            if (node.children) {
                stack.push(...node.children);
            }
        }
        return result;
    }

    createCategory(parentId?: number): void {
        this.#openDialog('create', undefined, parentId);
    }

    editCategory(node: CategoryTreeResponse): void {
        const item: Category = {
            id: node.id,
            uid: node.uid,
            code: node.code,
            name: node.name,
            parentId: node.parentId,
            sortOrder: node.sortOrder,
            isVisible: node.isVisible
        };
        this.#openDialog('edit', item);
    }

    deleteCategory(node: CategoryTreeResponse): void {
        if (node.children && node.children.length > 0) {
            this.#notificationService.warning('admin.products.categories.errors.hasChildren');
            return;
        }

        this.#confirmationService.confirm('admin.products.categories.delete.title', 'admin.products.categories.delete.message')
            .pipe(take(1))
            .subscribe(confirmed => {
                if (confirmed) {
                    this.#categoryService.delete(node.id).pipe(take(1)).subscribe({
                        next: () => {
                            this.#notificationService.success('admin.common.messages.deleteSuccess');
                            this.loadTree();
                        },
                        error: (err) => this.#notificationService.alert(err?.error?.message || 'admin.common.errors.deleteFailed')
                    });
                }
            });
    }

    #openDialog(mode: 'create' | 'edit', item?: Category, parentId?: number): void {
        this.#matDialog.open(CategoryEditDialogComponent, {
            width: '600px',
            disableClose: true,
            data: { 
                mode, 
                item, 
                parentId,
                categories: this.flatCategories 
            }
        }).afterClosed().pipe(take(1)).subscribe(result => {
            if (result) {
                this.loadTree();
            }
        });
    }
}
