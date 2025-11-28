import { Injectable } from '@angular/core';
import { SpaSelectOption } from '@shared/components/custom-ui/spa-select/spa-select.component';
import { GeneralFieldConfig, I18nFieldConfig, ItemDialogSchema } from '@shared/types/item-dialog.types';

@Injectable({ providedIn: 'root' })
export class CategorySchemaBuilderService {
  buildCategorySchema(parentOptions: ReadonlyArray<SpaSelectOption<number>>): ItemDialogSchema {
    return {
      general: this.#buildGeneralFields(parentOptions),
      i18n: this.#buildI18nFields(),
    };
  }

  #buildGeneralFields(parentOptions: ReadonlyArray<SpaSelectOption<number>>): ReadonlyArray<GeneralFieldConfig> {
    return [
      {
        key: 'uid',
        type: 'text',
        labelKey: 'admin.pageBuilder.fields.uid',
        required: false,
        maxLength: 100,
      },
      {
        key: 'parentId',
        type: 'select',
        labelKey: 'admin.pageBuilder.fields.parentCategory',
        options: parentOptions,
      },
      {
        key: 'active',
        type: 'checkbox',
        labelKey: 'admin.pageBuilder.fields.active',
      },
      {
        key: 'styleClasses',
        type: 'text',
        labelKey: 'admin.pageBuilder.fields.styleClasses',
        required: false,
        maxLength: 500,
      },
      {
        key: 'sortOrder',
        type: 'number',
        labelKey: 'admin.pageBuilder.fields.sortOrder',
      },
    ];
  }

  #buildI18nFields(): ReadonlyArray<I18nFieldConfig> {
    return [
      {
        key: 'url',
        type: 'text',
        labelKey: 'admin.pageBuilder.fields.url',
        required: false,
        maxLength: 200,
      },
      {
        key: 'title',
        type: 'text',
        labelKey: 'admin.pageBuilder.fields.title',
        required: false,
        maxLength: 200,
      },
      {
        key: 'metaTitle',
        type: 'text',
        labelKey: 'admin.pageBuilder.fields.metaTitle',
        required: false,
        maxLength: 200,
      },
      {
        key: 'metaDescription',
        type: 'textarea',
        labelKey: 'admin.pageBuilder.fields.metaDescription',
        required: false,
        maxLength: 500,
      },
      {
        key: 'active',
        type: 'checkbox',
        labelKey: 'admin.pageBuilder.fields.active',
      },
    ];
  }
}




