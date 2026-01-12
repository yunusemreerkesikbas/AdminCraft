export type DynamicFieldType = 'text' | 'richtext' | 'number' | 'boolean' | 'date' | 'media' | 'select' | 'textarea';

export interface DynamicFieldConfig {
    key: string;
    label?: string;
    labelKey?: string;
    type: DynamicFieldType;
    required?: boolean;
    maxLength?: number;
    minLength?: number;
    minValue?: number;
    maxValue?: number;
    options?: { value: any; label: string }[];
    defaultValue?: any;
    hint?: string;
    styleClasses?: string;
}
