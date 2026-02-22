import { Injectable } from '@angular/core';
import { VALIDATION_LIMITS } from '@shared/constants/validation.constants';
import { GeneralFieldConfig, ItemDialogSchema, LangFieldConfig } from '@shared/types/item-dialog.types';
import { COMPONENT_CATEGORIES } from '../models/component-categories.constants';
import { ComponentTypeDto } from '../models/component-library.types';

@Injectable({
    providedIn: 'root'
})
export class ComponentSchemaBuilderService {

    buildComponentCreateSchema(types: ComponentTypeDto[]): ItemDialogSchema {
        return this.#buildBaseSchema(types);
    }

    buildComponentEditSchema(types: ComponentTypeDto[]): ItemDialogSchema {
        return this.#buildBaseSchema(types);
    }

    buildComponentTypeSchema(): ItemDialogSchema {
        return {
            general: this.#buildComponentTypeFields(),
            i18n: []
        };
    }

    transformTypeOptions(types: ComponentTypeDto[]): ReadonlyArray<{ value: number; label: string }> {
        return types.map(type => ({
            value: type.id,
            label: type.name
        }));
    }

    #buildBaseSchema(types: ComponentTypeDto[]): ItemDialogSchema {
        return {
            general: this.#buildGeneralFields(types),
            i18n: this.#buildI18nFields()
        };
    }

    #buildGeneralFields(types: ComponentTypeDto[]): ReadonlyArray<GeneralFieldConfig> {
        return [
            {
                key: 'componentTypeId',
                type: 'select',
                labelKey: 'admin.components.fields.type',
                required: true,
                options: this.transformTypeOptions(types)
            },
            {
                key: 'name',
                type: 'text',
                labelKey: 'admin.common.fields.name',
                required: true,
                maxLength: VALIDATION_LIMITS.COMPONENT_NAME_MAX
            },
            {
                key: 'status',
                type: 'select',
                labelKey: 'admin.common.fields.status',
                required: true,
                options: [
                    { value: 'DRAFT', label: 'DRAFT' },
                    { value: 'PUBLISHED', label: 'PUBLISHED' },
                    { value: 'SCHEDULED', label: 'SCHEDULED' },
                    { value: 'ARCHIVED', label: 'ARCHIVED' }
                ]
            },
            {
                key: 'order',
                type: 'number',
                labelKey: 'admin.common.fields.order',
                minValue: 0
            },
            {
                key: 'isVisible',
                type: 'checkbox',
                labelKey: 'admin.common.fields.isVisible'
            },
            {
                key: 'styleClasses',
                type: 'text',
                labelKey: 'admin.common.fields.styleClasses',
                maxLength: VALIDATION_LIMITS.COMPONENT_STYLE_CLASSES_MAX
            }
        ];
    }

    #buildI18nFields(): ReadonlyArray<LangFieldConfig> {
        return [
            {
                key: 'title',
                type: 'text',
                labelKey: 'admin.common.fields.title',
                maxLength: 200
            },
            {
                key: 'subtitle',
                type: 'text',
                labelKey: 'admin.common.fields.subtitle',
                maxLength: 200
            },
            {
                key: 'description',
                type: 'textarea',
                labelKey: 'admin.common.fields.description',
                maxLength: 2000
            }
        ];
    }

    #buildComponentTypeFields(): ReadonlyArray<GeneralFieldConfig> {
        return [
            {
                key: 'name',
                type: 'text',
                labelKey: 'admin.common.fields.name',
                required: true,
                maxLength: VALIDATION_LIMITS.COMPONENT_TYPE_NAME_MAX
            },
            {
                key: 'category',
                type: 'select',
                labelKey: 'admin.components.fields.category',
                required: false,
                options: COMPONENT_CATEGORIES,
                maxLength: VALIDATION_LIMITS.COMPONENT_TYPE_CATEGORY_MAX
            },
            {
                key: 'navigationAware',
                type: 'checkbox',
                labelKey: 'admin.components.types.fields.navigationAware'
            }
        ];
    }
}
