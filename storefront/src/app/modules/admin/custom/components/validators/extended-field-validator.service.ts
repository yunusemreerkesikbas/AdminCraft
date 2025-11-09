import { Injectable } from '@angular/core';
import { ValidatorFn, Validators } from '@angular/forms';
import { ExtendedFieldDefinition, ExtendedFieldsSchema, ExtendedFieldType } from '../models/component-library.types';

export interface ValidationResult {
    valid: boolean;
    errors: string[];
}

@Injectable({
    providedIn: 'root'
})
export class ExtendedFieldValidatorService {
    private readonly MAX_FIELDS = 20;
    private readonly MAX_SCHEMA_SIZE = 100 * 1024; // 100KB
    private readonly MAX_KEY_LENGTH = 50;
    private readonly MAX_LABEL_LENGTH = 100;
    private readonly KEY_PATTERN = /^[a-zA-Z][a-zA-Z0-9_]*$/;
    private readonly VALID_TYPES: ExtendedFieldType[] = ['text', 'textarea', 'number', 'boolean', 'select'];

    validateSchema(schema: ExtendedFieldsSchema | null | undefined): ValidationResult {
        const errors: string[] = [];

        if (!schema) {
            return { valid: true, errors: [] };
        }

        if (!schema.i18n || !Array.isArray(schema.i18n)) {
            errors.push('Schema must have an i18n array');
            return { valid: false, errors };
        }
        if (schema.i18n.length > this.MAX_FIELDS) {
            errors.push(`Maximum ${this.MAX_FIELDS} fields allowed, found ${schema.i18n.length}`);
        }
        const schemaSize = JSON.stringify(schema).length;
        if (schemaSize > this.MAX_SCHEMA_SIZE) {
            errors.push(`Schema size exceeds ${this.MAX_SCHEMA_SIZE} bytes (current: ${schemaSize})`);
        }
        const keys = schema.i18n.map(field => field.key);
        const duplicateKeys = keys.filter((key, index) => keys.indexOf(key) !== index);
        if (duplicateKeys.length > 0) {
            errors.push(`Duplicate field keys found: ${duplicateKeys.join(', ')}`);
        }
        schema.i18n.forEach((field, index) => {
            const fieldErrors = this.validateFieldDefinition(field);
            fieldErrors.forEach(error => {
                errors.push(`Field ${index + 1} (${field.key || 'unnamed'}): ${error}`);
            });
        });

        return {
            valid: errors.length === 0,
            errors
        };
    }

    validateFieldDefinition(field: ExtendedFieldDefinition): string[] {
        const errors: string[] = [];
        if (!field.key) {
            errors.push('Field key is required');
        } else {
            if (!this.validateFieldKey(field.key)) {
                errors.push(`Field key '${field.key}' must start with a letter and contain only letters, numbers, and underscores`);
            }
            if (field.key.length > this.MAX_KEY_LENGTH) {
                errors.push(`Field key exceeds maximum length of ${this.MAX_KEY_LENGTH}`);
            }
        }

        if (!field.type) {
            errors.push('Field type is required');
        } else if (!this.VALID_TYPES.includes(field.type)) {
            errors.push(`Invalid field type '${field.type}'. Allowed: ${this.VALID_TYPES.join(', ')}`);
        }

        if (!field.label) {
            errors.push('Field label is required');
        } else if (field.label.length > this.MAX_LABEL_LENGTH) {
            errors.push(`Field label exceeds maximum length of ${this.MAX_LABEL_LENGTH}`);
        }
        const constraintErrors = this.validateFieldConstraints(field);
        errors.push(...constraintErrors);

        return errors;
    }

    validateFieldKey(key: string): boolean {
        return this.KEY_PATTERN.test(key);
    }

    validateFieldConstraints(field: ExtendedFieldDefinition): string[] {
        const errors: string[] = [];

        switch (field.type) {
            case 'text':
            case 'textarea':
                if (field.minLength !== undefined) {
                    if (field.minLength < 0) {
                        errors.push('minLength must be >= 0');
                    }
                }
                if (field.maxLength !== undefined) {
                    if (field.maxLength < 1) {
                        errors.push('maxLength must be >= 1');
                    }
                }
                if (field.minLength !== undefined && field.maxLength !== undefined) {
                    if (field.minLength > field.maxLength) {
                        errors.push('minLength must be <= maxLength');
                    }
                }
                if (field.pattern) {
                    try {
                        new RegExp(field.pattern);
                    } catch (e) {
                        errors.push('Invalid regex pattern');
                    }
                }
                break;

            case 'number':
                if (field.min !== undefined && field.max !== undefined) {
                    if (field.min > field.max) {
                        errors.push('min must be <= max');
                    }
                }
                break;

            case 'select':
                if (!field.options || !Array.isArray(field.options)) {
                    errors.push('Select field must have options array');
                } else if (field.options.length === 0) {
                    errors.push('Select field must have at least one option');
                } else {
                    const emptyOptions = field.options.filter(opt => !opt || opt.trim() === '');
                    if (emptyOptions.length > 0) {
                        errors.push('Select options cannot be empty');
                    }
                }
                break;

            case 'boolean':
                break;
        }

        return errors;
    }

    buildValidators(field: ExtendedFieldDefinition): ValidatorFn[] {
        const validators: ValidatorFn[] = [];
        if (field.required) {
            validators.push(Validators.required);
        }
        switch (field.type) {
            case 'text':
            case 'textarea':
                if (field.minLength !== undefined) {
                    validators.push(Validators.minLength(field.minLength));
                }
                if (field.maxLength !== undefined) {
                    validators.push(Validators.maxLength(field.maxLength));
                }
                if (field.pattern) {
                    try {
                        validators.push(Validators.pattern(field.pattern));
                    } catch (e) {
                        console.warn('Invalid pattern for field:', field.key);
                    }
                }
                break;

            case 'number':
                if (field.min !== undefined) {
                    validators.push(Validators.min(field.min));
                }
                if (field.max !== undefined) {
                    validators.push(Validators.max(field.max));
                }
                break;

            case 'select':
                break;

            case 'boolean':
                break;
        }

        return validators;
    }

    getValidationRules() {
        return {
            MAX_FIELDS: this.MAX_FIELDS,
            MAX_SCHEMA_SIZE: this.MAX_SCHEMA_SIZE,
            MAX_KEY_LENGTH: this.MAX_KEY_LENGTH,
            MAX_LABEL_LENGTH: this.MAX_LABEL_LENGTH,
            KEY_PATTERN: this.KEY_PATTERN.source,
            VALID_TYPES: this.VALID_TYPES
        };
    }
}
