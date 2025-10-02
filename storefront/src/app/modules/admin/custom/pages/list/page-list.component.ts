import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { Router } from '@angular/router';
import { TenantContextService } from '@core/tenant/tenant-context.service';
import { TranslocoModule } from '@jsverse/transloco';
import { NotificationService } from '@shared/notifications/notification.service';
import { ItemDialogService } from '@shared/services/item-dialog.service';
import { ItemDialogOptions, ItemDialogSchema } from '@shared/types/item-dialog.types';
import { Observable, Subject, forkJoin, take, takeUntil } from 'rxjs';
import { PageBuilderService } from '../page-builder.service';
import { CreatePageRequest, PageCategoryDto, PageDto, UpdatePageRequest } from '../page-builder.types';
import { ErrorHandlingService } from '../services/error-handling.service';
import { LOADING_OPERATIONS, LoadingStateService } from '../services/loading-state.service';

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
    MatProgressBarModule,
    TranslocoModule,
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
export class PageListComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();
  #notify = inject(NotificationService);
  #cdr = inject(ChangeDetectorRef);
  #itemDialogService = inject(ItemDialogService);

  isLoading = false;
  tenantId = 1;
  language: 'TR' | 'EN' | '' = '';
  pages: PageDto[] = [];
  filtered: PageDto[] = [];
  search = '';
  subdomain = '';

  constructor(
    private _svc: PageBuilderService,
    private _tenantCtx: TenantContextService,
    private _router: Router,
    private _errorHandler: ErrorHandlingService,
    private _loadingState: LoadingStateService
  ) {}

  ngOnInit(): void {
    const storedId = this._tenantCtx.getCurrentTenantId();
    const storedSub = this._tenantCtx.getCurrentSubdomain();
    if (storedId) {
      this.tenantId = storedId;
    }
    if (storedSub) {
      this.subdomain = storedSub;
    }
    this.load();

    this._tenantCtx.tenant$
      .pipe(takeUntil(this.destroy$))
      .subscribe((t) => {
        if (!t) return;
        const nextId = t.id;
        const nextSub = t.subdomain;
        const changed = nextId !== this.tenantId || nextSub !== this.subdomain;
        this.tenantId = nextId;
        this.subdomain = nextSub;
        if (changed) {
          this.load();
        }
      });

    this._svc.createRequested$
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.openCreateDialog();
      });
  }

  load(): void {
    if (!this.tenantId) {
      this.#notify.warning('admin.pageBuilder.errors.noTenant');
      return;
    }

    this._loadingState.startLoading(LOADING_OPERATIONS.LOAD_PAGES);
    this.isLoading = true;

    this._svc.listPages(this.tenantId, this.language || undefined)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (list) => {
          this.pages = list;
          this.applyFilter();
          this.isLoading = false;
          this._loadingState.stopLoading(LOADING_OPERATIONS.LOAD_PAGES);
          this.#cdr.markForCheck();
        },
        error: (error) => {
          const errorMessage = this._errorHandler.handleError(error);
          this._errorHandler.logError(error, 'Loading pages');
          this.#notify.alert(errorMessage);
          this.isLoading = false;
          this._loadingState.stopLoading(LOADING_OPERATIONS.LOAD_PAGES);
          this.#cdr.markForCheck();
        },
      });
  }

  applyFilter(): void {
    const q = (this.search || '').toLowerCase();
    this.filtered = !q
      ? this.pages
      : this.pages.filter(
          (p) =>
            p.title.toLowerCase().includes(q) ||
            p.slug.toLowerCase().includes(q)
        );
  }

  onSearchChange(q: string): void {
    this.search = q || '';
    this.applyFilter();
  }

  refresh(): void {
    this.load();
  }

  openCreateDialog(): void {
    this._svc.listCategories(this.tenantId).pipe(take(1)).subscribe(categories => {
      const schema = this.#buildDynamicSchema(categories);
      const emptyGeneralData = this.#buildEmptyGeneralData(schema);
      const emptyI18nData = this.#buildEmptyI18nData(schema);
      
      const options: ItemDialogOptions<any> = {
        titleKey: 'admin.pageBuilder.title',
        mode: 'create',
        schema,
        languages: ['tr', 'en'],
        initial: {
          ...emptyGeneralData,
          status: 'DRAFT',
          language: 'TR',
          tr: emptyI18nData,
          en: emptyI18nData
        },
        modalData: {
          disableClose: true,
          width: '720px',
          height: '80vh'
        }
      };

      this.#itemDialogService.open(options).subscribe(result => {
        if (result) {
          const langData = (result as any)[result.language?.toLowerCase() || 'tr'];

          const payload: CreatePageRequest = {
            tenantId: this.tenantId,
            title: langData?.title || 'Untitled',
            slug: result.slug,
            language: result.language,
            categoryId: result.categoryId || null,
            metaTitle: langData?.metaTitle || null,
            metaDescription: langData?.metaDescription || null,
            canonicalUrl: langData?.canonicalUrl || null,
            subtitle: langData?.subtitle || null,
            styleClasses: result.styleClasses || null,
            description: langData?.description || null
          };

          this._svc.createPage(payload).pipe(take(1)).subscribe({
            next: (newPage) => {
              this.pages = [newPage, ...this.pages];
              this.applyFilter();
              this.#notify.success('admin.pageBuilder.messages.pageCreated');
              this.#cdr.markForCheck();
            },
            error: (error) => {
              const msg = this._errorHandler.handleError(error);
              this.#notify.alert(msg);
            }
          });
        }
      });
    });
  }

  openEditDialog(page: PageDto): void {
    forkJoin({
      categories: this._svc.listCategories(this.tenantId).pipe(take(1)),
      languageVersions: this._svc.getPageVersionsBySlug(this.tenantId, page.slug).pipe(take(1))
    }).subscribe(({ categories, languageVersions }) => {
      const schema = this.#buildDynamicSchema(categories);

      const trVersion = languageVersions.find(p => p.language === 'TR');
      const enVersion = languageVersions.find(p => p.language === 'EN');

      const basePage = page;
      const fallbackData = {
        title: basePage.title || '',
        subtitle: basePage.subtitle || '',
        metaTitle: basePage.metaTitle || '',
        metaDescription: basePage.metaDescription || '',
        canonicalUrl: basePage.canonicalUrl || '',
        description: basePage.description || ''
      };

      const options: ItemDialogOptions<any, number> = {
        titleKey: 'admin.pageBuilder.title',
        mode: 'edit',
        schema,
        languages: ['tr', 'en'],
        initial: {
          slug: basePage.slug,
          status: basePage.status,
          language: basePage.language,
          categoryId: basePage.categoryId,
          styleClasses: basePage.styleClasses,
          tr: trVersion ? {
            title: trVersion.title || '',
            subtitle: trVersion.subtitle || '',
            metaTitle: trVersion.metaTitle || '',
            metaDescription: trVersion.metaDescription || '',
            canonicalUrl: trVersion.canonicalUrl || '',
            description: trVersion.description || ''
          } : fallbackData,
          en: enVersion ? {
            title: enVersion.title || '',
            subtitle: enVersion.subtitle || '',
            metaTitle: enVersion.metaTitle || '',
            metaDescription: enVersion.metaDescription || '',
            canonicalUrl: enVersion.canonicalUrl || '',
            description: enVersion.description || ''
          } : fallbackData
        },
        id: basePage.id,
        modalData: {
          disableClose: true,
          width: '720px',
          height: '80vh'
        }
      };

      this.#itemDialogService.open(options).subscribe(result => {
        if (result) {
          this.#handleEditSave(result, languageVersions);
        }
      });
    });
  }

  #handleEditSave(result: any, languageVersions: PageDto[]): void {
    const requests: Observable<PageDto>[] = [];

    if (result.tr) {
      const trVersion = languageVersions.find(p => p.language === 'TR');
      if (trVersion) {
        const payload: UpdatePageRequest = {
          id: trVersion.id,
          tenantId: this.tenantId,
          slug: result.slug,
          language: 'TR',
          categoryId: result.categoryId || null,
          styleClasses: result.styleClasses || null,
          title: result.tr.title,
          subtitle: result.tr.subtitle || null,
          metaTitle: result.tr.metaTitle || null,
          metaDescription: result.tr.metaDescription || null,
          canonicalUrl: result.tr.canonicalUrl || null,
          description: result.tr.description || null,
          featuredImage: trVersion.featuredImage || null
        };
        requests.push(this._svc.updatePage(payload));
      }
    }

    if (result.en) {
      const enVersion = languageVersions.find(p => p.language === 'EN');
      if (enVersion) {
        const payload: UpdatePageRequest = {
          id: enVersion.id,
          tenantId: this.tenantId,
          slug: result.slug,
          language: 'EN',
          categoryId: result.categoryId || null,
          styleClasses: result.styleClasses || null,
          title: result.en.title,
          subtitle: result.en.subtitle || null,
          metaTitle: result.en.metaTitle || null,
          metaDescription: result.en.metaDescription || null,
          canonicalUrl: result.en.canonicalUrl || null,
          description: result.en.description || null,
          featuredImage: enVersion.featuredImage || null
        };
        requests.push(this._svc.updatePage(payload));
      }
    }

    if (requests.length === 0) {
      return;
    }

    forkJoin(requests).pipe(take(1)).subscribe({
      next: (updatedPages) => {
        updatedPages.forEach(updated => {
          const idx = this.pages.findIndex(p => p.id === updated.id);
          if (idx > -1) {
            this.pages[idx] = updated;
          }
        });
        this.applyFilter();
        this.#notify.success('admin.pageBuilder.messages.pageUpdated');
        this.#cdr.markForCheck();
      },
      error: (error) => {
        const msg = this._errorHandler.handleError(error);
        this.#notify.alert(msg);
      }
    });
  }

  deletePage(page: PageDto): void {
    this._svc.deletePage(page.id).pipe(take(1)).subscribe({
      next: () => {
        this.pages = this.pages.filter(p => p.id !== page.id);
        this.applyFilter();
        this.#notify.success('admin.common.messages.operationSuccess');
        this.#cdr.markForCheck();
      },
      error: (error) => {
        const msg = this._errorHandler.handleError(error);
        this.#notify.alert(msg);
      }
    });
  }

  publishPage(page: PageDto): void {
    this._svc.publishPage(page.id).pipe(take(1)).subscribe({
      next: (updated) => {
        const idx = this.pages.findIndex(p => p.id === updated.id);
        if (idx > -1) {
          this.pages[idx] = updated;
        }
        this.applyFilter();
        this.#notify.success('admin.common.messages.operationSuccess');
        this.#cdr.markForCheck();
      },
      error: (error) => {
        const msg = this._errorHandler.handleError(error);
        this.#notify.alert(msg);
      }
    });
  }

  unpublishPage(page: PageDto): void {
    this._svc.unpublishPage(page.id).pipe(take(1)).subscribe({
      next: (updated) => {
        const idx = this.pages.findIndex(p => p.id === updated.id);
        if (idx > -1) {
          this.pages[idx] = updated;
        }
        this.applyFilter();
        this.#notify.success('admin.common.messages.operationSuccess');
        this.#cdr.markForCheck();
      },
      error: (error) => {
        const msg = this._errorHandler.handleError(error);
        this.#notify.alert(msg);
      }
    });
  }

  #buildEmptyGeneralData(schema: ItemDialogSchema): Record<string, any> {
    const emptyData: Record<string, any> = {};
    schema.general.forEach(field => {
      if (field.type === 'checkbox') {
        emptyData[field.key] = false;
      } else if (field.type === 'number') {
        emptyData[field.key] = null;
      } else if (field.type === 'select') {
        emptyData[field.key] = null;
      } else {
        emptyData[field.key] = '';
      }
    });
    return emptyData;
  }

  #buildEmptyI18nData(schema: ItemDialogSchema): Record<string, any> {
    const emptyData: Record<string, any> = {};
    schema.i18n.forEach(field => {
      if (field.type === 'checkbox') {
        emptyData[field.key] = false;
      } else if (field.type === 'number') {
        emptyData[field.key] = null;
      } else {
        emptyData[field.key] = '';
      }
    });
    return emptyData;
  }

  #buildDynamicSchema(categories: PageCategoryDto[]): ItemDialogSchema {
    return {
      general: [
        {
          key: 'slug',
          type: 'text',
          labelKey: 'admin.pageBuilder.fields.slug',
          required: true,
          maxLength: 200
        },
        {
          key: 'status',
          type: 'select',
          labelKey: 'admin.pageBuilder.fields.status',
          required: true,
          options: [
            { value: 'DRAFT', labelKey: 'admin.pageBuilder.status.draft' },
            { value: 'PUBLISHED', labelKey: 'admin.pageBuilder.status.published' },
            { value: 'ARCHIVED', labelKey: 'admin.pageBuilder.status.archived' },
            { value: 'SCHEDULED', labelKey: 'admin.pageBuilder.status.scheduled' }
          ]
        },
        {
          key: 'language',
          type: 'select',
          labelKey: 'admin.pageBuilder.fields.language',
          required: true,
          options: [
            { value: 'TR', labelKey: 'admin.common.languages.tr' },
            { value: 'EN', labelKey: 'admin.common.languages.en' }
          ]
        },
        {
          key: 'categoryId',
          type: 'select',
          labelKey: 'admin.pageBuilder.fields.category',
          required: false,
          options: categories.map(c => ({
            value: c.id,
            label: c.name
          }))
        },
        {
          key: 'styleClasses',
          type: 'text',
          labelKey: 'admin.pageBuilder.fields.styleClasses',
          required: false,
          maxLength: 255
        }
      ],
      i18n: [
        {
          key: 'title',
          type: 'text',
          labelKey: 'admin.pageBuilder.fields.pageTitle',
          required: true,
          maxLength: 200
        },
        {
          key: 'subtitle',
          type: 'text',
          labelKey: 'admin.pageBuilder.fields.subtitle',
          required: false,
          maxLength: 200
        },
        {
          key: 'metaTitle',
          type: 'text',
          labelKey: 'admin.pageBuilder.fields.metaTitle',
          required: false,
          maxLength: 60
        },
        {
          key: 'metaDescription',
          type: 'textarea',
          labelKey: 'admin.pageBuilder.fields.metaDescription',
          required: false,
          maxLength: 160
        },
        {
          key: 'canonicalUrl',
          type: 'text',
          labelKey: 'admin.pageBuilder.fields.canonicalUrl',
          required: false,
          maxLength: 500
        },
        {
          key: 'description',
          type: 'textarea',
          labelKey: 'admin.pageBuilder.fields.description',
          required: false,
          maxLength: 1000
        }
      ]
    };
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
