import { Injectable } from '@angular/core';
import { GeneralFieldConfig, ItemDialogSchema, LangFieldConfig } from '@shared/types/item-dialog.types';
import { PageTemplate } from '../../templates/page-template.types';

@Injectable({
  providedIn: 'root'
})
export class PageSchemaBuilderService {

  buildPageCreateSchema(templates: PageTemplate[] = []): ItemDialogSchema {
    return this.#buildBaseSchema(templates);
  }

  buildPageEditSchema(templates: PageTemplate[] = []): ItemDialogSchema {
    return this.#buildBaseSchema(templates);
  }

  transformTemplateOptions(templates: PageTemplate[]): ReadonlyArray<{ value: number; label: string }> {
    return templates.map(template => ({
      value: template.id,
      label: template.name
    }));
  }

  #buildBaseSchema(templates: PageTemplate[]): ItemDialogSchema {
    return {
      general: this.#buildGeneralFields(templates),
      i18n: this.#buildI18nFields()
    };
  }

  #buildGeneralFields(templates: PageTemplate[]): ReadonlyArray<GeneralFieldConfig> {
    return [
      {
        key: 'templateId',
        type: 'select',
        labelKey: 'admin.common.fields.template',
        options: this.transformTemplateOptions(templates)
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
