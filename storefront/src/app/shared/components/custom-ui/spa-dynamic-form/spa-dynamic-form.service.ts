import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { DynamicFieldConfig } from './spa-dynamic-form.types';

@Injectable({
    providedIn: 'root'
})
export class SpaDynamicFormService {

    createFormGroup(fields: DynamicFieldConfig[], initialValues: Record<string, any> = {}): FormGroup {
        const group: any = {};

        fields.forEach(field => {
            group[field.key] = this.createControl(field, initialValues[field.key]);
        });

        return new FormGroup(group);
    }

    addControlsToFormGroup(form: FormGroup, fields: DynamicFieldConfig[], initialValues: Record<string, any> = {}): void {
        fields.forEach(field => {
            if (!form.contains(field.key)) {
                form.addControl(field.key, this.createControl(field, initialValues[field.key]));
            }
        });
    }

    createControl(field: DynamicFieldConfig, initialValue?: any): FormControl {
        const validators = [];
        if (field.required) {
            validators.push(Validators.required);
        }
        if (field.maxLength !== undefined) {
            validators.push(Validators.maxLength(field.maxLength));
        }
        if (field.minLength !== undefined) {
            validators.push(Validators.minLength(field.minLength));
        }
        if (field.minValue !== undefined) {
            validators.push(Validators.min(field.minValue));
        }
        if (field.maxValue !== undefined) {
            validators.push(Validators.max(field.maxValue));
        }
        if (field.pattern !== undefined && field.pattern !== '') {
            validators.push(Validators.pattern(field.pattern));
        }

        const value = initialValue !== undefined ? initialValue : (field.defaultValue ?? this.#getDefaultValue(field.type));

        return new FormControl(value, validators);
    }

    #getDefaultValue(type: string): any {
        switch (type) {
            case 'boolean': return false;
            case 'number': return null;
            case 'media': return null;
            default: return '';
        }
    }
}
