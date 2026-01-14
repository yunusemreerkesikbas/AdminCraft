import { Injectable } from '@angular/core';
import { VALIDATION_LIMITS } from '@shared/constants/validation.constants';
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
        key: 'styleClasses',
        type: 'text',
        labelKey: 'admin.common.fields.styleClasses',
        maxLength: VALIDATION_LIMITS.PAGE_STYLE_CLASSES_MAX
      }
    ];
  }

  #buildI18nFields(): ReadonlyArray<LangFieldConfig> {
    return [
      {
        key: 'canonicalUrl',
        type: 'text',
        labelKey: 'admin.common.fields.canonicalUrl',
        required: false,
        maxLength: VALIDATION_LIMITS.PAGE_CANONICAL_URL_MAX
      },
      {
        key: 'title',
        type: 'text',
        labelKey: 'admin.common.fields.title',
        required: false,
        maxLength: VALIDATION_LIMITS.PAGE_TITLE_MAX
      },

      {
        key: 'description',
        type: 'textarea',
        labelKey: 'admin.common.fields.description',
        maxLength: VALIDATION_LIMITS.PAGE_DESCRIPTION_MAX
      }
    ];
  }
}
