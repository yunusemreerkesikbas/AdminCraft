import { Injectable } from '@angular/core';
import { SpaSelectOption } from '@shared/components/custom-ui/spa-select/spa-select.component';
import { GeneralFieldConfig, ItemDialogSchema } from '@shared/types/item-dialog.types';

@Injectable({ providedIn: 'root' })
export class CategorySchemaBuilderService {
  buildCategorySchema(parentOptions: ReadonlyArray<SpaSelectOption<number>>): ItemDialogSchema {
    return {
      general: this.#buildGeneralFields(parentOptions),
      i18n: [],
    };
  }

  #buildGeneralFields(parentOptions: ReadonlyArray<SpaSelectOption<number>>): ReadonlyArray<GeneralFieldConfig> {
    return [
      {
        key: 'name',
        type: 'text',
        labelKey: 'admin.pageBuilder.fields.name',
        required: true,
        maxLength: 200,
      },
      {
        key: 'slug',
        type: 'text',
        labelKey: 'admin.pageBuilder.fields.slug',
        required: true,
        maxLength: 200,
      },
      {
        key: 'parentId',
        type: 'select',
        labelKey: 'admin.pageBuilder.fields.parentCategory',
        options: parentOptions,
      },
    ];
  }
}



