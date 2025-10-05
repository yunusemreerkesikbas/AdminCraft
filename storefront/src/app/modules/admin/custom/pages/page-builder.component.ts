import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { Router, RouterModule } from '@angular/router';
import { TenantContextService } from '@core/tenant/tenant-context.service';
import { TranslocoPipe } from '@jsverse/transloco';
import { SpaSearchInputComponent } from '@shared/components/custom-ui/spa-search-input/spa-search-input.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { ItemDialogService } from '@shared/services/item-dialog.service';
import { ItemDialogOptions, ItemDialogSchema } from '@shared/types/item-dialog.types';
import { Observable, Subject, forkJoin, take } from 'rxjs';
import { PageBuilderService } from './page-builder.service';
import { CreatePageRequest, Language, PageCategoryDto, PageI18nRequest } from './page-builder.types';
import { TenantsService } from '../tenants/tenants.service';

@Component({
  selector: 'spa-page-builder',
  templateUrl: './page-builder.component.html',
  styleUrls: ['./page-builder.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatProgressBarModule,
    MatButtonModule,
    MatIconModule,
    SpaSearchInputComponent,
    TranslocoPipe,
  ],
})
export class PageBuilderComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();
  #pageBuilderService = inject(PageBuilderService);
  #tenantContext = inject(TenantContextService);
  #tenantsService = inject(TenantsService);
  #router = inject(Router);
  #itemDialogService = inject(ItemDialogService);
  #notificationService = inject(NotificationService);
  #cdr = inject(ChangeDetectorRef);

  isLoading = false;
  #cachedCategories: PageCategoryDto[] = [];
  #supportedLanguages: string[] = ['tr', 'en'];

  ngOnInit(): void {
    const tenantId = this.#tenantContext.getCurrentTenantId();
    if (tenantId) {
      this.#loadTenantLanguages(tenantId);
      this.#loadCategories(tenantId);
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  create(): void {
    const tenantId = this.#tenantContext.getCurrentTenantId();
    if (!tenantId) {
      this.#notificationService.warning('admin.pageBuilder.errors.noTenant');
      return;
    }

    const schema = this.#buildPageSchema();
    const initial: any = {
      status: 'DRAFT',
      isHome: false,
      sortOrder: 0
    };

    this.#supportedLanguages.forEach(lang => {
      initial[lang] = {};
    });

    const options: ItemDialogOptions<any> = {
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

            this.#supportedLanguages.forEach(lang => {
              const hasContent = result[lang] && (
                result[lang].urlPath ||
                result[lang].title ||
                result[lang].subtitle ||
                result[lang].metaTitle ||
                result[lang].metaDescription ||
                result[lang].description
              );

              if (hasContent) {
                const i18nReq: PageI18nRequest = {
                  language: lang.toUpperCase() as Language,
                  urlPath: result[lang].urlPath || null,
                  title: result[lang].title || null,
                  subtitle: result[lang].subtitle || null,
                  metaTitle: result[lang].metaTitle || null,
                  metaDescription: result[lang].metaDescription || null,
                  description: result[lang].description || null,
                  status: result.status || 'DRAFT'
                };
                i18nUpdates.push(this.#pageBuilderService.updatePageI18n(createdPage.id, lang.toUpperCase() as Language, i18nReq));
              }
            });

            if (i18nUpdates.length > 0) {
              forkJoin(i18nUpdates).pipe(take(1)).subscribe({
                next: () => {
                  this.#notificationService.success('admin.pageBuilder.messages.pageCreated');
                  this.#router.navigate(['/admin/pages']);
                },
                error: (err) => {
                  this.#notificationService.alert('admin.pageBuilder.errors.creationFailed');
                }
              });
            } else {
              this.#notificationService.success('admin.pageBuilder.messages.pageCreated');
              this.#router.navigate(['/admin/pages']);
            }
          },
          error: (error) => {
            this.#notificationService.alert('admin.pageBuilder.errors.creationFailed');
          }
        });
      } catch (err) {
        this.#notificationService.alert('admin.pageBuilder.errors.creationFailed');
      }
    });
  }

  #loadTenantLanguages(tenantId: number): void {
    if (!tenantId) return;

    this.#tenantsService.getTenantLanguages(tenantId).pipe(take(1)).subscribe({
      next: (data) => {
        this.#supportedLanguages = (data.supportedLanguages || []).map(lang => lang.toLowerCase());
        this.#cdr.markForCheck();
      },
      error: () => {
        this.#supportedLanguages = ['tr', 'en'];
      }
    });
  }

  #loadCategories(tenantId: number): void {
    this.#pageBuilderService.listCategories(tenantId).pipe(take(1)).subscribe({
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
}
