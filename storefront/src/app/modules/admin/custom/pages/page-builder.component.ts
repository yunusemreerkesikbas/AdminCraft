import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
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
import { take } from 'rxjs';
import { PageBuilderService } from './page-builder.service';
import { PageCategoryDto } from './page-builder.types';

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
export class PageBuilderComponent {
  #pageBuilderService = inject(PageBuilderService);
  #tenantContext = inject(TenantContextService);
  #router = inject(Router);
  #itemDialogService = inject(ItemDialogService);
  #notificationService = inject(NotificationService);

  isLoading = false;

  create(): void {
    const tenantId = this.#tenantContext.getCurrentTenantId();
    if (!tenantId) {
      this.#notificationService.warning('admin.pageBuilder.errors.noTenant');
      return;
    }

    this.#pageBuilderService.listCategories(tenantId)
      .pipe(take(1))
      .subscribe(categories => {
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

        this.#itemDialogService
          .open(options)
          .pipe(take(1))
          .subscribe(result => {
            if (result) {
              console.log('Dialog result:', result);
              this.#notificationService.success(
                'admin.pageBuilder.messages.pageCreated'
              );
            }
          });
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
}
