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
import { CreatePageRequest, Language, PageCategoryDto, PageDto, PageI18nRequest, UpdatePageRequest } from '../page-builder.types';
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
  #pageBuilderService = inject(PageBuilderService);
  #tenantContext = inject(TenantContextService);
  #router = inject(Router);
  #errorHandler = inject(ErrorHandlingService);
  #loadingState = inject(LoadingStateService);

  isLoading = false;
  tenantId = 1;
  pages: PageDto[] = [];
  filtered: PageDto[] = [];
  search = '';
  subdomain = '';
  #cachedCategories: PageCategoryDto[] = [];

  ngOnInit(): void {
    const storedId = this.#tenantContext.getCurrentTenantId();
    const storedSub = this.#tenantContext.getCurrentSubdomain();
    if (storedId) {
      this.tenantId = storedId;
    }
    if (storedSub) {
      this.subdomain = storedSub;
    }
    this.#loadCategories();
    this.load();

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
          this.load();
        }
      });

    this.#pageBuilderService.createRequested$
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.createPage();
      });
  }

  load(): void {
    if (!this.tenantId) {
      this.#notify.warning('admin.pageBuilder.errors.noTenant');
      return;
    }

    this.#loadingState.startLoading(LOADING_OPERATIONS.LOAD_PAGES);
    this.isLoading = true;

    this.#pageBuilderService.listPages()
      .pipe(take(1))
      .subscribe({
        next: (list) => {
          this.pages = list;
          this.applyFilter();
          this.isLoading = false;
          this.#loadingState.stopLoading(LOADING_OPERATIONS.LOAD_PAGES);
          this.#cdr.markForCheck();
        },
        error: (error) => {
          const errorMessage = this.#errorHandler.handleError(error);
          this.#errorHandler.logError(error, 'Loading pages');
          this.#notify.alert(errorMessage);
          this.isLoading = false;
          this.#loadingState.stopLoading(LOADING_OPERATIONS.LOAD_PAGES);
          this.#cdr.markForCheck();
        },
      });
  }

  applyFilter(): void {
    const q = (this.search || '').toLowerCase();
    this.filtered = !q
      ? this.pages
      : this.pages.filter((p) => p.uid.toLowerCase().includes(q));
  }

  onSearchChange(q: string): void {
    this.search = q || '';
    this.applyFilter();
  }

  refresh(): void {
    this.load();
  }

  createPage(): void {
    const schema = this.#buildPageSchema();
    const options: ItemDialogOptions<any> = {
      titleKey: 'admin.dialog.title.create',
      mode: 'create',
      schema,
      languages: ['tr', 'en'],
      initial: {
        status: 'DRAFT',
        isHome: false,
        sortOrder: 0,
        tr: {},
        en: {}
      },
      modalData: {
        disableClose: true,
        width: '720px',
        height: '80vh'
      }
    };

    this.#itemDialogService.open(options).pipe(take(1)).subscribe(result => {
      if (!result) return;

      try {
        const generalReq: CreatePageRequest = {
          categoryId: result.categoryId || null,
          status: result.status || 'DRAFT',
          isHome: result.isHome || false,
          sortOrder: result.sortOrder || 0,
          styleClasses: result.styleClasses || null,
          featuredImage: null
        };

        this.#pageBuilderService.createPage(generalReq).pipe(take(1)).subscribe({
          next: (createdPage) => {
            const i18nUpdates: Observable<any>[] = [];

            if (result.tr) {
              const trReq: PageI18nRequest = {
                urlPath: result.tr.urlPath || null,
                title: result.tr.title || null,
                subtitle: result.tr.subtitle || null,
                metaTitle: result.tr.metaTitle || null,
                metaDescription: result.tr.metaDescription || null,
                description: result.tr.description || null,
                status: result.status || 'DRAFT'
              };
              i18nUpdates.push(this.#pageBuilderService.updatePageI18n(createdPage.id, 'TR', trReq));
            }

            if (result.en) {
              const enReq: PageI18nRequest = {
                urlPath: result.en.urlPath || null,
                title: result.en.title || null,
                subtitle: result.en.subtitle || null,
                metaTitle: result.en.metaTitle || null,
                metaDescription: result.en.metaDescription || null,
                description: result.en.description || null,
                status: result.status || 'DRAFT'
              };
              i18nUpdates.push(this.#pageBuilderService.updatePageI18n(createdPage.id, 'EN', enReq));
            }

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

  editPage(page: PageDto): void {
    this.#pageBuilderService.getPageWithI18n(page.id).pipe(take(1)).subscribe({
      next: (pageWithI18n) => {
        const schema = this.#buildPageSchema();
        const options: ItemDialogOptions<any, number> = {
          titleKey: 'admin.dialog.title.edit',
          mode: 'edit',
          schema,
          languages: ['tr', 'en'],
          initial: {
            categoryId: pageWithI18n.page.categoryId,
            status: pageWithI18n.page.status,
            isHome: pageWithI18n.page.isHome,
            sortOrder: pageWithI18n.page.sortOrder,
            styleClasses: pageWithI18n.page.styleClasses,
            tr: {
              urlPath: pageWithI18n.translations.TR?.urlPath || '',
              title: pageWithI18n.translations.TR?.title || '',
              subtitle: pageWithI18n.translations.TR?.subtitle || '',
              metaTitle: pageWithI18n.translations.TR?.metaTitle || '',
              metaDescription: pageWithI18n.translations.TR?.metaDescription || '',
              description: pageWithI18n.translations.TR?.description || ''
            },
            en: {
              urlPath: pageWithI18n.translations.EN?.urlPath || '',
              title: pageWithI18n.translations.EN?.title || '',
              subtitle: pageWithI18n.translations.EN?.subtitle || '',
              metaTitle: pageWithI18n.translations.EN?.metaTitle || '',
              metaDescription: pageWithI18n.translations.EN?.metaDescription || '',
              description: pageWithI18n.translations.EN?.description || ''
            }
          },
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
            const updatePageReq: UpdatePageRequest = {
              id: page.id,
              categoryId: result.categoryId || null,
              status: result.status || 'DRAFT',
              isHome: result.isHome || false,
              sortOrder: result.sortOrder || 0,
              styleClasses: result.styleClasses || null,
              featuredImage: pageWithI18n.page.featuredImage
            };

            const updates: Observable<any>[] = [
              this.#pageBuilderService.updatePage(page.id, updatePageReq)
            ];

            if (result.tr) {
              const trReq: PageI18nRequest = {
                urlPath: result.tr.urlPath || null,
                title: result.tr.title || null,
                subtitle: result.tr.subtitle || null,
                metaTitle: result.tr.metaTitle || null,
                metaDescription: result.tr.metaDescription || null,
                description: result.tr.description || null,
                status: result.status || 'DRAFT'
              };
              updates.push(this.#pageBuilderService.updatePageI18n(page.id, 'TR', trReq));
            }

            if (result.en) {
              const enReq: PageI18nRequest = {
                urlPath: result.en.urlPath || null,
                title: result.en.title || null,
                subtitle: result.en.subtitle || null,
                metaTitle: result.en.metaTitle || null,
                metaDescription: result.en.metaDescription || null,
                description: result.en.description || null,
                status: result.status || 'DRAFT'
              };
              updates.push(this.#pageBuilderService.updatePageI18n(page.id, 'EN', enReq));
            }

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

  deletePage(page: PageDto): void {
    this.#pageBuilderService.deletePage(page.id).pipe(take(1)).subscribe({
      next: () => {
        this.pages = this.pages.filter(p => p.id !== page.id);
        this.applyFilter();
        this.#notify.success('admin.common.messages.operationSuccess');
        this.#cdr.markForCheck();
      },
      error: (error) => {
        const msg = this.#errorHandler.handleError(error);
        this.#notify.alert(msg);
      }
    });
  }

  setAsHome(page: PageDto): void {
    this.#pageBuilderService.setPageAsHome(page.id).pipe(take(1)).subscribe({
      next: (updated) => {
        const idx = this.pages.findIndex(p => p.id === updated.id);
        if (idx > -1) {
          this.pages[idx] = updated;
        }
        this.pages.forEach((p, i) => {
          if (p.id !== updated.id && p.isHome) {
            this.pages[i] = { ...p, isHome: false };
          }
        });
        this.applyFilter();
        this.#notify.success('admin.common.messages.operationSuccess');
        this.#cdr.markForCheck();
      },
      error: (error) => {
        const msg = this.#errorHandler.handleError(error);
        this.#notify.alert(msg);
      }
    });
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

  #buildPageSchema(): ItemDialogSchema {
    return {
      general: [
        {
          key: 'categoryId',
          type: 'select',
          labelKey: 'admin.common.fields.category',
          options: this.#cachedCategories.map(c => ({
            value: c.id,
            label: c.name
          }))
        },
        {
          key: 'status',
          type: 'select',
          labelKey: 'admin.common.fields.status',
          required: true,
          options: [
            { value: 'DRAFT', labelKey: 'admin.common.status.draft' },
            { value: 'PUBLISHED', labelKey: 'admin.common.status.published' }
          ]
        },
        {
          key: 'isHome',
          type: 'checkbox',
          labelKey: 'admin.pages.fields.isHome'
        },
        {
          key: 'sortOrder',
          type: 'number',
          labelKey: 'admin.common.fields.sortOrder',
          minValue: 0
        }
      ],
      i18n: [
        {
          key: 'urlPath',
          type: 'text',
          labelKey: 'admin.common.fields.urlPath',
          required: true,
          maxLength: 255
        },
        {
          key: 'title',
          type: 'text',
          labelKey: 'admin.common.fields.title',
          required: true,
          maxLength: 200
        },
        {
          key: 'subtitle',
          type: 'text',
          labelKey: 'admin.common.fields.subtitle',
          maxLength: 200
        },
        {
          key: 'metaTitle',
          type: 'text',
          labelKey: 'admin.common.fields.metaTitle',
          maxLength: 200
        },
        {
          key: 'metaDescription',
          type: 'textarea',
          labelKey: 'admin.common.fields.metaDescription',
          maxLength: 500
        },
        {
          key: 'description',
          type: 'textarea',
          labelKey: 'admin.common.fields.description',
          maxLength: 2000
        }
      ]
    };
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
