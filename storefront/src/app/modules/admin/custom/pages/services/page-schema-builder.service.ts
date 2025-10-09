import { Injectable } from '@angular/core';
import { GeneralFieldConfig, ItemDialogSchema, LangFieldConfig } from '@shared/types/item-dialog.types';
import { PageCategoryDto } from '../page-builder.types';

@Injectable({
  providedIn: 'root'
})
export class PageSchemaBuilderService {

  buildPageCreateSchema(categories: PageCategoryDto[]): ItemDialogSchema {
    return this.#buildBaseSchema(categories);
  }

  buildPageEditSchema(categories: PageCategoryDto[]): ItemDialogSchema {
    return this.#buildBaseSchema(categories);
  }

  transformCategoryOptions(categories: PageCategoryDto[]): ReadonlyArray<{ value: number; label: string }> {
    return categories.map(category => ({
      value: category.id,
      label: category.uid
    }));
  }

  #buildBaseSchema(categories: PageCategoryDto[]): ItemDialogSchema {
    return {
      general: this.#buildGeneralFields(categories),
      i18n: this.#buildI18nFields()
    };
  }

  #buildGeneralFields(categories: PageCategoryDto[]): ReadonlyArray<GeneralFieldConfig> {
    return [
      {
        key: 'categoryId',
        type: 'select',
        labelKey: 'admin.common.fields.category',
        options: this.transformCategoryOptions(categories)
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
        key: 'sortOrder',
        type: 'number',
        labelKey: 'admin.common.fields.sortOrder',
        minValue: 0
      },
      {
        key: 'styleClasses',
        type: 'text',
        labelKey: 'admin.common.fields.styleClasses',
        maxLength: 500
      }
    ];
  }

  #buildI18nFields(): ReadonlyArray<LangFieldConfig> {
    return [
      {
        key: 'urlPath',
        type: 'text',
        labelKey: 'admin.common.fields.urlPath',
        required: false,
        maxLength: 255
      },
      {
        key: 'title',
        type: 'text',
        labelKey: 'admin.common.fields.title',
        required: false,
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
    ];
  }
}
