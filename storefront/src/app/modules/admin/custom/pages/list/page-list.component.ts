import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Router } from '@angular/router';
import { BaseCrudListComponent, CrudStore } from '@core/crud';
import { LanguageContextService } from '@core/services/language-context.service';
import { TenantContextService } from '@core/tenant/tenant-context.service';
import { TranslocoModule } from '@jsverse/transloco';
import { AdminPageHeaderComponent } from '@shared/components/admin-page-header/admin-page-header.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { ItemDialogService } from '@shared/services/item-dialog.service';
import { ItemDialogOptions } from '@shared/types/item-dialog.types';
import { Observable, forkJoin, take, takeUntil } from 'rxjs';
import { CreatePageFormData, EditPageFormData } from '../models/page-form.types';
import { PageBuilderService } from '../page-builder.service';
import { CreatePageRequest, Language, PageCategoryDto, PageI18nRequest, PageListDto, UpdatePageRequest } from '../page-builder.types';
import { ErrorHandlingService } from '../services/error-handling.service';
import { PageFormMapperService } from '../services/page-form-mapper.service';
import { PageSchemaBuilderService } from '../services/page-schema-builder.service';

@Component({
  selector: 'spa-page-list',
  templateUrl: './page-list.component.html',
  styleUrls: [],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    TranslocoModule,
    AdminPageHeaderComponent,
  ],
  styles: [
    `
        .inventory-grid {
            grid-template-columns: auto 180px 80px 120px 160px;

            @screen md {
                grid-template-columns: auto 220px 100px 140px 180px;
            }

            @screen lg {
                grid-template-columns: auto 260px 120px 160px 200px;
            }
        }
    `,
],
})
export class PageListComponent extends BaseCrudListComponent<PageListDto, CreatePageRequest, UpdatePageRequest> {
  protected service = inject(PageBuilderService);
  protected store = new CrudStore<PageListDto>();

  #notify = inject(NotificationService);
  #itemDialogService = inject(ItemDialogService);
  #pageBuilderService = inject(PageBuilderService);
  #tenantContext = inject(TenantContextService);
  #languageContext = inject(LanguageContextService);
  #router = inject(Router);
  #errorHandler = inject(ErrorHandlingService);
  #schemaBuilder = inject(PageSchemaBuilderService);
  #formMapper = inject(PageFormMapperService);

  tenantId = 1;
  subdomain = '';
  #cachedCategories: PageCategoryDto[] = [];
  #supportedLanguages: string[] = [];

  protected override onInit(): void {
    const storedId = this.#tenantContext.getCurrentTenantId();
    const storedSub = this.#tenantContext.getCurrentSubdomain();
    if (storedId) {
      this.tenantId = storedId;
    }
    if (storedSub) {
      this.subdomain = storedSub;
    }
    this.#languageContext.supportedLanguages$
      .pipe(takeUntil(this.destroy$))
      .subscribe((languages) => {
        this.#supportedLanguages = languages;
      });

    this.#loadCategories();

    this.#tenantContext.tenant$
      .pipe(takeUntil(this.destroy$))
      .subscribe((t) => {
        if (!t) return;
        const nextId = t.id;
        const nextSub = t.subdomain;
        const changed = nextId !== this.tenantId || nextSub !== this.subdomain;
        this.tenantId = nextId;
        this.subdomain = nextSub;
        if (changed) {
          this.#loadCategories();
          this.loadItems();
        }
      });

    this.#pageBuilderService.createRequested$
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.createPage();
      });
  }

  protected override beforeLoad(): boolean {
    if (!this.tenantId) {
      this.#notify.warning('admin.pageBuilder.errors.noTenant');
      return false;
    }
    return true;
  }

  protected override fetchItems(): Observable<PageListDto[]> {
    return this.#pageBuilderService.listPages();
  }

  protected override onLoadError(error: any): void {
    const errorMessage = this.#errorHandler.handleError(error);
    this.#errorHandler.logError(error, 'Loading pages');
    this.#notify.alert(errorMessage);
  }

  onSearchChange(q: string): void {
    super.onSearchChange(q);
  }

  load(): void {
    this.loadItems();
  }

  refresh(): void {
    this.loadItems();
  }

  create(): void {
    this.createPage();
  }

  createPage(): void {
    const schema = this.#schemaBuilder.buildPageCreateSchema(this.#cachedCategories);
    const initial: CreatePageFormData = {
      status: 'DRAFT',
      sortOrder: 0
    };

    this.#supportedLanguages.forEach(lang => {
      initial[lang] = {};
    });

    const options: ItemDialogOptions<CreatePageFormData> = {
      titleKey: 'admin.dialog.title.create',
      mode: 'create',
      schema,
      languages: this.#supportedLanguages,
      initial,
      modalData: {
        disableClose: true,
        width: '720px',
        height: '80vh'
      }
    };

    this.#itemDialogService.open(options).pipe(take(1)).subscribe(result => {
      if (!result) return;

      try {
        const generalReq: CreatePageRequest = this.#formMapper.toCreatePageRequest(result);

        this.#pageBuilderService.createPage(generalReq).pipe(take(1)).subscribe({
          next: (createdPage) => {
            const mapped = this.#formMapper.toI18nRequests(this.#supportedLanguages, result, 'DRAFT');
            const i18nUpdates: Observable<PageI18nRequest>[] = mapped.map(m =>
              this.#pageBuilderService.updatePageI18n(createdPage.id, m.lang, m.req)
            );

            if (i18nUpdates.length > 0) {
              forkJoin(i18nUpdates).pipe(take(1)).subscribe({
                next: () => {
                  this.#notify.success('admin.pageBuilder.messages.pageCreated');
                  this.load();
                },
                error: (err) => {
                  const msg = this.#errorHandler.handleError(err);
                  this.#notify.alert(msg);
                }
              });
            } else {
              this.#notify.success('admin.pageBuilder.messages.pageCreated');
              this.load();
            }
          },
          error: (error) => {
            const msg = this.#errorHandler.handleError(error);
            this.#notify.alert(msg);
          }
        });
      } catch (err) {
        this.#notify.alert('admin.pageBuilder.errors.creationFailed');
      }
    });
  }

  editPage(page: PageListDto): void {
    this.#pageBuilderService.getPageDetail(page.id).pipe(take(1)).subscribe({
      next: (pageDetail) => {
        const schema = this.#schemaBuilder.buildPageEditSchema(this.#cachedCategories);
        const initial: EditPageFormData = {
          categoryId: pageDetail.categoryId,
          status: pageDetail.status,
          sortOrder: pageDetail.sortOrder,
          styleClasses: pageDetail.styleClasses
        };

        this.#supportedLanguages.forEach(lang => {
          const langKey = lang.toUpperCase() as Language;
          const translation = pageDetail.translations[langKey];
          initial[lang] = {
            urlPath: translation?.urlPath || '',
            title: translation?.title || '',
            subtitle: translation?.subtitle || '',
            metaTitle: translation?.metaTitle || '',
            metaDescription: translation?.metaDescription || '',
            description: translation?.description || ''
          };
        });

        const options: ItemDialogOptions<EditPageFormData, number> = {
          titleKey: 'admin.dialog.title.edit',
          mode: 'edit',
          schema,
          languages: this.#supportedLanguages,
          initial,
          id: page.id,
          modalData: {
            disableClose: true,
            width: '720px',
            height: '80vh'
          }
        };

        this.#itemDialogService.open(options).pipe(take(1)).subscribe(result => {
          if (!result) return;

          try {
            const updatePageReq: UpdatePageRequest = this.#formMapper.toUpdatePageRequest(page.id, result, pageDetail.featuredImage);

            const updates: Observable<UpdatePageRequest | PageI18nRequest>[] = [
              this.#pageBuilderService.updatePage(page.id, updatePageReq)
            ];

            const mapped = this.#formMapper.toI18nRequests(this.#supportedLanguages, result, 'DRAFT');
            mapped.forEach(m => updates.push(this.#pageBuilderService.updatePageI18n(page.id, m.lang, m.req)));

            forkJoin(updates).pipe(take(1)).subscribe({
              next: () => {
                this.#notify.success('admin.pageBuilder.messages.pageUpdated');
                this.load();
              },
              error: (err) => {
                const msg = this.#errorHandler.handleError(err);
                this.#notify.alert(msg);
              }
            });
          } catch (err) {
            this.#notify.alert('admin.pageBuilder.errors.updateFailed');
          }
        });
      },
      error: (error) => {
        const msg = this.#errorHandler.handleError(error);
        this.#notify.alert(msg);
      }
    });
  }

  deletePage(page: PageListDto): void {
    this.deleteItem(page);
  }

  protected override onDeleteSuccess(item: PageListDto): void {
    this.#notify.success('admin.common.messages.operationSuccess');
  }

  protected override onDeleteError(error: any): void {
    const msg = this.#errorHandler.handleError(error);
    this.#notify.alert(msg);
  }

  #loadCategories(): void {
    this.#pageBuilderService.listCategories().pipe(take(1)).subscribe({
      next: (categories) => {
        this.#cachedCategories = categories;
      },
      error: () => {
        this.#cachedCategories = [];
      }
    });
  }
}
