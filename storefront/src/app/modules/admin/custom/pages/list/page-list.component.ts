import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { BaseCrudListComponent, CrudStore } from '@core/crud';
import { LanguageContextService } from '@core/services/language-context.service';
import { TenantContextService } from '@core/tenant/tenant-context.service';
import { TranslocoModule } from '@jsverse/transloco';
import { AdminPageHeaderComponent } from '@shared/components/admin-page-header/admin-page-header.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { ItemDialogService } from '@shared/services/item-dialog.service';
import { ItemDialogOptions } from '@shared/types/item-dialog.types';
import { Observable, forkJoin, take, takeUntil } from 'rxjs';
import { PageTemplateService } from '../../templates/page-template.service';
import { PageTemplate } from '../../templates/page-template.types';
import { CreatePageFormData, EditPageFormData } from '../models/page-form.types';
import { PageBuilderService } from '../page-builder.service';
import { CreatePageRequest, Language, PageI18nRequest, PageListDto, UpdatePageRequest } from '../page-builder.types';
import { PageFormMapperService } from '../services/page-form-mapper.service';
import { PageSchemaBuilderService } from '../services/page-schema-builder.service';
import { PageSlotDialogComponent } from '../slots/dialog/page-slot-dialog.component';

@Component({
  selector: 'spa-page-list',
  templateUrl: './page-list.component.html',
  styleUrls: [],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatButtonModule,
    MatDialogModule,
    MatIconModule,
    TranslocoModule,
    AdminPageHeaderComponent,
  ],
  styles: [
    `
        .inventory-grid {
            grid-template-columns: auto 80px 160px;

            @screen md {
                grid-template-columns: auto 100px 180px;
            }

            @screen lg {
                grid-template-columns: auto 120px 200px;
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
  #schemaBuilder = inject(PageSchemaBuilderService);
  #formMapper = inject(PageFormMapperService);
  #templateService = inject(PageTemplateService);
  protected dialog = inject(MatDialog);


  tenantId = 1;
  subdomain = '';
  #cachedTemplates: PageTemplate[] = [];

  #getSupportedLanguages(): string[] {
    return this.#languageContext.supportedLanguages();
  }

  protected override onInit(): void {
    const storedId = this.#tenantContext.getCurrentTenantId();
    const storedSub = this.#tenantContext.getCurrentSubdomain();
    if (storedId) {
      this.tenantId = storedId;
    }
    if (storedSub) {
      this.subdomain = storedSub;
    }

    this.#loadTemplates();

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
          this.#loadTemplates();
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
    this.#notify.alert(error?.error?.message || 'admin.common.errors.server');
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
    const schema = this.#schemaBuilder.buildPageCreateSchema(this.#cachedTemplates);
    const initial: CreatePageFormData = {
      status: 'DRAFT',
      sortOrder: 0,
      templateId: null
    };

    this.#getSupportedLanguages().forEach(lang => {
      initial[lang] = {};
    });

    const options: ItemDialogOptions<CreatePageFormData> = {
      titleKey: 'admin.dialog.title.create',
      mode: 'create',
      schema,
      languages: this.#getSupportedLanguages(),
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
            const mapped = this.#formMapper.toI18nRequests(this.#getSupportedLanguages(), result, 'DRAFT');
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
                  this.#notify.alert(err?.error?.message || 'admin.common.errors.server');
                }
              });
            } else {
              this.#notify.success('admin.pageBuilder.messages.pageCreated');
              this.load();
            }
          },
          error: (error) => {
            this.#notify.alert(error?.error?.message || 'admin.common.errors.server');
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
        const schema = this.#schemaBuilder.buildPageEditSchema(this.#cachedTemplates);
        const initial: EditPageFormData = {
          templateId: pageDetail.templateId,
          status: pageDetail.status,
          sortOrder: pageDetail.sortOrder,
          styleClasses: pageDetail.styleClasses
        };

        this.#getSupportedLanguages().forEach(lang => {
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
          languages: this.#getSupportedLanguages(),
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

            const mapped = this.#formMapper.toI18nRequests(this.#getSupportedLanguages(), result, 'DRAFT');
            mapped.forEach(m => updates.push(this.#pageBuilderService.updatePageI18n(page.id, m.lang, m.req)));

            forkJoin(updates).pipe(take(1)).subscribe({
              next: () => {
                this.#notify.success('admin.pageBuilder.messages.pageUpdated');
                this.load();
              },
              error: (err) => {
                this.#notify.alert(err?.error?.message || 'admin.common.errors.server');
              }
            });
          } catch (err) {
            this.#notify.alert('admin.pageBuilder.errors.updateFailed');
          }
        });
      },
      error: (error) => {
        this.#notify.alert(error?.error?.message || 'admin.common.errors.server');
      }
    });
  }

  manageSlots(page: PageListDto): void {
    this.dialog.open(PageSlotDialogComponent, {
      data: { pageId: page.id },
      width: '900px',
      maxWidth: '95vw',
      height: '80vh',
      panelClass: 'spa-dialog-panel'
    });
  }

  deletePage(page: PageListDto): void {
    this.deleteItem(page);
  }

  protected override onDeleteSuccess(item: PageListDto): void {
    this.#notify.success('admin.common.messages.operationSuccess');
  }

  protected override onDeleteError(error: any): void {
    this.#notify.alert(error?.error?.message || 'admin.common.errors.server');
  }

  #loadTemplates(): void {
    this.#templateService.getActive().pipe(take(1)).subscribe({
      next: (templates) => {
        this.#cachedTemplates = templates;
      },
      error: () => {
        this.#cachedTemplates = [];
      }
    });
  }
}
