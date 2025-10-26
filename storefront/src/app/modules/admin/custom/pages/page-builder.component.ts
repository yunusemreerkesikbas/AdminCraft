import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { LanguageContextService } from '@core/services/language-context.service';
import { TenantContextService } from '@core/tenant/tenant-context.service';
import { NotificationService } from '@shared/notifications/notification.service';
import { ItemDialogService } from '@shared/services/item-dialog.service';
import { ItemDialogOptions } from '@shared/types/item-dialog.types';
import { Observable, Subject, forkJoin, take, takeUntil } from 'rxjs';
import { CreatePageFormData, PageI18nFormData } from './models/page-form.types';
import { PageBuilderService } from './page-builder.service';
import { CreatePageRequest, Language, PageCategoryDto, PageI18nRequest } from './page-builder.types';
import { PageSchemaBuilderService } from './services/page-schema-builder.service';

@Component({
  selector: 'spa-page-builder',
  templateUrl: './page-builder.component.html',
  styleUrls: ['./page-builder.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
  ],
})
export class PageBuilderComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();
  #pageBuilderService = inject(PageBuilderService);
  #tenantContext = inject(TenantContextService);
  #languageContext = inject(LanguageContextService);
  #router = inject(Router);
  #itemDialogService = inject(ItemDialogService);
  #notificationService = inject(NotificationService);
  #cdr = inject(ChangeDetectorRef);
  #schemaBuilder = inject(PageSchemaBuilderService);

  isLoading = false;
  #cachedCategories: PageCategoryDto[] = [];
  #supportedLanguages: string[] = [];

  ngOnInit(): void {
    this.#languageContext.supportedLanguages$
      .pipe(takeUntil(this.destroy$))
      .subscribe((languages) => {
        this.#supportedLanguages = languages;
        this.#cdr.markForCheck();
      });
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
    if (this.#cachedCategories.length === 0) {
      this.#loadCategories();
    }

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
        const generalReq: CreatePageRequest = {
          categoryId: result.categoryId || null,
          status: result.status || 'DRAFT',
          sortOrder: result.sortOrder || 0,
          styleClasses: result.styleClasses || null,
          featuredImage: null
        };

        this.#pageBuilderService.createPage(generalReq).pipe(take(1)).subscribe({
          next: (createdPage) => {
            const i18nUpdates: Observable<PageI18nRequest>[] = [];

            this.#supportedLanguages.forEach(lang => {
              const langData = result[lang] as PageI18nFormData | undefined;
              const hasContent = langData && (
                langData.urlPath ||
                langData.title ||
                langData.subtitle ||
                langData.metaTitle ||
                langData.metaDescription ||
                langData.description
              );

              if (hasContent && langData) {
                const i18nReq: PageI18nRequest = {
                  language: lang.toUpperCase() as Language,
                  urlPath: langData.urlPath || null,
                  title: langData.title || null,
                  subtitle: langData.subtitle || null,
                  metaTitle: langData.metaTitle || null,
                  metaDescription: langData.metaDescription || null,
                  description: langData.description || null,
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
