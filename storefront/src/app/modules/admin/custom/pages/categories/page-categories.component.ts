import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
  inject,
} from '@angular/core';
 
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTreeModule } from '@angular/material/tree';
import { TenantContextService } from '@core/tenant/tenant-context.service';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaSelectOption } from '@shared/components/custom-ui/spa-select/spa-select.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { ItemDialogService } from '@shared/services/item-dialog.service';
import { ItemDialogOptions } from '@shared/types/item-dialog.types';
import { Subject, take, takeUntil } from 'rxjs';
import { PageBuilderService } from '../page-builder.service';
import {
  CreateCategoryRequest,
  PageCategoryDto,
  PageCategoryTreeNode,
  UpdateCategoryRequest,
} from '../page-builder.types';
import { CategorySchemaBuilderService } from '../services/category-schema-builder.service';
import { ErrorHandlingService } from '../services/error-handling.service';

@Component({
  selector: 'spa-page-categories',
  templateUrl: './page-categories.component.html',
  styleUrls: ['./page-categories.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatTreeModule,
    MatProgressBarModule,
    TranslocoModule,
  ],
  styles: [
    /* language=SCSS */
    `
        .inventory-grid {
            grid-template-columns: auto 180px 80px 96px;

            @screen md {
                grid-template-columns: auto 220px 100px 120px;
            }

            @screen lg {
                grid-template-columns: auto 260px 120px 140px;
            }
        }
    `,
],
})
export class PageCategoriesComponent implements OnInit, OnDestroy {
  #cdr: ChangeDetectorRef;
  #destroy$ = new Subject<void>();
  #notify = inject(NotificationService);

  tenantId?: number;
  language: 'TR' | 'EN' | '' = '';
  tree: PageCategoryTreeNode[] = [];
  flat: Array<{
    id: number;
    tenantId: number;
    name: string;
    slug: string;
    parentId: number | null;
    level: number;
  }> = [];
  isLoading: boolean = false;
  filtered: Array<{ id: number; name: string; slug: string; level: number; parentId: number | null }> = [];
  search: string = '';
  selectedCategoryId: number | null = null;
  parentOptions: SpaSelectOption<number>[] = [];

  selected: PageCategoryDto | null = null;

  constructor(
    private _svc: PageBuilderService,
    private _tenantCtx: TenantContextService,
    private _errorHandler: ErrorHandlingService,
    private _dialog: ItemDialogService,
    private _schema: CategorySchemaBuilderService,
    cdr: ChangeDetectorRef
  ) {
    this.#cdr = cdr;
  }

  ngOnInit(): void {
    const storedId = this._tenantCtx.getCurrentTenantId();
    if (!storedId) {
      this.#notify.warning('admin.pageBuilder.errors.noTenant');
      return;
    }
    this.tenantId = storedId;
    this.loadTree();

    this._tenantCtx.tenant$.pipe(takeUntil(this.#destroy$)).subscribe((t) => {
      if (!t) return;
      if (t.id !== this.tenantId) {
        this.tenantId = t.id;
        this.loadTree();
      }
    });
  }

  loadTree(): void {
    if (!this.tenantId) {
      this.#notify.warning('admin.pageBuilder.errors.noTenant');
      return;
    }
    this.isLoading = true;
    this._svc
      .getCategoryTree(null)
      .pipe(takeUntil(this.#destroy$))
      .subscribe({
        next: (nodes) => {
          this.tree = Array.isArray(nodes) ? nodes : [];
          this.flat = [];
          this.#flatten(this.tree, 1);
          this.isLoading = false;
          this.#cdr.markForCheck();
        },
        error: (error) => {
          const msg = this._errorHandler.handleError(error);
          this.#notify.alert(msg);
          this.tree = [];
          this.flat = [];
          this.isLoading = false;
          this.#cdr.markForCheck();
        },
      });
  }

  select(node: PageCategoryTreeNode | { id: number; tenantId: number; name: string; slug: string; parentId: number | null }): void {
    this.selected = {
      id: node.id,
      tenantId: node.tenantId,
      name: node.name,
      slug: node.slug,
      parentId: node.parentId,
    };
    this.#cdr.markForCheck();
  }

  createChild(parent?: PageCategoryTreeNode | { id: number }): void {
    if (!this.tenantId) {
      this.#notify.warning('admin.pageBuilder.errors.noTenant');
      return;
    }
    const schema = this._schema.buildCategorySchema(this.parentOptions);
    const initial = {
      name: '',
      slug: `kategori-${Date.now()}`,
      parentId: parent?.id ?? null,
    } as any;
    const options: ItemDialogOptions<typeof initial> = {
      titleKey: 'admin.dialog.title.create',
      mode: 'create',
      schema,
      languages: [],
      initial,
      modalData: { disableClose: true, width: '640px', height: '70vh' },
    };
    this._dialog
      .open(options)
      .pipe(take(1))
      .subscribe((result) => {
        if (!result) return;
        const payload: CreateCategoryRequest = {
          tenantId: this.tenantId!,
          name: String(result.name || '').trim(),
          slug: String(result.slug || '').trim(),
          parentId: result.parentId ?? null,
        };
        this._svc
          .createCategory(payload)
          .pipe(takeUntil(this.#destroy$))
          .subscribe({
            next: () => {
              this.#notify.success('admin.pageBuilder.messages.categoryCreated');
              this.loadTree();
            },
            error: (error) => {
              const msg = this._errorHandler.handleError(error);
              this.#notify.alert(msg);
              this.#cdr.markForCheck();
            },
          });
      });
  }

  createRoot(): void {
    this.createChild(undefined);
  }

  save(): void {
    if (!this.selected || !this.tenantId) return;
    const schema = this._schema.buildCategorySchema(this.parentOptions);
    const initial = {
      name: this.selected.name,
      slug: this.selected.slug,
      parentId: this.selected.parentId ?? null,
    } as any;
    const options: ItemDialogOptions<typeof initial, number> = {
      titleKey: 'admin.dialog.title.edit',
      mode: 'edit',
      schema,
      languages: [],
      initial,
      id: this.selected.id,
      modalData: { disableClose: true, width: '640px', height: '70vh' },
    };
    this._dialog
      .open(options)
      .pipe(take(1))
      .subscribe((result) => {
        if (!result) return;
        const payload: UpdateCategoryRequest = {
          id: this.selected!.id,
          tenantId: this.tenantId!,
          name: String(result.name || '').trim(),
          slug: String(result.slug || '').trim(),
          parentId: result.parentId ?? null,
        };
    this._svc
      .updateCategory(payload)
      .pipe(takeUntil(this.#destroy$))
      .subscribe({
        next: () => {
          this.#notify.success('admin.pageBuilder.messages.categoryUpdated');
          this.loadTree();
        },
        error: (error) => {
          const msg = this._errorHandler.handleError(error);
          this.#notify.alert(msg);
          this.#cdr.markForCheck();
        },
      });
      });
  }

  remove(node: PageCategoryTreeNode): void {
    this._svc
      .deleteCategory(node.id)
      .pipe(takeUntil(this.#destroy$))
      .subscribe({
        next: () => {
          this.#notify.success('admin.pageBuilder.messages.categoryDeleted');
          this.loadTree();
        },
        error: (error) => {
          const msg = this._errorHandler.handleError(error);
          this.#notify.alert(msg);
          this.#cdr.markForCheck();
        },
      });
  }

  editCategory(cat: { id: number; name: string; slug: string; parentId: number | null }): void {
    if (!this.tenantId) {
      this.#notify.warning('admin.pageBuilder.errors.noTenant');
      return;
    }
    const schema = this._schema.buildCategorySchema(this.parentOptions);
    const initial = {
      name: cat.name,
      slug: cat.slug,
      parentId: cat.parentId ?? null,
    } as any;
    const options: ItemDialogOptions<typeof initial, number> = {
      titleKey: 'admin.dialog.title.edit',
      mode: 'edit',
      schema,
      languages: [],
      initial,
      id: cat.id,
      modalData: { disableClose: true, width: '640px', height: '70vh' },
    };
    this._dialog
      .open(options)
      .pipe(take(1))
      .subscribe((result) => {
        if (!result) return;
        const payload: UpdateCategoryRequest = {
          id: cat.id,
          tenantId: this.tenantId!,
          name: String(result.name || '').trim(),
          slug: String(result.slug || '').trim(),
          parentId: result.parentId ?? null,
        };
        this._svc
          .updateCategory(payload)
          .pipe(takeUntil(this.#destroy$))
          .subscribe({
            next: () => {
              this.#notify.success('admin.pageBuilder.messages.categoryUpdated');
              this.loadTree();
            },
            error: (error) => {
              const msg = this._errorHandler.handleError(error);
              this.#notify.alert(msg);
              this.#cdr.markForCheck();
            },
          });
      });
  }

  deleteCategory(id: number): void {
    this._svc
      .deleteCategory(id)
      .pipe(takeUntil(this.#destroy$))
      .subscribe({
        next: () => {
          this.#notify.success('admin.pageBuilder.messages.categoryDeleted');
          this.loadTree();
        },
        error: (error) => {
          const msg = this._errorHandler.handleError(error);
          this.#notify.alert(msg);
          this.#cdr.markForCheck();
        },
      });
  }

  moveToRoot(nodeId: number): void {
    this._svc
      .moveCategory({ id: nodeId, newParentId: null })
      .pipe(takeUntil(this.#destroy$))
      .subscribe({ 
        next: () => { 
          this.#notify.success('admin.common.messages.operationSuccess');
          this.loadTree();
        }, 
        error: (error) => {
          const msg = this._errorHandler.handleError(error);
          this.#notify.alert(msg);
          this.#cdr.markForCheck();
        }
      });
  }

  reorderUp(nodeId: number, parentId: number | null): void {
    const siblings = this.#getSiblings(parentId);
    const idx = siblings.findIndex((n) => n.id === nodeId);
    if (idx <= 0) return;
    const orderedIds = siblings.map((n) => n.id);
    [orderedIds[idx - 1], orderedIds[idx]] = [orderedIds[idx], orderedIds[idx - 1]];
    this._svc
      .reorderCategories({ parentId, orderedIds })
      .pipe(takeUntil(this.#destroy$))
      .subscribe({ 
        next: () => { 
          this.#notify.success('admin.common.messages.operationSuccess');
          this.loadTree();
        }, 
        error: (error) => {
          const msg = this._errorHandler.handleError(error);
          this.#notify.alert(msg);
          this.#cdr.markForCheck();
        }
      });
  }

  reorderDown(nodeId: number, parentId: number | null): void {
    const siblings = this.#getSiblings(parentId);
    const idx = siblings.findIndex((n) => n.id === nodeId);
    if (idx < 0 || idx >= siblings.length - 1) return;
    const orderedIds = siblings.map((n) => n.id);
    [orderedIds[idx + 1], orderedIds[idx]] = [orderedIds[idx], orderedIds[idx + 1]];
    this._svc
      .reorderCategories({ parentId, orderedIds })
      .pipe(takeUntil(this.#destroy$))
      .subscribe({ 
        next: () => { 
          this.#notify.success('admin.common.messages.operationSuccess');
          this.loadTree();
        }, 
        error: (error) => {
          const msg = this._errorHandler.handleError(error);
          this.#notify.alert(msg);
          this.#cdr.markForCheck();
        }
      });
  }

  #flatten(nodes: PageCategoryTreeNode[], level: number): void {
    nodes.forEach((n) => {
      this.flat.push({
        id: n.id,
        tenantId: n.tenantId,
        name: n.name,
        slug: n.slug,
        parentId: n.parentId ?? null,
        level,
      });
      if (n.children && n.children.length > 0) {
        this.#flatten(n.children, level + 1);
      }
    });
  }

  #getSiblings(parentId: number | null): Array<{ id: number }> {
    if (parentId == null) {
      return this.tree.map((n) => ({ id: n.id }));
    }
    const parent = this.#findNode(this.tree, parentId);
    if (!parent) return [];
    return (parent.children || []).map((n) => ({ id: n.id }));
  }

  #findNode(list: PageCategoryTreeNode[], id: number): PageCategoryTreeNode | null {
    for (const n of list) {
      if (n.id === id) return n;
      if (n.children && n.children.length > 0) {
        const found = this.#findNode(n.children, id);
        if (found) return found;
      }
    }
    return null;
  }




  




  

 

  ngOnDestroy(): void {
    this.#destroy$.next();
    this.#destroy$.complete();
  }
}


