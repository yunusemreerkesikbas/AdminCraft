import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { BaseCrudListComponent, CrudStore } from '@core/crud';
import { TenantContextService } from '@core/tenant/tenant-context.service';
import { TranslocoModule } from '@jsverse/transloco';
import { AdminPageHeaderComponent } from '@shared/components/admin-page-header/admin-page-header.component';
import { SpaSelectOption } from '@shared/components/custom-ui/spa-select/spa-select.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { ItemDialogService } from '@shared/services/item-dialog.service';
import type { ItemDialogOptions } from '@shared/types/item-dialog.types';
import { forkJoin, take, takeUntil } from 'rxjs';
import { TenantsService } from '../../tenants/tenants.service';
import { PageBuilderService } from '../page-builder.service';
import {
  CreateCategoryRequest,
  Language,
  PageCategoryDetailDto,
  PageCategoryListDto,
  UpdateCategoryRequest,
  UpsertCategoryI18nRequest
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
    MatTabsModule,
    TranslocoModule,
    AdminPageHeaderComponent,
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
export class PageCategoriesComponent extends BaseCrudListComponent<PageCategoryListDto, CreateCategoryRequest, UpdateCategoryRequest> {
  protected service = inject(PageBuilderService) as any;
  protected store = new CrudStore<PageCategoryListDto>();

  #notify = inject(NotificationService);
  #pageBuilderService = inject(PageBuilderService);
  #tenantCtx = inject(TenantContextService);
  #tenantsSvc = inject(TenantsService);
  #errorHandler = inject(ErrorHandlingService);
  #dialog = inject(ItemDialogService);
  #schema = inject(CategorySchemaBuilderService);

  protected supportedLanguages: Language[] = [];
  
  tenantId?: number;
  currentLanguage: Language = 'TR';
  categories: PageCategoryListDto[] = [];
  parentOptions: SpaSelectOption<number>[] = [];
  selectedCategory: PageCategoryDetailDto | null = null;

  protected override onInit(): void {
    const storedId = this.#tenantCtx.getCurrentTenantId();
    if (!storedId) {
      this.#notify.warning('admin.pageBuilder.errors.noTenant');
      return;
    }
    this.tenantId = storedId;
    this.#loadTenantLanguages();

    this.#tenantCtx.tenant$.pipe(takeUntil(this.destroy$)).subscribe((t) => {
      if (!t) return;
      if (t.id !== this.tenantId) {
        this.tenantId = t.id;
        this.#loadTenantLanguages();
        this.loadCategories();
      }
    });
  }

  #loadTenantLanguages(): void {
    if (!this.tenantId) return;
    
    this.#tenantsSvc
      .getTenantLanguages(this.tenantId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (languagesDto) => {
          this.supportedLanguages = languagesDto.supportedLanguages || [];
          this.currentLanguage = languagesDto.defaultLanguage || 'TR';
          this.cdr.markForCheck();
        },
        error: () => {
          this.supportedLanguages = ['TR', 'EN'];
          this.cdr.markForCheck();
        },
      });
  }

  protected override loadItems(): void {
    if (!this.tenantId) {
      this.#notify.warning('admin.pageBuilder.errors.noTenant');
      return;
    }
    
    this.store.setLoading(true);
    this.updateFromStore();
    
    this.#pageBuilderService
      .listCategories()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (items) => {
          this.store.setItems(items);
          this.updateFromStore();
          this.onLoadSuccess(items);
        },
        error: (error) => {
          this.store.setError(this.extractErrorMessage(error));
          this.updateFromStore();
          this.onLoadError(error);
        },
        complete: () => {
          this.store.setLoading(false);
          this.updateFromStore();
        }
      });
  }

  protected override onLoadSuccess(items: PageCategoryListDto[]): void {
    const list = Array.isArray(items) ? items : [];
    this.categories = list;
    this.parentOptions = list.map((c) => ({ 
      label: c.uid || `#${c.id}`, 
      value: c.id 
    }));
  }

  protected override onLoadError(error: any): void {
    const msg = this.#errorHandler.handleError(error);
    this.#notify.alert(msg);
    this.categories = [];
  }

  loadCategories(): void {
    this.loadItems();
  }

  createCategory(): void {
    if (!this.tenantId) {
      this.#notify.warning('admin.pageBuilder.errors.noTenant');
      return;
    }
    
    if (this.supportedLanguages.length === 0) {
      this.#notify.warning('admin.pageBuilder.errors.noTenant');
      return;
    }
    
    const schema = this.#schema.buildCategorySchema(this.parentOptions);
    const initial = {
      uid: null,
      parentId: null,
      active: true,
      styleClasses: null,
      sortOrder: 0,
    };
    const i18nInitial: Record<string, any> = {};
    this.supportedLanguages.forEach((lang) => {
      i18nInitial[lang] = {
        url: '',
        title: '',
        metaTitle: '',
        metaDescription: '',
        active: true,
      };
    });
    
    const options: ItemDialogOptions<typeof initial> = {
      titleKey: 'admin.dialog.title.create',
      mode: 'create',
      schema,
      languages: this.supportedLanguages as unknown as string[],
      initial,
      i18nInitial,
      modalData: { disableClose: true, width: '800px', height: '80vh' },
    };
    
    this.#dialog
      .open(options)
      .pipe(take(1))
      .subscribe((result) => {
        if (!result) return;
        
        const basePayload: CreateCategoryRequest = {
          uid: result.uid || null,
          parentId: result.parentId ?? null,
          active: result.active ?? true,
          styleClasses: result.styleClasses || null,
          sortOrder: result.sortOrder ?? 0,
        };
        
        this.#pageBuilderService
          .createCategory(basePayload)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: (created) => {
              this.#saveI18nForCategory(created.id, result, true);
            },
            error: (error) => {
              const msg = this.#errorHandler.handleError(error);
              this.#notify.alert(msg);
              this.cdr.markForCheck();
            },
          });
      });
  }

  editCategory(categoryId: number): void {
    if (!this.tenantId) {
      this.#notify.warning('admin.pageBuilder.errors.noTenant');
      return;
    }
    
    if (this.supportedLanguages.length === 0) {
      this.#notify.warning('admin.pageBuilder.errors.noTenant');
      return;
    }
    
    this.store.setLoading(true);
    this.updateFromStore();
    
    this.#pageBuilderService
      .getCategoryDetail(categoryId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (detail) => {
          this.store.setLoading(false);
          this.updateFromStore();
          this.#openEditDialog(detail);
        },
        error: (error) => {
          this.store.setLoading(false);
          this.updateFromStore();
          const msg = this.#errorHandler.handleError(error);
          this.#notify.alert(msg);
        },
      });
  }

  #openEditDialog(detail: PageCategoryDetailDto): void {
    const schema = this.#schema.buildCategorySchema(this.parentOptions);
    const initial = {
      uid: detail.uid,
      parentId: detail.parentId ?? null,
      active: detail.active,
      styleClasses: detail.styleClasses || null,
      sortOrder: detail.sortOrder,
    };

    const i18nInitial: Record<string, any> = {};
    this.supportedLanguages.forEach((lang) => {
      const existing = detail.translations?.[lang];
      i18nInitial[lang] = {
        url: existing?.url || '',
        title: existing?.title || '',
        metaTitle: existing?.metaTitle || '',
        metaDescription: existing?.metaDescription || '',
        active: existing?.active ?? true,
      };
    });
    
    const options: ItemDialogOptions<typeof initial, number> = {
      titleKey: 'admin.dialog.title.edit',
      mode: 'edit',
      schema,
      languages: this.supportedLanguages as unknown as string[],
      initial,
      i18nInitial,
      id: detail.id,
      modalData: { disableClose: true, width: '800px', height: '80vh' },
    };
    
    this.#dialog
      .open(options)
      .pipe(take(1))
      .subscribe((result) => {
        if (!result) return;
        
        const basePayload: UpdateCategoryRequest = {
          parentId: result.parentId ?? null,
          active: result.active ?? true,
          styleClasses: result.styleClasses || null,
          sortOrder: result.sortOrder ?? 0,
        };
        
        this.#pageBuilderService
          .updateCategory(detail.id, basePayload)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: () => {
              this.#saveI18nForCategory(detail.id, result, false);
            },
            error: (error) => {
              const msg = this.#errorHandler.handleError(error);
              this.#notify.alert(msg);
              this.cdr.markForCheck();
            },
          });
      });
  }

  #saveI18nForCategory(categoryId: number, formData: any, isNew: boolean): void {
    const i18nRequests = this.supportedLanguages
      .filter((lang) => {
        const data = formData[lang];
        return data && (data.url || data.title);
      })
      .map((lang) => {
        const data = formData[lang];
        const payload: UpsertCategoryI18nRequest = {
          url: data.url || null,
          title: data.title || null,
          metaTitle: data.metaTitle || null,
          metaDescription: data.metaDescription || null,
          active: data.active ?? true,
        };
        return this.#pageBuilderService.upsertCategoryI18n(categoryId, lang, payload);
      });
    
    if (i18nRequests.length === 0) {
      this.#notify.success(
        isNew ? 'admin.pageBuilder.messages.categoryCreated' : 'admin.pageBuilder.messages.categoryUpdated'
      );
      this.loadCategories();
      return;
    }
    
    forkJoin(i18nRequests)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.#notify.success(
            isNew ? 'admin.pageBuilder.messages.categoryCreated' : 'admin.pageBuilder.messages.categoryUpdated'
          );
          this.loadCategories();
        },
        error: (error) => {
          const msg = this.#errorHandler.handleError(error);
          this.#notify.alert(msg);
          this.cdr.markForCheck();
        },
      });
  }

  deleteCategory(id: number): void {
    const category = this.categories.find(c => c.id === id);
    if (!category) return;
    
    this.deleteItem(category);
  }

  protected override onDeleteSuccess(item: PageCategoryListDto): void {
    this.#notify.success('admin.pageBuilder.messages.categoryDeleted');
    this.loadCategories();
  }

  protected override onDeleteError(error: any): void {
    const msg = this.#errorHandler.handleError(error);
    this.#notify.alert(msg);
  }

  getParentCategoryName(parentId: number): string {
    const parent = this.categories.find((c) => c.id === parentId);
    return parent?.uid || `#${parentId}`;
  }
}


