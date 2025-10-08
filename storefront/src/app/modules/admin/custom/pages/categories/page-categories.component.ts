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
import { TenantContextService } from '@core/tenant/tenant-context.service';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaSelectOption } from '@shared/components/custom-ui/spa-select/spa-select.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { ItemDialogService } from '@shared/services/item-dialog.service';
import { ItemDialogOptions } from '@shared/types/item-dialog.types';
import { Subject, take, takeUntil } from 'rxjs';
import { PageBuilderService } from '../page-builder.service';
import { CreateCategoryRequest, PageCategoryDto, UpdateCategoryRequest } from '../page-builder.types';
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
  categories: PageCategoryDto[] = [];
  isLoading: boolean = false;
  filtered: Array<{ id: number; name: string; slug: string; level: number; parentId: number | null }> = [];
  search: string = '';
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
    this.loadCategories();

    this._tenantCtx.tenant$.pipe(takeUntil(this.#destroy$)).subscribe((t) => {
      if (!t) return;
      if (t.id !== this.tenantId) {
        this.tenantId = t.id;
        this.loadCategories();
      }
    });
  }

  loadCategories(): void {
    if (!this.tenantId) {
      this.#notify.warning('admin.pageBuilder.errors.noTenant');
      return;
    }
    this.isLoading = true;
    this._svc
      .listCategories(undefined)
      .pipe(takeUntil(this.#destroy$))
      .subscribe({
        next: (items) => {
          const list = Array.isArray(items) ? items : [];
          this.categories = list;
          this.filtered = list.map((c) => ({
            id: c.id,
            name: c.name,
            slug: c.slug,
            level: 1,
            parentId: c.parentId ?? null,
          }));
          this.parentOptions = list.map((c) => ({ label: c.name, value: c.id }));
          this.isLoading = false;
          this.#cdr.markForCheck();
        },
        error: (error) => {
          const msg = this._errorHandler.handleError(error);
          this.#notify.alert(msg);
          this.categories = [];
          this.filtered = [];
          this.isLoading = false;
          this.#cdr.markForCheck();
        },
      });
  }

  select(node: { id: number; tenantId: number; name: string; slug: string; parentId: number | null }): void {
    this.selected = {
      id: node.id,
      tenantId: node.tenantId,
      name: node.name,
      slug: node.slug,
      parentId: node.parentId,
    };
    this.#cdr.markForCheck();
  }

  createChild(parent?: { id: number }): void {
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
              this.loadCategories();
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
          this.loadCategories();
        },
        error: (error) => {
          const msg = this._errorHandler.handleError(error);
          this.#notify.alert(msg);
          this.#cdr.markForCheck();
        },
      });
      });
  }

  remove(item: { id: number }): void {
    this._svc
      .deleteCategory(item.id)
      .pipe(takeUntil(this.#destroy$))
      .subscribe({
        next: () => {
          this.#notify.success('admin.pageBuilder.messages.categoryDeleted');
          this.loadCategories();
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
              this.loadCategories();
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
          this.loadCategories();
        },
        error: (error) => {
          const msg = this._errorHandler.handleError(error);
          this.#notify.alert(msg);
          this.#cdr.markForCheck();
        },
      });
  }

  ngOnDestroy(): void {
    this.#destroy$.next();
    this.#destroy$.complete();
  }
}


