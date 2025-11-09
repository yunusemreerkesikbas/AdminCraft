import { Injectable } from '@angular/core';
import { GeneralFieldConfig, ItemDialogSchema, LangFieldConfig } from '@shared/types/item-dialog.types';
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
                key: 'code',
                type: 'text',
                labelKey: 'admin.common.fields.code',
                required: true,
                maxLength: 100
            },
            {
                key: 'name',
                type: 'text',
                labelKey: 'admin.common.fields.name',
                required: true,
                maxLength: 200
            },
            {
                key: 'status',
                type: 'select',
                labelKey: 'admin.common.fields.status',
                required: true,
                options: [
                    { value: 'DRAFT', labelKey: 'admin.common.status.draft' },
                    { value: 'ACTIVE', labelKey: 'admin.common.status.active' },
                    { value: 'INACTIVE', labelKey: 'admin.common.status.inactive' }
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
                maxLength: 500
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
            },
            {
                key: 'imageUrl',
                type: 'text',
                labelKey: 'admin.common.fields.imageUrl',
                maxLength: 500
            },
            {
                key: 'imageAlt',
                type: 'text',
                labelKey: 'admin.common.fields.imageAlt',
                maxLength: 200
            },
            {
                key: 'buttonText',
                type: 'text',
                labelKey: 'admin.common.fields.buttonText',
                maxLength: 100
            },
            {
                key: 'buttonUrl',
                type: 'text',
                labelKey: 'admin.common.fields.buttonUrl',
                maxLength: 500
            },
            {
                key: 'buttonStyle',
                type: 'text',
                labelKey: 'admin.common.fields.buttonStyle',
                maxLength: 100
            }
        ];
    }

    #buildComponentTypeFields(): ReadonlyArray<GeneralFieldConfig> {
        return [
            {
                key: 'code',
                type: 'text',
                labelKey: 'admin.common.fields.code',
                required: true,
                maxLength: 50
            },
            {
                key: 'name',
                type: 'text',
                labelKey: 'admin.common.fields.name',
                required: true,
                maxLength: 100
            },
            {
                key: 'category',
                type: 'text',
                labelKey: 'admin.components.fields.category',
                maxLength: 50
            },
            {
                key: 'icon',
                type: 'text',
                labelKey: 'admin.components.fields.icon',
                maxLength: 100
            }
        ];
    }
}
